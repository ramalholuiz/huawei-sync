package dev.lui.huaweisync.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {
    private lateinit var context: Context
    private val databaseName = "migration-v1-v2-test.db"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrationPreservesKnownV1FactsAndMarksUnknownSemanticsForReconciliation() = runTest {
        createPopulatedVersionOneDatabase()

        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabaseMigrations.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        try {
            val row = database.syncLedgerDao().findByClientRecordId("stable-client-id")!!
            assertEquals("synthetic", row.sourceProvider)
            assertEquals("legacy-source-id", row.sourceRecordId)
            assertEquals(7L, row.clientRecordVersion)
            assertEquals("health-connect-id", row.healthConnectRecordId)
            assertEquals(3, row.attemptCount)
            assertEquals(SyncStatus.RECONCILIATION_PENDING, row.status)
            assertEquals(123456789L, row.createdAtEpochMillis)
            assertEquals(123456789L, row.updatedAtEpochMillis)
            assertEquals(123456789L, row.acceptedAtEpochMillis)
            assertNull(row.sourceVersion)
            assertNull(row.contentHash)
            assertNull(row.confirmedAtEpochMillis)
            assertNull(row.lastErrorCode)
            assertNull(row.lastErrorPhase)
            assertNull(row.lastErrorAtEpochMillis)
            assertNull(row.lastErrorMessage)

            val neverAccepted = database.syncLedgerDao().findByClientRecordId("no-health-id")!!
            assertNull(neverAccepted.healthConnectRecordId)
            assertNull(neverAccepted.acceptedAtEpochMillis)
            assertEquals(SyncStatus.RECONCILIATION_PENDING, neverAccepted.status)
        } finally {
            database.close()
        }
    }

    @Test
    fun migrationRejectsAmbiguousLegacyLogicalSourcesWithoutDroppingRows() {
        createPopulatedVersionOneDatabase()
        SQLiteDatabase.openDatabase(
            context.getDatabasePath(databaseName).path,
            null,
            SQLiteDatabase.OPEN_READWRITE,
        ).use { database ->
            database.execSQL(
                """
                INSERT INTO `sync_ledger` VALUES (
                    'duplicate-client-id', 'SYNTHETIC', 'legacy-source-id',
                    'synthetic:legacy-source-id', 1, NULL, 123456791, 0
                )
                """.trimIndent(),
            )
        }

        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabaseMigrations.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        val failure = runCatching { database.openHelper.writableDatabase }.exceptionOrNull()
        database.close()

        assertTrue(failure.causeChainContains<SQLiteConstraintException>())
    }

    private fun createPopulatedVersionOneDatabase() {
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `sync_ledger` (
                    `clientRecordId` TEXT NOT NULL,
                    `source` TEXT NOT NULL,
                    `sourceWorkoutId` TEXT NOT NULL,
                    `dedupeKey` TEXT NOT NULL,
                    `clientRecordVersion` INTEGER NOT NULL,
                    `healthConnectRecordId` TEXT,
                    `lastSyncedAtEpochMillis` INTEGER NOT NULL,
                    `successfulWriteCount` INTEGER NOT NULL,
                    PRIMARY KEY(`clientRecordId`)
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `sync_ledger` VALUES (
                    'stable-client-id', 'SYNTHETIC', 'legacy-source-id',
                    'synthetic:legacy-source-id', 7, 'health-connect-id', 123456789, 3
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `sync_ledger` VALUES (
                    'no-health-id', 'SYNTHETIC', 'not-accepted-source-id',
                    'synthetic:not-accepted-source-id', 1, NULL, 123456790, 0
                )
                """.trimIndent(),
            )
            database.version = 1
        }
    }

    private inline fun <reified T : Throwable> Throwable?.causeChainContains(): Boolean {
        var current = this
        while (current != null) {
            if (current is T) return true
            current = current.cause
        }
        return false
    }
}
