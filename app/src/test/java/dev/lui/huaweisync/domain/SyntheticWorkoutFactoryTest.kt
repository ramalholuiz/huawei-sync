package dev.lui.huaweisync.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class SyntheticWorkoutFactoryTest {
    @Test
    fun syntheticWorkoutIdentityAndContentAreStableAcrossClocksAndTimeZones() {
        val first = SyntheticWorkoutFactory.create(
            Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneId.of("UTC")),
        )
        val second = SyntheticWorkoutFactory.create(
            Clock.fixed(Instant.parse("2030-01-01T01:02:03Z"), ZoneId.of("Pacific/Auckland")),
        )
        val firstMetadata = WorkoutMetadataPolicy.resolve(first)
        val secondMetadata = WorkoutMetadataPolicy.resolve(second)

        assertEquals(first, second)
        assertEquals(firstMetadata, secondMetadata)
        assertEquals(SyntheticWorkoutFactory.CLIENT_RECORD_ID, firstMetadata.clientRecordId)
        assertEquals(1L, firstMetadata.clientRecordVersion)
    }

    @Test
    fun syntheticWorkoutDefinesTheCompleteStableGateOneSemantics() {
        val workout = SyntheticWorkoutFactory.create()

        assertEquals(WorkoutSource.SYNTHETIC, workout.source)
        assertEquals("gate1-strength-training", workout.sourceWorkoutId)
        assertEquals("Gate 1 synthetic strength training", workout.title)
        assertEquals(DomainActivityKind.STRENGTH_TRAINING, workout.activityKind)
        assertEquals(Instant.parse("2026-07-14T11:15:00Z"), workout.startTime)
        assertEquals(Instant.parse("2026-07-14T12:00:00Z"), workout.endTime)
        assertEquals(ZoneOffset.UTC, workout.startZoneOffset)
        assertEquals(ZoneOffset.UTC, workout.endZoneOffset)
        assertEquals("Synthetic Gate 1 record written by huawei-sync.", workout.notes)
        assertEquals("huawei-sync synthetic gate", workout.deviceName)
    }
}
