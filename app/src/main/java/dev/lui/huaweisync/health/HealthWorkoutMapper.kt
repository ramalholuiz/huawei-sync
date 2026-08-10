package dev.lui.huaweisync.health

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import dev.lui.huaweisync.domain.DomainActivityKind
import dev.lui.huaweisync.domain.DomainWorkout
import dev.lui.huaweisync.domain.ResolvedWorkoutMetadata
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy

object HealthWorkoutMapper {
    fun toExerciseSessionRecord(
        workout: DomainWorkout,
        metadata: ResolvedWorkoutMetadata,
    ): ExerciseSessionRecord {
        require(workout.activityKind == DomainActivityKind.STRENGTH_TRAINING) {
            "The current Health Connect writer only supports strength training sessions."
        }
        require(workout.endTime.isAfter(workout.startTime)) {
            "Workout endTime must be after startTime."
        }
        require(metadata.clientRecordId == WorkoutMetadataPolicy.clientRecordIdFor(workout)) {
            "Resolved clientRecordId does not match the workout source identity."
        }
        require(metadata.contentHash == WorkoutMetadataPolicy.contentHashFor(workout)) {
            "Resolved contentHash does not match the mapped workout content."
        }
        require(metadata.clientRecordVersion >= WorkoutMetadataPolicy.INITIAL_CLIENT_RECORD_VERSION) {
            "Resolved clientRecordVersion must be at least 1."
        }

        return ExerciseSessionRecord(
            startTime = workout.startTime,
            startZoneOffset = workout.startZoneOffset,
            endTime = workout.endTime,
            endZoneOffset = workout.endZoneOffset,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
            title = workout.title,
            notes = workout.notes,
            metadata = Metadata.manualEntry(
                clientRecordId = metadata.clientRecordId,
                clientRecordVersion = metadata.clientRecordVersion,
            ),
        )
    }
}
