package dev.lui.huaweisync.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SyncLedgerEntity::class], version = 2, exportSchema = false)
@TypeConverters(SyncLedgerConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncLedgerDao(): SyncLedgerDao
}
