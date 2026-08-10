package dev.lui.huaweisync.domain

import java.time.Instant
import java.time.ZoneOffset

data class DomainWorkout(
    val source: WorkoutSource,
    val sourceWorkoutId: String,
    val title: String,
    val activityKind: DomainActivityKind,
    val startTime: Instant,
    val endTime: Instant,
    val startZoneOffset: ZoneOffset?,
    val endZoneOffset: ZoneOffset?,
    val notes: String?,
    val deviceName: String?,
) {
    /** Transitional compatibility for S01 callers; durable identity uses source + sourceWorkoutId. */
    val dedupeKey: String
        get() = "${source.stableName}:$sourceWorkoutId"

    /** Transitional compatibility for S01 callers; persisted history resolves the real version. */
    val version: Long
        get() = WorkoutMetadataPolicy.INITIAL_CLIENT_RECORD_VERSION
}

enum class WorkoutSource(val stableName: String) {
    BLUETOOTH("bluetooth"),
    SYNTHETIC("synthetic"),
}

enum class DomainActivityKind(val stableName: String) {
    STRENGTH_TRAINING("strength-training"),
}
