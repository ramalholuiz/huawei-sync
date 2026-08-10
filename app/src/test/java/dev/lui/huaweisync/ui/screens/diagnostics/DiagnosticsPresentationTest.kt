package dev.lui.huaweisync.ui.screens.diagnostics

import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.health.HealthConnectAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsPresentationTest {
    @Test
    fun `mapping preserves every exhaustive diagnostic fact without rewriting safe wording`() {
        val diagnostic = diagnostic(
            statusCode = "WRITE_FAILED",
            safeMessage = "The Health Connect write failed before acceptance.",
            nextAction = DiagnosticNextAction.RETRY_SYNC,
            evidence = DiagnosticEvidence.FAILED,
            durableStatus = SyncStatus.RETRYABLE_ERROR,
        ).copy(
            phase = "WRITE",
            code = "java.io.IOException",
            roomRowCount = 1,
            writeAttemptCount = 3,
            clientRecordVersion = 4,
            healthConnectMatchCount = 2,
            healthConnectExpectedVersionMatchCount = 1,
            versionMatch = false,
            localFinalization = "PERSISTED",
        )

        val presentation = diagnostic.toDiagnosticsPresentation(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            busy = false,
        )

        assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_ID, presentation.clientRecordId)
        assertEquals(diagnostic.statusCode, presentation.statusCode)
        assertEquals(diagnostic.safeMessage, presentation.safeMessage)
        assertEquals(diagnostic.nextAction, presentation.nextAction)
        assertEquals(diagnostic.evidence, presentation.evidence)
        assertEquals(diagnostic.durableStatus, presentation.durableStatus)
        assertEquals(diagnostic.phase, presentation.phase)
        assertEquals(diagnostic.code, presentation.code)
        assertEquals(diagnostic.roomRowCount, presentation.roomRowCount)
        assertEquals(diagnostic.writeAttemptCount, presentation.writeAttemptCount)
        assertEquals(diagnostic.clientRecordVersion, presentation.clientRecordVersion)
        assertEquals(diagnostic.healthConnectMatchCount, presentation.healthConnectMatchCount)
        assertEquals(
            diagnostic.healthConnectExpectedVersionMatchCount,
            presentation.healthConnectExpectedVersionMatchCount,
        )
        assertEquals(diagnostic.versionMatch, presentation.versionMatch)
        assertEquals(diagnostic.localFinalization, presentation.localFinalization)
        assertEquals(
            listOf("RETRY_SYNC", "FAILED", "WRITE", "java.io.IOException"),
            listOf(
                presentation.nextAction.name,
                presentation.evidence.name,
                presentation.phase,
                presentation.code,
            ),
        )
    }

    @Test
    fun `nullable readback and classification facts stay explicitly unknown`() {
        val presentation = diagnostic().toDiagnosticsPresentation(
            availability = HealthConnectAvailability.ProviderUpdateRequired,
            permission = HealthConnectPermission.NOT_REQUESTED,
            busy = false,
        )

        assertEquals(
            listOf("unknown", "unknown", "unknown"),
            presentation.readbackFacts.map(DiagnosticFact::value),
        )
        assertEquals(
            listOf("READY", "NONE", "unknown", "unknown"),
            presentation.classificationFacts.map(DiagnosticFact::value),
        )
    }

    @Test
    fun `closed next action mapping exposes only the action selected by the model`() {
        val expected = mapOf(
            DiagnosticNextAction.RUN_SYNC to DiagnosticsAction.RUN_SYNC,
            DiagnosticNextAction.RETRY_SYNC to DiagnosticsAction.RUN_SYNC,
            DiagnosticNextAction.CONFIRM to DiagnosticsAction.CONFIRM,
            DiagnosticNextAction.RECONCILE to DiagnosticsAction.RECONCILE,
            DiagnosticNextAction.REQUEST_PERMISSION to DiagnosticsAction.REQUEST_PERMISSION,
            DiagnosticNextAction.CHECK_HEALTH_CONNECT to DiagnosticsAction.CHECK_HEALTH_CONNECT,
        )

        expected.forEach { (nextAction, expectedAction) ->
            val primary = diagnostic(nextAction = nextAction).toDiagnosticsPresentation(
                availability = HealthConnectAvailability.Available,
                permission = HealthConnectPermission.NOT_GRANTED,
                busy = false,
            ).primaryAction

            assertEquals(nextAction.name, expectedAction, primary?.action)
        }
        assertNull(
            diagnostic(nextAction = DiagnosticNextAction.NONE).toDiagnosticsPresentation(
                availability = HealthConnectAvailability.Available,
                permission = HealthConnectPermission.GRANTED,
                busy = false,
            ).primaryAction,
        )
    }

    @Test
    fun `verified state keeps the explicit deterministic idempotency rerun`() {
        val primary = diagnostic(
            statusCode = "VERIFIED",
            safeMessage = "Health Connect contains exactly one matching record at the expected version.",
            nextAction = DiagnosticNextAction.NONE,
            evidence = DiagnosticEvidence.VERIFIED,
            durableStatus = SyncStatus.VERIFIED,
        ).toDiagnosticsPresentation(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            busy = false,
        ).primaryAction

        assertEquals(DiagnosticsAction.RUN_SYNC, primary?.action)
        assertEquals("Run idempotency check", primary?.label)
        assertTrue(primary?.enabled == true)
    }

    @Test
    fun `busy and unavailable states close mutation actions while retaining their truthful labels`() {
        val busy = diagnostic(nextAction = DiagnosticNextAction.RECONCILE).toDiagnosticsPresentation(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            busy = true,
        )
        assertFalse(busy.primaryAction!!.enabled)
        assertTrue(busy.utilityActions.all { !it.enabled })

        val unavailable = diagnostic(nextAction = DiagnosticNextAction.CONFIRM).toDiagnosticsPresentation(
            availability = HealthConnectAvailability.Unavailable,
            permission = HealthConnectPermission.NOT_REQUESTED,
            busy = false,
        )
        assertEquals("Confirm Health Connect record", unavailable.primaryAction?.label)
        assertFalse(unavailable.primaryAction!!.enabled)
    }

    @Test
    fun `sanitized error code and message are the only error detail projected`() {
        val presentation = diagnostic(
            statusCode = "REFRESH_FAILED",
            safeMessage = "Diagnostics refresh failed; retry the refresh.",
            nextAction = DiagnosticNextAction.CHECK_HEALTH_CONNECT,
            evidence = DiagnosticEvidence.FAILED,
            durableStatus = SyncStatus.RETRYABLE_ERROR,
        ).copy(code = "java.io.IOException").toDiagnosticsPresentation(
            availability = HealthConnectAvailability.Available,
            permission = HealthConnectPermission.GRANTED,
            busy = false,
        )

        assertEquals("Diagnostics refresh failed; retry the refresh.", presentation.safeMessage)
        assertEquals("java.io.IOException", presentation.code)
        assertEquals(DiagnosticsAction.CHECK_HEALTH_CONNECT, presentation.primaryAction?.action)
    }

    private fun diagnostic(
        statusCode: String = "READY",
        safeMessage: String = "Ready to run the synthetic sync.",
        nextAction: DiagnosticNextAction = DiagnosticNextAction.NONE,
        evidence: DiagnosticEvidence = DiagnosticEvidence.READY,
        durableStatus: SyncStatus = SyncStatus.PENDING,
    ) = Gate1Diagnostic(
        statusCode = statusCode,
        safeMessage = safeMessage,
        nextAction = nextAction,
        evidence = evidence,
        durableStatus = durableStatus,
        phase = null,
        code = null,
        roomRowCount = 1,
        writeAttemptCount = 0,
        clientRecordVersion = 1,
        healthConnectMatchCount = null,
        healthConnectExpectedVersionMatchCount = null,
        versionMatch = null,
        localFinalization = null,
    )
}
