package dev.lui.huaweisync.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_ledger",
    indices = [Index(value = ["sourceProvider", "sourceRecordId"], unique = true)],
)
data class SyncLedgerEntity(
    @PrimaryKey val clientRecordId: String,
    val sourceProvider: String,
    val sourceRecordId: String,
    val sourceVersion: String?,
    /** Null only for migrated v1 rows whose original semantic content is unavailable. */
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
) {
    init {
        require(clientRecordId.isNotBlank()) { "clientRecordId must not be blank." }
        require(sourceProvider.isNotBlank()) { "sourceProvider must not be blank." }
        require(sourceRecordId.isNotBlank()) { "sourceRecordId must not be blank." }
        require(contentHash == null || SHA_256_PATTERN.matches(contentHash)) {
            "contentHash must be a lowercase SHA-256 digest when present."
        }
        require(clientRecordVersion >= 1L) { "clientRecordVersion must be at least 1." }
        require(attemptCount >= 0) { "attemptCount must not be negative." }
        require(lastErrorCode == null || lastErrorCode.length <= MAX_ERROR_CODE_LENGTH) {
            "lastErrorCode must be at most $MAX_ERROR_CODE_LENGTH characters."
        }
        require(lastErrorMessage == null || lastErrorMessage.length <= MAX_ERROR_MESSAGE_LENGTH) {
            "lastErrorMessage must be at most $MAX_ERROR_MESSAGE_LENGTH characters."
        }
    }

    companion object {
        const val MAX_ERROR_CODE_LENGTH = 64
        const val MAX_ERROR_MESSAGE_LENGTH = 256
        private val SHA_256_PATTERN = Regex("^[0-9a-f]{64}$")
    }
}
