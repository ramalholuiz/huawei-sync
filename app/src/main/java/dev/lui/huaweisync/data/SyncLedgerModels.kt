package dev.lui.huaweisync.data

import androidx.room.TypeConverter
import dev.lui.huaweisync.domain.ResolvedWorkoutMetadata
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy

/** Durable state of one logical source workout. */
enum class SyncStatus {
    PENDING,
    WRITING,
    SYNCED,
    VERIFICATION_PENDING,
    VERIFIED,
    PERMISSION_BLOCKED,
    ENVIRONMENT_BLOCKED,
    RETRYABLE_ERROR,
    PERMANENT_ERROR,
    RECONCILIATION_PENDING,
}

/** Closed user actions exposed by diagnostics; no free-form recovery instructions are exported. */
enum class DiagnosticNextAction {
    NONE,
    RUN_SYNC,
    RETRY_SYNC,
    CONFIRM,
    RECONCILE,
    REQUEST_PERMISSION,
    CHECK_HEALTH_CONNECT,
}

/** Low-cardinality classification of the evidence currently available for Gate 1. */
enum class DiagnosticEvidence {
    READY,
    ACCEPTED,
    NEEDS_CONFIRMATION,
    NEEDS_RECONCILIATION,
    VERIFIED,
    BLOCKED,
    FAILED,
}

/** Privacy-safe stage at which a sync failure occurred. */
enum class SyncErrorPhase {
    PREPARATION,
    WRITE,
    ACCEPTANCE,
    VERIFICATION,
    RECONCILIATION,
}

/** Fully resolved input accepted by the ledger; callers never construct persistence entities. */
data class PreparedLedgerWorkout(
    val sourceProvider: String,
    val sourceRecordId: String,
    val sourceVersion: String?,
    val metadata: ResolvedWorkoutMetadata,
) {
    init {
        require(sourceProvider.isNotBlank()) { "sourceProvider must not be blank." }
        require(sourceRecordId.isNotBlank()) { "sourceRecordId must not be blank." }
        require(
            metadata.clientRecordId == WorkoutMetadataPolicy.clientRecordIdFor(
                sourceProvider,
                sourceRecordId,
            ),
        ) { "Resolved clientRecordId does not match the logical source identity." }
        require(SHA_256_PATTERN.matches(metadata.contentHash)) {
            "contentHash must be a lowercase SHA-256 digest."
        }
        require(metadata.clientRecordVersion >= 1L) {
            "clientRecordVersion must be at least 1."
        }
    }
}

enum class SyncFailureDisposition { RETRYABLE, PERMANENT }

enum class SyncBlockReason { PERMISSION, ENVIRONMENT }

/** Closed, privacy-safe messages; variable user, workout, exception, and SDK data cannot enter. */
enum class SyncDiagnosticMessage(val durableValue: String) {
    PERMISSION_REQUIRED("A required permission is unavailable."),
    ENVIRONMENT_UNAVAILABLE("A required service is unavailable."),
    WRITE_FAILED("The Health Connect write failed."),
    VERIFICATION_FAILED("Health Connect verification failed."),
    INVALID_WORKOUT("The workout could not be prepared."),
    RECONCILIATION_REQUIRED("The existing ledger record requires reconciliation."),
    ;

    init {
        require(durableValue.length <= SyncLedgerEntity.MAX_ERROR_MESSAGE_LENGTH)
        require('\n' !in durableValue && '\r' !in durableValue)
    }
}

/** A bounded typed failure safe for durable storage and export. */
data class SyncFailure(
    val disposition: SyncFailureDisposition,
    val code: String,
    val phase: SyncErrorPhase,
    val safeMessage: SyncDiagnosticMessage? = null,
) {
    init {
        validateSafeErrorCode(code)
    }
}

data class SyncBlock(
    val reason: SyncBlockReason,
    val code: String,
    val phase: SyncErrorPhase,
    val safeMessage: SyncDiagnosticMessage? = null,
) {
    init {
        validateSafeErrorCode(code)
    }
}

/** Read-only typed projection returned to orchestration and diagnostics. */
data class SyncLedgerEntry(
    val clientRecordId: String,
    val sourceProvider: String,
    val sourceRecordId: String,
    val sourceVersion: String?,
    val contentHash: String?,
    val clientRecordVersion: Long,
    val healthConnectRecordId: String?,
    val status: SyncStatus,
    val attemptCount: Int,
    val acceptedAtEpochMillis: Long?,
    val confirmedAtEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val lastErrorCode: String?,
    val lastErrorPhase: SyncErrorPhase?,
    val lastErrorAtEpochMillis: Long?,
    val lastErrorMessage: String?,
)

private val SHA_256_PATTERN = Regex("^[0-9a-f]{64}$")
private val SAFE_ERROR_CODE_PATTERN = Regex("^[A-Z][A-Z0-9_]{0,63}$")

private fun validateSafeErrorCode(code: String) {
    require(SAFE_ERROR_CODE_PATTERN.matches(code)) {
        "Error code must be an uppercase identifier of at most 64 characters."
    }
}

internal object SyncLedgerConverters {
    @TypeConverter
    @JvmStatic
    fun statusToString(value: SyncStatus): String = value.name

    @TypeConverter
    @JvmStatic
    fun stringToStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    @TypeConverter
    @JvmStatic
    fun errorPhaseToString(value: SyncErrorPhase?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun stringToErrorPhase(value: String?): SyncErrorPhase? = value?.let(SyncErrorPhase::valueOf)
}
