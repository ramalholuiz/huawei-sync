package dev.lui.huaweisync.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.lui.huaweisync.domain.ResolvedWorkoutMetadata
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncLedgerHistoryTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var clock: MutableLedgerClock
    private lateinit var store: SyncLedgerStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        clock = MutableLedgerClock(100L)
        store = database.syncLedgerStore(clock)
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(DURABLE_DATABASE_NAME)
    }

    @Test
    fun emptyLedgerReturnsEmptyHistory() = runTest {
        assertEquals(emptyList<SyncLedgerEntry>(), store.listHistory())
    }

    @Test
    fun historyIsOrderedByMostRecentUpdateWithDeterministicTieBreaking() = runTest {
        val tiedRows = listOf(
            store.prepare(workout("workout-b")),
            store.prepare(workout("workout-a")),
        )

        clock.now = 200L
        val mostRecent = store.prepare(workout("workout-c"))

        assertEquals(
            listOf(mostRecent.clientRecordId) + tiedRows.map(SyncLedgerEntry::clientRecordId).sorted(),
            store.listHistory().map(SyncLedgerEntry::clientRecordId),
        )
    }

    @Test
    fun updatedRowMovesToFrontAndReflectsLatestTransition() = runTest {
        val older = store.prepare(workout("older"))
        clock.now = 200L
        store.prepare(workout("newer"))

        clock.now = 300L
        store.beginWrite(older.clientRecordId)

        val history = store.listHistory()
        assertEquals(listOf("older", "newer"), history.map(SyncLedgerEntry::sourceRecordId))
        assertEquals(SyncStatus.WRITING, history.first().status)
        assertEquals(1, history.first().attemptCount)
        assertEquals(300L, history.first().updatedAtEpochMillis)
    }

    @Test
    fun durableHistoryRowsRemainReadableAfterDatabaseReopen() = runTest {
        database.close()
        context.deleteDatabase(DURABLE_DATABASE_NAME)

        val originalDatabase = openDurableDatabase()
        val originalClock = MutableLedgerClock(400L)
        val originalStore = originalDatabase.syncLedgerStore(originalClock)
        val prepared = originalStore.prepare(workout("durable-row"))
        originalClock.now = 500L
        originalStore.beginWrite(prepared.clientRecordId)
        originalDatabase.close()

        database = openDurableDatabase()
        val reopenedHistory = database.syncLedgerStore(MutableLedgerClock(600L)).listHistory()

        assertEquals(1, reopenedHistory.size)
        assertEquals(prepared.clientRecordId, reopenedHistory.single().clientRecordId)
        assertEquals("synthetic", reopenedHistory.single().sourceProvider)
        assertEquals("durable-row", reopenedHistory.single().sourceRecordId)
        assertEquals(SyncStatus.WRITING, reopenedHistory.single().status)
        assertEquals(1, reopenedHistory.single().attemptCount)
        assertEquals(400L, reopenedHistory.single().createdAtEpochMillis)
        assertEquals(500L, reopenedHistory.single().updatedAtEpochMillis)
    }

    private fun openDurableDatabase(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DURABLE_DATABASE_NAME)
            .allowMainThreadQueries()
            .build()

    private fun workout(sourceRecordId: String): PreparedLedgerWorkout {
        val clientRecordId = WorkoutMetadataPolicy.clientRecordIdFor("synthetic", sourceRecordId)
        return PreparedLedgerWorkout(
            sourceProvider = "synthetic",
            sourceRecordId = sourceRecordId,
            sourceVersion = "source-v1",
            metadata = ResolvedWorkoutMetadata(
                clientRecordId = clientRecordId,
                contentHash = "a".repeat(64),
                clientRecordVersion = 1L,
            ),
        )
    }

    private class MutableLedgerClock(var now: Long) : LedgerClock {
        override fun nowEpochMillis(): Long = now
    }

    private companion object {
        const val DURABLE_DATABASE_NAME = "sync-ledger-history-test.db"
    }
}
