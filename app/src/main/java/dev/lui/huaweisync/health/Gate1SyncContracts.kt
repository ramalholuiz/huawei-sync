package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.PreparedLedgerWorkout
import dev.lui.huaweisync.data.SyncBlock
import dev.lui.huaweisync.data.SyncFailure
import dev.lui.huaweisync.data.SyncLedgerEntry

/** Coordinator-facing persistence boundary; implementations must keep each transition atomic. */
interface Gate1SyncLedger {
    suspend fun findBySource(sourceProvider: String, sourceRecordId: String): SyncLedgerEntry?

    suspend fun prepare(workout: PreparedLedgerWorkout): SyncLedgerEntry

    suspend fun beginWrite(clientRecordId: String): SyncLedgerEntry

    suspend fun recordBlocked(clientRecordId: String, block: SyncBlock): SyncLedgerEntry

    suspend fun recordFailure(clientRecordId: String, failure: SyncFailure): SyncLedgerEntry

    suspend fun recordAccepted(
        clientRecordId: String,
        healthConnectRecordId: String?,
    ): SyncLedgerEntry

    /**
     * Best-effort recovery after the external system accepted a write but normal local
     * finalization failed. The accepted timestamp and optional external ID are durable facts.
     */
    suspend fun recordAcceptanceUncertain(
        clientRecordId: String,
        healthConnectRecordId: String?,
    ): SyncLedgerEntry
}

fun interface SyncPreflight {
    suspend fun check(): SyncPreflightResult
}

sealed interface SyncPreflightResult {
    data object Ready : SyncPreflightResult

    data class Blocked(val block: SyncBlock) : SyncPreflightResult
}

enum class SyncPhase {
    PREFLIGHT,
    EXTERNAL_WRITE,
    LOCAL_FINALIZATION,
}

enum class LocalFinalizationStatus {
    FINALIZED,
    RECONCILIATION_PENDING,
    FAILED,
}
