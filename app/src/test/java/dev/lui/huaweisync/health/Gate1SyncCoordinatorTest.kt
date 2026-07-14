package dev.lui.huaweisync.health

import android.content.Context
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.lui.huaweisync.data.AppDatabase
import dev.lui.huaweisync.data.LedgerClock
import dev.lui.huaweisync.data.SyncBlock
import dev.lui.huaweisync.data.SyncBlockReason
import dev.lui.huaweisync.data.SyncDiagnosticMessage
import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncFailure
import dev.lui.huaweisync.data.SyncFailureDisposition
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
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
    fun acceptedExternalWriteSurvivesLocalFinalizationFailure() = runTest {
        val realLedger = database.syncLedgerStore(LedgerClock { 100L })
        val expected = IllegalStateException("database unavailable")
        val failingLedger = object : Gate1SyncLedger by realLedger {
            override suspend fun recordAccepted(
                clientRecordId: String,
                healthConnectRecordId: String?,
            ) = throw expected
        }
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = failingLedger,
            writer = writer,
            clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneId.of("UTC")),
        )

        val result = coordinator.runSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.ExternalAccepted)
        result as Gate1SyncResult.ExternalAccepted
        assertEquals(SyncPhase.LOCAL_FINALIZATION, result.phase)
        assertEquals("LOCAL_FINALIZATION_FAILED", result.code)
        assertEquals(LocalFinalizationStatus.RECONCILIATION_PENDING, result.localFinalizationStatus)
        assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_ID, result.clientRecordId)
        assertEquals(1L, result.clientRecordVersion)
        assertEquals("health-connect-id-1", result.externalRecordId)

        val durable = realLedger.findByClientRecordId(result.clientRecordId)!!
        assertEquals(SyncStatus.RECONCILIATION_PENDING, durable.status)
        assertEquals(100L, durable.acceptedAtEpochMillis)
        assertEquals("health-connect-id-1", durable.healthConnectRecordId)
        assertEquals(1, durable.attemptCount)
    }

    @Test
    fun externalAcceptanceMetadataSurvivesWhenRecoveryPersistenceAlsoFails() = runTest {
        val realLedger = database.syncLedgerStore(LedgerClock { 100L })
        val failingLedger = object : Gate1SyncLedger by realLedger {
            override suspend fun recordAccepted(
                clientRecordId: String,
                healthConnectRecordId: String?,
            ) = throw IllegalStateException("database unavailable")

            override suspend fun recordAcceptanceUncertain(
                clientRecordId: String,
                healthConnectRecordId: String?,
            ) = throw IllegalStateException("database still unavailable")
        }
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = failingLedger,
            writer = writer,
            clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneId.of("UTC")),
        )

        val result = coordinator.runSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.ExternalAccepted)
        result as Gate1SyncResult.ExternalAccepted
        assertEquals(LocalFinalizationStatus.FAILED, result.localFinalizationStatus)
        assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_ID, result.clientRecordId)
        assertEquals(1L, result.clientRecordVersion)
        assertEquals("health-connect-id-1", result.externalRecordId)
        val durable = realLedger.findByClientRecordId(result.clientRecordId)!!
        assertEquals(SyncStatus.WRITING, durable.status)
        assertEquals(1, durable.attemptCount)
        assertNull(durable.acceptedAtEpochMillis)
    }

    @Test
    fun permissionBlockIsPersistedBeforeAttemptAndSkipsWriter() = runTest {
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = writer,
            preflight = SyncPreflight {
                SyncPreflightResult.Blocked(
                    SyncBlock(
                        reason = SyncBlockReason.PERMISSION,
                        code = "WRITE_PERMISSION_REQUIRED",
                        phase = SyncErrorPhase.PREPARATION,
                        safeMessage = SyncDiagnosticMessage.PERMISSION_REQUIRED,
                    ),
                )
            },
        )

        val result = coordinator.runSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.Blocked)
        result as Gate1SyncResult.Blocked
        assertEquals(SyncBlockReason.PERMISSION, result.reason)
        assertEquals(SyncPhase.PREFLIGHT, result.phase)
        assertEquals("WRITE_PERMISSION_REQUIRED", result.code)
        assertEquals(0, result.writeCountForClientRecordId)
        val ledger = database.syncLedgerStore()
            .findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.PERMISSION_BLOCKED, ledger.status)
        assertEquals(0, ledger.attemptCount)
        assertEquals(0, writer.records.size)
    }

    @Test
    fun environmentBlockIsPersistedBeforeAttemptAndSkipsWriter() = runTest {
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = writer,
            preflight = SyncPreflight {
                SyncPreflightResult.Blocked(
                    SyncBlock(
                        reason = SyncBlockReason.ENVIRONMENT,
                        code = "HEALTH_CONNECT_UNAVAILABLE",
                        phase = SyncErrorPhase.PREPARATION,
                        safeMessage = SyncDiagnosticMessage.ENVIRONMENT_UNAVAILABLE,
                    ),
                )
            },
        )

        val result = coordinator.runSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.Blocked)
        result as Gate1SyncResult.Blocked
        assertEquals(SyncBlockReason.ENVIRONMENT, result.reason)
        assertEquals(0, result.writeCountForClientRecordId)
        val ledger = database.syncLedgerStore()
            .findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.ENVIRONMENT_BLOCKED, ledger.status)
        assertEquals(0, ledger.attemptCount)
        assertEquals(0, writer.records.size)
    }

    @Test
    fun unexpectedPreflightFailureBubblesWithoutCountingAnAttempt() = runTest {
        val expected = IllegalStateException("preflight adapter unavailable")
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = writer,
            preflight = SyncPreflight { throw expected },
        )

        val observed = try {
            coordinator.runSyntheticStrengthSync()
            throw AssertionError("Expected preflight failure")
        } catch (failure: IllegalStateException) {
            failure
        }

        assertSame(expected, observed)
        val durable = database.syncLedgerStore()
            .findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.PENDING, durable.status)
        assertEquals(0, durable.attemptCount)
        assertEquals(0, writer.records.size)
    }

    @Test
    fun readyWriteIsDurablyWritingBeforeExternalCall() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        val observingWriter = HealthWorkoutWriter { record ->
            val durable = ledger.findByClientRecordId(requireNotNull(record.metadata.clientRecordId))!!
            assertEquals(SyncStatus.WRITING, durable.status)
            assertEquals(1, durable.attemptCount)
            HealthWriteResult.Accepted("observed-id")
        }
        val coordinator = Gate1SyncCoordinator(ledgerStore = ledger, writer = observingWriter)

        val result = coordinator.runSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.Completed)
    }

    @Test
    fun retryableWriteFailureIsStructuredAndDurableWithoutAcceptance() = runTest {
        assertWriteFailure(
            failure = SyncFailure(
                disposition = SyncFailureDisposition.RETRYABLE,
                code = "HEALTH_CONNECT_IO",
                phase = SyncErrorPhase.WRITE,
                safeMessage = SyncDiagnosticMessage.WRITE_FAILED,
            ),
            expectedStatus = SyncStatus.RETRYABLE_ERROR,
        )
    }

    @Test
    fun permanentWriteFailureIsStructuredAndDurableWithoutAcceptance() = runTest {
        assertWriteFailure(
            failure = SyncFailure(
                disposition = SyncFailureDisposition.PERMANENT,
                code = "INVALID_EXERCISE_RECORD",
                phase = SyncErrorPhase.WRITE,
                safeMessage = SyncDiagnosticMessage.WRITE_FAILED,
            ),
            expectedStatus = SyncStatus.PERMANENT_ERROR,
        )
    }

    @Test
    fun failurePersistenceErrorBubblesAndRetainsDurableWritingEvidence() = runTest {
        val realLedger = database.syncLedgerStore(LedgerClock { 100L })
        val expected = IllegalStateException("database unavailable")
        val failingLedger = object : Gate1SyncLedger by realLedger {
            override suspend fun recordFailure(
                clientRecordId: String,
                failure: SyncFailure,
            ) = throw expected
        }
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = failingLedger,
            writer = HealthWorkoutWriter {
                HealthWriteResult.Failed(
                    SyncFailure(
                        disposition = SyncFailureDisposition.RETRYABLE,
                        code = "HEALTH_CONNECT_IO",
                        phase = SyncErrorPhase.WRITE,
                        safeMessage = SyncDiagnosticMessage.WRITE_FAILED,
                    ),
                )
            },
        )

        val observed = try {
            coordinator.runSyntheticStrengthSync()
            throw AssertionError("Expected persistence failure")
        } catch (failure: IllegalStateException) {
            failure
        }

        assertSame(expected, observed)
        val durable = realLedger.findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.WRITING, durable.status)
        assertEquals(1, durable.attemptCount)
        assertNull(durable.acceptedAtEpochMillis)
    }

    @Test
    fun retryAfterWriteFailurePreservesMetadataAndIncrementsAttempt() = runTest {
        val records = mutableListOf<ExerciseSessionRecord>()
        var calls = 0
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = HealthWorkoutWriter { record ->
                records += record
                calls += 1
                if (calls == 1) {
                    HealthWriteResult.Failed(
                        SyncFailure(
                            disposition = SyncFailureDisposition.RETRYABLE,
                            code = "HEALTH_CONNECT_IO",
                            phase = SyncErrorPhase.WRITE,
                            safeMessage = SyncDiagnosticMessage.WRITE_FAILED,
                        ),
                    )
                } else {
                    HealthWriteResult.Accepted("health-connect-id-2")
                }
            },
        )

        coordinator.runSyntheticStrengthSync()
        val result = coordinator.runSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.Completed)
        assertEquals(2, records.size)
        assertEquals(records[0].metadata.clientRecordId, records[1].metadata.clientRecordId)
        assertEquals(records[0].metadata.clientRecordVersion, records[1].metadata.clientRecordVersion)
        val durable = database.syncLedgerStore()
            .findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.SYNCED, durable.status)
        assertEquals(2, durable.attemptCount)
        assertEquals(100L, durable.acceptedAtEpochMillis)
    }

    @Test
    fun acceptedWriteWithoutReturnedHealthIdIsStillIdempotentByClientIdentity() = runTest {
        var calls = 0
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = HealthWorkoutWriter {
                calls += 1
                HealthWriteResult.Accepted(null)
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

    private suspend fun assertWriteFailure(
        failure: SyncFailure,
        expectedStatus: SyncStatus,
    ) {
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = HealthWorkoutWriter { HealthWriteResult.Failed(failure) },
        )

        val result = coordinator.runSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.WriteFailed)
        result as Gate1SyncResult.WriteFailed
        assertEquals(failure.disposition, result.disposition)
        assertEquals(failure.code, result.code)
        assertEquals(SyncPhase.EXTERNAL_WRITE, result.phase)
        assertEquals(1, result.writeCountForClientRecordId)
        val ledger = database.syncLedgerStore()
            .findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(expectedStatus, ledger.status)
        assertEquals(1, ledger.attemptCount)
        assertNull(ledger.acceptedAtEpochMillis)
        assertNull(ledger.healthConnectRecordId)
        assertEquals(failure.code, ledger.lastErrorCode)
        assertEquals(SyncErrorPhase.WRITE, ledger.lastErrorPhase)
    }

    private class RecordingWriter : HealthWorkoutWriter {
        val records = mutableListOf<ExerciseSessionRecord>()

        override suspend fun write(record: ExerciseSessionRecord): HealthWriteResult {
            records += record
            return HealthWriteResult.Accepted("health-connect-id-${records.size}")
        }
    }
}
