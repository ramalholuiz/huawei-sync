package dev.lui.huaweisync.ui.state

import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncFailureDisposition
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.diagnostics.Gate1Diagnostic
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.health.Gate1SyncResult
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.health.ReconciliationResolution
import dev.lui.huaweisync.health.SyncPhase

/** Product wording backed only by facts already known to the Gate 1 vertical slice. */
enum class ProductHealthConnectStatus(val label: String) {
    UNAVAILABLE("Health Connect unavailable"),
    UPDATE_REQUIRED("Health Connect update required"),
    PERMISSION_REQUIRED("Health Connect permission required"),
    READY_TO_SYNC("Ready to sync"),
    WRITE_IN_PROGRESS("Writing to Health Connect"),
    ACCEPTED_AWAITING_READBACK("Accepted; awaiting Health Connect confirmation"),
    CONFIRMED_IN_HEALTH_CONNECT("Confirmed in Health Connect"),
    RECONCILIATION_REQUIRED("Reconciliation required"),
    RETRY_REQUIRED("Retry required"),
    ACTION_REQUIRED("Action required"),
    FAILED("Sync failed"),
}

/** Gate 2 has not passed, so no stronger GymRats claim is representable. */
enum class ProductGymRatsStatus(val label: String) {
    READY_TO_READ("Available for GymRats to import"),
}

enum class ProductSyncPhase {
    IDLE,
    PREFLIGHT,
    WRITE,
    ACCEPTANCE,
    VERIFICATION,
    RECONCILIATION,
}

data class ProductVerificationEvidence(
    val diagnosticEvidence: DiagnosticEvidence?,
    val realReadbackConfirmed: Boolean,
    val healthConnectMatchCount: Int?,
    val expectedVersionMatchCount: Int?,
    val versionMatch: Boolean?,
)

data class ProductSyncState(
    val healthConnectStatus: ProductHealthConnectStatus,
    val gymRatsStatus: ProductGymRatsStatus,
    val phase: ProductSyncPhase,
    val attemptCount: Int,
    val ledgerWorkoutCount: Int,
    val verification: ProductVerificationEvidence,
    val sanitizedFailureSummary: String?,
) {
    init {
        require(attemptCount >= 0)
        require(ledgerWorkoutCount >= 0)
        require(
            healthConnectStatus != ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT ||
                verification.realReadbackConfirmed,
        ) { "Health Connect confirmation requires real readback evidence." }
    }
}

/** Pure, exhaustive projection from Gate 1 facts into immutable product state. */
object ProductSyncStateMapper {
    fun from(
        availability: HealthConnectAvailability,
        permission: HealthConnectPermission,
        result: Gate1SyncResult? = null,
        diagnostic: Gate1Diagnostic? = null,
        ledgerEntries: List<SyncLedgerEntry> = emptyList(),
    ): ProductSyncState {
        val latestLedger = ledgerEntries.maxByOrNull(SyncLedgerEntry::updatedAtEpochMillis)
        val readbackConfirmed = when {
            result != null -> result is Gate1SyncResult.Confirmed
            diagnostic != null -> diagnostic.hasExactReadback()
            else -> latestLedger?.hasDurableReadback() == true
        }

        val factStatus = when {
            result != null -> mapResult(result)
            diagnostic != null -> mapDiagnostic(diagnostic)
            latestLedger != null -> mapLedgerStatus(
                status = latestLedger.status,
                confirmedAtEpochMillis = latestLedger.confirmedAtEpochMillis,
            )
            else -> ProductHealthConnectStatus.READY_TO_SYNC
        }
        val status = when {
            availability != HealthConnectAvailability.Available -> mapAvailability(availability)
            permission != HealthConnectPermission.GRANTED -> mapPermission(permission)
            readbackConfirmed -> ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT
            factStatus == ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT ->
                ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK
            else -> factStatus
        }

        val phase = when {
            result != null -> phaseFromResult(result)
            diagnostic?.phase != null -> phaseFromDiagnostic(diagnostic.phase)
            latestLedger != null -> phaseFromLedger(latestLedger)
            else -> ProductSyncPhase.IDLE
        }
        val attemptCount = maxOf(
            result?.writeCountForClientRecordId ?: 0,
            diagnostic?.writeAttemptCount ?: 0,
            ledgerEntries.sumOf { it.attemptCount },
        )
        val failureSummary = if (status.isFailureOrBlock()) {
            diagnostic?.safeMessage ?: latestLedger?.lastErrorMessage ?: result.failureCodeOrNull()
        } else {
            null
        }

        return ProductSyncState(
            healthConnectStatus = status,
            gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
            phase = phase,
            attemptCount = attemptCount,
            ledgerWorkoutCount = ledgerEntries.size,
            verification = ProductVerificationEvidence(
                diagnosticEvidence = diagnostic?.evidence,
                realReadbackConfirmed = readbackConfirmed,
                healthConnectMatchCount = diagnostic?.healthConnectMatchCount,
                expectedVersionMatchCount = diagnostic?.healthConnectExpectedVersionMatchCount,
                versionMatch = diagnostic?.versionMatch,
            ),
            sanitizedFailureSummary = failureSummary,
        )
    }

