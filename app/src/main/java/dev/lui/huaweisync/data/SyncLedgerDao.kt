package dev.lui.huaweisync.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SyncLedgerDao {
    @Query("SELECT * FROM sync_ledger WHERE clientRecordId = :clientRecordId LIMIT 1")
    suspend fun findByClientRecordId(clientRecordId: String): SyncLedgerEntity?

    @Query("SELECT COUNT(*) FROM sync_ledger WHERE clientRecordId = :clientRecordId")
    suspend fun countByClientRecordId(clientRecordId: String): Int

    @Upsert
    suspend fun upsert(entity: SyncLedgerEntity)
}
