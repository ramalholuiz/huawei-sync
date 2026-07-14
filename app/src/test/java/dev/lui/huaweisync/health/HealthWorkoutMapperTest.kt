package dev.lui.huaweisync.health

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class HealthWorkoutMapperTest {
    @Test
    fun mapsSyntheticWorkoutToStrengthExerciseSessionRecord() {
        val workout = SyntheticWorkoutFactory.create(
            Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneId.of("UTC")),
        )

        val record = HealthWorkoutMapper.toExerciseSessionRecord(workout)

        assertEquals(ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, record.exerciseType)
        assertEquals("Gate 1 synthetic strength training", record.title)
        assertEquals("huawei-sync:synthetic:gate1-strength-training", record.metadata.clientRecordId)
        assertEquals(1L, record.metadata.clientRecordVersion)
        assertEquals(Metadata.RECORDING_METHOD_MANUAL_ENTRY, record.metadata.recordingMethod)
    }
}
