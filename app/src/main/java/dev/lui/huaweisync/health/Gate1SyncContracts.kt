package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.PreparedLedgerWorkout
import dev.lui.huaweisync.data.SyncBlock
import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncFailure
import dev.lui.huaweisync.data.SyncFailureDisposition
import dev.lui.huaweisync.data.SyncLedgerEntry

/** Coordinator-facing persistence boundary; implementations must keep each transition atomic. */
interface Gate1SyncLedger {
    suspend fun findBySource(sourceProvider: String, sourceRecordId: String): SyncLedgerEntry?

    suspend fun prepare(workout: PreparedLedgerWorkout): SyncLedgerEntry

    suspend fun beginWrite(clientRecordId: String): SyncLedgerEntry

    suspend fun recordBlocked(clientRecordId: String, block: SyncBlock): SyncLedgerEntry

    suspend fun recordFailure(clientRecordId: String, failure: SyncFailure): SyncLedgerEntry

    suspend fun recordAccepted(
        clientRecordId: String,
        healthConnectRecordId: String?,
    ): SyncLedgerEntry

    /**
     * Best-effort recovery after the external system accepted a write but normal local
     * finalization failed. The accepted timestamp and optional external ID are durable facts.
     */
    suspend fun recordAcceptanceUncertain(
        clientRecordId: String,
        healthConnectRecordId: String?,
    ): SyncLedgerEntry

    suspend fun beginVerification(clientRecordId: String): SyncLedgerEntry

    suspend fun confirm(clientRecordId: String): SyncLedgerEntry

    /** Keeps an unknown external write quarantined until reconciliation is conclusive. */
    suspend fun recordReconciliationPending(
        clientRecordId: String,
        failure: SyncFailure,
    ): SyncLedgerEntry

    /** Unlocks a new write only after the external system authoritatively reports absence. */
    suspend fun reconcileForRetry(clientRecordId: String): SyncLedgerEntry

    /** Records an existing external record found by deterministic reconciliation. */
    suspend fun reconcileAsAccepted(
        clientRecordId: String,
        healthConnectRecordId: String? = null,
    ): SyncLedgerEntry
}

fun interface SyncPreflight {
    suspend fun check(): SyncPreflightResult
}

sealed interface SyncPreflightResult {
    data object Ready : SyncPreflightResult

    data class Blocked(val block: SyncBlock) : SyncPreflightResult
}

data class HealthConfirmationRequest(
    val clientRecordId: String,
    val clientRecordVersion: Long,
    val externalRecordId: String?,
)

fun interface HealthWorkoutConfirmer {
    suspend fun confirm(request: HealthConfirmationRequest): HealthConfirmationResult
}

data class HealthReconciliationRequest(
    val clientRecordId: String,
    val clientRecordVersion: Long,
    val externalRecordId: String?,
)

fun interface HealthWorkoutReconciler {
    suspend fun reconcile(request: HealthReconciliationRequest): HealthReconciliationResult
}

sealed interface HealthReconciliationResult {
    data class Found(val externalRecordId: String) : HealthReconciliationResult {
        init {
            require(externalRecordId.isNotBlank()) { "A found record must have an external ID." }
        }
    }

    data class AuthoritativelyAbsent(val code: String) : HealthReconciliationResult {
        init {
            requireStableReconciliationCode(code)
        }
    }

    data class Inconclusive(val code: String) : HealthReconciliationResult {
        init {
            requireStableReconciliationCode(code)
        }
    }

    data class Failed(val failure: SyncFailure) : HealthReconciliationResult {
        init {
            require(failure.disposition == SyncFailureDisposition.RETRYABLE) {
                "Reconciliation failures must remain retryable."
            }
            require(failure.phase == SyncErrorPhase.RECONCILIATION) {
                "Reconciliation failures must use the reconciliation phase."
            }
        }
    }
}

sealed interface HealthConfirmationResult {
    data object Confirmed : HealthConfirmationResult

    data class Absent(val code: String) : HealthConfirmationResult

    data class Inconclusive(val code: String) : HealthConfirmationResult

    data class Failed(val failure: SyncFailure) : HealthConfirmationResult {
        init {
            require(failure.disposition == SyncFailureDisposition.RETRYABLE) {
                "Confirmation failures must remain retryable."
            }
            require(failure.phase == SyncErrorPhase.VERIFICATION) {
                "Confirmation failures must use the verification phase."
            }
        }
    }
}

private fun requireStableReconciliationCode(code: String) {
    require(code.matches(Regex("[A-Z][A-Z0-9_]{0,63}"))) {
        "Reconciliation codes must be stable uppercase identifiers of at most 64 characters."
    }
}

enum class SyncPhase {
    PREFLIGHT,
    EXTERNAL_WRITE,
    LOCAL_FINALIZATION,
    CONFIRMATION,
    RECONCILIATION,
}

enum class LocalFinalizationStatus {
    FINALIZED,
    RECONCILIATION_PENDING,
    FAILED,
}