    fun mapAvailability(availability: HealthConnectAvailability): ProductHealthConnectStatus =
        when (availability) {
            HealthConnectAvailability.Available -> ProductHealthConnectStatus.READY_TO_SYNC
            HealthConnectAvailability.ProviderUpdateRequired -> ProductHealthConnectStatus.UPDATE_REQUIRED
            HealthConnectAvailability.Unavailable -> ProductHealthConnectStatus.UNAVAILABLE
        }

    fun mapPermission(permission: HealthConnectPermission): ProductHealthConnectStatus =
        when (permission) {
            HealthConnectPermission.GRANTED -> ProductHealthConnectStatus.READY_TO_SYNC
            HealthConnectPermission.NOT_GRANTED,
            HealthConnectPermission.NOT_REQUESTED,
            -> ProductHealthConnectStatus.PERMISSION_REQUIRED
        }

    fun mapEvidence(evidence: DiagnosticEvidence): ProductHealthConnectStatus = when (evidence) {
        DiagnosticEvidence.READY -> ProductHealthConnectStatus.READY_TO_SYNC
        DiagnosticEvidence.ACCEPTED,
        DiagnosticEvidence.NEEDS_CONFIRMATION,
        DiagnosticEvidence.VERIFIED,
        -> ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK
        DiagnosticEvidence.NEEDS_RECONCILIATION -> ProductHealthConnectStatus.RECONCILIATION_REQUIRED
        DiagnosticEvidence.BLOCKED -> ProductHealthConnectStatus.ACTION_REQUIRED
        DiagnosticEvidence.FAILED -> ProductHealthConnectStatus.FAILED
    }

    fun mapLedgerStatus(
        status: SyncStatus,
        confirmedAtEpochMillis: Long?,
    ): ProductHealthConnectStatus = when (status) {
        SyncStatus.PENDING -> ProductHealthConnectStatus.READY_TO_SYNC
        SyncStatus.WRITING -> ProductHealthConnectStatus.WRITE_IN_PROGRESS
        SyncStatus.SYNCED,
        SyncStatus.VERIFICATION_PENDING,
        -> ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK
        SyncStatus.VERIFIED -> if (confirmedAtEpochMillis != null) {
            ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT
        } else {
            ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK
        }
        SyncStatus.PERMISSION_BLOCKED -> ProductHealthConnectStatus.PERMISSION_REQUIRED
        SyncStatus.ENVIRONMENT_BLOCKED -> ProductHealthConnectStatus.ACTION_REQUIRED
        SyncStatus.RETRYABLE_ERROR -> ProductHealthConnectStatus.RETRY_REQUIRED
        SyncStatus.PERMANENT_ERROR -> ProductHealthConnectStatus.FAILED
        SyncStatus.RECONCILIATION_PENDING -> ProductHealthConnectStatus.RECONCILIATION_REQUIRED
    }

    fun mapResult(result: Gate1SyncResult): ProductHealthConnectStatus = when (result) {
        is Gate1SyncResult.Completed -> ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK
        is Gate1SyncResult.Blocked -> when (result.reason) {
            SyncBlockReason.PERMISSION -> ProductHealthConnectStatus.PERMISSION_REQUIRED
            SyncBlockReason.ENVIRONMENT -> ProductHealthConnectStatus.ACTION_REQUIRED
        }
        is Gate1SyncResult.WriteFailed -> when (result.disposition) {
            SyncFailureDisposition.RETRYABLE -> ProductHealthConnectStatus.RETRY_REQUIRED
            SyncFailureDisposition.PERMANENT -> ProductHealthConnectStatus.FAILED
        }
        is Gate1SyncResult.Confirmed -> ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT
        is Gate1SyncResult.ConfirmationPending -> ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK
        is Gate1SyncResult.Reconciled -> when (result.resolution) {
            ReconciliationResolution.EXISTING_ACCEPTED -> ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK
            ReconciliationResolution.RETRY_ALLOWED -> ProductHealthConnectStatus.READY_TO_SYNC
        }
        is Gate1SyncResult.ReconciliationPending -> ProductHealthConnectStatus.RECONCILIATION_REQUIRED
        is Gate1SyncResult.ExternalAccepted -> ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK
    }

    private fun mapDiagnostic(diagnostic: Gate1Diagnostic): ProductHealthConnectStatus =
        if (diagnostic.hasExactReadback()) {
            ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT
        } else {
            mapEvidence(diagnostic.evidence)
        }

    private fun Gate1Diagnostic.hasExactReadback(): Boolean =
        evidence == DiagnosticEvidence.VERIFIED &&
            healthConnectMatchCount == 1 &&
            healthConnectExpectedVersionMatchCount == 1 &&
            versionMatch == true

    private fun SyncLedgerEntry.hasDurableReadback(): Boolean =
        status == SyncStatus.VERIFIED && confirmedAtEpochMillis != null

