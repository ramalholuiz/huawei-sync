package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.SyncLedgerDao
import dev.lui.huaweisync.data.SyncLedgerEntity
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
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

        ledgerDao.upsert(
            SyncLedgerEntity(
                clientRecordId = clientRecordId,
                source = workout.source.name,
                sourceWorkoutId = workout.sourceWorkoutId,
                dedupeKey = workout.dedupeKey,
                clientRecordVersion = workout.version,
                healthConnectRecordId = healthConnectRecordId ?: previous?.healthConnectRecordId,
                lastSyncedAtEpochMillis = clock.millis(),
                successfulWriteCount = (previous?.successfulWriteCount ?: 0) + 1,
            ),
        )

        val ledgerRows = ledgerDao.countByClientRecordId(clientRecordId)
        val updated = ledgerDao.findByClientRecordId(clientRecordId)
        return Gate1SyncResult(
            clientRecordId = clientRecordId,
            clientRecordVersion = workout.version,
            ledgerRowsForClientRecordId = ledgerRows,
            writeCountForClientRecordId = updated?.successfulWriteCount ?: 0,
        )
    }
}
