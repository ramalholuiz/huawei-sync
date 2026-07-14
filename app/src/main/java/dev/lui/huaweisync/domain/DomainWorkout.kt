package dev.lui.huaweisync.domain

import java.time.Instant
import java.time.ZoneOffset

data class DomainWorkout(
    val source: WorkoutSource,
    val sourceWorkoutId: String,
    val dedupeKey: String,
    val version: Long,
    val title: String,
    val activityKind: DomainActivityKind,
    val startTime: Instant,
    val endTime: Instant,
    val startZoneOffset: ZoneOffset?,
    val endZoneOffset: ZoneOffset?,
    val deviceName: String?,
)

enum class WorkoutSource { SYNTHETIC }

enum class DomainActivityKind { STRENGTH_TRAINING }
