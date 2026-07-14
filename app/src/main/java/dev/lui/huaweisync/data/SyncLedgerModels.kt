package dev.lui.huaweisync.data

import androidx.room.TypeConverter

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

/** Privacy-safe stage at which a sync failure occurred. */
enum class SyncErrorPhase {
    PREPARATION,
    WRITE,
    ACCEPTANCE,
    VERIFICATION,
    RECONCILIATION,
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
