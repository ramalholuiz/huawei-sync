package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.PreparedLedgerWorkout
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.domain.PreviousWorkoutMetadata
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy
import java.time.Clock
import kotlinx.coroutines.CancellationException

class Gate1SyncCoordinator(
    private val ledgerStore: Gate1SyncLedger,
    private val writer: HealthWorkoutWriter,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val preflight: SyncPreflight = SyncPreflight { SyncPreflightResult.Ready },
) {
    suspend fun runSyntheticStrengthSync(): Gate1SyncResult {
        val workout = SyntheticWorkoutFactory.create(clock)
        val existing = ledgerStore.findBySource(
            workout.source.stableName,
            workout.sourceWorkoutId,
        )
        val previousMetadata = existing?.contentHash?.let { contentHash ->
            PreviousWorkoutMetadata(
                contentHash = contentHash,
                clientRecordVersion = existing.clientRecordVersion,
            )
        }
        val metadata = WorkoutMetadataPolicy.resolve(workout, previousMetadata)
        val prepared = ledgerStore.prepare(
            PreparedLedgerWorkout(
                sourceProvider = workout.source.stableName,
                sourceRecordId = workout.sourceWorkoutId,
                sourceVersion = null,
                metadata = metadata,
            ),
        )

        if (
            prepared.acceptedAtEpochMillis != null &&
            prepared.status != SyncStatus.RECONCILIATION_PENDING
        ) {
            return prepared.toResult()
        }

        // Stranded or uncertain writes must be reconciled explicitly; preflight must not relabel
        // them as blockers because blocker states are eligible for a later write.
        check(prepared.status !in setOf(SyncStatus.WRITING, SyncStatus.RECONCILIATION_PENDING)) {
            "An uncertain write must be reconciled before another write attempt."
        }

        when (val preflightResult = preflight.check()) {
            is SyncPreflightResult.Blocked -> {
                val blocked = ledgerStore.recordBlocked(metadata.clientRecordId, preflightResult.block)
                return Gate1SyncResult.Blocked(
                    clientRecordId = blocked.clientRecordId,
                    clientRecordVersion = blocked.clientRecordVersion,
                    ledgerRowsForClientRecordId = 1,
                    writeCountForClientRecordId = blocked.attemptCount,
                    reason = preflightResult.block.reason,
                    phase = SyncPhase.PREFLIGHT,
                    code = preflightResult.block.code,
                )
            }
            SyncPreflightResult.Ready -> Unit
        }

        val writing = ledgerStore.beginWrite(metadata.clientRecordId)
        val record = HealthWorkoutMapper.toExerciseSessionRecord(workout, metadata)
        return when (val writeResult = writer.write(record)) {
            is HealthWriteResult.Failed -> {
                val failed = ledgerStore.recordFailure(metadata.clientRecordId, writeResult.failure)
                Gate1SyncResult.WriteFailed(
                    clientRecordId = failed.clientRecordId,
                    clientRecordVersion = failed.clientRecordVersion,
                    ledgerRowsForClientRecordId = 1,
                    writeCountForClientRecordId = failed.attemptCount,
                    disposition = writeResult.failure.disposition,
                    phase = SyncPhase.EXTERNAL_WRITE,
                    code = writeResult.failure.code,
                )
            }
            is HealthWriteResult.Accepted -> finalizeAcceptedWrite(
                metadata.clientRecordId,
                metadata.clientRecordVersion,
                writing,
                writeResult.externalRecordId,
            )
        }
    }

    private suspend fun finalizeAcceptedWrite(
        clientRecordId: String,
        clientRecordVersion: Long,
        writing: SyncLedgerEntry,
        healthConnectRecordId: String?,
    ): Gate1SyncResult = try {
            ledgerStore.recordAccepted(clientRecordId, healthConnectRecordId).toResult()
        } catch (failure: Exception) {
            if (failure is CancellationException) throw failure
            val localFinalizationStatus = try {
                ledgerStore.recordAcceptanceUncertain(
                    clientRecordId,
                    healthConnectRecordId,
                )
                LocalFinalizationStatus.RECONCILIATION_PENDING
            } catch (recoveryFailure: Exception) {
                if (recoveryFailure is CancellationException) throw recoveryFailure
                LocalFinalizationStatus.FAILED
            }
            Gate1SyncResult.ExternalAccepted(
                clientRecordId = clientRecordId,
                clientRecordVersion = clientRecordVersion,
                ledgerRowsForClientRecordId = 1,
                writeCountForClientRecordId = writing.attemptCount,
                externalRecordId = healthConnectRecordId,
                phase = SyncPhase.LOCAL_FINALIZATION,
                code = "LOCAL_FINALIZATION_FAILED",
                localFinalizationStatus = localFinalizationStatus,
            )
        }

    private fun SyncLedgerEntry.toResult() = Gate1SyncResult.Completed(
        clientRecordId = clientRecordId,
        clientRecordVersion = clientRecordVersion,
        ledgerRowsForClientRecordId = 1,
        writeCountForClientRecordId = attemptCount,
    )
}
