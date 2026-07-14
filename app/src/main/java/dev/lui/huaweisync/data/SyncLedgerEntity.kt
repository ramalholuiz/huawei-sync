package dev.lui.huaweisync.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_ledger",
    indices = [Index(value = ["clientRecordId"], unique = true)],
)
data class SyncLedgerEntity(
    @PrimaryKey val clientRecordId: String,
    val source: String,
    val sourceWorkoutId: String,
    val dedupeKey: String,
    val clientRecordVersion: Long,
    val healthConnectRecordId: String?,
    val lastSyncedAtEpochMillis: Long,
    val successfulWriteCount: Int,
)
