package dev.lui.huaweisync.diagnostics

import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncFailureDisposition
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.health.ConfirmationPendingReason
import dev.lui.huaweisync.health.Gate1SyncResult
import dev.lui.huaweisync.health.LocalFinalizationStatus
import dev.lui.huaweisync.health.ReconciliationResolution
import dev.lui.huaweisync.health.SyncPhase
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gate1DiagnosticsTest {
    @Test
    fun `every durable status maps to closed guidance`() {
        val expected = mapOf(
            SyncStatus.PENDING to DiagnosticNextAction.RUN_SYNC,
            SyncStatus.WRITING to DiagnosticNextAction.RECONCILE,
            SyncStatus.SYNCED to DiagnosticNextAction.CONFIRM,
            SyncStatus.VERIFICATION_PENDING to DiagnosticNextAction.CONFIRM,
            SyncStatus.VERIFIED to DiagnosticNextAction.NONE,
            SyncStatus.PERMISSION_BLOCKED to DiagnosticNextAction.REQUEST_PERMISSION,
            SyncStatus.ENVIRONMENT_BLOCKED to DiagnosticNextAction.CHECK_HEALTH_CONNECT,
            SyncStatus.RETRYABLE_ERROR to DiagnosticNextAction.RETRY_SYNC,
            SyncStatus.PERMANENT_ERROR to DiagnosticNextAction.NONE,
            SyncStatus.RECONCILIATION_PENDING to DiagnosticNextAction.RECONCILE,
        )

        assertEquals(SyncStatus.entries.toSet(), expected.keys)
        expected.forEach { (status, action) ->
            val diagnostic = Gate1Diagnostics.fromLedger(entry(status))
            assertEquals(status, diagnostic.durableStatus)
            assertEquals(action, diagnostic.nextAction)
            assertTrue(diagnostic.safeMessage.length in 1..160)
            assertFalse(diagnostic.safeMessage.contains(CLIENT_ID))
        }
    }

    @Test
    fun `every coordinator result maps to safe actionable guidance`() {
        val results = listOf(
            result { Gate1SyncResult.Completed(CLIENT_ID, 2, 1, 1) },
            result { Gate1SyncResult.Blocked(CLIENT_ID, 2, 1, 1, SyncBlockReason.PERMISSION, SyncPhase.PREFLIGHT, "PERMISSION_REQUIRED") },
            result { Gate1SyncResult.WriteFailed(CLIENT_ID, 2, 1, 1, SyncFailureDisposition.RETRYABLE, SyncPhase.EXTERNAL_WRITE, "WRITE_FAILED") },
            result { Gate1SyncResult.Confirmed(CLIENT_ID, 2, 1, 1, EXTERNAL_ID, SyncPhase.CONFIRMATION, "CONFIRMED", LocalFinalizationStatus.FINALIZED) },
            result { Gate1SyncResult.ConfirmationPending(CLIENT_ID, 2, 1, 1, EXTERNAL_ID, ConfirmationPendingReason.INCONCLUSIVE, SyncPhase.CONFIRMATION, "READ_INCONCLUSIVE", LocalFinalizationStatus.FINALIZED) },
            result { Gate1SyncResult.Reconciled(CLIENT_ID, 2, 1, 1, EXTERNAL_ID, ReconciliationResolution.EXISTING_ACCEPTED, SyncPhase.RECONCILIATION, "RECONCILED_EXISTING", LocalFinalizationStatus.FINALIZED) },
            result { Gate1SyncResult.ReconciliationPending(CLIENT_ID, 2, 1, 1, EXTERNAL_ID, SyncPhase.RECONCILIATION, "READ_FAILED", LocalFinalizationStatus.RECONCILIATION_PENDING) },
            result { Gate1SyncResult.ExternalAccepted(CLIENT_ID, 2, 1, 1, EXTERNAL_ID, SyncPhase.LOCAL_FINALIZATION, "LOCAL_FINALIZATION_FAILED", LocalFinalizationStatus.FAILED) },
        )

        assertEquals(8, results.size)
        results.forEach { diagnostic ->
            assertTrue(diagnostic.safeMessage.isNotBlank())
            assertFalse(diagnostic.safeMessage.contains(CLIENT_ID))
            assertFalse(diagnostic.safeMessage.contains(EXTERNAL_ID))
            assertEquals(1, diagnostic.roomRowCount)
            assertEquals(1, diagnostic.writeAttemptCount)
        }
        assertEquals(DiagnosticEvidence.VERIFIED, results[3].evidence)
        assertEquals(DiagnosticNextAction.RECONCILE, results.last().nextAction)
    }

    @Test
    fun `nested failure branches remain closed and never authorize a blind write`() {
        val cases = listOf(
            Gate1Diagnostics.fromResult(
                Gate1SyncResult.Blocked(
                    CLIENT_ID, 2, 1, 0, SyncBlockReason.ENVIRONMENT,
                    SyncPhase.PREFLIGHT, "ENVIRONMENT_UNAVAILABLE",
                ),
            ) to DiagnosticNextAction.CHECK_HEALTH_CONNECT,
            Gate1Diagnostics.fromResult(
                Gate1SyncResult.WriteFailed(
                    CLIENT_ID, 2, 1, 1, SyncFailureDisposition.PERMANENT,
                    SyncPhase.EXTERNAL_WRITE, "INVALID_WORKOUT",
                ),
            ) to DiagnosticNextAction.NONE,
            Gate1Diagnostics.fromResult(
                Gate1SyncResult.ConfirmationPending(
                    CLIENT_ID, 2, 1, 1, null, ConfirmationPendingReason.ABSENT,
                    SyncPhase.CONFIRMATION, "HEALTH_RECORD_NOT_FOUND",
                    LocalFinalizationStatus.FINALIZED,
                ),
            ) to DiagnosticNextAction.RECONCILE,
            Gate1Diagnostics.fromResult(
                Gate1SyncResult.ConfirmationPending(
                    CLIENT_ID, 2, 1, 1, null, ConfirmationPendingReason.FAILURE,
                    SyncPhase.CONFIRMATION, "HEALTH_CONNECT_INSPECTION_FAILED",
                    LocalFinalizationStatus.FINALIZED,
                ),
            ) to DiagnosticNextAction.CONFIRM,
        )

        cases.forEach { (diagnostic, expectedAction) ->
            assertEquals(expectedAction, diagnostic.nextAction)
            assertFalse(diagnostic.safeMessage.contains(CLIENT_ID))
        }
    }

    @Test
    fun `retry-allowed reconciliation is the only reconciliation outcome that authorizes sync`() {
        val retryAllowed = Gate1Diagnostics.fromResult(
            Gate1SyncResult.Reconciled(
                CLIENT_ID, 2, 1, 1, null, ReconciliationResolution.RETRY_ALLOWED,
                SyncPhase.RECONCILIATION, "AUTHORITATIVELY_ABSENT", LocalFinalizationStatus.FINALIZED,
            ),
        )
        val pending = Gate1Diagnostics.fromResult(
            Gate1SyncResult.ReconciliationPending(
                CLIENT_ID, 2, 1, 1, null, SyncPhase.RECONCILIATION,
                "READ_INCONCLUSIVE", LocalFinalizationStatus.RECONCILIATION_PENDING,
            ),
        )

        assertEquals(DiagnosticNextAction.RUN_SYNC, retryAllowed.nextAction)
        assertEquals(DiagnosticEvidence.READY, retryAllowed.evidence)
        assertEquals(DiagnosticNextAction.RECONCILE, pending.nextAction)
    }

    @Test
    fun `malformed dynamic code is replaced rather than exported`() {
        val injected = "sdk failed: Jane Doe\n$CLIENT_ID"
        val diagnostic = Gate1Diagnostics.fromResult(
            Gate1SyncResult.WriteFailed(
                CLIENT_ID, 1, 1, 1, SyncFailureDisposition.PERMANENT,
                SyncPhase.EXTERNAL_WRITE, injected,
            ),
        )

        assertEquals("UNSAFE_CODE_REDACTED", diagnostic.code)
        assertFalse(Gate1Diagnostics.export(exportInput(diagnostic)).contains(injected))
    }

    @Test
    fun `export contains fixed low-cardinality evidence and no identifiers or payload text`() {
        val diagnostic = Gate1Diagnostics.fromResult(
            Gate1SyncResult.Confirmed(
                CLIENT_ID, 7, 1, 3, EXTERNAL_ID, SyncPhase.CONFIRMATION,
                "CONFIRMED", LocalFinalizationStatus.FINALIZED,
            ),
            inspection = Gate1InspectionFacts(
                matchingRecordCount = 1,
                expectedVersionMatchCount = 1,
            ),
        )

        val report = Gate1Diagnostics.export(exportInput(diagnostic))

        listOf(
            "schema_version=1", "app_version=0.1.0-gate1", "build_type=debug",
            "generated_at=2026-07-15T12:00:00Z", "availability=AVAILABLE",
            "permission=GRANTED", "status=VERIFIED", "phase=CONFIRMATION",
            "code=CONFIRMED", "next_action=NONE", "room_row_count=1",
            "write_attempt_count=3", "client_record_version=7",
            "health_connect_match_count=1",
            "health_connect_expected_version_match_count=1", "version_match=true",
            "local_finalization=FINALIZED", "evidence=VERIFIED",
        ).forEach { expected -> assertTrue("missing $expected", report.contains(expected)) }
        listOf(CLIENT_ID, EXTERNAL_ID, "workout", "notes", "exception").forEach {
            assertFalse("unsafe export contained $it", report.contains(it, ignoreCase = true))
        }
        assertTrue(report.length <= Gate1Diagnostics.MAX_EXPORT_LENGTH)
    }

    @Test
    fun `duplicates and version mismatches never report a version match`() {
        val duplicate = Gate1Diagnostics.fromLedger(
            entry(SyncStatus.RECONCILIATION_PENDING),
            Gate1InspectionFacts(matchingRecordCount = 2, expectedVersionMatchCount = 2),
        )
        val mismatchedVersion = Gate1Diagnostics.fromLedger(
            entry(SyncStatus.VERIFICATION_PENDING),
            Gate1InspectionFacts(matchingRecordCount = 1, expectedVersionMatchCount = 0),
        )

        assertEquals(false, duplicate.versionMatch)
        assertEquals(false, mismatchedVersion.versionMatch)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `inspection rejects a version count larger than the record count`() {
        Gate1InspectionFacts(matchingRecordCount = 1, expectedVersionMatchCount = 2)
    }

    @Test
    fun `unsafe build metadata is redacted as a whole`() {
        val input = exportInput(Gate1Diagnostics.fromLedger(entry(SyncStatus.PENDING))).copy(
            appVersion = "private user data\n$CLIENT_ID",
        )

        val report = Gate1Diagnostics.export(input)

        assertTrue(report.contains("app_version=redacted"))
        assertFalse(report.contains(CLIENT_ID))
    }

    @Test
    fun `unknown inspection facts stay explicitly unknown`() {
        val report = Gate1Diagnostics.export(
            exportInput(Gate1Diagnostics.fromLedger(entry(SyncStatus.VERIFICATION_PENDING))),
        )

        assertTrue(report.contains("health_connect_match_count=unknown"))
        assertTrue(report.contains("version_match=unknown"))
        assertTrue(report.contains("phase=unknown"))
        assertTrue(report.contains("code=unknown"))
    }

    private fun result(create: () -> Gate1SyncResult) = Gate1Diagnostics.fromResult(create())

    private fun entry(status: SyncStatus) = SyncLedgerEntry(
        clientRecordId = CLIENT_ID,
        sourceProvider = "synthetic",
        sourceRecordId = "private-source-id",
        sourceVersion = null,
        contentHash = "a".repeat(64),
        clientRecordVersion = 2,
        healthConnectRecordId = EXTERNAL_ID,
        status = status,
        attemptCount = 1,
        acceptedAtEpochMillis = null,
        confirmedAtEpochMillis = null,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 2,
        lastErrorCode = if (status.name.endsWith("ERROR")) "SAFE_ERROR" else null,
        lastErrorPhase = null,
        lastErrorAtEpochMillis = null,
        lastErrorMessage = "private exception detail",
    )

    private fun exportInput(diagnostic: Gate1Diagnostic) = Gate1ExportInput(
        appVersion = "0.1.0-gate1",
        buildType = "debug",
        generatedAt = Instant.parse("2026-07-15T12:00:00Z"),
        availability = HealthConnectAvailability.AVAILABLE,
        permission = HealthConnectPermission.GRANTED,
        diagnostic = diagnostic,
    )

    private companion object {
        const val CLIENT_ID = "huawei-sync:synthetic:very-private-id"
        const val EXTERNAL_ID = "health-connect-external-private-id"
    }
}
