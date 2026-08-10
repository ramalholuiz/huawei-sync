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
import dev.lui.huaweisync.domain.DomainActivityKind
import dev.lui.huaweisync.domain.DomainWorkout
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.domain.WorkoutSource
import dev.lui.huaweisync.source.WorkoutSourceReader
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
    fun retryAfterBlockRunsPreflightAgainWithoutChargingTheBlockedAttempt() = runTest {
        var preflightChecks = 0
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = writer,
            preflight = SyncPreflight {
                preflightChecks += 1
                if (preflightChecks == 1) {
                    SyncPreflightResult.Blocked(
                        SyncBlock(
                            reason = SyncBlockReason.PERMISSION,
                            code = "EXERCISE_SESSION_PERMISSION_REQUIRED",
                            phase = SyncErrorPhase.PREPARATION,
                            safeMessage = SyncDiagnosticMessage.PERMISSION_REQUIRED,
                        ),
                    )
                } else {
                    SyncPreflightResult.Ready
                }
            },
        )

        val blocked = coordinator.runSyntheticStrengthSync()
        val accepted = coordinator.runSyntheticStrengthSync()

        assertTrue(blocked is Gate1SyncResult.Blocked)
        assertEquals(0, blocked.writeCountForClientRecordId)
        assertTrue(accepted is Gate1SyncResult.Completed)
        assertEquals(1, accepted.writeCountForClientRecordId)
        assertEquals(2, preflightChecks)
        assertEquals(1, writer.records.size)
        val durable = database.syncLedgerStore()
            .findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.SYNCED, durable.status)
        assertEquals(1, durable.attemptCount)
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
    fun sourceReaderFeedsTheExistingLedgerAndHealthConnectPipelineWithoutDuplicates() = runTest {
        val workout = DomainWorkout(
            source = WorkoutSource.BLUETOOTH,
            sourceWorkoutId = "watch-fit-5-pro:activity-42",
            title = "Watch strength",
            activityKind = DomainActivityKind.STRENGTH_TRAINING,
            startTime = Instant.parse("2026-08-10T10:00:00Z"),
            endTime = Instant.parse("2026-08-10T10:45:00Z"),
            startZoneOffset = ZoneId.of("America/Fortaleza").rules.getOffset(Instant.parse("2026-08-10T10:00:00Z")),
            endZoneOffset = ZoneId.of("America/Fortaleza").rules.getOffset(Instant.parse("2026-08-10T10:45:00Z")),
            notes = "Transferred over controlled Bluetooth loopback.",
            deviceName = "Huawei Watch Fit 5 Pro",
        )
        var sourceReads = 0
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = writer,
            sourceReader = WorkoutSourceReader {
                sourceReads += 1
                workout
            },
        )

        val results = mutableListOf<Gate1SyncResult>()
        repeat(3) { results += coordinator.runSyntheticStrengthSync() }

        val ledger = database.syncLedgerStore()
            .findBySource(WorkoutSource.BLUETOOTH.stableName, "watch-fit-5-pro:activity-42")!!
        assertEquals(SyncStatus.SYNCED, ledger.status)
        assertEquals(1, ledger.attemptCount)
        assertEquals(WorkoutSource.BLUETOOTH.stableName, ledger.sourceProvider)
        assertEquals("watch-fit-5-pro:activity-42", ledger.sourceRecordId)
        assertEquals(ledger.clientRecordId, writer.records.single().metadata.clientRecordId)
        assertEquals(1, writer.records.size)
        assertEquals(3, sourceReads)
        assertEquals(List(3) { 1 }, results.map { it.writeCountForClientRecordId })
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

    @Test
    fun confirmationIsSeparateAndOnlyConfirmedAdvancesToVerified() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        var confirmationCalls = 0
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            confirmer = HealthWorkoutConfirmer { request ->
                confirmationCalls += 1
                assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_ID, request.clientRecordId)
                assertEquals(1L, request.clientRecordVersion)
                assertEquals("health-connect-id-1", request.externalRecordId)
                assertEquals(Instant.parse("2026-07-14T11:15:00Z"), request.startTime)
                assertEquals(Instant.parse("2026-07-14T12:00:00Z"), request.endTime)
                HealthConfirmationResult.Confirmed
            },
        )

        val writeResult = coordinator.runSyntheticStrengthSync()
        val afterWrite = ledger.findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertTrue(writeResult is Gate1SyncResult.Completed)
        assertEquals(SyncStatus.SYNCED, afterWrite.status)
        assertNull(afterWrite.confirmedAtEpochMillis)
        assertEquals(0, confirmationCalls)

        val confirmation = coordinator.confirmSyntheticStrengthSync()

        assertTrue(confirmation is Gate1SyncResult.Confirmed)
        confirmation as Gate1SyncResult.Confirmed
        assertEquals(SyncPhase.CONFIRMATION, confirmation.phase)
        assertEquals("CONFIRMED", confirmation.code)
        assertEquals("health-connect-id-1", confirmation.externalRecordId)
        assertEquals(1, confirmationCalls)
        assertEquals(1, writer.records.size)
        val durable = ledger.findByClientRecordId(confirmation.clientRecordId)!!
        assertEquals(SyncStatus.VERIFIED, durable.status)
        assertEquals(100L, durable.acceptedAtEpochMillis)
        assertEquals(100L, durable.confirmedAtEpochMillis)
        assertEquals(1, durable.attemptCount)
    }

    @Test
    fun rerunningAnAlreadyVerifiedRecordIsANoOp() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            confirmer = HealthWorkoutConfirmer { HealthConfirmationResult.Confirmed },
        )

        val first = coordinator.runSyntheticStrengthSync()
        val confirmed = coordinator.confirmSyntheticStrengthSync()
        val rerun = coordinator.runSyntheticStrengthSync()

        assertTrue(first is Gate1SyncResult.Completed)
        assertTrue(confirmed is Gate1SyncResult.Confirmed)
        assertTrue(rerun is Gate1SyncResult.Confirmed)
        rerun as Gate1SyncResult.Confirmed
        assertEquals("ALREADY_VERIFIED", rerun.code)
        assertEquals(SyncPhase.CONFIRMATION, rerun.phase)
        assertEquals(1, writer.records.size)
        val durable = ledger.findByClientRecordId(rerun.clientRecordId)!!
        assertEquals(SyncStatus.VERIFIED, durable.status)
        assertEquals(1, durable.attemptCount)
    }

    @Test
    fun absentConfirmationPreservesAcceptanceAndCanBeRetriedWithoutWriting() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        var confirmationCalls = 0
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            confirmer = HealthWorkoutConfirmer {
                confirmationCalls += 1
                if (confirmationCalls == 1) {
                    HealthConfirmationResult.Absent("HEALTH_RECORD_ABSENT")
                } else {
                    HealthConfirmationResult.Confirmed
                }
            },
        )
        coordinator.runSyntheticStrengthSync()

        val absent = coordinator.confirmSyntheticStrengthSync()

        assertTrue(absent is Gate1SyncResult.ConfirmationPending)
        absent as Gate1SyncResult.ConfirmationPending
        assertEquals(ConfirmationPendingReason.ABSENT, absent.reason)
        assertEquals("HEALTH_RECORD_ABSENT", absent.code)
        assertEquals(SyncPhase.CONFIRMATION, absent.phase)
        assertEquals("health-connect-id-1", absent.externalRecordId)
        val afterAbsent = ledger.findByClientRecordId(absent.clientRecordId)!!
        assertEquals(SyncStatus.RETRYABLE_ERROR, afterAbsent.status)
        assertEquals(100L, afterAbsent.acceptedAtEpochMillis)
        assertEquals("health-connect-id-1", afterAbsent.healthConnectRecordId)
        assertNull(afterAbsent.confirmedAtEpochMillis)
        assertEquals(1, afterAbsent.attemptCount)

        val confirmed = coordinator.confirmSyntheticStrengthSync()

        assertTrue(confirmed is Gate1SyncResult.Confirmed)
        assertEquals(2, confirmationCalls)
        assertEquals(1, writer.records.size)
        assertEquals(SyncStatus.VERIFIED, ledger.findByClientRecordId(absent.clientRecordId)!!.status)
    }

    @Test
    fun inconclusiveAndFailedConfirmationRemainConfirmationRetryPaths() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        val outcomes = ArrayDeque<HealthConfirmationResult>().apply {
            add(HealthConfirmationResult.Inconclusive("CONFIRMATION_INCONCLUSIVE"))
            add(
                HealthConfirmationResult.Failed(
                    SyncFailure(
                        disposition = SyncFailureDisposition.RETRYABLE,
                        code = "CONFIRMATION_READ_FAILED",
                        phase = SyncErrorPhase.VERIFICATION,
                        safeMessage = SyncDiagnosticMessage.VERIFICATION_FAILED,
                    ),
                ),
            )
        }
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            confirmer = HealthWorkoutConfirmer { outcomes.removeFirst() },
        )
        coordinator.runSyntheticStrengthSync()

        val inconclusive = coordinator.confirmSyntheticStrengthSync() as Gate1SyncResult.ConfirmationPending
        assertEquals(ConfirmationPendingReason.INCONCLUSIVE, inconclusive.reason)
        assertEquals("CONFIRMATION_INCONCLUSIVE", inconclusive.code)
        val afterInconclusive = ledger.findByClientRecordId(inconclusive.clientRecordId)!!
        assertEquals(100L, afterInconclusive.acceptedAtEpochMillis)
        assertEquals(1, afterInconclusive.attemptCount)

        val failed = coordinator.confirmSyntheticStrengthSync() as Gate1SyncResult.ConfirmationPending
        assertEquals(ConfirmationPendingReason.FAILURE, failed.reason)
        assertEquals("CONFIRMATION_READ_FAILED", failed.code)
        val afterFailure = ledger.findByClientRecordId(failed.clientRecordId)!!
        assertEquals(SyncStatus.RETRYABLE_ERROR, afterFailure.status)
        assertEquals(100L, afterFailure.acceptedAtEpochMillis)
        assertEquals("health-connect-id-1", afterFailure.healthConnectRecordId)
        assertNull(afterFailure.confirmedAtEpochMillis)
        assertEquals(1, writer.records.size)
    }

    @Test
    fun localConfirmationFinalizationFailureIsStructuredAndLeavesVerificationPending() = runTest {
        val realLedger = database.syncLedgerStore(LedgerClock { 100L })
        val failingLedger = object : Gate1SyncLedger by realLedger {
            override suspend fun confirm(clientRecordId: String) =
                throw IllegalStateException("database unavailable")
        }
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = failingLedger,
            writer = writer,
            confirmer = HealthWorkoutConfirmer { HealthConfirmationResult.Confirmed },
        )
        coordinator.runSyntheticStrengthSync()

        val result = coordinator.confirmSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.ConfirmationPending)
        result as Gate1SyncResult.ConfirmationPending
        assertEquals(ConfirmationPendingReason.FAILURE, result.reason)
        assertEquals("LOCAL_CONFIRMATION_FAILED", result.code)
        assertEquals(LocalFinalizationStatus.FAILED, result.localFinalizationStatus)
        assertEquals("health-connect-id-1", result.externalRecordId)
        val durable = realLedger.findByClientRecordId(result.clientRecordId)!!
        assertEquals(SyncStatus.VERIFICATION_PENDING, durable.status)
        assertEquals(100L, durable.acceptedAtEpochMillis)
        assertEquals("health-connect-id-1", durable.healthConnectRecordId)
        assertNull(durable.confirmedAtEpochMillis)
        assertEquals(1, durable.attemptCount)
    }

    @Test
    fun unexpectedConfirmerFailureBubblesWithoutErasingAcceptance() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        val expected = IllegalStateException("confirmation adapter unavailable")
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            confirmer = HealthWorkoutConfirmer { throw expected },
        )
        coordinator.runSyntheticStrengthSync()

        val observed = try {
            coordinator.confirmSyntheticStrengthSync()
            throw AssertionError("Expected confirmer failure")
        } catch (failure: IllegalStateException) {
            failure
        }

        assertSame(expected, observed)
        val durable = ledger.findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.VERIFICATION_PENDING, durable.status)
        assertEquals(100L, durable.acceptedAtEpochMillis)
        assertEquals("health-connect-id-1", durable.healthConnectRecordId)
        assertNull(durable.confirmedAtEpochMillis)
        assertEquals(1, writer.records.size)
    }

    @Test
    fun reconciliationFindsExistingRecordWithoutBlindReinsertion() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        val uncertainLedger = object : Gate1SyncLedger by ledger {
            override suspend fun recordAccepted(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database unavailable")
        }
        Gate1SyncCoordinator(
            ledgerStore = uncertainLedger,
            writer = writer,
        ).runSyntheticStrengthSync()
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            reconciler = HealthWorkoutReconciler { request ->
                assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_ID, request.clientRecordId)
                assertEquals(1L, request.clientRecordVersion)
                assertEquals("health-connect-id-1", request.externalRecordId)
                assertEquals(Instant.parse("2026-07-14T11:15:00Z"), request.startTime)
                assertEquals(Instant.parse("2026-07-14T12:00:00Z"), request.endTime)
                HealthReconciliationResult.Found("health-connect-id-reconciled")
            },
        )

        val result = coordinator.reconcileSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.Reconciled)
        result as Gate1SyncResult.Reconciled
        assertEquals(ReconciliationResolution.EXISTING_ACCEPTED, result.resolution)
        assertEquals("RECONCILED_EXISTING", result.code)
        assertEquals("health-connect-id-reconciled", result.externalRecordId)
        assertEquals(1, writer.records.size)
        val durable = ledger.findByClientRecordId(result.clientRecordId)!!
        assertEquals(SyncStatus.SYNCED, durable.status)
        assertEquals(1, durable.attemptCount)
        assertEquals(100L, durable.acceptedAtEpochMillis)
    }

    @Test
    fun onlyAuthoritativeAbsenceUnlocksAWriteRetry() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        val failingLedger = object : Gate1SyncLedger by ledger {
            override suspend fun recordAccepted(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database unavailable")
            override suspend fun recordAcceptanceUncertain(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database still unavailable")
        }
        val stranded = Gate1SyncCoordinator(failingLedger, writer)
        stranded.runSyntheticStrengthSync()
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            reconciler = HealthWorkoutReconciler {
                HealthReconciliationResult.AuthoritativelyAbsent("HEALTH_RECORD_NOT_FOUND")
            },
        )

        val reconciled = coordinator.reconcileSyntheticStrengthSync()
        val retried = coordinator.runSyntheticStrengthSync()

        assertTrue(reconciled is Gate1SyncResult.Reconciled)
        reconciled as Gate1SyncResult.Reconciled
        assertEquals(ReconciliationResolution.RETRY_ALLOWED, reconciled.resolution)
        assertEquals("HEALTH_RECORD_NOT_FOUND", reconciled.code)
        assertTrue(retried is Gate1SyncResult.Completed)
        assertEquals(2, writer.records.size)
        val durable = ledger.findByClientRecordId(reconciled.clientRecordId)!!
        assertEquals(SyncStatus.SYNCED, durable.status)
        assertEquals(2, durable.attemptCount)
    }

    @Test
    fun inconclusiveAndFailedReconciliationNeverUnlockReinsertion() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        val failingLedger = object : Gate1SyncLedger by ledger {
            override suspend fun recordAccepted(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database unavailable")
            override suspend fun recordAcceptanceUncertain(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database still unavailable")
        }
        Gate1SyncCoordinator(failingLedger, writer).runSyntheticStrengthSync()
        val outcomes = ArrayDeque<HealthReconciliationResult>().apply {
            add(HealthReconciliationResult.Inconclusive("RECONCILIATION_INCONCLUSIVE"))
            add(
                HealthReconciliationResult.Failed(
                    SyncFailure(
                        disposition = SyncFailureDisposition.RETRYABLE,
                        code = "RECONCILIATION_READ_FAILED",
                        phase = SyncErrorPhase.RECONCILIATION,
                        safeMessage = SyncDiagnosticMessage.VERIFICATION_FAILED,
                    ),
                ),
            )
        }
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            reconciler = HealthWorkoutReconciler { outcomes.removeFirst() },
        )

        val inconclusive = coordinator.reconcileSyntheticStrengthSync()
        val failed = coordinator.reconcileSyntheticStrengthSync()

        assertTrue(inconclusive is Gate1SyncResult.ReconciliationPending)
        assertTrue(failed is Gate1SyncResult.ReconciliationPending)
        assertEquals("RECONCILIATION_READ_FAILED", (failed as Gate1SyncResult.ReconciliationPending).code)
        val durable = ledger.findByClientRecordId(failed.clientRecordId)!!
        assertEquals(SyncStatus.RECONCILIATION_PENDING, durable.status)
        assertEquals(1, durable.attemptCount)
        assertNull(durable.acceptedAtEpochMillis)
        assertEquals(1, writer.records.size)
        val blockedRetry = runCatching { coordinator.runSyntheticStrengthSync() }
        assertTrue(blockedRetry.isFailure)
        assertEquals(1, writer.records.size)
    }

    @Test
    fun localReconciliationFinalizationFailureRemainsStructuredAndQuarantined() = runTest {
        val realLedger = database.syncLedgerStore(LedgerClock { 100L })
        val strandedLedger = object : Gate1SyncLedger by realLedger {
            override suspend fun recordAccepted(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database unavailable")
            override suspend fun recordAcceptanceUncertain(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database still unavailable")
        }
        Gate1SyncCoordinator(strandedLedger, writer).runSyntheticStrengthSync()
        val failingFinalization = object : Gate1SyncLedger by realLedger {
            override suspend fun reconcileForRetry(clientRecordId: String) =
                throw IllegalStateException("database unavailable")
        }
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = failingFinalization,
            writer = writer,
            reconciler = HealthWorkoutReconciler {
                HealthReconciliationResult.AuthoritativelyAbsent("HEALTH_RECORD_NOT_FOUND")
            },
        )

        val result = coordinator.reconcileSyntheticStrengthSync()

        assertTrue(result is Gate1SyncResult.ReconciliationPending)
        result as Gate1SyncResult.ReconciliationPending
        assertEquals("LOCAL_RECONCILIATION_FAILED", result.code)
        assertEquals(LocalFinalizationStatus.FAILED, result.localFinalizationStatus)
        val durable = realLedger.findByClientRecordId(result.clientRecordId)!!
        assertEquals(SyncStatus.WRITING, durable.status)
        assertEquals(1, durable.attemptCount)
        assertNull(durable.acceptedAtEpochMillis)
        assertEquals(1, writer.records.size)
    }

    @Test
    fun unexpectedReconcilerFailureBubblesAndPreservesUnknownWrite() = runTest {
        val ledger = database.syncLedgerStore(LedgerClock { 100L })
        val strandedLedger = object : Gate1SyncLedger by ledger {
            override suspend fun recordAccepted(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database unavailable")
            override suspend fun recordAcceptanceUncertain(clientRecordId: String, healthConnectRecordId: String?) =
                throw IllegalStateException("database still unavailable")
        }
        Gate1SyncCoordinator(strandedLedger, writer).runSyntheticStrengthSync()
        val expected = IllegalStateException("reconciliation adapter unavailable")
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = ledger,
            writer = writer,
            reconciler = HealthWorkoutReconciler { throw expected },
        )

        val observed = try {
            coordinator.reconcileSyntheticStrengthSync()
            throw AssertionError("Expected reconciler failure")
        } catch (failure: IllegalStateException) {
            failure
        }

        assertSame(expected, observed)
        val durable = ledger.findByClientRecordId(SyntheticWorkoutFactory.CLIENT_RECORD_ID)!!
        assertEquals(SyncStatus.WRITING, durable.status)
        assertEquals(1, durable.attemptCount)
        assertNull(durable.acceptedAtEpochMillis)
        assertEquals(1, writer.records.size)
    }

    @Test
    fun malformedConfirmationCodeIsRejectedAtContractBoundary() {
        val observed = try {
            HealthConfirmationResult.Inconclusive("raw response text")
            throw AssertionError("Expected malformed confirmation code rejection")
        } catch (error: IllegalArgumentException) {
            error
        }

        assertTrue(observed.message!!.contains("stable uppercase identifiers"))
    }

    @Test
    fun malformedReconciliationCodeIsRejectedAtContractBoundary() {
        val observed = try {
            HealthReconciliationResult.AuthoritativelyAbsent("raw response text")
            throw AssertionError("Expected malformed reconciliation code rejection")
        } catch (error: IllegalArgumentException) {
            error
        }

        assertTrue(observed.message!!.contains("stable uppercase identifiers"))
    }

    @Test
    fun invalidPermanentReconciliationFailureIsRejectedAtContractBoundary() {
        val failure = SyncFailure(
            disposition = SyncFailureDisposition.PERMANENT,
            code = "PERMANENT_RECONCILIATION_FAILURE",
            phase = SyncErrorPhase.RECONCILIATION,
        )

        val observed = try {
            HealthReconciliationResult.Failed(failure)
            throw AssertionError("Expected permanent reconciliation failure rejection")
        } catch (error: IllegalArgumentException) {
            error
        }

        assertTrue(observed.message!!.contains("must remain retryable"))
    }

    @Test
    fun invalidPermanentConfirmationFailureIsRejectedAtContractBoundary() {
        val failure = SyncFailure(
            disposition = SyncFailureDisposition.PERMANENT,
            code = "PERMANENT_CONFIRMATION_FAILURE",
            phase = SyncErrorPhase.VERIFICATION,
        )

        val observed = try {
            HealthConfirmationResult.Failed(failure)
            throw AssertionError("Expected permanent confirmation failure rejection")
        } catch (error: IllegalArgumentException) {
            error
        }

        assertTrue(observed.message!!.contains("must remain retryable"))
    }

    @Test
    fun confirmationWithoutAcceptedWriteIsRejectedWithoutCallingDependencies() = runTest {
        var confirmationCalls = 0
        val coordinator = Gate1SyncCoordinator(
            ledgerStore = database.syncLedgerStore(LedgerClock { 100L }),
            writer = writer,
            confirmer = HealthWorkoutConfirmer {
                confirmationCalls += 1
                HealthConfirmationResult.Confirmed
            },
        )

        val observed = try {
            coordinator.confirmSyntheticStrengthSync()
            throw AssertionError("Expected missing acceptance rejection")
        } catch (failure: IllegalStateException) {
            failure
        }

        assertTrue(observed.message!!.contains("prepared ledger row"))
        assertEquals(0, confirmationCalls)
        assertEquals(0, writer.records.size)
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
