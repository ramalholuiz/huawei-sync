package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncFailureDisposition
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class HealthConnectWorkoutWriterTest {
    @Test
    fun acceptedInsertReturnsExplicitAcceptanceWithOptionalExternalId() = runTest {
        val writer = HealthConnectWorkoutWriter(insertRecord = { "health-connect-id" })

        val result = writer.write(record())

        assertEquals(HealthWriteResult.Accepted("health-connect-id"), result)
    }

    @Test
    fun ioFailureMapsToPrivacySafeRetryableWriteFailure() = runTest {
        val writer = HealthConnectWorkoutWriter(insertRecord = { throw IOException("private detail") })

        val result = writer.write(record()) as HealthWriteResult.Failed

        assertEquals(SyncFailureDisposition.RETRYABLE, result.failure.disposition)
        assertEquals("HEALTH_CONNECT_WRITE_FAILED", result.failure.code)
        assertEquals(SyncErrorPhase.WRITE, result.failure.phase)
    }

    @Test
    fun invalidRecordMapsToPrivacySafePermanentWriteFailure() = runTest {
        val writer = HealthConnectWorkoutWriter(
            insertRecord = { throw IllegalArgumentException("private record detail") },
        )

        val result = writer.write(record()) as HealthWriteResult.Failed

        assertEquals(SyncFailureDisposition.PERMANENT, result.failure.disposition)
        assertEquals("INVALID_EXERCISE_RECORD", result.failure.code)
        assertEquals(SyncErrorPhase.WRITE, result.failure.phase)
    }

    @Test
    fun securityFailureMapsToPrivacySafePermanentWriteFailure() = runTest {
        val writer = HealthConnectWorkoutWriter(insertRecord = { throw SecurityException("private detail") })

        val result = writer.write(record()) as HealthWriteResult.Failed

        assertEquals(SyncFailureDisposition.PERMANENT, result.failure.disposition)
        assertEquals("HEALTH_CONNECT_WRITE_NOT_AUTHORIZED", result.failure.code)
        assertEquals(SyncErrorPhase.WRITE, result.failure.phase)
    }

    @Test
    fun cancellationIsNeverConvertedToAWriteFailure() = runTest {
        val expected = CancellationException("cancel")
        val writer = HealthConnectWorkoutWriter(insertRecord = { throw expected })

        val observed = try {
            writer.write(record())
            throw AssertionError("Expected cancellation")
        } catch (failure: CancellationException) {
            failure
        }

        assertSame(expected, observed)
    }

    private fun record() = SyntheticWorkoutFactory.create(
        Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC),
    ).let { workout ->
        HealthWorkoutMapper.toExerciseSessionRecord(
            workout,
            WorkoutMetadataPolicy.resolve(workout, previous = null),
        )
    }
}
