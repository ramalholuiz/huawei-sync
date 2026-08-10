# Data mapping

## Principle
External Huawei and later Strava types must not leak into the domain model. All source payloads map into source-independent domain types before any Health Connect write. Strava is out of scope until Gate 1 and Gate 2 pass.

## Domain model draft

```kotlin
data class DomainWorkout(
    val source: WorkoutSource,
    val sourceWorkoutId: String,
    val dedupeKey: String,
    val version: Long,
    val title: String?,
    val activityKind: DomainActivityKind,
    val startTime: Instant,
    val endTime: Instant,
    val startZoneOffset: ZoneOffset?,
    val endZoneOffset: ZoneOffset?,
    val deviceName: String?,
    val metrics: DomainWorkoutMetrics
)
```

```kotlin
enum class WorkoutSource { HUAWEI, STRAVA, SYNTHETIC }
```

```kotlin
data class DomainWorkoutMetrics(
    val distanceMeters: Double?,
    val activeCaloriesKcal: Double?,
    val totalCaloriesKcal: Double?,
    val averageHeartRateBpm: Long?,
    val maxHeartRateBpm: Long?
)
```

## Deterministic identifiers

Health Connect `clientRecordId` format:

```text
huawei-sync:<source>:<stable-source-workout-id-or-hash>
```

Examples:

```text
huawei-sync:synthetic:gate1-strength-fixed
huawei-sync:huawei:<huawei-activity-record-id>
huawei-sync:strava:<strava-activity-id>
```

If the source lacks a stable workout ID, compute a deterministic hash over normalized fields:

```text
source + startTime + endTime + activityKind + duration + distance + calories
```

The hash must use canonical units and UTC timestamps.

## Cross-source deduplication for later Strava fallback

Do not implement Strava before Gate 1 and Gate 2 pass. When the optional Strava flow is later considered, it may represent the same Huawei-origin workout. Deduplication must compare:

- Source-specific IDs where a provider exposes original/external IDs.
- Start/end time overlap.
- Activity type.
- Duration tolerance.
- Distance tolerance when available.
- Calories tolerance when available.

A likely duplicate from Strava must not be written if a matching Huawei workout already exists in the ledger.

## Health Connect mapping for vertical slice

| Domain field | Health Connect field |
| --- | --- |
| `startTime` | `ExerciseSessionRecord.startTime` |
| `endTime` | `ExerciseSessionRecord.endTime` |
| `startZoneOffset` | `ExerciseSessionRecord.startZoneOffset` |
| `endZoneOffset` | `ExerciseSessionRecord.endZoneOffset` |
| `activityKind` | `ExerciseSessionRecord.exerciseType` |
| deterministic ID | `metadata.clientRecordId` |
| `version` | `metadata.clientRecordVersion` |
| `deviceName` | `metadata.device`, when supported by chosen SDK |

## Metrics after vertical slice

Do not add these before Gate 1 and Gate 2 pass:

- Heart rate associated with a workout.
- Calories associated with a workout.
- Distance associated with a workout.

When added, only write metrics that are present or confidently derivable from the source. Do not fabricate missing values.

## Out of MVP initial scope

- Steps and `StepsRecord`.
- Sleep.
- SpO2.
- Weight.
- Body composition.
- Direct GymRats integration of any kind.
