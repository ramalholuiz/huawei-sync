package dev.lui.huaweisync.health

import android.content.Context
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.lui.huaweisync.data.AppDatabase
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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
            ledgerDao = database.syncLedgerDao(),
            writer = writer,
            clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneId.of("UTC")),
        )

        repeat(3) { coordinator.runSyntheticStrengthSync() }

        val clientRecordId = SyntheticWorkoutFactory.CLIENT_RECORD_ID
        val ledger = database.syncLedgerDao().findByClientRecordId(clientRecordId)
        assertEquals(1, database.syncLedgerDao().countByClientRecordId(clientRecordId))
        assertEquals(3, ledger?.successfulWriteCount)
        assertEquals(3, writer.records.size)
        assertEquals(setOf(clientRecordId), writer.records.map { it.metadata.clientRecordId }.toSet())
    }

    private class RecordingWriter : HealthWorkoutWriter {
        val records = mutableListOf<ExerciseSessionRecord>()

        override suspend fun write(record: ExerciseSessionRecord): String? {
            records += record
            return "health-connect-id-${records.size}"
        }
    }
}
