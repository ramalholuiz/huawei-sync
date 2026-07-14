package dev.lui.huaweisync.health

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import dev.lui.huaweisync.domain.DomainActivityKind
import dev.lui.huaweisync.domain.DomainWorkout
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory

object HealthWorkoutMapper {
    fun toExerciseSessionRecord(workout: DomainWorkout): ExerciseSessionRecord {
        require(workout.activityKind == DomainActivityKind.STRENGTH_TRAINING) {
            "Gate 1 only supports synthetic strength training sessions."
        }
        require(workout.endTime.isAfter(workout.startTime)) {
            "Workout endTime must be after startTime."
        }

        return ExerciseSessionRecord(
            startTime = workout.startTime,
            startZoneOffset = workout.startZoneOffset,
            endTime = workout.endTime,
            endZoneOffset = workout.endZoneOffset,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
            title = workout.title,
            notes = "Synthetic Gate 1 record written by huawei-sync.",
            metadata = Metadata.manualEntry(
                clientRecordId = SyntheticWorkoutFactory.clientRecordIdFor(workout),
                clientRecordVersion = workout.version,
            ),
        )
    }
}
