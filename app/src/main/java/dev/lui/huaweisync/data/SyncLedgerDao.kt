package dev.lui.huaweisync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SyncLedgerDao {
    @Query("SELECT * FROM sync_ledger WHERE clientRecordId = :clientRecordId LIMIT 1")
    suspend fun findByClientRecordId(clientRecordId: String): SyncLedgerEntity?

    @Query("SELECT * FROM sync_ledger WHERE sourceProvider = :sourceProvider AND sourceRecordId = :sourceRecordId LIMIT 1")
    suspend fun findBySource(sourceProvider: String, sourceRecordId: String): SyncLedgerEntity?

    @Query("SELECT COUNT(*) FROM sync_ledger WHERE clientRecordId = :clientRecordId")
    suspend fun countByClientRecordId(clientRecordId: String): Int

    @Insert
    suspend fun insert(entity: SyncLedgerEntity)

    @Upsert
    suspend fun upsert(entity: SyncLedgerEntity)
}
