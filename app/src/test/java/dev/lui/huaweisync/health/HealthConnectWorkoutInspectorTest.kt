package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncFailureDisposition
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectWorkoutInspectorTest {
    private val request = HealthWorkoutInspectionRequest(
        clientRecordId = "expected-client-id",
        clientRecordVersion = 7L,
        startTime = Instant.parse("2026-07-14T11:15:00Z"),
        endTime = Instant.parse("2026-07-14T12:00:00Z"),
    )

    @Test
    fun exactExpectedVersionMatchConfirmsAndReconciles() = runTest {
        val inspector = inspectorWith(
            page(
                record("other", 7L, "other-id"),
                record("expected-client-id", 7L, "expected-id"),
            ),
        )

        assertEquals(HealthConfirmationResult.Confirmed, inspector.confirm(request.toConfirmation()))
        assertEquals(
            HealthReconciliationResult.Found("expected-id"),
            inspector.reconcile(request.toReconciliation()),
        )
        assertEquals(
            HealthWorkoutInspectionResult.Complete(
                HealthWorkoutInspection(
                    matchingRecordCount = 1,
                    expectedVersionMatchCount = 1,
                    externalRecordId = "expected-id",
                ),
            ),
            inspector.inspect(request),
        )
    }

    @Test
    fun allPagesAreReadAndOnlyExactClientIdsAreCounted() = runTest {
        val seenTokens = mutableListOf<String?>()
        val inspector = HealthConnectWorkoutInspector(readPage = { observed, token ->
            assertEquals(request, observed)
            seenTokens += token
            when (token) {
                null -> HealthWorkoutInspectionPage(
                    records = listOf(record("expected-client-id-prefix", 7L, "wrong-id")),
                    nextPageToken = "page-2",
                )
                "page-2" -> page(record("expected-client-id", 7L, "expected-id"))
                else -> error("Unexpected token")
            }
        })

        val result = inspector.inspect(request) as HealthWorkoutInspectionResult.Complete

        assertEquals(listOf(null, "page-2"), seenTokens)
        assertEquals(1, result.inspection.matchingRecordCount)
        assertEquals(1, result.inspection.expectedVersionMatchCount)
    }

    @Test
    fun noExactMatchIsAuthoritativeAbsence() = runTest {
        val inspector = inspectorWith(page(record("different-client-id", 7L, "other-id")))

        assertEquals(
            HealthConfirmationResult.Absent("HEALTH_RECORD_NOT_FOUND"),
            inspector.confirm(request.toConfirmation()),
        )
        assertEquals(
            HealthReconciliationResult.AuthoritativelyAbsent("HEALTH_RECORD_NOT_FOUND"),
            inspector.reconcile(request.toReconciliation()),
        )
    }

    @Test
    fun wrongVersionIsInconclusiveAndNeverUnlocksRetry() = runTest {
        val inspector = inspectorWith(page(record("expected-client-id", 6L, "old-id")))

        assertEquals(
            HealthConfirmationResult.Inconclusive("HEALTH_RECORD_VERSION_MISMATCH"),
            inspector.confirm(request.toConfirmation()),
        )
        assertEquals(
            HealthReconciliationResult.Inconclusive("HEALTH_RECORD_VERSION_MISMATCH"),
            inspector.reconcile(request.toReconciliation()),
        )
        val inspection = (inspector.inspect(request) as HealthWorkoutInspectionResult.Complete).inspection
        assertEquals(1, inspection.matchingRecordCount)
        assertEquals(0, inspection.expectedVersionMatchCount)
        assertEquals("old-id", inspection.externalRecordId)
    }

    @Test
    fun duplicateExactClientIdsAreInconclusiveEvenWhenVersionsMatch() = runTest {
        val inspector = inspectorWith(
            page(
                record("expected-client-id", 7L, "first-id"),
                record("expected-client-id", 7L, "second-id"),
            ),
        )

        assertEquals(
            HealthConfirmationResult.Inconclusive("MULTIPLE_HEALTH_RECORDS_FOUND"),
            inspector.confirm(request.toConfirmation()),
        )
        assertEquals(
            HealthReconciliationResult.Inconclusive("MULTIPLE_HEALTH_RECORDS_FOUND"),
            inspector.reconcile(request.toReconciliation()),
        )
        val inspection = (inspector.inspect(request) as HealthWorkoutInspectionResult.Complete).inspection
        assertEquals(2, inspection.matchingRecordCount)
        assertEquals(2, inspection.expectedVersionMatchCount)
        assertEquals(null, inspection.externalRecordId)
    }

    @Test
    fun sdkFailureUsesSafeRetryablePhaseSpecificFailures() = runTest {
        val inspector = HealthConnectWorkoutInspector(
            readPage = { _, _ -> throw IOException("private provider response") },
        )

        val direct = inspector.inspect(request)
        assertEquals(
            HealthWorkoutInspectionResult.Inconclusive("HEALTH_CONNECT_INSPECTION_FAILED"),
            direct,
        )

        val confirmation = inspector.confirm(request.toConfirmation()) as HealthConfirmationResult.Failed
        assertEquals(SyncFailureDisposition.RETRYABLE, confirmation.failure.disposition)
        assertEquals(SyncErrorPhase.VERIFICATION, confirmation.failure.phase)
        assertEquals("HEALTH_CONNECT_INSPECTION_FAILED", confirmation.failure.code)

        val reconciliation = inspector.reconcile(request.toReconciliation()) as HealthReconciliationResult.Failed
        assertEquals(SyncFailureDisposition.RETRYABLE, reconciliation.failure.disposition)
        assertEquals(SyncErrorPhase.RECONCILIATION, reconciliation.failure.phase)
        assertEquals("HEALTH_CONNECT_INSPECTION_FAILED", reconciliation.failure.code)
    }

    @Test
    fun permissionFailureUsesDistinctSafeCodeWithoutExceptionText() = runTest {
        val inspector = HealthConnectWorkoutInspector(
            readPage = { _, _ -> throw SecurityException("private permission detail") },
        )

        val result = inspector.inspect(request)

        assertEquals(
            HealthWorkoutInspectionResult.Inconclusive(
                "HEALTH_CONNECT_INSPECTION_NOT_AUTHORIZED",
            ),
            result,
        )
        assertTrue(result.toString().contains("private permission detail").not())
    }

    @Test
    fun malformedExternalIdIsInconclusiveInsteadOfClaimingAFoundRecord() = runTest {
        val inspector = inspectorWith(page(record("expected-client-id", 7L, "")))

        assertEquals(
            HealthWorkoutInspectionResult.Inconclusive("HEALTH_CONNECT_INSPECTION_FAILED"),
            inspector.inspect(request),
        )
    }

    @Test
    fun blankPageTokenIsInconclusiveWithoutIssuingAnotherRead() = runTest {
        var calls = 0
        val inspector = HealthConnectWorkoutInspector(readPage = { _, _ ->
            calls += 1
            HealthWorkoutInspectionPage(emptyList(), nextPageToken = " ")
        })

        assertEquals(
            HealthWorkoutInspectionResult.Inconclusive("HEALTH_CONNECT_PAGINATION_INVALID"),
            inspector.inspect(request),
        )
        assertEquals(1, calls)
    }

    @Test
    fun repeatedPageTokenIsInconclusiveInsteadOfLoopingOrClaimingAbsence() = runTest {
        var calls = 0
        val inspector = HealthConnectWorkoutInspector(readPage = { _, _ ->
            calls += 1
            HealthWorkoutInspectionPage(emptyList(), nextPageToken = "same-token")
        })

        assertEquals(
            HealthWorkoutInspectionResult.Inconclusive("HEALTH_CONNECT_PAGINATION_INVALID"),
            inspector.inspect(request),
        )
        assertEquals(2, calls)
    }

    @Test
    fun cancellationIsNeverConvertedToAnInspectionResult() = runTest {
        val expected = CancellationException("cancel")
        val inspector = HealthConnectWorkoutInspector(readPage = { _, _ -> throw expected })

        val observed = try {
            inspector.inspect(request)
            throw AssertionError("Expected cancellation")
        } catch (failure: CancellationException) {
            failure
        }

        assertSame(expected, observed)
    }

    @Test
    fun invalidBoundsAndIdentityAreRejectedAtContractBoundary() {
        assertTrue(
            runCatching { request.copy(clientRecordId = "") }.exceptionOrNull() is IllegalArgumentException,
        )
        assertTrue(
            runCatching { request.copy(clientRecordVersion = 0L) }.exceptionOrNull() is IllegalArgumentException,
        )
        assertTrue(
            runCatching { request.copy(endTime = request.startTime) }.exceptionOrNull() is IllegalArgumentException,
        )
    }

    private fun inspectorWith(page: HealthWorkoutInspectionPage) =
        HealthConnectWorkoutInspector(readPage = { _, _ -> page })

    private fun page(vararg records: HealthWorkoutInspectionRecord) =
        HealthWorkoutInspectionPage(records.toList(), nextPageToken = null)

    private fun record(clientId: String, version: Long, externalId: String) =
        HealthWorkoutInspectionRecord(clientId, version, externalId)

    private fun HealthWorkoutInspectionRequest.toConfirmation() = HealthConfirmationRequest(
        clientRecordId,
        clientRecordVersion,
        externalRecordId = null,
        startTime,
        endTime,
    )

    private fun HealthWorkoutInspectionRequest.toReconciliation() = HealthReconciliationRequest(
        clientRecordId,
        clientRecordVersion,
        externalRecordId = null,
        startTime,
        endTime,
    )
}
