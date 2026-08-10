package dev.lui.huaweisync.bluetooth

import dev.lui.huaweisync.domain.DomainActivityKind
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy
import dev.lui.huaweisync.domain.WorkoutSource
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BluetoothWorkoutSummaryCodecTest {
    @Test
    fun decodesWorkoutSummaryIntoDomainWorkout() {
        val workout = BluetoothWorkoutSummaryCodec.decode(PAYLOAD.toByteArray(StandardCharsets.UTF_8))

        assertEquals(WorkoutSource.BLUETOOTH, workout.source)
        assertEquals("watch-fit-5-pro:activity-42", workout.sourceWorkoutId)
        assertEquals("Watch strength", workout.title)
        assertEquals(DomainActivityKind.STRENGTH_TRAINING, workout.activityKind)
        assertEquals(Instant.parse("2026-08-10T10:00:00Z"), workout.startTime)
        assertEquals(Instant.parse("2026-08-10T10:45:00Z"), workout.endTime)
        assertEquals(ZoneOffset.ofHours(-3), workout.startZoneOffset)
        assertEquals(ZoneOffset.ofHours(-3), workout.endZoneOffset)
        assertEquals("Huawei Watch Fit 5 Pro", workout.deviceName)
        assertEquals(
            WorkoutMetadataPolicy.clientRecordIdFor("bluetooth", "watch-fit-5-pro:activity-42"),
            WorkoutMetadataPolicy.clientRecordIdFor(workout),
        )
    }

    @Test
    fun rejectsMalformedPayloadsBeforeTheyReachTheLedger() {
        listOf(
            "wrong-header\nsourceRecordId=id\n",
            "huawei-sync-workout-v1\nsourceRecordId=\nactivityKind=strength-training\n",
            PAYLOAD + "sourceRecordId=duplicate\n",
            PAYLOAD.replace("activityKind=strength-training", "activityKind=steps"),
        ).forEach { payload ->
            assertThrows(IllegalArgumentException::class.java) {
                BluetoothWorkoutSummaryCodec.decode(payload.toByteArray(StandardCharsets.UTF_8))
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            BluetoothWorkoutSummaryCodec.decode(byteArrayOf(0xc3.toByte()))
        }
    }

    private companion object {
        const val PAYLOAD = """
huawei-sync-workout-v1
sourceRecordId=watch-fit-5-pro:activity-42
title=Watch strength
activityKind=strength-training
startTime=2026-08-10T10:00:00Z
endTime=2026-08-10T10:45:00Z
startZoneOffset=-03:00
endZoneOffset=-03:00
notes=Transferred over controlled Bluetooth loopback.
deviceName=Huawei Watch Fit 5 Pro
"""
    }
}
