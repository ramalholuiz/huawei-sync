package dev.lui.huaweisync.diagnostics

import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncFailureDisposition
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.health.ConfirmationPendingReason
import dev.lui.huaweisync.health.Gate1SyncResult
import dev.lui.huaweisync.health.ReconciliationResolution
import java.time.Instant

/** Privacy-safe, low-cardinality projection shared by on-screen and exported diagnostics. */
data class Gate1Diagnostic(
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
) {
    init {
        require(SAFE_TOKEN.matches(statusCode))
        require(safeMessage.isNotBlank() && safeMessage.length <= MAX_MESSAGE_LENGTH)
        require(roomRowCount >= 0)
        require(writeAttemptCount >= 0)
        require(clientRecordVersion >= 1)
        require(healthConnectMatchCount == null || healthConnectMatchCount >= 0)
        require(
            healthConnectExpectedVersionMatchCount == null ||
                healthConnectExpectedVersionMatchCount >= 0,
        )
        require(
            healthConnectMatchCount == null ||
                healthConnectExpectedVersionMatchCount == null ||
                healthConnectExpectedVersionMatchCount <= healthConnectMatchCount,
        )
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 160
        val SAFE_TOKEN = Regex("^[A-Z][A-Z0-9_]{0,63}$")
    }
}

enum class HealthConnectAvailability { AVAILABLE, NOT_INSTALLED, UPDATE_REQUIRED, UNAVAILABLE }
enum class HealthConnectPermission { GRANTED, NOT_GRANTED, NOT_REQUESTED }

data class Gate1InspectionFacts(
    val matchingRecordCount: Int,
    val expectedVersionMatchCount: Int,
) {
    init {
        require(matchingRecordCount >= 0)
        require(expectedVersionMatchCount in 0..matchingRecordCount)
    }

    val versionMatch: Boolean
        get() = matchingRecordCount == 1 && expectedVersionMatchCount == 1
}

data class Gate1ExportInput(
    val appVersion: String,
    val buildType: String,
    val generatedAt: Instant,
    val availability: HealthConnectAvailability,
    val permission: HealthConnectPermission,
    val diagnostic: Gate1Diagnostic,
)

/** Exhaustive pure mapper. It never carries record IDs, payloads, or provider/exception text. */
object Gate1Diagnostics {
    const val MAX_EXPORT_LENGTH = 2_048
    private const val UNSAFE_CODE = "UNSAFE_CODE_REDACTED"
    private val safeCodePattern = Regex("^[A-Z][A-Z0-9_]{0,63}$")
    private val safeBuildValuePattern = Regex("^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$")

    fun fromLedger(
        entry: SyncLedgerEntry,
        inspection: Gate1InspectionFacts? = null,
    ): Gate1Diagnostic {
        val guidance = when (entry.status) {
            SyncStatus.PENDING -> Guidance("READY", "Ready to run the synthetic sync.", DiagnosticNextAction.RUN_SYNC, DiagnosticEvidence.READY)
            SyncStatus.WRITING -> Guidance("WRITE_INTERRUPTED", "A write may have been interrupted and must be reconciled.", DiagnosticNextAction.RECONCILE, DiagnosticEvidence.NEEDS_RECONCILIATION)
            SyncStatus.SYNCED -> Guidance("ACCEPTED", "The write was accepted and needs confirmation.", DiagnosticNextAction.CONFIRM, DiagnosticEvidence.ACCEPTED)
            SyncStatus.VERIFICATION_PENDING -> Guidance("CONFIRMATION_PENDING", "Health Connect confirmation is still required.", DiagnosticNextAction.CONFIRM, DiagnosticEvidence.NEEDS_CONFIRMATION)
            SyncStatus.VERIFIED -> Guidance("VERIFIED", "The deterministic record is verified in Health Connect.", DiagnosticNextAction.NONE, DiagnosticEvidence.VERIFIED)
            SyncStatus.PERMISSION_BLOCKED -> Guidance("PERMISSION_BLOCKED", "Health Connect permission is required.", DiagnosticNextAction.REQUEST_PERMISSION, DiagnosticEvidence.BLOCKED)
            SyncStatus.ENVIRONMENT_BLOCKED -> Guidance("ENVIRONMENT_BLOCKED", "Health Connect is unavailable or needs attention.", DiagnosticNextAction.CHECK_HEALTH_CONNECT, DiagnosticEvidence.BLOCKED)
            SyncStatus.RETRYABLE_ERROR -> Guidance("RETRYABLE_ERROR", "The last safe operation failed and may be retried.", DiagnosticNextAction.RETRY_SYNC, DiagnosticEvidence.FAILED)
            SyncStatus.PERMANENT_ERROR -> Guidance("PERMANENT_ERROR", "The operation cannot be retried without an app change.", DiagnosticNextAction.NONE, DiagnosticEvidence.FAILED)
            SyncStatus.RECONCILIATION_PENDING -> Guidance("RECONCILIATION_PENDING", "Health Connect and the local ledger must be reconciled.", DiagnosticNextAction.RECONCILE, DiagnosticEvidence.NEEDS_RECONCILIATION)
        }
        return diagnostic(
            guidance = guidance,
            durableStatus = entry.status,
            phase = entry.lastErrorPhase?.name,
            code = safeCode(entry.lastErrorCode),
            roomRows = 1,
            attempts = entry.attemptCount,
            clientRecordVersion = entry.clientRecordVersion,
            inspection = inspection,
            localFinalization = null,
        )
    }

