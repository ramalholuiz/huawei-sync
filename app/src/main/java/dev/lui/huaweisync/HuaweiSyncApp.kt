package dev.lui.huaweisync

import android.app.Application
import androidx.room.Room
import dev.lui.huaweisync.data.AppDatabase

class HuaweiSyncApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "huawei-sync.db").build()
    }
}
