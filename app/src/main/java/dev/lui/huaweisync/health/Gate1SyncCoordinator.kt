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

        val writing = ledgerStore.beginWrite(metadata.clientRecordId)
        val record = HealthWorkoutMapper.toExerciseSessionRecord(workout, metadata)
        val healthConnectRecordId = writer.write(record)
        return try {
            ledgerStore.recordAccepted(metadata.clientRecordId, healthConnectRecordId).toResult()
        } catch (failure: Exception) {
            if (failure is CancellationException) throw failure
            val localFinalizationStatus = try {
                ledgerStore.recordAcceptanceUncertain(
                    metadata.clientRecordId,
                    healthConnectRecordId,
                )
                LocalFinalizationStatus.RECONCILIATION_PENDING
            } catch (recoveryFailure: Exception) {
                if (recoveryFailure is CancellationException) throw recoveryFailure
                LocalFinalizationStatus.FAILED
            }
            Gate1SyncResult.ExternalAccepted(
                clientRecordId = metadata.clientRecordId,
                clientRecordVersion = metadata.clientRecordVersion,
                ledgerRowsForClientRecordId = 1,
                writeCountForClientRecordId = writing.attemptCount,
                externalRecordId = healthConnectRecordId,
                phase = SyncPhase.LOCAL_FINALIZATION,
                code = "LOCAL_FINALIZATION_FAILED",
                localFinalizationStatus = localFinalizationStatus,
            )
        }
    }

    private fun SyncLedgerEntry.toResult() = Gate1SyncResult.Completed(
        clientRecordId = clientRecordId,
        clientRecordVersion = clientRecordVersion,
        ledgerRowsForClientRecordId = 1,
        writeCountForClientRecordId = attemptCount,
    )
}
