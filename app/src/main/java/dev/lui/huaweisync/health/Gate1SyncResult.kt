package dev.lui.huaweisync.health

data class Gate1SyncResult(
    val clientRecordId: String,
    val clientRecordVersion: Long,
    val ledgerRowsForClientRecordId: Int,
    val writeCountForClientRecordId: Int,
)