    private fun phaseFromResult(result: Gate1SyncResult): ProductSyncPhase = when (result) {
        is Gate1SyncResult.Completed -> ProductSyncPhase.ACCEPTANCE
        is Gate1SyncResult.Blocked -> mapPhase(result.phase)
        is Gate1SyncResult.WriteFailed -> mapPhase(result.phase)
        is Gate1SyncResult.Confirmed -> mapPhase(result.phase)
        is Gate1SyncResult.ConfirmationPending -> mapPhase(result.phase)
        is Gate1SyncResult.Reconciled -> mapPhase(result.phase)
        is Gate1SyncResult.ReconciliationPending -> mapPhase(result.phase)
        is Gate1SyncResult.ExternalAccepted -> mapPhase(result.phase)
    }

    private fun mapPhase(phase: SyncPhase): ProductSyncPhase = when (phase) {
        SyncPhase.PREFLIGHT -> ProductSyncPhase.PREFLIGHT
        SyncPhase.EXTERNAL_WRITE -> ProductSyncPhase.WRITE
        SyncPhase.LOCAL_FINALIZATION -> ProductSyncPhase.ACCEPTANCE
        SyncPhase.CONFIRMATION -> ProductSyncPhase.VERIFICATION
        SyncPhase.RECONCILIATION -> ProductSyncPhase.RECONCILIATION
    }

    private fun phaseFromDiagnostic(phase: String): ProductSyncPhase = when (phase) {
        SyncPhase.PREFLIGHT.name, SyncErrorPhase.PREPARATION.name -> ProductSyncPhase.PREFLIGHT
        SyncPhase.EXTERNAL_WRITE.name, SyncErrorPhase.WRITE.name -> ProductSyncPhase.WRITE
        SyncPhase.LOCAL_FINALIZATION.name, SyncErrorPhase.ACCEPTANCE.name -> ProductSyncPhase.ACCEPTANCE
        SyncPhase.CONFIRMATION.name, SyncErrorPhase.VERIFICATION.name -> ProductSyncPhase.VERIFICATION
        SyncPhase.RECONCILIATION.name, SyncErrorPhase.RECONCILIATION.name -> ProductSyncPhase.RECONCILIATION
        else -> ProductSyncPhase.IDLE
    }

    private fun phaseFromLedger(entry: SyncLedgerEntry): ProductSyncPhase = when (entry.status) {
        SyncStatus.PENDING -> ProductSyncPhase.IDLE
        SyncStatus.WRITING -> ProductSyncPhase.WRITE
        SyncStatus.SYNCED -> ProductSyncPhase.ACCEPTANCE
        SyncStatus.VERIFICATION_PENDING,
        SyncStatus.VERIFIED,
        -> ProductSyncPhase.VERIFICATION
        SyncStatus.PERMISSION_BLOCKED,
        SyncStatus.ENVIRONMENT_BLOCKED,
        -> ProductSyncPhase.PREFLIGHT
        SyncStatus.RETRYABLE_ERROR,
        SyncStatus.PERMANENT_ERROR,
        -> entry.lastErrorPhase?.let(::mapErrorPhase) ?: ProductSyncPhase.IDLE
        SyncStatus.RECONCILIATION_PENDING -> ProductSyncPhase.RECONCILIATION
    }

    private fun mapErrorPhase(phase: SyncErrorPhase): ProductSyncPhase = when (phase) {
        SyncErrorPhase.PREPARATION -> ProductSyncPhase.PREFLIGHT
        SyncErrorPhase.WRITE -> ProductSyncPhase.WRITE
        SyncErrorPhase.ACCEPTANCE -> ProductSyncPhase.ACCEPTANCE
        SyncErrorPhase.VERIFICATION -> ProductSyncPhase.VERIFICATION
        SyncErrorPhase.RECONCILIATION -> ProductSyncPhase.RECONCILIATION
    }

    private fun ProductHealthConnectStatus.isFailureOrBlock(): Boolean = when (this) {
        ProductHealthConnectStatus.UNAVAILABLE,
        ProductHealthConnectStatus.UPDATE_REQUIRED,
        ProductHealthConnectStatus.PERMISSION_REQUIRED,
        ProductHealthConnectStatus.RECONCILIATION_REQUIRED,
        ProductHealthConnectStatus.RETRY_REQUIRED,
        ProductHealthConnectStatus.ACTION_REQUIRED,
        ProductHealthConnectStatus.FAILED,
        -> true
        ProductHealthConnectStatus.READY_TO_SYNC,
        ProductHealthConnectStatus.WRITE_IN_PROGRESS,
        ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK,
        ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
        -> false
    }

    private fun Gate1SyncResult?.failureCodeOrNull(): String? = when (this) {
        null,
        is Gate1SyncResult.Completed,
        is Gate1SyncResult.Confirmed,
        is Gate1SyncResult.ConfirmationPending,
        is Gate1SyncResult.Reconciled,
        is Gate1SyncResult.ReconciliationPending,
        is Gate1SyncResult.ExternalAccepted,
        -> null
        is Gate1SyncResult.Blocked -> code
        is Gate1SyncResult.WriteFailed -> code
    }
}
