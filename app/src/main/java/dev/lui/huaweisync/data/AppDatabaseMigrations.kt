package dev.lui.huaweisync.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE `sync_ledger_v2` (
                    `clientRecordId` TEXT NOT NULL,
                    `sourceProvider` TEXT NOT NULL,
                    `sourceRecordId` TEXT NOT NULL,
                    `sourceVersion` TEXT,
                    `contentHash` TEXT,
                    `clientRecordVersion` INTEGER NOT NULL,
                    `healthConnectRecordId` TEXT,
                    `status` TEXT NOT NULL,
                    `attemptCount` INTEGER NOT NULL,
                    `acceptedAtEpochMillis` INTEGER,
                    `confirmedAtEpochMillis` INTEGER,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    `lastErrorCode` TEXT,
                    `lastErrorPhase` TEXT,
                    `lastErrorAtEpochMillis` INTEGER,
                    `lastErrorMessage` TEXT,
                    PRIMARY KEY(`clientRecordId`)
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `sync_ledger_v2` (
                    `clientRecordId`, `sourceProvider`, `sourceRecordId`, `sourceVersion`,
                    `contentHash`, `clientRecordVersion`, `healthConnectRecordId`, `status`,
                    `attemptCount`, `acceptedAtEpochMillis`, `confirmedAtEpochMillis`,
                    `createdAtEpochMillis`, `updatedAtEpochMillis`, `lastErrorCode`,
                    `lastErrorPhase`, `lastErrorAtEpochMillis`, `lastErrorMessage`
                )
                SELECT
                    `clientRecordId`, lower(`source`), `sourceWorkoutId`, NULL,
                    NULL, `clientRecordVersion`, `healthConnectRecordId`,
                    'RECONCILIATION_PENDING', `successfulWriteCount`,
                    CASE WHEN `healthConnectRecordId` IS NOT NULL THEN `lastSyncedAtEpochMillis` ELSE NULL END,
                    NULL, `lastSyncedAtEpochMillis`, `lastSyncedAtEpochMillis`,
                    NULL, NULL, NULL, NULL
                FROM `sync_ledger`
                """.trimIndent(),
            )
            database.execSQL("DROP TABLE `sync_ledger`")
            database.execSQL("ALTER TABLE `sync_ledger_v2` RENAME TO `sync_ledger`")
            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_sync_ledger_sourceProvider_sourceRecordId` " +
                    "ON `sync_ledger` (`sourceProvider`, `sourceRecordId`)",
            )
        }
    }
}
