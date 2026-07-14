package dev.lui.huaweisync.domain

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Metadata stored beside one logical source workout in the durable sync ledger. */
data class ResolvedWorkoutMetadata(
    val clientRecordId: String,
    val contentHash: String,
    val clientRecordVersion: Long,
)

/** The only persisted history needed to resolve the next Health Connect metadata version. */
data class PreviousWorkoutMetadata(
    val contentHash: String,
    val clientRecordVersion: Long,
)

/**
 * Pure metadata policy separating source identity from semantic ExerciseSessionRecord content.
 * Canonical formats are versioned so future formats can coexist without silently changing IDs.
 */
object WorkoutMetadataPolicy {
    const val INITIAL_CLIENT_RECORD_VERSION = 1L

    private const val IDENTITY_SCHEMA = "huawei-sync-identity-v1"
    private const val CONTENT_SCHEMA = "huawei-sync-exercise-session-v1"
    private val sha256Pattern = Regex("^[0-9a-f]{64}$")

    fun resolve(
        workout: DomainWorkout,
        previous: PreviousWorkoutMetadata? = null,
    ): ResolvedWorkoutMetadata {
        validateWorkout(workout)
        previous?.let(::validatePrevious)

        val contentHash = contentHashFor(workout)
        val version = when {
            previous == null -> INITIAL_CLIENT_RECORD_VERSION
            previous.contentHash == contentHash -> previous.clientRecordVersion
            previous.clientRecordVersion == Long.MAX_VALUE -> {
                throw IllegalStateException("Client record version cannot advance beyond Long.MAX_VALUE.")
            }
            else -> previous.clientRecordVersion + 1L
        }

        return ResolvedWorkoutMetadata(
            clientRecordId = clientRecordIdFor(workout),
            contentHash = contentHash,
            clientRecordVersion = version,
        )
    }

    fun clientRecordIdFor(workout: DomainWorkout): String = clientRecordIdFor(
        sourceProvider = workout.source.stableName,
        sourceRecordId = workout.sourceWorkoutId,
    )

    fun clientRecordIdFor(sourceProvider: String, sourceRecordId: String): String {
        require(sourceProvider.isNotBlank()) { "sourceProvider must not be blank." }
        require(sourceRecordId.isNotBlank()) { "sourceRecordId must not be blank." }

        val canonicalIdentity = buildString {
            append(IDENTITY_SCHEMA).append('\n')
            appendField("sourceProvider", sourceProvider)
            appendField("sourceRecordId", sourceRecordId)
        }
        return "huawei-sync:v1:${sha256(canonicalIdentity)}"
    }

    fun contentHashFor(workout: DomainWorkout): String {
        validateWorkout(workout)
        val canonicalContent = buildString {
            append(CONTENT_SCHEMA).append('\n')
            appendField("activityKind", workout.activityKind.stableName)
            appendField("startTime", workout.startTime.toString())
            appendField("endTime", workout.endTime.toString())
            appendNullableField("startZoneOffset", workout.startZoneOffset?.id)
            appendNullableField("endZoneOffset", workout.endZoneOffset?.id)
            appendField("title", workout.title)
            appendNullableField("notes", workout.notes)
        }
        return sha256(canonicalContent)
    }

    private fun validateWorkout(workout: DomainWorkout) {
        require(workout.sourceWorkoutId.isNotBlank()) { "sourceWorkoutId must not be blank." }
        require(workout.endTime.isAfter(workout.startTime)) {
            "Workout endTime must be after startTime."
        }
    }

    private fun validatePrevious(previous: PreviousWorkoutMetadata) {
        require(sha256Pattern.matches(previous.contentHash)) {
            "Previous contentHash must be a lowercase SHA-256 digest."
        }
        require(previous.clientRecordVersion >= INITIAL_CLIENT_RECORD_VERSION) {
            "Previous clientRecordVersion must be at least $INITIAL_CLIENT_RECORD_VERSION."
        }
    }

    private fun StringBuilder.appendField(name: String, value: String) {
        val byteLength = value.toByteArray(StandardCharsets.UTF_8).size
        append(name).append('=').append(byteLength).append(':').append(value).append('\n')
    }

    private fun StringBuilder.appendNullableField(name: String, value: String?) {
        if (value == null) {
            append(name).append("=-1:").append('\n')
        } else {
            appendField(name, value)
        }
    }

    private fun sha256(value: String): String = MessageDigest
        .getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
