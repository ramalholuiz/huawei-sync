package dev.lui.huaweisync.diagnostics

import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.health.Gate1SyncResult
import dev.lui.huaweisync.health.HealthWorkoutInspection
import dev.lui.huaweisync.health.HealthWorkoutInspectionRequest
import dev.lui.huaweisync.health.HealthWorkoutInspectionResult
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class Gate1RuntimeDiagnosticsTest {
    @Test
    fun `run confirm and reconcile delegate then attach authoritative inspection facts`() = runTest {
        val actions = mutableListOf<String>()
        val requests = mutableListOf<HealthWorkoutInspectionRequest>()
        val runtime = runtime(
            runSync = { actions += "run"; completed() },
            confirmSync = { actions += "confirm"; completed() },
            reconcileSync = { actions += "reconcile"; completed() },
            inspect = { request ->
                requests += request
                HealthWorkoutInspectionResult.Complete(
                    HealthWorkoutInspection(1, 1, "external-private-id"),
                )
            },
        )

        val diagnostics = listOf(runtime.run(), runtime.confirm(), runtime.reconcile())

        assertEquals(listOf("run", "confirm", "reconcile"), actions)
        assertEquals(3, requests.size)
        requests.forEach { request ->
            assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_ID, request.clientRecordId)
            assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_VERSION, request.clientRecordVersion)
            assertEquals(SyntheticWorkoutFactory.create().startTime, request.startTime)
            assertEquals(SyntheticWorkoutFactory.create().endTime, request.endTime)
        }
        diagnostics.forEach { diagnostic ->
            assertEquals(1, diagnostic.healthConnectMatchCount)
            assertEquals(1, diagnostic.healthConnectExpectedVersionMatchCount)
            assertEquals(true, diagnostic.versionMatch)
        }
    }

    @Test
    fun `refresh uses durable row and reports duplicates without exposing identifiers`() = runTest {
        val runtime = runtime(
            findLedger = { entry(SyncStatus.VERIFIED, version = 4L) },
            inspect = { request ->
                assertEquals(4L, request.clientRecordVersion)
                HealthWorkoutInspectionResult.Complete(
                    HealthWorkoutInspection(2, 2, externalRecordId = null),
                )
            },
        )

        val diagnostic = runtime.refresh()

        assertEquals(SyncStatus.VERIFIED, diagnostic.durableStatus)
        assertEquals(2, diagnostic.healthConnectMatchCount)
        assertEquals(false, diagnostic.versionMatch)
        assertFalse(diagnostic.toString().contains(SyntheticWorkoutFactory.CLIENT_RECORD_ID))
    }

    @Test
    fun `refresh without a Room row remains ready and still performs bounded inspection`() = runTest {
        var request: HealthWorkoutInspectionRequest? = null
        val runtime = runtime(
            findLedger = { null },
            inspect = {
                request = it
                HealthWorkoutInspectionResult.Complete(
                    HealthWorkoutInspection(0, 0, externalRecordId = null),
                )
            },
        )

        val diagnostic = runtime.refresh()

        assertEquals("READY", diagnostic.statusCode)
        assertEquals(0, diagnostic.roomRowCount)
        assertEquals(0, diagnostic.writeAttemptCount)
        assertEquals(0, diagnostic.healthConnectMatchCount)
        assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_VERSION, request?.clientRecordVersion)
    }

    @Test
    fun `inspection failure is a stable low-cardinality code with unknown counts`() = runTest {
        val runtime = runtime(
            findLedger = { entry(SyncStatus.SYNCED) },
            inspect = {
                HealthWorkoutInspectionResult.Inconclusive("HEALTH_CONNECT_INSPECTION_NOT_AUTHORIZED")
            },
        )

        val diagnostic = runtime.refresh()

        assertEquals("HEALTH_CONNECT_INSPECTION_NOT_AUTHORIZED", diagnostic.code)
        assertNull(diagnostic.healthConnectMatchCount)
        assertNull(diagnostic.versionMatch)
        assertFalse(diagnostic.toString().contains("permission detail", ignoreCase = true))
    }

    @Test
    fun `action and Room failures are sanitized and never include exception text`() = runTest {
        val privateText = "private provider response for Jane Doe"
        val reportedFailures = mutableListOf<String>()
        val actionFailure = runtime(
            runSync = { throw IOException(privateText) },
            findLedger = { null },
            reportActionFailure = reportedFailures::add,
        ).run()
        val roomFailure = runtime(
            findLedger = { throw IOException(privateText) },
        ).refresh()

        assertEquals("GATE1_RUNTIME_ACTION_FAILED", actionFailure.code)
        assertEquals("ROOM_LEDGER_READ_FAILED", roomFailure.code)
        assertEquals(1, reportedFailures.size)
        assertTrue(reportedFailures.single().contains(IOException::class.java.name))
        assertFalse(reportedFailures.single().contains(privateText))
        assertFalse(actionFailure.toString().contains(privateText))
        assertFalse(roomFailure.toString().contains(privateText))
    }

    @Test
    fun `unexpected inspector exception is sanitized`() = runTest {
        val diagnostic = runtime(
            findLedger = { entry(SyncStatus.VERIFIED) },
            inspect = { throw IOException("raw SDK response") },
        ).refresh()

        assertEquals("HEALTH_CONNECT_INSPECTION_FAILED", diagnostic.code)
        assertNull(diagnostic.healthConnectMatchCount)
        assertFalse(diagnostic.toString().contains("raw SDK response"))
    }

    @Test
    fun `runtime diagnostic export contains counts but no private runtime values`() = runTest {
        val privateValues = listOf(
            SyntheticWorkoutFactory.CLIENT_RECORD_ID,
            "private-source-id",
            "external-private-id",
            "private exception text",
        )
        val diagnostic = runtime(
            findLedger = { entry(SyncStatus.VERIFIED) },
        ).refresh()

        val report = Gate1Diagnostics.export(
            Gate1ExportInput(
                appVersion = "0.1.0-gate1",
                buildType = "debug",
                generatedAt = Instant.parse("2026-07-15T12:00:00Z"),
                availability = HealthConnectAvailability.AVAILABLE,
                permission = HealthConnectPermission.GRANTED,
                diagnostic = diagnostic,
            ),
        )

        assertTrue(report.contains("room_row_count=1"))
        assertTrue(report.contains("health_connect_match_count=1"))
        privateValues.forEach { value -> assertFalse(report.contains(value)) }
    }

    @Test
    fun `cancellation is propagated from actions ledger and inspector`() = runTest {
        val expected = CancellationException("cancel")
        val calls = listOf<suspend () -> Unit>(
            { runtime(runSync = { throw expected }).run() },
            { runtime(findLedger = { throw expected }).refresh() },
            {
                runtime(
                    findLedger = { entry(SyncStatus.VERIFIED) },
                    inspect = { throw expected },
                ).refresh()
            },
        )

        calls.forEach { call ->
            val observed = try {
                call()
                throw AssertionError("Expected cancellation")
            } catch (failure: CancellationException) {
                failure
            }
            assertSame(expected, observed)
        }
    }

    private fun runtime(
        runSync: suspend () -> Gate1SyncResult = { completed() },
        confirmSync: suspend () -> Gate1SyncResult = { completed() },
        reconcileSync: suspend () -> Gate1SyncResult = { completed() },
        findLedger: suspend () -> SyncLedgerEntry? = { entry(SyncStatus.SYNCED) },
        inspect: suspend (HealthWorkoutInspectionRequest) -> HealthWorkoutInspectionResult = {
            HealthWorkoutInspectionResult.Complete(
                HealthWorkoutInspection(1, 1, "external-private-id"),
            )
        },
        reportActionFailure: (String) -> Unit = {},
    ) = Gate1RuntimeDiagnostics(
        runSync = runSync,
        confirmSync = confirmSync,
        reconcileSync = reconcileSync,
        findLedger = findLedger,
        inspect = inspect,
        reportActionFailure = reportActionFailure,
    )

    private fun completed() = Gate1SyncResult.Completed(
        clientRecordId = SyntheticWorkoutFactory.CLIENT_RECORD_ID,
        clientRecordVersion = SyntheticWorkoutFactory.CLIENT_RECORD_VERSION,
        ledgerRowsForClientRecordId = 1,
        writeCountForClientRecordId = 1,
    )

    private fun entry(status: SyncStatus, version: Long = SyntheticWorkoutFactory.CLIENT_RECORD_VERSION) =
        SyncLedgerEntry(
            clientRecordId = SyntheticWorkoutFactory.CLIENT_RECORD_ID,
            sourceProvider = "synthetic",
            sourceRecordId = "private-source-id",
            sourceVersion = null,
            contentHash = "a".repeat(64),
            clientRecordVersion = version,
            healthConnectRecordId = "external-private-id",
            status = status,
            attemptCount = 1,
            acceptedAtEpochMillis = 1,
            confirmedAtEpochMillis = null,
            createdAtEpochMillis = 1,
            updatedAtEpochMillis = 2,
            lastErrorCode = null,
            lastErrorPhase = null,
            lastErrorAtEpochMillis = null,
            lastErrorMessage = "private exception text",
        )
}
