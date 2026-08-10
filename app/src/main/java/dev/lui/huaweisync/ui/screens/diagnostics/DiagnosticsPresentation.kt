package dev.lui.huaweisync.ui.screens.diagnostics

import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.health.HealthConnectAvailability

enum class DiagnosticsAction {
    CHECK_HEALTH_CONNECT,
    REQUEST_PERMISSION,
    RUN_SYNC,
    CONFIRM,
    RECONCILE,
    REFRESH,
    EXPORT,
}

data class DiagnosticsActionPresentation(
    val action: DiagnosticsAction,
    val label: String,
    val enabled: Boolean,
)

data class DiagnosticFact(
    val label: String,
    val value: String,
)

data class DiagnosticsPresentation(
    val availability: HealthConnectAvailability,
    val permission: HealthConnectPermission,
    val clientRecordId: String,
    val statusCode: String,
    val safeMessage: String,
    val nextAction: DiagnosticNextAction,
    val evidence: DiagnosticEvidence,
    val durableStatus: SyncStatus?,
    val phase: String?,
    val code: String?,
    val roomRowCount: Int,
    val writeAttemptCount: Int,
    val clientRecordVersion: Long,
    val healthConnectMatchCount: Int?,
    val healthConnectExpectedVersionMatchCount: Int?,
    val versionMatch: Boolean?,
    val localFinalization: String?,
    val primaryAction: DiagnosticsActionPresentation?,
    val utilityActions: List<DiagnosticsActionPresentation>,
) {
    val identityFacts: List<DiagnosticFact>
        get() = listOf(
            DiagnosticFact("Deterministic client record ID", clientRecordId),
            DiagnosticFact("Client record version", clientRecordVersion.toString()),
        )

    val ledgerFacts: List<DiagnosticFact>
        get() = listOf(
            DiagnosticFact("Durable status", durableStatus?.name ?: UNKNOWN),
            DiagnosticFact("Room rows", roomRowCount.toString()),
            DiagnosticFact("Write attempts", writeAttemptCount.toString()),
            DiagnosticFact("Local finalization", localFinalization ?: UNKNOWN),
        )

    val readbackFacts: List<DiagnosticFact>
        get() = listOf(
            DiagnosticFact("Health Connect matches", healthConnectMatchCount?.toString() ?: UNKNOWN),
            DiagnosticFact(
                "Expected-version matches",
                healthConnectExpectedVersionMatchCount?.toString() ?: UNKNOWN,
            ),
            DiagnosticFact("Exact version match", versionMatch?.toString() ?: UNKNOWN),
        )

    val classificationFacts: List<DiagnosticFact>
        get() = listOf(
            DiagnosticFact("Evidence", evidence.name),
            DiagnosticFact("Next action", nextAction.name),
            DiagnosticFact("Phase", phase ?: UNKNOWN),
            DiagnosticFact("Code", code ?: UNKNOWN),
        )
}

fun Gate1Diagnostic.toDiagnosticsPresentation(
    availability: HealthConnectAvailability,
    permission: HealthConnectPermission,
    busy: Boolean,
    clientRecordId: String = SyntheticWorkoutFactory.CLIENT_RECORD_ID,
): DiagnosticsPresentation = DiagnosticsPresentation(
    availability = availability,
    permission = permission,
    clientRecordId = clientRecordId,
    statusCode = statusCode,
    safeMessage = safeMessage,
    nextAction = nextAction,
    evidence = evidence,
    durableStatus = durableStatus,
    phase = phase,
    code = code,
    roomRowCount = roomRowCount,
    writeAttemptCount = writeAttemptCount,
    clientRecordVersion = clientRecordVersion,
    healthConnectMatchCount = healthConnectMatchCount,
    healthConnectExpectedVersionMatchCount = healthConnectExpectedVersionMatchCount,
    versionMatch = versionMatch,
    localFinalization = localFinalization,
    primaryAction = primaryAction(availability, permission, busy),
    utilityActions = utilityActions(busy),
)

private fun Gate1Diagnostic.primaryAction(
    availability: HealthConnectAvailability,
    permission: HealthConnectPermission,
    busy: Boolean,
): DiagnosticsActionPresentation? {
    val actionAndLabel = when (nextAction) {
        DiagnosticNextAction.NONE -> if (durableStatus == SyncStatus.VERIFIED) {
            DiagnosticsAction.RUN_SYNC to "Run idempotency check"
        } else {
            null
        }
        DiagnosticNextAction.RUN_SYNC -> DiagnosticsAction.RUN_SYNC to "Run synthetic sync"
        DiagnosticNextAction.RETRY_SYNC -> DiagnosticsAction.RUN_SYNC to "Retry synthetic sync"
        DiagnosticNextAction.CONFIRM -> DiagnosticsAction.CONFIRM to "Confirm Health Connect record"
        DiagnosticNextAction.RECONCILE -> DiagnosticsAction.RECONCILE to "Reconcile Room and Health Connect"
        DiagnosticNextAction.REQUEST_PERMISSION -> DiagnosticsAction.REQUEST_PERMISSION to "Request exercise permission"
        DiagnosticNextAction.CHECK_HEALTH_CONNECT -> DiagnosticsAction.CHECK_HEALTH_CONNECT to "Check Health Connect"
    } ?: return null

    val environmentAllowsAction = when (actionAndLabel.first) {
        DiagnosticsAction.CHECK_HEALTH_CONNECT -> true
        DiagnosticsAction.REQUEST_PERMISSION ->
            availability == HealthConnectAvailability.Available && permission != HealthConnectPermission.GRANTED
        DiagnosticsAction.RUN_SYNC,
        DiagnosticsAction.CONFIRM,
        DiagnosticsAction.RECONCILE,
        -> availability == HealthConnectAvailability.Available
        DiagnosticsAction.REFRESH,
        DiagnosticsAction.EXPORT,
        -> error("Utility action cannot be primary")
    }
    return DiagnosticsActionPresentation(
        action = actionAndLabel.first,
        label = actionAndLabel.second,
        enabled = !busy && environmentAllowsAction,
    )
}

private fun Gate1Diagnostic.utilityActions(busy: Boolean): List<DiagnosticsActionPresentation> = listOf(
    DiagnosticsActionPresentation(
        action = DiagnosticsAction.REFRESH,
        label = "Refresh Room and Health Connect counts",
        enabled = !busy,
    ),
    DiagnosticsActionPresentation(
        action = DiagnosticsAction.EXPORT,
        label = "Export privacy-safe diagnostics",
        enabled = !busy,
    ),
)

internal const val UNKNOWN = "unknown"