    fun fromResult(
        result: Gate1SyncResult,
        inspection: Gate1InspectionFacts? = null,
    ): Gate1Diagnostic {
        val guidance = when (result) {
            is Gate1SyncResult.Completed -> Guidance("ACCEPTED", "The write was accepted and needs confirmation.", DiagnosticNextAction.CONFIRM, DiagnosticEvidence.ACCEPTED)
            is Gate1SyncResult.Blocked -> when (result.reason) {
                SyncBlockReason.PERMISSION -> Guidance("PERMISSION_BLOCKED", "Health Connect permission is required.", DiagnosticNextAction.REQUEST_PERMISSION, DiagnosticEvidence.BLOCKED)
                SyncBlockReason.ENVIRONMENT -> Guidance("ENVIRONMENT_BLOCKED", "Health Connect is unavailable or needs attention.", DiagnosticNextAction.CHECK_HEALTH_CONNECT, DiagnosticEvidence.BLOCKED)
            }
            is Gate1SyncResult.WriteFailed -> when (result.disposition) {
                SyncFailureDisposition.RETRYABLE -> Guidance("RETRYABLE_ERROR", "The write failed safely and may be retried.", DiagnosticNextAction.RETRY_SYNC, DiagnosticEvidence.FAILED)
                SyncFailureDisposition.PERMANENT -> Guidance("PERMANENT_ERROR", "The write cannot be retried without an app change.", DiagnosticNextAction.NONE, DiagnosticEvidence.FAILED)
            }
            is Gate1SyncResult.Confirmed -> Guidance("VERIFIED", "The deterministic record is verified in Health Connect.", DiagnosticNextAction.NONE, DiagnosticEvidence.VERIFIED)
            is Gate1SyncResult.ConfirmationPending -> when (result.reason) {
                ConfirmationPendingReason.ABSENT -> Guidance("CONFIRMATION_ABSENT", "The expected record was not confirmed; reconcile before retrying.", DiagnosticNextAction.RECONCILE, DiagnosticEvidence.NEEDS_RECONCILIATION)
                ConfirmationPendingReason.INCONCLUSIVE,
                ConfirmationPendingReason.FAILURE,
                -> Guidance("CONFIRMATION_PENDING", "Health Connect confirmation is inconclusive.", DiagnosticNextAction.CONFIRM, DiagnosticEvidence.NEEDS_CONFIRMATION)
            }
            is Gate1SyncResult.Reconciled -> when (result.resolution) {
                ReconciliationResolution.EXISTING_ACCEPTED -> Guidance("ACCEPTED", "An existing Health Connect record was accepted and needs confirmation.", DiagnosticNextAction.CONFIRM, DiagnosticEvidence.ACCEPTED)
                ReconciliationResolution.RETRY_ALLOWED -> Guidance("READY", "Authoritative absence allows the synthetic sync to run.", DiagnosticNextAction.RUN_SYNC, DiagnosticEvidence.READY)
            }
            is Gate1SyncResult.ReconciliationPending -> Guidance("RECONCILIATION_PENDING", "Reconciliation is inconclusive and no blind retry is allowed.", DiagnosticNextAction.RECONCILE, DiagnosticEvidence.NEEDS_RECONCILIATION)
            is Gate1SyncResult.ExternalAccepted -> Guidance("LOCAL_FINALIZATION_PENDING", "Health Connect accepted the record but local finalization needs reconciliation.", DiagnosticNextAction.RECONCILE, DiagnosticEvidence.NEEDS_RECONCILIATION)
        }
        val phase = when (result) {
            is Gate1SyncResult.Completed -> null
            is Gate1SyncResult.Blocked -> result.phase.name
            is Gate1SyncResult.WriteFailed -> result.phase.name
            is Gate1SyncResult.Confirmed -> result.phase.name
            is Gate1SyncResult.ConfirmationPending -> result.phase.name
            is Gate1SyncResult.Reconciled -> result.phase.name
            is Gate1SyncResult.ReconciliationPending -> result.phase.name
            is Gate1SyncResult.ExternalAccepted -> result.phase.name
        }
        val code = when (result) {
            is Gate1SyncResult.Completed -> null
            is Gate1SyncResult.Blocked -> result.code
            is Gate1SyncResult.WriteFailed -> result.code
            is Gate1SyncResult.Confirmed -> result.code
            is Gate1SyncResult.ConfirmationPending -> result.code
            is Gate1SyncResult.Reconciled -> result.code
            is Gate1SyncResult.ReconciliationPending -> result.code
            is Gate1SyncResult.ExternalAccepted -> result.code
        }
        val finalization = when (result) {
            is Gate1SyncResult.Confirmed -> result.localFinalizationStatus.name
            is Gate1SyncResult.ConfirmationPending -> result.localFinalizationStatus.name
            is Gate1SyncResult.Reconciled -> result.localFinalizationStatus.name
            is Gate1SyncResult.ReconciliationPending -> result.localFinalizationStatus.name
            is Gate1SyncResult.ExternalAccepted -> result.localFinalizationStatus.name
            else -> null
        }
        return diagnostic(
            guidance = guidance,
            durableStatus = null,
            phase = phase,
            code = safeCode(code),
            roomRows = result.ledgerRowsForClientRecordId,
            attempts = result.writeCountForClientRecordId,
            clientRecordVersion = result.clientRecordVersion,
            inspection = inspection,
            localFinalization = finalization,
        )
    }

