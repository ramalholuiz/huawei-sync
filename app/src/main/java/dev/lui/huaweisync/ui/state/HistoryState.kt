package dev.lui.huaweisync.ui.state

import androidx.compose.runtime.Immutable
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus

/** Screen-level history state. Ledger failures are intentionally isolated from sync controls. */
sealed interface HistoryState {
    data object Loading : HistoryState
    data object Empty : HistoryState

    @Immutable
    data class RetryableError(val safeCode: String = "LEDGER_HISTORY_READ_FAILED") : HistoryState

    @Immutable
    data class Content(val activities: List<ActivityHistoryItem>) : HistoryState
}

/** Truthful readback state derived only from durable ledger facts. */
enum class ActivityReadbackState {
    PREPARED,
    RECONCILIATION_REQUIRED,
    PENDING_READBACK,
    VERIFIED,
    RETRYABLE_ERROR,
    ACTION_REQUIRED,
}

@Immutable
data class ActivityHistoryItem(
    val clientRecordId: String,
    val clientRecordVersion: Long,
    val sourceProvider: String,
    val attemptCount: Int,
    val acceptedAtEpochMillis: Long?,
    val confirmedAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long,
    val readbackState: ActivityReadbackState,
    val safeErrorCode: String?,
)

/** Pure ledger-to-presentation boundary. Room and Health Connect records never enter UI state. */
object HistoryStateMapper {
    fun from(entries: List<SyncLedgerEntry>): HistoryState =
        if (entries.isEmpty()) {
            HistoryState.Empty
        } else {
            HistoryState.Content(entries.map(::toHistoryItem))
        }

    fun toHistoryItem(entry: SyncLedgerEntry): ActivityHistoryItem = ActivityHistoryItem(
        clientRecordId = entry.clientRecordId,
        clientRecordVersion = entry.clientRecordVersion,
        sourceProvider = entry.sourceProvider,
        attemptCount = entry.attemptCount,
        acceptedAtEpochMillis = entry.acceptedAtEpochMillis,
        confirmedAtEpochMillis = entry.confirmedAtEpochMillis,
        updatedAtEpochMillis = entry.updatedAtEpochMillis,
        readbackState = entry.toReadbackState(),
        safeErrorCode = entry.lastErrorCode,
    )

    /** Selection is exact and reinstall-stable because only deterministic client identity is used. */
    fun select(state: HistoryState, clientRecordId: String): ActivityHistoryItem? =
        (state as? HistoryState.Content)
            ?.activities
            ?.singleOrNull { it.clientRecordId == clientRecordId }

    private fun SyncLedgerEntry.toReadbackState(): ActivityReadbackState = when (status) {
        SyncStatus.VERIFIED -> ActivityReadbackState.VERIFIED
        SyncStatus.WRITING,
        SyncStatus.RECONCILIATION_PENDING,
        -> ActivityReadbackState.RECONCILIATION_REQUIRED
        SyncStatus.SYNCED,
        SyncStatus.VERIFICATION_PENDING,
        -> ActivityReadbackState.PENDING_READBACK
        SyncStatus.RETRYABLE_ERROR -> ActivityReadbackState.RETRYABLE_ERROR
        SyncStatus.PERMISSION_BLOCKED,
        SyncStatus.ENVIRONMENT_BLOCKED,
        SyncStatus.PERMANENT_ERROR,
        -> ActivityReadbackState.ACTION_REQUIRED
        SyncStatus.PENDING -> ActivityReadbackState.PREPARED
    }
}
