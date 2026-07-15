package dev.lui.huaweisync.diagnostics

import dev.lui.huaweisync.data.DiagnosticEvidence
import dev.lui.huaweisync.data.DiagnosticNextAction
import dev.lui.huaweisync.data.SyncLedgerEntry
import dev.lui.huaweisync.domain.SyntheticWorkoutFactory
import dev.lui.huaweisync.health.Gate1SyncCoordinator
import dev.lui.huaweisync.health.Gate1SyncLedger
import dev.lui.huaweisync.health.Gate1SyncResult
import dev.lui.huaweisync.health.HealthWorkoutInspectionRequest
import dev.lui.huaweisync.health.HealthWorkoutInspectionResult
import dev.lui.huaweisync.health.HealthWorkoutInspector
import kotlinx.coroutines.CancellationException

/**
 * Runtime boundary for Gate 1 UI actions.
 *
 * Every successful action is followed by the same exact, bounded Health Connect inspection used
 * by refresh. Provider, Room, and unexpected coordinator details are collapsed to stable codes;
 * cancellation remains structured concurrency and is always propagated.
 */
class Gate1RuntimeDiagnostics internal constructor(
    private val runSync: suspend () -> Gate1SyncResult,
    private val confirmSync: suspend () -> Gate1SyncResult,
    private val reconcileSync: suspend () -> Gate1SyncResult,
    private val findLedger: suspend () -> SyncLedgerEntry?,
    private val inspect: suspend (HealthWorkoutInspectionRequest) -> HealthWorkoutInspectionResult,
) {
    constructor(
        coordinator: Gate1SyncCoordinator,
        ledger: Gate1SyncLedger,
        inspector: HealthWorkoutInspector,
    ) : this(
        runSync = coordinator::runSyntheticStrengthSync,
        confirmSync = coordinator::confirmSyntheticStrengthSync,
        reconcileSync = coordinator::reconcileSyntheticStrengthSync,
        findLedger = {
            val workout = SyntheticWorkoutFactory.create()
            ledger.findBySource(workout.source.stableName, workout.sourceWorkoutId)
        },
        inspect = inspector::inspect,
    )

    suspend fun run(): Gate1Diagnostic = execute(runSync)

    suspend fun confirm(): Gate1Diagnostic = execute(confirmSync)

    suspend fun reconcile(): Gate1Diagnostic = execute(reconcileSync)

    suspend fun refresh(): Gate1Diagnostic = refreshWithCode(null)

    private suspend fun execute(action: suspend () -> Gate1SyncResult): Gate1Diagnostic = try {
        val result = action()
        val inspection = inspectSafely(result.clientRecordVersion)
        Gate1Diagnostics.fromResult(result, inspection.facts).withCodeIfAbsent(inspection.code)
    } catch (failure: CancellationException) {
        throw failure
    } catch (_: Exception) {
        refreshWithCode(ACTION_FAILED)
    }

    private suspend fun refreshWithCode(forcedCode: String?): Gate1Diagnostic {
        val entry = try {
            findLedger()
        } catch (failure: CancellationException) {
            throw failure
        } catch (_: Exception) {
            return readyDiagnostic().copy(code = ROOM_READ_FAILED)
        }
        val version = entry?.clientRecordVersion ?: SyntheticWorkoutFactory.CLIENT_RECORD_VERSION
        val inspection = inspectSafely(version)
        val diagnostic = if (entry == null) {
            readyDiagnostic(inspection.facts)
        } else {
            Gate1Diagnostics.fromLedger(entry, inspection.facts)
        }
        return diagnostic.copy(code = forcedCode ?: diagnostic.code ?: inspection.code)
    }

    private suspend fun inspectSafely(clientRecordVersion: Long): InspectionOutcome {
        val workout = SyntheticWorkoutFactory.create()
        val request = HealthWorkoutInspectionRequest(
            clientRecordId = SyntheticWorkoutFactory.CLIENT_RECORD_ID,
            clientRecordVersion = clientRecordVersion,
            startTime = workout.startTime,
            endTime = workout.endTime,
        )
        return try {
            when (val result = inspect(request)) {
                is HealthWorkoutInspectionResult.Complete -> InspectionOutcome(
                    facts = Gate1InspectionFacts(
                        matchingRecordCount = result.inspection.matchingRecordCount,
                        expectedVersionMatchCount = result.inspection.expectedVersionMatchCount,
                    ),
                    code = null,
                )
                is HealthWorkoutInspectionResult.Inconclusive -> InspectionOutcome(
                    facts = null,
                    code = result.code,
                )
            }
        } catch (failure: CancellationException) {
            throw failure
        } catch (_: Exception) {
            InspectionOutcome(facts = null, code = INSPECTION_FAILED)
        }
    }

    private fun readyDiagnostic(inspection: Gate1InspectionFacts? = null) = Gate1Diagnostic(
        statusCode = "READY",
        safeMessage = "Ready to run the synthetic sync.",
        nextAction = DiagnosticNextAction.RUN_SYNC,
        evidence = DiagnosticEvidence.READY,
        durableStatus = null,
        phase = null,
        code = null,
        roomRowCount = 0,
        writeAttemptCount = 0,
        clientRecordVersion = SyntheticWorkoutFactory.CLIENT_RECORD_VERSION,
        healthConnectMatchCount = inspection?.matchingRecordCount,
        healthConnectExpectedVersionMatchCount = inspection?.expectedVersionMatchCount,
        versionMatch = inspection?.versionMatch,
        localFinalization = null,
    )

    private fun Gate1Diagnostic.withCodeIfAbsent(code: String?) =
        if (this.code == null && code != null) copy(code = code) else this

    private data class InspectionOutcome(
        val facts: Gate1InspectionFacts?,
        val code: String?,
    )

    private companion object {
        const val ACTION_FAILED = "GATE1_RUNTIME_ACTION_FAILED"
        const val ROOM_READ_FAILED = "ROOM_LEDGER_READ_FAILED"
        const val INSPECTION_FAILED = "HEALTH_CONNECT_INSPECTION_FAILED"
    }
}
