package dev.lui.huaweisync.health

import dev.lui.huaweisync.data.PreparedLedgerWorkout
import dev.lui.huaweisync.data.SyncDiagnosticMessage
import dev.lui.huaweisync.data.SyncErrorPhase
import dev.lui.huaweisync.data.SyncFailure
import dev.lui.huaweisync.data.SyncFailureDisposition
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
    private val confirmer: HealthWorkoutConfirmer = HealthWorkoutConfirmer {
        HealthConfirmationResult.Inconclusive("CONFIRMER_NOT_CONFIGURED")
    },
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

    suspend fun confirmSyntheticStrengthSync(): Gate1SyncResult {
        val workout = SyntheticWorkoutFactory.create(clock)
        val accepted = checkNotNull(
            ledgerStore.findBySource(workout.source.stableName, workout.sourceWorkoutId),
        ) { "Confirmation requires a prepared ledger row." }
        check(accepted.acceptedAtEpochMillis != null) {
            "Confirmation requires a recorded external acceptance."
        }

        val verifying = ledgerStore.beginVerification(accepted.clientRecordId)
        val request = HealthConfirmationRequest(
            clientRecordId = verifying.clientRecordId,
            clientRecordVersion = verifying.clientRecordVersion,
            externalRecordId = verifying.healthConnectRecordId,
        )
        return when (val result = confirmer.confirm(request)) {
            HealthConfirmationResult.Confirmed -> finalizeConfirmation(verifying)
            is HealthConfirmationResult.Absent -> recordConfirmationPending(
                verifying,
                ConfirmationPendingReason.ABSENT,
                result.code,
            )
            is HealthConfirmationResult.Inconclusive -> recordConfirmationPending(
                verifying,
                ConfirmationPendingReason.INCONCLUSIVE,
                result.code,
            )
            is HealthConfirmationResult.Failed -> recordConfirmationPending(
                verifying,
                ConfirmationPendingReason.FAILURE,
                result.failure,
            )
        }
    }

    private suspend fun finalizeConfirmation(verifying: SyncLedgerEntry): Gate1SyncResult = try {
        val confirmed = ledgerStore.confirm(verifying.clientRecordId)
        Gate1SyncResult.Confirmed(
            clientRecordId = confirmed.clientRecordId,
            clientRecordVersion = confirmed.clientRecordVersion,
            ledgerRowsForClientRecordId = 1,
            writeCountForClientRecordId = confirmed.attemptCount,
            externalRecordId = confirmed.healthConnectRecordId,
            phase = SyncPhase.CONFIRMATION,
            code = "CONFIRMED",
            localFinalizationStatus = LocalFinalizationStatus.FINALIZED,
        )
    } catch (failure: Exception) {
        if (failure is CancellationException) throw failure
        Gate1SyncResult.ConfirmationPending(
            clientRecordId = verifying.clientRecordId,
            clientRecordVersion = verifying.clientRecordVersion,
            ledgerRowsForClientRecordId = 1,
            writeCountForClientRecordId = verifying.attemptCount,
            externalRecordId = verifying.healthConnectRecordId,
            reason = ConfirmationPendingReason.FAILURE,
            phase = SyncPhase.CONFIRMATION,
            code = "LOCAL_CONFIRMATION_FAILED",
            localFinalizationStatus = LocalFinalizationStatus.FAILED,
        )
    }

    private suspend fun recordConfirmationPending(
        verifying: SyncLedgerEntry,
        reason: ConfirmationPendingReason,
        code: String,
    ): Gate1SyncResult = recordConfirmationPending(
        verifying = verifying,
        reason = reason,
        failure = SyncFailure(
            disposition = SyncFailureDisposition.RETRYABLE,
            code = code,
            phase = SyncErrorPhase.VERIFICATION,
            safeMessage = SyncDiagnosticMessage.VERIFICATION_FAILED,
        ),
    )

    private suspend fun recordConfirmationPending(
        verifying: SyncLedgerEntry,
        reason: ConfirmationPendingReason,
        failure: SyncFailure,
    ): Gate1SyncResult {
        val persisted = ledgerStore.recordFailure(verifying.clientRecordId, failure)
        return Gate1SyncResult.ConfirmationPending(
            clientRecordId = persisted.clientRecordId,
            clientRecordVersion = persisted.clientRecordVersion,
            ledgerRowsForClientRecordId = 1,
            writeCountForClientRecordId = persisted.attemptCount,
            externalRecordId = persisted.healthConnectRecordId,
            reason = reason,
            phase = SyncPhase.CONFIRMATION,
            code = failure.code,
            localFinalizationStatus = LocalFinalizationStatus.FINALIZED,
        )
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
