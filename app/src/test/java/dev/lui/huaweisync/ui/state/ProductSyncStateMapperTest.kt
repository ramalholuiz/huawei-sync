package dev.lui.huaweisync.ui.state

import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncFailureDisposition
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.health.ConfirmationPendingReason
import dev.lui.huaweisync.health.Gate1SyncResult
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.health.LocalFinalizationStatus
import dev.lui.huaweisync.health.ReconciliationResolution
import dev.lui.huaweisync.health.SyncPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductSyncStateMapperTest {
    @Test
    fun `availability and permission mappings are exhaustive and actionable`() {
        val availability = HealthConnectAvailability.values().associateWith(
            ProductSyncStateMapper::mapAvailability,
        )
        assertEquals(ProductHealthConnectStatus.READY_TO_SYNC, availability[HealthConnectAvailability.Available])
        assertEquals(ProductHealthConnectStatus.UPDATE_REQUIRED, availability[HealthConnectAvailability.ProviderUpdateRequired])
        assertEquals(ProductHealthConnectStatus.UNAVAILABLE, availability[HealthConnectAvailability.Unavailable])

        val permission = HealthConnectPermission.values().associateWith(
            ProductSyncStateMapper::mapPermission,
        )
        assertEquals(ProductHealthConnectStatus.READY_TO_SYNC, permission[HealthConnectPermission.GRANTED])
        assertEquals(ProductHealthConnectStatus.PERMISSION_REQUIRED, permission[HealthConnectPermission.NOT_GRANTED])
        assertEquals(ProductHealthConnectStatus.PERMISSION_REQUIRED, permission[HealthConnectPermission.NOT_REQUESTED])
    }

    @Test
    fun `immutable state rejects a confirmation claim without readback evidence`() {
        assertThrows(IllegalArgumentException::class.java) {
            ProductSyncState(
                healthConnectStatus = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
                phase = ProductSyncPhase.VERIFICATION,
                attemptCount = 1,
                ledgerWorkoutCount = 1,
                verification = ProductVerificationEvidence(
                    diagnosticEvidence = DiagnosticEvidence.ACCEPTED,
                    realReadbackConfirmed = false,
                    healthConnectMatchCount = null,
                    expectedVersionMatchCount = null,
                    versionMatch = null,
                ),
                sanitizedFailureSummary = null,
            )
        }
    }

    @Test
    fun `write acceptance never claims Health Connect confirmation`() {
        val state = ProductSyncStateMapper.from(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            result = Gate1SyncResult.Completed(
                clientRecordId = "client-id",
                clientRecordVersion = 1,
                ledgerRowsForClientRecordId = 1,
                writeCountForClientRecordId = 1,
            ),
        )

        assertEquals(ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK, state.healthConnectStatus)
        assertFalse(state.verification.realReadbackConfirmed)
        assertEquals("Available for GymRats to import", state.gymRatsStatus.label)
    }

    @Test
    fun `all non-confirmed coordinator outcomes are forbidden from claiming confirmation`() {
        val outcomes = listOf<Gate1SyncResult>(
            Gate1SyncResult.Completed("id", 1, 1, 1),
            Gate1SyncResult.Blocked("id", 1, 1, 0, SyncBlockReason.PERMISSION, SyncPhase.PREFLIGHT, "BLOCKED"),
            Gate1SyncResult.WriteFailed("id", 1, 1, 1, SyncFailureDisposition.RETRYABLE, SyncPhase.EXTERNAL_WRITE, "FAILED"),
            Gate1SyncResult.ConfirmationPending(
                "id", 1, 1, 1, "external", ConfirmationPendingReason.INCONCLUSIVE,
                SyncPhase.CONFIRMATION, "PENDING", LocalFinalizationStatus.FINALIZED,
            ),
            Gate1SyncResult.Reconciled(
                "id", 1, 1, 1, "external", ReconciliationResolution.EXISTING_ACCEPTED,
                SyncPhase.RECONCILIATION, "ACCEPTED", LocalFinalizationStatus.FINALIZED,
            ),
            Gate1SyncResult.ReconciliationPending(
                "id", 1, 1, 1, "external", SyncPhase.RECONCILIATION, "PENDING",
                LocalFinalizationStatus.RECONCILIATION_PENDING,
            ),
            Gate1SyncResult.ExternalAccepted(
                "id", 1, 1, 1, "external", SyncPhase.LOCAL_FINALIZATION, "ACCEPTED",
                LocalFinalizationStatus.RECONCILIATION_PENDING,
            ),
        )

        outcomes.forEach { outcome ->
            val state = ProductSyncStateMapper.from(
                availability = HealthConnectAvailability.Available,
                permission = HealthConnectPermission.GRANTED,
                result = outcome,
            )
            assertFalse("${outcome::class.simpleName} made a forbidden confirmation claim", state.verification.realReadbackConfirmed)
            assertFalse(state.healthConnectStatus == ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT)
        }
    }

    @Test
    fun `confirmed result is the only result mapping that proves real readback`() {
        val state = ProductSyncStateMapper.from(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            result = Gate1SyncResult.Confirmed(
                clientRecordId = "client-id",
                clientRecordVersion = 2,
                ledgerRowsForClientRecordId = 1,
                writeCountForClientRecordId = 2,
                externalRecordId = "external-id",
                phase = SyncPhase.CONFIRMATION,
                code = "CONFIRMED",
                localFinalizationStatus = LocalFinalizationStatus.FINALIZED,
            ),
        )

        assertEquals(ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT, state.healthConnectStatus)
        assertTrue(state.verification.realReadbackConfirmed)
        assertEquals(ProductSyncPhase.VERIFICATION, state.phase)
    }

    @Test
    fun `verified diagnostic without exact readback facts cannot claim confirmation`() {
        val state = ProductSyncStateMapper.from(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            diagnostic = diagnostic(
                evidence = DiagnosticEvidence.VERIFIED,
                matchCount = null,
                expectedVersionMatchCount = null,
                versionMatch = null,
            ),
        )

        assertEquals(ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK, state.healthConnectStatus)
        assertFalse(state.verification.realReadbackConfirmed)
    }

    @Test
    fun `exact diagnostic readback permits Health Connect confirmation`() {
        val state = ProductSyncStateMapper.from(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            diagnostic = diagnostic(
                evidence = DiagnosticEvidence.VERIFIED,
                matchCount = 1,
                expectedVersionMatchCount = 1,
                versionMatch = true,
            ),
        )

        assertEquals(ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT, state.healthConnectStatus)
        assertTrue(state.verification.realReadbackConfirmed)
    }

    @Test
    fun `durable verified ledger requires confirmation timestamp`() {
        val unproved = ProductSyncStateMapper.from(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            ledgerEntries = listOf(ledger(SyncStatus.VERIFIED, confirmedAt = null)),
        )
        val proved = ProductSyncStateMapper.from(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            ledgerEntries = listOf(ledger(SyncStatus.VERIFIED, confirmedAt = 300)),
        )

        assertEquals(ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK, unproved.healthConnectStatus)
        assertEquals(ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT, proved.healthConnectStatus)
        assertFalse(unproved.verification.realReadbackConfirmed)
        assertTrue(proved.verification.realReadbackConfirmed)
    }

    @Test
    fun `every ledger status and diagnostic evidence has a pure mapping`() {
        assertEquals(SyncStatus.values().toSet(), SyncStatus.values().associateWith {
            ProductSyncStateMapper.mapLedgerStatus(it, confirmedAtEpochMillis = null)
        }.keys)
        assertEquals(DiagnosticEvidence.values().toSet(), DiagnosticEvidence.values().associateWith(
            ProductSyncStateMapper::mapEvidence,
        ).keys)
    }

    @Test
    fun `sanitized diagnostic failure is exposed without provider details`() {
        val diagnostic = diagnostic(
            evidence = DiagnosticEvidence.FAILED,
            safeMessage = "The write failed safely.",
            phase = "EXTERNAL_WRITE",
        )
        val state = ProductSyncStateMapper.from(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            diagnostic = diagnostic,
            ledgerEntries = listOf(ledger(SyncStatus.RETRYABLE_ERROR, confirmedAt = null)),
        )

        assertEquals("The write failed safely.", state.sanitizedFailureSummary)
        assertEquals(ProductSyncPhase.WRITE, state.phase)
        assertEquals(4, state.attemptCount)
        assertNull(state.verification.healthConnectMatchCount)
    }

    @Test
    fun `GymRats claim vocabulary cannot imply Gate 2 completion`() {
        val labels = ProductGymRatsStatus.values().map(ProductGymRatsStatus::label)

        assertEquals(listOf("Available for GymRats to import"), labels)
        assertTrue(labels.none { label ->
            listOf("confirmed", "imported", "synced", "visible").any(label.lowercase()::contains)
        })
    }

    private fun diagnostic(
        evidence: DiagnosticEvidence,
        safeMessage: String = "Safe status.",
        matchCount: Int? = null,
        expectedVersionMatchCount: Int? = null,
        versionMatch: Boolean? = null,
        phase: String? = null,
    ) = Gate1Diagnostic(
        statusCode = "TEST_STATUS",
        safeMessage = safeMessage,
        nextAction = DiagnosticNextAction.NONE,
        evidence = evidence,
        durableStatus = null,
        phase = phase,
        code = null,
        roomRowCount = 1,
        writeAttemptCount = 2,
        clientRecordVersion = 1,
        healthConnectMatchCount = matchCount,
        healthConnectExpectedVersionMatchCount = expectedVersionMatchCount,
        versionMatch = versionMatch,
        localFinalization = null,
    )

    private fun ledger(status: SyncStatus, confirmedAt: Long?) = SyncLedgerEntry(
        clientRecordId = "client-id",
        sourceProvider = "synthetic",
        sourceRecordId = "source-id",
        sourceVersion = "1",
        contentHash = "a".repeat(64),
        clientRecordVersion = 1,
        healthConnectRecordId = if (status == SyncStatus.PENDING) null else "external-id",
        status = status,
        attemptCount = 4,
        acceptedAtEpochMillis = if (status == SyncStatus.PENDING) null else 200,
        confirmedAtEpochMillis = confirmedAt,
        createdAtEpochMillis = 100,
        updatedAtEpochMillis = 400,
        lastErrorCode = null,
        lastErrorPhase = null,
        lastErrorAtEpochMillis = null,
        lastErrorMessage = null,
    )
}