    /** Stable line-oriented format suitable for Android's text sharing intent. */
    fun export(input: Gate1ExportInput): String {
        val diagnostic = input.diagnostic
        val report = buildString {
            appendLine("huawei_sync_gate1_diagnostics")
            appendLine("schema_version=1")
            appendLine("app_version=${safeBuildValue(input.appVersion)}")
            appendLine("build_type=${safeBuildValue(input.buildType)}")
            appendLine("generated_at=${input.generatedAt}")
            appendLine("availability=${input.availability.name}")
            appendLine("permission=${input.permission.name}")
            appendLine("status=${diagnostic.statusCode}")
            appendLine("message=${diagnostic.safeMessage}")
            appendLine("next_action=${diagnostic.nextAction.name}")
            appendLine("durable_status=${diagnostic.durableStatus?.name ?: "unknown"}")
            appendLine("phase=${diagnostic.phase ?: "unknown"}")
            appendLine("code=${diagnostic.code ?: "unknown"}")
            appendLine("room_row_count=${diagnostic.roomRowCount}")
            appendLine("write_attempt_count=${diagnostic.writeAttemptCount}")
            appendLine("client_record_version=${diagnostic.clientRecordVersion}")
            appendLine("health_connect_match_count=${diagnostic.healthConnectMatchCount ?: "unknown"}")
            appendLine("health_connect_expected_version_match_count=${diagnostic.healthConnectExpectedVersionMatchCount ?: "unknown"}")
            appendLine("version_match=${diagnostic.versionMatch ?: "unknown"}")
            appendLine("local_finalization=${diagnostic.localFinalization ?: "unknown"}")
            appendLine("evidence=${diagnostic.evidence.name}")
        }
        check(report.length <= MAX_EXPORT_LENGTH)
        return report
    }

    private fun diagnostic(
        guidance: Guidance,
        durableStatus: SyncStatus?,
        phase: String?,
        code: String?,
        roomRows: Int,
        attempts: Int,
        clientRecordVersion: Long,
        inspection: Gate1InspectionFacts?,
        localFinalization: String?,
    ) = Gate1Diagnostic(
        statusCode = guidance.statusCode,
        safeMessage = guidance.safeMessage,
        nextAction = guidance.nextAction,
        evidence = guidance.evidence,
        durableStatus = durableStatus,
        phase = phase,
        code = code,
        roomRowCount = roomRows,
        writeAttemptCount = attempts,
        clientRecordVersion = clientRecordVersion,
        healthConnectMatchCount = inspection?.matchingRecordCount,
        healthConnectExpectedVersionMatchCount = inspection?.expectedVersionMatchCount,
        versionMatch = inspection?.versionMatch,
        localFinalization = localFinalization,
    )

    private fun safeCode(code: String?): String? = code?.let {
        if (safeCodePattern.matches(it)) it else UNSAFE_CODE
    }

    private fun safeBuildValue(value: String): String =
        if (safeBuildValuePattern.matches(value)) value else "redacted"

    private data class Guidance(
        val statusCode: String,
        val safeMessage: String,
        val nextAction: DiagnosticNextAction,
        val evidence: DiagnosticEvidence,
    )
}
