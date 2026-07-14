package dev.lui.huaweisync.data

import androidx.room.withTransaction
import dev.lui.huaweisync.health.Gate1SyncLedger

fun interface LedgerClock {
    fun nowEpochMillis(): Long
}

object SystemLedgerClock : LedgerClock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}

class SyncLedgerConflictException(message: String) : IllegalStateException(message)

/**
 * Atomic transition boundary for the durable sync ledger.
 *
 * Callers provide typed commands and receive immutable projections; entity construction and
 * transition bookkeeping remain private to this module.
 */
class SyncLedgerStore(
    private val database: AppDatabase,
    private val clock: LedgerClock = SystemLedgerClock,
) : Gate1SyncLedger {
    private val dao: SyncLedgerDao
        get() = database.syncLedgerDao()

    suspend fun findByClientRecordId(clientRecordId: String): SyncLedgerEntry? =
        dao.findByClientRecordId(clientRecordId)?.toEntry()

    override suspend fun findBySource(sourceProvider: String, sourceRecordId: String): SyncLedgerEntry? =
        dao.findBySource(sourceProvider, sourceRecordId)?.toEntry()

    override suspend fun prepare(workout: PreparedLedgerWorkout): SyncLedgerEntry = database.withTransaction {
        val bySource = dao.findBySource(workout.sourceProvider, workout.sourceRecordId)
        val byClient = dao.findByClientRecordId(workout.metadata.clientRecordId)
        if (bySource != null && byClient != null && bySource.clientRecordId != byClient.clientRecordId) {
            throw SyncLedgerConflictException("Source identity and client identity resolve to different rows.")
        }

        val existing = bySource ?: byClient
        if (existing == null) {
            val now = checkedNow()
            val created = SyncLedgerEntity(
                clientRecordId = workout.metadata.clientRecordId,
                sourceProvider = workout.sourceProvider,
                sourceRecordId = workout.sourceRecordId,
                sourceVersion = workout.sourceVersion,
                contentHash = workout.metadata.contentHash,
                clientRecordVersion = workout.metadata.clientRecordVersion,
                healthConnectRecordId = null,
                status = SyncStatus.PENDING,
                attemptCount = 0,
                acceptedAtEpochMillis = null,
                confirmedAtEpochMillis = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                lastErrorCode = null,
                lastErrorPhase = null,
                lastErrorAtEpochMillis = null,
                lastErrorMessage = null,
            )
            dao.insert(created)
            return@withTransaction created.toEntry()
        }

        ensureSameIdentity(existing, workout)
        val migratedUnknownContent = existing.contentHash == null
        val semanticContentChanged = !migratedUnknownContent &&
            existing.contentHash != workout.metadata.contentHash
        when {
            !migratedUnknownContent && !semanticContentChanged &&
                existing.clientRecordVersion != workout.metadata.clientRecordVersion -> {
                throw SyncLedgerConflictException("Unchanged semantic content must preserve its client version.")
            }
            semanticContentChanged &&
                (
                    existing.clientRecordVersion == Long.MAX_VALUE ||
                        workout.metadata.clientRecordVersion != existing.clientRecordVersion + 1L
                ) -> {
                throw SyncLedgerConflictException(
                    "Changed semantic content must advance its client version by exactly one.",
                )
            }
        }

        val prepared = if (semanticContentChanged) {
            existing.copy(
                sourceVersion = workout.sourceVersion,
                contentHash = workout.metadata.contentHash,
                clientRecordVersion = workout.metadata.clientRecordVersion,
                healthConnectRecordId = null,
                status = SyncStatus.PENDING,
                acceptedAtEpochMillis = null,
                confirmedAtEpochMillis = null,
                updatedAtEpochMillis = transitionTime(existing),
            ).withoutError()
        } else {
            existing.copy(
                sourceVersion = workout.sourceVersion,
                contentHash = workout.metadata.contentHash,
                clientRecordVersion = workout.metadata.clientRecordVersion,
                updatedAtEpochMillis = transitionTime(existing),
            )
        }
        updateExactlyOne(prepared)
        prepared.toEntry()
    }

    override suspend fun beginWrite(clientRecordId: String): SyncLedgerEntry = mutate(clientRecordId) { row ->
        check(row.acceptedAtEpochMillis == null) {
            "An accepted workout must be verified instead of written again."
        }
        check(
            row.status in setOf(
                SyncStatus.PENDING,
                SyncStatus.RETRYABLE_ERROR,
                SyncStatus.PERMISSION_BLOCKED,
                SyncStatus.ENVIRONMENT_BLOCKED,
            ),
        ) { "Ledger status ${row.status} cannot begin a write." }
        check(row.attemptCount < Int.MAX_VALUE) { "Ledger attempt count cannot advance beyond Int.MAX_VALUE." }
        row.copy(
            status = SyncStatus.WRITING,
            attemptCount = row.attemptCount + 1,
            updatedAtEpochMillis = transitionTime(row),
        ).withoutError()
    }

    override suspend fun recordAccepted(
        clientRecordId: String,
        healthConnectRecordId: String?,
    ): SyncLedgerEntry = mutate(clientRecordId) { row ->
        check(row.status == SyncStatus.WRITING) { "Only an active write can be recorded as accepted." }
        val now = transitionTime(row)
        row.copy(
            healthConnectRecordId = healthConnectRecordId ?: row.healthConnectRecordId,
            status = SyncStatus.SYNCED,
            acceptedAtEpochMillis = now,
            confirmedAtEpochMillis = null,
            updatedAtEpochMillis = now,
        ).withoutError()
    }

    override suspend fun recordAcceptanceUncertain(
        clientRecordId: String,
        healthConnectRecordId: String?,
    ): SyncLedgerEntry = mutate(clientRecordId) { row ->
        check(row.status in setOf(SyncStatus.WRITING, SyncStatus.RECONCILIATION_PENDING)) {
            "Only an active or uncertain write can be marked for reconciliation."
        }
        val now = transitionTime(row)
        row.copy(
            healthConnectRecordId = healthConnectRecordId ?: row.healthConnectRecordId,
            status = SyncStatus.RECONCILIATION_PENDING,
            acceptedAtEpochMillis = row.acceptedAtEpochMillis ?: now,
            confirmedAtEpochMillis = null,
            updatedAtEpochMillis = now,
            lastErrorCode = "LOCAL_FINALIZATION_FAILED",
            lastErrorPhase = SyncErrorPhase.ACCEPTANCE,
            lastErrorAtEpochMillis = now,
            lastErrorMessage = null,
        )
    }

    suspend fun beginVerification(clientRecordId: String): SyncLedgerEntry = mutate(clientRecordId) { row ->
        check(row.acceptedAtEpochMillis != null) { "Verification requires a recorded acceptance." }
        check(
            row.status in setOf(
                SyncStatus.SYNCED,
                SyncStatus.RETRYABLE_ERROR,
                SyncStatus.PERMISSION_BLOCKED,
                SyncStatus.ENVIRONMENT_BLOCKED,
                SyncStatus.VERIFICATION_PENDING,
            ),
        ) { "Ledger status ${row.status} cannot begin verification." }
        row.copy(
            status = SyncStatus.VERIFICATION_PENDING,
            updatedAtEpochMillis = transitionTime(row),
        ).withoutError()
    }

    suspend fun confirm(clientRecordId: String): SyncLedgerEntry = mutate(clientRecordId) { row ->
        check(row.status == SyncStatus.VERIFICATION_PENDING) {
            "Confirmation requires verification to be pending."
        }
        check(row.acceptedAtEpochMillis != null) { "Confirmation requires a recorded acceptance." }
        val now = transitionTime(row)
        row.copy(
            status = SyncStatus.VERIFIED,
            confirmedAtEpochMillis = now,
            updatedAtEpochMillis = now,
        ).withoutError()
    }

    suspend fun recordBlocked(clientRecordId: String, block: SyncBlock): SyncLedgerEntry =
        mutate(clientRecordId) { row ->
            check(
                row.status in setOf(
                    SyncStatus.PENDING,
                    SyncStatus.WRITING,
                    SyncStatus.RETRYABLE_ERROR,
                    SyncStatus.PERMISSION_BLOCKED,
                    SyncStatus.ENVIRONMENT_BLOCKED,
                ),
            ) { "Ledger status ${row.status} cannot become blocked." }
            val now = transitionTime(row)
            row.copy(
                status = when (block.reason) {
                    SyncBlockReason.PERMISSION -> SyncStatus.PERMISSION_BLOCKED
                    SyncBlockReason.ENVIRONMENT -> SyncStatus.ENVIRONMENT_BLOCKED
                },
                updatedAtEpochMillis = now,
                lastErrorCode = block.code,
                lastErrorPhase = block.phase,
                lastErrorAtEpochMillis = now,
                lastErrorMessage = block.safeMessage?.durableValue,
            )
        }

    suspend fun recordFailure(clientRecordId: String, failure: SyncFailure): SyncLedgerEntry =
        mutate(clientRecordId) { row ->
            check(
                row.status in setOf(
                    SyncStatus.PENDING,
                    SyncStatus.WRITING,
                    SyncStatus.SYNCED,
                    SyncStatus.VERIFICATION_PENDING,
                    SyncStatus.RETRYABLE_ERROR,
                    SyncStatus.PERMISSION_BLOCKED,
                    SyncStatus.ENVIRONMENT_BLOCKED,
                ),
            ) { "Ledger status ${row.status} cannot become an error." }
            val now = transitionTime(row)
            row.copy(
                status = when (failure.disposition) {
                    SyncFailureDisposition.RETRYABLE -> SyncStatus.RETRYABLE_ERROR
                    SyncFailureDisposition.PERMANENT -> SyncStatus.PERMANENT_ERROR
                },
                updatedAtEpochMillis = now,
                lastErrorCode = failure.code,
                lastErrorPhase = failure.phase,
                lastErrorAtEpochMillis = now,
                lastErrorMessage = failure.safeMessage?.durableValue,
            )
        }

    /** Explicitly abandons uncertain migrated acceptance facts before a deliberate retry. */
    suspend fun reconcileForRetry(clientRecordId: String): SyncLedgerEntry = mutate(clientRecordId) { row ->
        check(row.status == SyncStatus.RECONCILIATION_PENDING) {
            "Only a reconciliation-pending row can be reconciled."
        }
        row.copy(
            healthConnectRecordId = null,
            status = SyncStatus.PENDING,
            acceptedAtEpochMillis = null,
            confirmedAtEpochMillis = null,
            updatedAtEpochMillis = transitionTime(row),
        ).withoutError()
    }

    /** Confirms that a reconciliation-pending row represents an already accepted record. */
    suspend fun reconcileAsAccepted(
        clientRecordId: String,
        healthConnectRecordId: String? = null,
    ): SyncLedgerEntry = mutate(clientRecordId) { row ->
        check(row.status == SyncStatus.RECONCILIATION_PENDING) {
            "Only a reconciliation-pending row can be reconciled."
        }
        val resolvedHealthId = healthConnectRecordId ?: row.healthConnectRecordId
        check(resolvedHealthId != null) { "Accepted reconciliation requires a Health Connect record ID." }
        val now = transitionTime(row)
        row.copy(
            healthConnectRecordId = resolvedHealthId,
            status = SyncStatus.SYNCED,
            acceptedAtEpochMillis = row.acceptedAtEpochMillis ?: now,
            confirmedAtEpochMillis = null,
            updatedAtEpochMillis = now,
        ).withoutError()
    }

    private suspend fun mutate(
        clientRecordId: String,
        transition: (SyncLedgerEntity) -> SyncLedgerEntity,
    ): SyncLedgerEntry = database.withTransaction {
        val current = dao.findByClientRecordId(clientRecordId)
            ?: throw NoSuchElementException("No ledger row exists for the client identity.")
        val updated = transition(current)
        ensureImmutableIdentity(current, updated)
        updateExactlyOne(updated)
        updated.toEntry()
    }

    private fun ensureSameIdentity(existing: SyncLedgerEntity, workout: PreparedLedgerWorkout) {
        if (
            existing.clientRecordId != workout.metadata.clientRecordId ||
            existing.sourceProvider != workout.sourceProvider ||
            existing.sourceRecordId != workout.sourceRecordId
        ) {
            throw SyncLedgerConflictException("Prepared workout conflicts with an existing logical identity.")
        }
    }

    private fun ensureImmutableIdentity(before: SyncLedgerEntity, after: SyncLedgerEntity) {
        check(before.clientRecordId == after.clientRecordId)
        check(before.sourceProvider == after.sourceProvider)
        check(before.sourceRecordId == after.sourceRecordId)
        check(before.sourceVersion == after.sourceVersion)
        check(before.contentHash == after.contentHash)
        check(before.clientRecordVersion == after.clientRecordVersion)
        check(before.createdAtEpochMillis == after.createdAtEpochMillis)
    }

    private suspend fun updateExactlyOne(entity: SyncLedgerEntity) {
        check(dao.update(entity) == 1) { "Atomic ledger update did not affect exactly one row." }
    }

    private fun checkedNow(): Long = clock.nowEpochMillis().also {
        require(it >= 0L) { "Ledger clock must return a non-negative epoch timestamp." }
    }

    private fun transitionTime(row: SyncLedgerEntity): Long = maxOf(checkedNow(), row.updatedAtEpochMillis)
}

private fun SyncLedgerEntity.withoutError() = copy(
    lastErrorCode = null,
    lastErrorPhase = null,
    lastErrorAtEpochMillis = null,
    lastErrorMessage = null,
)

private fun SyncLedgerEntity.toEntry() = SyncLedgerEntry(
    clientRecordId = clientRecordId,
    sourceProvider = sourceProvider,
    sourceRecordId = sourceRecordId,
    sourceVersion = sourceVersion,
    contentHash = contentHash,
    clientRecordVersion = clientRecordVersion,
    healthConnectRecordId = healthConnectRecordId,
    status = status,
    attemptCount = attemptCount,
    acceptedAtEpochMillis = acceptedAtEpochMillis,
    confirmedAtEpochMillis = confirmedAtEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    lastErrorCode = lastErrorCode,
    lastErrorPhase = lastErrorPhase,
    lastErrorAtEpochMillis = lastErrorAtEpochMillis,
    lastErrorMessage = lastErrorMessage,
)
