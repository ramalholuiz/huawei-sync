package dev.lui.huaweisync.bluetooth

import dev.lui.huaweisync.domain.DomainActivityKind
import dev.lui.huaweisync.domain.WorkoutSource
import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothWorkoutSourceTest {
    @Test
    fun readsPayloadThroughTheWorkoutSourceSeam() = runTest {
        val source = BluetoothWorkoutSource {
            PAYLOAD.toByteArray(StandardCharsets.UTF_8)
        }

        val workout = source.readWorkout()

        assertEquals(WorkoutSource.BLUETOOTH, workout.source)
        assertEquals("watch-fit-5-pro:activity-99", workout.sourceWorkoutId)
        assertEquals(DomainActivityKind.STRENGTH_TRAINING, workout.activityKind)
        assertEquals(Instant.parse("2026-08-10T12:00:00Z"), workout.startTime)
    }

    @Test
    fun rejectsBadPayloadBeforeTheLedgerSeesIt() = runTest {
        val source = BluetoothWorkoutSource {
            "bad\nsourceRecordId=x\n".toByteArray(StandardCharsets.UTF_8)
        }

        val failure = runCatching { source.readWorkout() }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    private companion object {
        const val PAYLOAD = """
huawei-sync-workout-v1
sourceRecordId=watch-fit-5-pro:activity-99
title=Watch strength
activityKind=strength-training
startTime=2026-08-10T12:00:00Z
endTime=2026-08-10T12:45:00Z
"""
    }
}
