package dev.lui.huaweisync.domain

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

object SyntheticWorkoutFactory {
    const val SOURCE_WORKOUT_ID = "gate1-strength-training"
    const val DEDUPE_KEY = "synthetic:gate1-strength-training"
    const val CLIENT_RECORD_VERSION = WorkoutMetadataPolicy.INITIAL_CLIENT_RECORD_VERSION
    const val TITLE = "Gate 1 synthetic strength training"
    const val NOTES = "Synthetic Gate 1 record written by huawei-sync."
    const val DEVICE_NAME = "huawei-sync synthetic gate"

    val CLIENT_RECORD_ID: String = WorkoutMetadataPolicy.clientRecordIdFor(
        sourceProvider = WorkoutSource.SYNTHETIC.stableName,
        sourceRecordId = SOURCE_WORKOUT_ID,
    )

    private val startTime = Instant.parse("2026-07-14T11:15:00Z")
    private val endTime = Instant.parse("2026-07-14T12:00:00Z")

    /**
     * The clock remains temporarily source-compatible with S01. Fixture semantics intentionally
     * never depend on wall time or the host time zone.
     */
    @Suppress("UNUSED_PARAMETER")
    fun create(clock: Clock = Clock.systemUTC()): DomainWorkout = DomainWorkout(
        source = WorkoutSource.SYNTHETIC,
        sourceWorkoutId = SOURCE_WORKOUT_ID,
        title = TITLE,
        activityKind = DomainActivityKind.STRENGTH_TRAINING,
        startTime = startTime,
        endTime = endTime,
        startZoneOffset = ZoneOffset.UTC,
        endZoneOffset = ZoneOffset.UTC,
        notes = NOTES,
        deviceName = DEVICE_NAME,
    )

    fun clientRecordIdFor(workout: DomainWorkout): String =
        WorkoutMetadataPolicy.clientRecordIdFor(workout)
}
