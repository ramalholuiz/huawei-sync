package dev.lui.huaweisync.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SyncLedgerEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncLedgerDao(): SyncLedgerDao
}
