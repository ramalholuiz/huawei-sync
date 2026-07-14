package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.PreparedLedgerWorkout
import dev.lui.huaweisync.data.SyncLedgerEntry

/** Coordinator-facing persistence boundary; implementations must keep each transition atomic. */
interface Gate1SyncLedger {
    suspend fun findBySource(sourceProvider: String, sourceRecordId: String): SyncLedgerEntry?

    suspend fun prepare(workout: PreparedLedgerWorkout): SyncLedgerEntry

    suspend fun beginWrite(clientRecordId: String): SyncLedgerEntry

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

enum class SyncPhase {
    LOCAL_FINALIZATION,
}

enum class LocalFinalizationStatus {
    FINALIZED,
    RECONCILIATION_PENDING,
    FAILED,
}
