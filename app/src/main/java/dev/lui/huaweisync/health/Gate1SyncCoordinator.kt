package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.PreparedLedgerWorkout
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.data.SyncLedgerStore
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.domain.PreviousWorkoutMetadata
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy
import java.time.Clock

class Gate1SyncCoordinator(
    private val ledgerStore: SyncLedgerStore,
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

        ledgerStore.beginWrite(metadata.clientRecordId)
        val record = HealthWorkoutMapper.toExerciseSessionRecord(workout, metadata)
        val healthConnectRecordId = writer.write(record)
        return ledgerStore.recordAccepted(metadata.clientRecordId, healthConnectRecordId).toResult()
    }

    private fun SyncLedgerEntry.toResult() = Gate1SyncResult(
        clientRecordId = clientRecordId,
        clientRecordVersion = clientRecordVersion,
        ledgerRowsForClientRecordId = 1,
        writeCountForClientRecordId = attemptCount,
    )
}
