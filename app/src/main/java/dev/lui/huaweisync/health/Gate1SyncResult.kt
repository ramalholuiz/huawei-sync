package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncFailureDisposition

enum class ConfirmationPendingReason {
    ABSENT,
    INCONCLUSIVE,
    FAILURE,
}

enum class ReconciliationResolution {
    EXISTING_ACCEPTED,
    RETRY_ALLOWED,
}

sealed interface Gate1SyncResult {
    val clientRecordId: String
    val clientRecordVersion: Long
    val ledgerRowsForClientRecordId: Int
    val writeCountForClientRecordId: Int

    data class Completed(
        override val clientRecordId: String,
        override val clientRecordVersion: Long,
        override val ledgerRowsForClientRecordId: Int,
        override val writeCountForClientRecordId: Int,
    ) : Gate1SyncResult

    data class Blocked(
        override val clientRecordId: String,
        override val clientRecordVersion: Long,
        override val ledgerRowsForClientRecordId: Int,
        override val writeCountForClientRecordId: Int,
        val reason: SyncBlockReason,
        val phase: SyncPhase,
        val code: String,
    ) : Gate1SyncResult

    data class WriteFailed(
        override val clientRecordId: String,
        override val clientRecordVersion: Long,
        override val ledgerRowsForClientRecordId: Int,
        override val writeCountForClientRecordId: Int,
        val disposition: SyncFailureDisposition,
        val phase: SyncPhase,
        val code: String,
    ) : Gate1SyncResult

    data class Confirmed(
        override val clientRecordId: String,
        override val clientRecordVersion: Long,
        override val ledgerRowsForClientRecordId: Int,
        override val writeCountForClientRecordId: Int,
        val externalRecordId: String?,
        val phase: SyncPhase,
        val code: String,
        val localFinalizationStatus: LocalFinalizationStatus,
    ) : Gate1SyncResult

    data class ConfirmationPending(
        override val clientRecordId: String,
        override val clientRecordVersion: Long,
        override val ledgerRowsForClientRecordId: Int,
        override val writeCountForClientRecordId: Int,
        val externalRecordId: String?,
        val reason: ConfirmationPendingReason,
        val phase: SyncPhase,
        val code: String,
        val localFinalizationStatus: LocalFinalizationStatus,
    ) : Gate1SyncResult

    data class Reconciled(
        override val clientRecordId: String,
        override val clientRecordVersion: Long,
        override val ledgerRowsForClientRecordId: Int,
        override val writeCountForClientRecordId: Int,
        val externalRecordId: String?,
        val resolution: ReconciliationResolution,
        val phase: SyncPhase,
        val code: String,
        val localFinalizationStatus: LocalFinalizationStatus,
    ) : Gate1SyncResult

    data class ReconciliationPending(
        override val clientRecordId: String,
        override val clientRecordVersion: Long,
        override val ledgerRowsForClientRecordId: Int,
        override val writeCountForClientRecordId: Int,
        val externalRecordId: String?,
        val phase: SyncPhase,
        val code: String,
        val localFinalizationStatus: LocalFinalizationStatus,
    ) : Gate1SyncResult

    /** The external write was accepted; local state needs recovery or reconciliation. */
    data class ExternalAccepted(
        override val clientRecordId: String,
        override val clientRecordVersion: Long,
        override val ledgerRowsForClientRecordId: Int,
        override val writeCountForClientRecordId: Int,
        val externalRecordId: String?,
        val phase: SyncPhase,
        val code: String,
        val localFinalizationStatus: LocalFinalizationStatus,
    ) : Gate1SyncResult
}
