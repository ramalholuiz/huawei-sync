package dev.lui.huaweisync.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.lui.huaweisync.domain.ResolvedWorkoutMetadata
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncLedgerStoreTest {
    private lateinit var database: AppDatabase
    private lateinit var clock: MutableLedgerClock
    private lateinit var store: SyncLedgerStore

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        clock = MutableLedgerClock(100L)
        store = database.syncLedgerStore(clock)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun uncertainAcceptancePreservesIdentityAndExternalWriteFacts() = runTest {
        val prepared = store.prepare(workout())
        val writing = store.beginWrite(prepared.clientRecordId)
        clock.now = 200L

        val uncertain = store.recordAcceptanceUncertain(
            prepared.clientRecordId,
            "health-connect-id-1",
        )

        assertEquals(writing.clientRecordId, uncertain.clientRecordId)
        assertEquals(writing.sourceProvider, uncertain.sourceProvider)
        assertEquals(writing.sourceRecordId, uncertain.sourceRecordId)
        assertEquals(writing.contentHash, uncertain.contentHash)
        assertEquals(writing.clientRecordVersion, uncertain.clientRecordVersion)
        assertEquals(SyncStatus.RECONCILIATION_PENDING, uncertain.status)
        assertEquals("health-connect-id-1", uncertain.healthConnectRecordId)
        assertEquals(200L, uncertain.acceptedAtEpochMillis)
        assertEquals(1, uncertain.attemptCount)
        assertEquals("LOCAL_FINALIZATION_FAILED", uncertain.lastErrorCode)
        assertEquals(SyncErrorPhase.ACCEPTANCE, uncertain.lastErrorPhase)
        assertEquals(200L, uncertain.lastErrorAtEpochMillis)
    }

    @Test
    fun acceptedVerificationFailureAndConfirmationPreserveDurableAcceptanceFacts() = runTest {
        val prepared = store.prepare(workout())
        assertEquals(SyncStatus.PENDING, prepared.status)
        assertEquals(100L, prepared.createdAtEpochMillis)
        assertEquals(0, prepared.attemptCount)

        clock.now = 200L
        val writing = store.beginWrite(CLIENT_ID)
        assertEquals(SyncStatus.WRITING, writing.status)
        assertEquals(1, writing.attemptCount)

        clock.now = 300L
        val accepted = store.recordAccepted(CLIENT_ID, "health-connect-1")
        assertEquals(SyncStatus.SYNCED, accepted.status)
        assertEquals("health-connect-1", accepted.healthConnectRecordId)
        assertEquals(300L, accepted.acceptedAtEpochMillis)

        clock.now = 400L
        assertEquals(
            SyncStatus.VERIFICATION_PENDING,
            store.beginVerification(CLIENT_ID).status,
        )

        clock.now = 500L
        val failed = store.recordFailure(
            CLIENT_ID,
            SyncFailure(
                disposition = SyncFailureDisposition.RETRYABLE,
                code = "VERIFY_READ_FAILED",
                phase = SyncErrorPhase.VERIFICATION,
                safeMessage = SyncDiagnosticMessage.VERIFICATION_FAILED,
            ),
        )
        assertEquals(SyncStatus.RETRYABLE_ERROR, failed.status)
        assertEquals("health-connect-1", failed.healthConnectRecordId)
        assertEquals(300L, failed.acceptedAtEpochMillis)
        assertEquals("VERIFY_READ_FAILED", failed.lastErrorCode)
        assertEquals(500L, failed.lastErrorAtEpochMillis)

        clock.now = 600L
        store.beginVerification(CLIENT_ID)
        clock.now = 700L
        val confirmed = store.confirm(CLIENT_ID)
        assertEquals(SyncStatus.VERIFIED, confirmed.status)
        assertEquals("health-connect-1", confirmed.healthConnectRecordId)
        assertEquals(300L, confirmed.acceptedAtEpochMillis)
        assertEquals(700L, confirmed.confirmedAtEpochMillis)
        assertNull(confirmed.lastErrorCode)
        assertEquals(1, confirmed.attemptCount)

        expectSuspendFailure(IllegalStateException::class.java) {
            store.recordFailure(
                CLIENT_ID,
                SyncFailure(
                    disposition = SyncFailureDisposition.RETRYABLE,
                    code = "LATE_FAILURE",
                    phase = SyncErrorPhase.VERIFICATION,
                ),
            )
        }
        assertEquals(confirmed, store.findByClientRecordId(CLIENT_ID))
    }

    @Test
    fun repeatedPreparePreservesStateAndChangedContentRequiresAdvancedVersion() = runTest {
        store.prepare(workout())
        store.beginWrite(CLIENT_ID)

        clock.now = 300L
        val unchanged = store.prepare(workout())
        assertEquals(SyncStatus.WRITING, unchanged.status)
        assertEquals(1L, unchanged.clientRecordVersion)
        assertEquals(1, unchanged.attemptCount)

        expectSuspendFailure(SyncLedgerConflictException::class.java) {
            store.prepare(workout(clientRecordVersion = 2L))
        }
        val staleVersionFailure = expectSuspendFailure(SyncLedgerConflictException::class.java) {
            store.prepare(workout(contentHash = "b".repeat(64)))
        }
        assertTrue(staleVersionFailure.message!!.contains("exactly one"))
        expectSuspendFailure(SyncLedgerConflictException::class.java) {
            store.prepare(
                workout(contentHash = "b".repeat(64), clientRecordVersion = 3L),
            )
        }
        assertEquals("a".repeat(64), store.findByClientRecordId(CLIENT_ID)!!.contentHash)

        clock.now = 400L
        val changed = store.prepare(
            workout(contentHash = "b".repeat(64), clientRecordVersion = 2L),
        )
        assertEquals(SyncStatus.PENDING, changed.status)
        assertEquals(2L, changed.clientRecordVersion)
        assertNull(changed.healthConnectRecordId)
        assertNull(changed.acceptedAtEpochMillis)
        assertEquals(1, changed.attemptCount)
    }

    @Test
    fun illegalTransitionsDoNotPartiallyMutateTheRow() = runTest {
        val prepared = store.prepare(workout())

        expectSuspendFailure(IllegalStateException::class.java) {
            store.recordAccepted(CLIENT_ID, "not-written")
        }
        expectSuspendFailure(IllegalStateException::class.java) {
            store.beginVerification(CLIENT_ID)
        }
        expectSuspendFailure(NoSuchElementException::class.java) {
            store.beginWrite("missing-client")
        }

        assertEquals(prepared, store.findByClientRecordId(CLIENT_ID))
    }

    @Test
    fun blockedAndFailureTransitionsStoreOnlyBoundedTypedDiagnostics() = runTest {
        store.prepare(workout())
        clock.now = 200L
        val blocked = store.recordBlocked(
            CLIENT_ID,
            SyncBlock(
                reason = SyncBlockReason.PERMISSION,
                code = "HC_PERMISSION_REQUIRED",
                phase = SyncErrorPhase.PREPARATION,
                safeMessage = SyncDiagnosticMessage.PERMISSION_REQUIRED,
            ),
        )
        assertEquals(SyncStatus.PERMISSION_BLOCKED, blocked.status)
        assertEquals(0, blocked.attemptCount)
        assertEquals("HC_PERMISSION_REQUIRED", blocked.lastErrorCode)
        assertEquals(SyncErrorPhase.PREPARATION, blocked.lastErrorPhase)
        assertEquals(
            SyncDiagnosticMessage.PERMISSION_REQUIRED.durableValue,
            blocked.lastErrorMessage,
        )

        clock.now = 300L
        val environmentBlocked = store.recordBlocked(
            CLIENT_ID,
            SyncBlock(
                reason = SyncBlockReason.ENVIRONMENT,
                code = "HC_UNAVAILABLE",
                phase = SyncErrorPhase.PREPARATION,
            ),
        )
        assertEquals(SyncStatus.ENVIRONMENT_BLOCKED, environmentBlocked.status)

        val permanent = store.recordFailure(
            CLIENT_ID,
            SyncFailure(
                disposition = SyncFailureDisposition.PERMANENT,
                code = "INVALID_WORKOUT",
                phase = SyncErrorPhase.PREPARATION,
            ),
        )
        assertEquals(SyncStatus.PERMANENT_ERROR, permanent.status)
        assertEquals("INVALID_WORKOUT", permanent.lastErrorCode)
        assertNull(permanent.lastErrorMessage)

        assertThrows(IllegalArgumentException::class.java) {
            SyncFailure(
                SyncFailureDisposition.RETRYABLE,
                "raw-payload",
                SyncErrorPhase.WRITE,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            PreparedLedgerWorkout(
                sourceProvider = "synthetic",
                sourceRecordId = "gate-1",
                sourceVersion = null,
                metadata = ResolvedWorkoutMetadata(
                    clientRecordId = "wrong-client-id",
                    contentHash = "a".repeat(64),
                    clientRecordVersion = 1L,
                ),
            )
        }
        assertTrue(
            SyncDiagnosticMessage.entries.all {
                it.durableValue.length <= SyncLedgerEntity.MAX_ERROR_MESSAGE_LENGTH &&
                    '\n' !in it.durableValue && '\r' !in it.durableValue
            },
        )
    }

    @Test
    fun clockFailureRollsBackPrepareWithoutLeavingAPartialRow() = runTest {
        val clockFailure = IllegalStateException("clock unavailable")
        val failingStore = database.syncLedgerStore(LedgerClock { throw clockFailure })

        val observed = expectSuspendFailure(IllegalStateException::class.java) {
            failingStore.prepare(workout())
        }

        assertEquals(clockFailure::class.java, observed::class.java)
        assertEquals(clockFailure.message, observed.message)
        assertEquals(0, database.syncLedgerDao().countByClientRecordId(CLIENT_ID))
    }

    @Test
    fun attemptOverflowIsRejectedWithoutChangingTheRow() = runTest {
        val prepared = store.prepare(workout())
        database.syncLedgerDao().update(
            SyncLedgerEntity(
                clientRecordId = prepared.clientRecordId,
                sourceProvider = prepared.sourceProvider,
                sourceRecordId = prepared.sourceRecordId,
                sourceVersion = prepared.sourceVersion,
                contentHash = prepared.contentHash,
                clientRecordVersion = prepared.clientRecordVersion,
                healthConnectRecordId = prepared.healthConnectRecordId,
                status = prepared.status,
                attemptCount = Int.MAX_VALUE,
                acceptedAtEpochMillis = prepared.acceptedAtEpochMillis,
                confirmedAtEpochMillis = prepared.confirmedAtEpochMillis,
                createdAtEpochMillis = prepared.createdAtEpochMillis,
                updatedAtEpochMillis = prepared.updatedAtEpochMillis,
                lastErrorCode = prepared.lastErrorCode,
                lastErrorPhase = prepared.lastErrorPhase,
                lastErrorAtEpochMillis = prepared.lastErrorAtEpochMillis,
                lastErrorMessage = prepared.lastErrorMessage,
            ),
        )

        expectSuspendFailure(IllegalStateException::class.java) {
            store.beginWrite(CLIENT_ID)
        }
        assertEquals(Int.MAX_VALUE, store.findByClientRecordId(CLIENT_ID)!!.attemptCount)
    }

    @Test
    fun migratedUnknownContentRemainsPendingUntilExplicitReconciliation() = runTest {
        insertLegacyRow(CLIENT_ID, "gate-1")

        val prepared = store.prepare(workout())
        assertEquals(SyncStatus.RECONCILIATION_PENDING, prepared.status)
        assertEquals("a".repeat(64), prepared.contentHash)

        clock.now = 200L
        val accepted = store.reconcileAsAccepted(CLIENT_ID)
        assertEquals(SyncStatus.SYNCED, accepted.status)
        assertEquals("legacy-health-id", accepted.healthConnectRecordId)
        assertEquals(50L, accepted.acceptedAtEpochMillis)
        assertEquals(2, accepted.attemptCount)
    }

    @Test
    fun reconciliationForRetryExplicitlyClearsUncertainAcceptanceFacts() = runTest {
        val sourceRecordId = "legacy-retry"
        val clientRecordId = WorkoutMetadataPolicy.clientRecordIdFor("synthetic", sourceRecordId)
        insertLegacyRow(clientRecordId, sourceRecordId)

        clock.now = 200L
        val retry = store.reconcileForRetry(clientRecordId)

        assertEquals(SyncStatus.PENDING, retry.status)
        assertNull(retry.healthConnectRecordId)
        assertNull(retry.acceptedAtEpochMillis)
        assertEquals(2, retry.attemptCount)
        assertEquals(10L, retry.createdAtEpochMillis)
    }

    @Test
    fun concurrentWriteAttemptsAreSerializedWithoutLostUpdates() = runTest {
        store.prepare(workout())

        val outcomes = coroutineScope {
            (1..10).map {
                async(Dispatchers.IO) { runCatching { store.beginWrite(CLIENT_ID) } }
            }.awaitAll()
        }

        assertEquals(1, outcomes.count { it.isSuccess })
        assertEquals(9, outcomes.count { it.isFailure })
        val persisted = store.findByClientRecordId(CLIENT_ID)!!
        assertEquals(SyncStatus.WRITING, persisted.status)
        assertEquals(1, persisted.attemptCount)
    }

    private suspend fun insertLegacyRow(clientRecordId: String, sourceRecordId: String) {
        database.syncLedgerDao().insert(
            SyncLedgerEntity(
                clientRecordId = clientRecordId,
                sourceProvider = "synthetic",
                sourceRecordId = sourceRecordId,
                sourceVersion = null,
                contentHash = null,
                clientRecordVersion = 1L,
                healthConnectRecordId = "legacy-health-id",
                status = SyncStatus.RECONCILIATION_PENDING,
                attemptCount = 2,
                acceptedAtEpochMillis = 50L,
                confirmedAtEpochMillis = null,
                createdAtEpochMillis = 10L,
                updatedAtEpochMillis = 50L,
                lastErrorCode = null,
                lastErrorPhase = null,
                lastErrorAtEpochMillis = null,
                lastErrorMessage = null,
            ),
        )
    }

    private suspend fun <T : Throwable> expectSuspendFailure(
        type: Class<T>,
        block: suspend () -> Unit,
    ): T {
        try {
            block()
        } catch (failure: Throwable) {
            if (type.isInstance(failure)) return type.cast(failure)
            throw failure
        }
        throw AssertionError("Expected ${type.simpleName} to be thrown.")
    }

    private fun workout(
        contentHash: String = "a".repeat(64),
        clientRecordVersion: Long = 1L,
    ) = PreparedLedgerWorkout(
        sourceProvider = "synthetic",
        sourceRecordId = "gate-1",
        sourceVersion = "source-v1",
        metadata = ResolvedWorkoutMetadata(
            clientRecordId = CLIENT_ID,
            contentHash = contentHash,
            clientRecordVersion = clientRecordVersion,
        ),
    )

    private class MutableLedgerClock(var now: Long) : LedgerClock {
        override fun nowEpochMillis(): Long = now
    }

    private companion object {
        val CLIENT_ID = WorkoutMetadataPolicy.clientRecordIdFor("synthetic", "gate-1")
    }
}
