package dev.lui.huaweisync.health

import android.content.Context
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.lui.huaweisync.data.AppDatabase
import dev.lui.huaweisync.data.LedgerClock
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class Gate1SyncCoordinatorTest {
    private lateinit var database: AppDatabase
    private lateinit var writer: RecordingWriter

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        writer = RecordingWriter()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun threeExecutionsKeepOneLedgerRowForTheDeterministicClientRecordId() = runTest {
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = writer,
            clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneId.of("UTC")),
        )

        val results = mutableListOf<Gate1SyncResult>()
        repeat(3) { results += coordinator.runSyntheticStrengthSync() }

        val clientRecordId = SyntheticWorkoutFactory.CLIENT_RECORD_ID
        val ledger = database.syncLedgerStore().findByClientRecordId(clientRecordId)!!
        assertEquals(1, database.syncLedgerDao().countByClientRecordId(clientRecordId))
        assertEquals(SyncStatus.SYNCED, ledger.status)
        assertEquals(1, ledger.attemptCount)
        assertEquals("health-connect-id-1", ledger.healthConnectRecordId)
        assertEquals(1, writer.records.size)
        assertEquals(clientRecordId, writer.records.single().metadata.clientRecordId)
        assertEquals(1L, writer.records.single().metadata.clientRecordVersion)
        assertEquals(List(3) { 1 }, results.map { it.writeCountForClientRecordId })
    }

    @Test
    fun writerFailureBubblesAndLeavesDurableWritingAttemptForLaterRecovery() = runTest {
        val expected = IllegalStateException("connection lost")
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = HealthWorkoutWriter { throw expected },
        )

        val observed = expectSuspendFailure<IllegalStateException> {
            coordinator.runSyntheticStrengthSync()
        }

        assertSame(expected, observed)
        val ledger = database.syncLedgerStore()
            .findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.WRITING, ledger.status)
        assertEquals(1, ledger.attemptCount)
        assertNull(ledger.acceptedAtEpochMillis)
    }

    @Test
    fun acceptedWriteWithoutReturnedHealthIdIsStillIdempotentByClientIdentity() = runTest {
        var calls = 0
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = HealthWorkoutWriter {
                calls += 1
                null
            },
        )

        coordinator.runSyntheticStrengthSync()
        coordinator.runSyntheticStrengthSync()

        val ledger = database.syncLedgerStore()
            .findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.SYNCED, ledger.status)
        assertEquals(1, calls)
        assertEquals(1, ledger.attemptCount)
        assertNull(ledger.healthConnectRecordId)
        assertEquals(100L, ledger.acceptedAtEpochMillis)
    }

    private suspend inline fun <reified T : Throwable> expectSuspendFailure(
        crossinline block: suspend () -> Unit,
    ): T {
        try {
            block()
        } catch (failure: Throwable) {
            if (failure is T) return failure
            throw failure
        }
        throw AssertionError("Expected ${T::class.java.simpleName} to be thrown.")
    }

    private class RecordingWriter : HealthWorkoutWriter {
        val records = mutableListOf<ExerciseSessionRecord>()

        override suspend fun write(record: ExerciseSessionRecord): String? {
            records += record
            return "health-connect-id-${records.size}"
        }
    }
}
