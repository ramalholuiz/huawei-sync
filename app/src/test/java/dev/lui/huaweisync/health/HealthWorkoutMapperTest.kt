package dev.lui.huaweisync.health

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import dev.lui.huaweisync.domain.ResolvedWorkoutMetadata
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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

        val resolvedMetadata = WorkoutMetadataPolicy.resolve(workout)
            .copy(clientRecordVersion = 7L)

        val record = HealthWorkoutMapper.toExerciseSessionRecord(workout, resolvedMetadata)

        assertEquals(ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING, record.exerciseType)
        assertEquals("Gate 1 synthetic strength training", record.title)
        assertEquals(SyntheticWorkoutFactory.NOTES, record.notes)
        assertEquals(resolvedMetadata.clientRecordId, record.metadata.clientRecordId)
        assertEquals(resolvedMetadata.clientRecordVersion, record.metadata.clientRecordVersion)
        assertEquals(Metadata.RECORDING_METHOD_MANUAL_ENTRY, record.metadata.recordingMethod)
    }

    @Test
    fun rejectsResolvedMetadataForDifferentIdentityOrContent() {
        val workout = SyntheticWorkoutFactory.create()
        val valid = WorkoutMetadataPolicy.resolve(workout)

        assertThrows(IllegalArgumentException::class.java) {
            HealthWorkoutMapper.toExerciseSessionRecord(
                workout,
                valid.copy(clientRecordId = "huawei-sync:v1:${"0".repeat(64)}"),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            HealthWorkoutMapper.toExerciseSessionRecord(
                workout,
                ResolvedWorkoutMetadata(
                    clientRecordId = valid.clientRecordId,
                    contentHash = "0".repeat(64),
                    clientRecordVersion = valid.clientRecordVersion,
                ),
            )
        }
    }
}
