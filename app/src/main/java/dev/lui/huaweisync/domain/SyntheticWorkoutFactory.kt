package dev.lui.huaweisync.domain

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

object SyntheticWorkoutFactory {
    const val SOURCE_WORKOUT_ID = "gate1-strength-training"
    const val DEDUPE_KEY = "synthetic:gate1:strength-training"
    const val CLIENT_RECORD_ID = "huawei-sync:synthetic:gate1-strength-training"

    /**
     * Version 1 is the baseline Gate 1 payload. Increase this only when the
     * semantic workout payload changes and Health Connect should receive an
     * update for the same deterministic clientRecordId.
     */
    const val CLIENT_RECORD_VERSION = 1L

    fun create(clock: Clock = Clock.systemDefaultZone()): DomainWorkout {
        val now = Instant.now(clock).truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
        val start = now.minus(Duration.ofMinutes(45))
        return DomainWorkout(
            source = WorkoutSource.SYNTHETIC,
            sourceWorkoutId = SOURCE_WORKOUT_ID,
            dedupeKey = DEDUPE_KEY,
            version = CLIENT_RECORD_VERSION,
            title = "Gate 1 synthetic strength training",
            activityKind = DomainActivityKind.STRENGTH_TRAINING,
            startTime = start,
            endTime = now,
            startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(start),
            endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(now),
            deviceName = "huawei-sync synthetic gate",
        )
    }

    fun clientRecordIdFor(workout: DomainWorkout): String = when (workout.source) {
        WorkoutSource.SYNTHETIC -> CLIENT_RECORD_ID
    }
}
