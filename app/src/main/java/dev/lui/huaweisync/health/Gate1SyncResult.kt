package dev.lui.huaweisync.health

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
