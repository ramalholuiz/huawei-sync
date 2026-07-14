package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.SyncLedgerDao
import dev.lui.huaweisync.data.SyncLedgerEntity
import dev.lui.huaweisync.data.SyncStatus
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.domain.WorkoutMetadataPolicy
import java.time.Clock

class Gate1SyncCoordinator(
    private val ledgerDao: SyncLedgerDao,
    private val writer: HealthWorkoutWriter,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend fun runSyntheticStrengthSync(): Gate1SyncResult {
        val workout = SyntheticWorkoutFactory.create(clock)
        val clientRecordId = SyntheticWorkoutFactory.clientRecordIdFor(workout)
        val record = HealthWorkoutMapper.toExerciseSessionRecord(workout)
        val previous = ledgerDao.findByClientRecordId(clientRecordId)
        val healthConnectRecordId = writer.write(record)

        val acceptedAt = clock.millis()
        ledgerDao.upsert(
            SyncLedgerEntity(
                clientRecordId = clientRecordId,
                sourceProvider = workout.source.stableName,
                sourceRecordId = workout.sourceWorkoutId,
                sourceVersion = null,
                contentHash = WorkoutMetadataPolicy.contentHashFor(workout),
                clientRecordVersion = workout.version,
                healthConnectRecordId = healthConnectRecordId ?: previous?.healthConnectRecordId,
                status = SyncStatus.SYNCED,
                attemptCount = (previous?.attemptCount ?: 0) + 1,
                acceptedAtEpochMillis = acceptedAt,
                confirmedAtEpochMillis = previous?.confirmedAtEpochMillis,
                createdAtEpochMillis = previous?.createdAtEpochMillis ?: acceptedAt,
                updatedAtEpochMillis = acceptedAt,
                lastErrorCode = null,
                lastErrorPhase = null,
                lastErrorAtEpochMillis = null,
                lastErrorMessage = null,
            ),
        )

        val ledgerRows = ledgerDao.countByClientRecordId(clientRecordId)
        val updated = ledgerDao.findByClientRecordId(clientRecordId)
        return Gate1SyncResult(
            clientRecordId = clientRecordId,
            clientRecordVersion = workout.version,
            ledgerRowsForClientRecordId = ledgerRows,
            writeCountForClientRecordId = updated?.attemptCount ?: 0,
        )
    }
}
