package dev.lui.huaweisync

import android.app.Application
import androidx.room.Room
import dev.lui.huaweisync.data.AppDatabase
import dev.lui.huaweisync.data.AppDatabaseMigrations
import dev.lui.huaweisync.data.SyncLedgerStore

class HuaweiSyncApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "huawei-sync.db")
            .addMigrations(AppDatabaseMigrations.MIGRATION_1_2)
            .build()
    }

    val ledgerStore: SyncLedgerStore by lazy { database.syncLedgerStore() }
}
