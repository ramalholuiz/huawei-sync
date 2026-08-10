package dev.lui.huaweisync.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
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
class SyncLedgerSchemaTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun completeRowsRoundTripEveryFactAndEveryStatus() = runTest {
        SyncStatus.entries.forEachIndexed { index, status ->
            val row = completeRow(index, status)
            database.syncLedgerDao().upsert(row)
            assertEquals(row, database.syncLedgerDao().findByClientRecordId(row.clientRecordId))
            assertEquals(
                row,
                database.syncLedgerDao().findBySource(row.sourceProvider, row.sourceRecordId),
            )
        }
        assertEquals(10, SyncStatus.entries.size)
    }

    @Test
    fun deterministicClientIdAndLogicalSourceAreBothUnique() = runTest {
        val first = completeRow(1, SyncStatus.PENDING)
        database.syncLedgerDao().insert(first)

        val duplicateSourceFailure = runCatching {
            database.syncLedgerDao().insert(
                first.copy(clientRecordId = "client-other", updatedAtEpochMillis = 200),
            )
        }.exceptionOrNull()
        assertTrue(duplicateSourceFailure is SQLiteConstraintException)

        val duplicateClientFailure = runCatching {
            database.syncLedgerDao().insert(
                first.copy(sourceRecordId = "source-other", updatedAtEpochMillis = 300),
            )
        }.exceptionOrNull()
        assertTrue(duplicateClientFailure is SQLiteConstraintException)
    }

    @Test
    fun rejectsInvalidCountersVersionsAndOversizedErrorsBeforePersistence() {
        assertFails<IllegalArgumentException> { completeRow().copy(attemptCount = -1) }
        assertFails<IllegalArgumentException> { completeRow().copy(clientRecordVersion = 0) }
        assertFails<IllegalArgumentException> { completeRow().copy(contentHash = "not-a-sha256") }
        assertFails<IllegalArgumentException> {
            completeRow().copy(lastErrorCode = "x".repeat(SyncLedgerEntity.MAX_ERROR_CODE_LENGTH + 1))
        }
        assertFails<IllegalArgumentException> {
            completeRow().copy(lastErrorMessage = "x".repeat(SyncLedgerEntity.MAX_ERROR_MESSAGE_LENGTH + 1))
        }
    }

    @Test
    fun nullableFactsRoundTripWithoutInventingErrorsOrConfirmations() = runTest {
        val row = completeRow().copy(
            sourceVersion = null,
            contentHash = null,
            healthConnectRecordId = null,
            acceptedAtEpochMillis = null,
            confirmedAtEpochMillis = null,
            lastErrorCode = null,
            lastErrorPhase = null,
            lastErrorAtEpochMillis = null,
            lastErrorMessage = null,
        )
        database.syncLedgerDao().upsert(row)

        val stored = database.syncLedgerDao().findByClientRecordId(row.clientRecordId)!!
        assertNull(stored.contentHash)
        assertNull(stored.confirmedAtEpochMillis)
        assertNull(stored.lastErrorCode)
    }

    private fun completeRow(index: Int = 0, status: SyncStatus = SyncStatus.RETRYABLE_ERROR) =
        SyncLedgerEntity(
            clientRecordId = "client-$index",
            sourceProvider = "synthetic",
            sourceRecordId = "source-$index",
            sourceVersion = "source-version-$index",
            contentHash = "%064x".format(index + 1),
            clientRecordVersion = index + 1L,
            healthConnectRecordId = "health-$index",
            status = status,
            attemptCount = index,
            acceptedAtEpochMillis = 1000L + index,
            confirmedAtEpochMillis = 2000L + index,
            createdAtEpochMillis = 100L,
            updatedAtEpochMillis = 3000L + index,
            lastErrorCode = "safe-code",
            lastErrorPhase = SyncErrorPhase.entries[index % SyncErrorPhase.entries.size],
            lastErrorAtEpochMillis = 2500L + index,
            lastErrorMessage = "bounded diagnostic",
        )

    private inline fun <reified T : Throwable> assertFails(block: () -> Unit) {
        val failure = runCatching(block).exceptionOrNull()
        assertTrue("Expected ${T::class.java.simpleName}, got $failure", failure is T)
    }
}
