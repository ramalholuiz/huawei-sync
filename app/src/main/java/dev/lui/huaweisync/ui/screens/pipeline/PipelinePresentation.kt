package dev.lui.huaweisync.ui.screens.pipeline

import androidx.compose.runtime.Immutable
import dev.lui.huaweisync.ui.components.HuaweiSyncMotionPolicy
import dev.lui.huaweisync.ui.components.SyncPipelineRailModel
import dev.lui.huaweisync.ui.components.SyncRailFlow
import dev.lui.huaweisync.ui.components.SyncRailNode
import dev.lui.huaweisync.ui.components.SyncRailState
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState

enum class PipelineStep {
    PREFLIGHT,
    WRITE,
    ACCEPTANCE,
    VERIFICATION,
    RECONCILIATION,
}

enum class PipelineStepState {
    PENDING,
    ACTIVE,
    COMPLETE,
    NEEDS_ATTENTION,
}

enum class PipelineMotionPresentation {
    ANIMATED,
    STATIC,
}

@Immutable
data class PipelineMotion(
    val active: Boolean,
) {
    fun forPolicy(policy: HuaweiSyncMotionPolicy): PipelineMotionPresentation =
        if (active && !policy.reducedMotion) {
            PipelineMotionPresentation.ANIMATED
        } else {
            PipelineMotionPresentation.STATIC
        }
}

@Immutable
data class PipelineStepPresentation(
    val step: PipelineStep,
    val label: String,
    val detail: String,
    val state: PipelineStepState,
)

@Immutable
data class PipelinePresentation(
    val statusLabel: String,
    val healthConnectLabel: String,
    val destinationLabel: String,
    val ledgerWorkoutCount: Int,
    val writeAttemptCount: Int,
    val steps: List<PipelineStepPresentation>,
    val awaitingCoordinatorEvidence: Boolean,
    val safeFailureSummary: String?,
    val motion: PipelineMotion,
    val rail: SyncPipelineRailModel,
    /** Phase index (1-based) of the current coordinator phase, or null when idle/awaiting evidence. */
    val phaseIndex: Int?,
    /** Total distinct product phases represented on the rail. */
    val phaseTotal: Int,
    /** Deliberately absent: the coordinator exposes phases, not quantitative completion. */
    val numericProgress: Int? = null,
) {
    init {
        require(numericProgress == null) {
            "Quantitative progress requires a real item-total contract from the coordinator."
        }
    }

    companion object {
        fun from(state: ProductSyncState, coordinatorBusy: Boolean): PipelinePresentation {
            val currentStep = state.phase.toPipelineStep()
            val terminalFailure = state.healthConnectStatus.requiresAttention()
            val completedThrough = state.completedThrough()
            val awaitingEvidence = coordinatorBusy && currentStep == null
            val steps = PipelineStep.entries.map { step ->
                val stepState = when {
                    step.ordinal <= completedThrough -> PipelineStepState.COMPLETE
                    step == currentStep && terminalFailure -> PipelineStepState.NEEDS_ATTENTION
                    step == currentStep && coordinatorBusy -> PipelineStepState.ACTIVE
                    else -> PipelineStepState.PENDING
                }
                PipelineStepPresentation(
                    step = step,
                    label = step.label,
                    detail = step.detail,
                    state = stepState,
                )
            }
            val active = steps.any { it.state == PipelineStepState.ACTIVE }
            val rail = buildRailModel(
                state = state,
                coordinatorBusy = coordinatorBusy,
                awaitingEvidence = awaitingEvidence,
            )
            val phaseTotal = PipelineStep.entries.size
            val phaseIndex = when {
                awaitingEvidence -> null
                state.phase == ProductSyncPhase.IDLE -> null
                else -> currentStep?.ordinal?.plus(1)
            }

            return PipelinePresentation(
                statusLabel = when {
                    awaitingEvidence -> "Waiting for coordinator evidence"
                    active -> currentStep?.activeLabel ?: state.healthConnectStatus.label
                    else -> state.healthConnectStatus.label
                },
                healthConnectLabel = state.healthConnectStatus.label,
                destinationLabel = state.gymRatsStatus.label,
                ledgerWorkoutCount = state.ledgerWorkoutCount,
                writeAttemptCount = state.attemptCount,
                steps = steps,
                awaitingCoordinatorEvidence = awaitingEvidence,
                safeFailureSummary = state.sanitizedFailureSummary,
                motion = PipelineMotion(active),
                rail = rail,
                phaseIndex = phaseIndex,
                phaseTotal = phaseTotal,
            )
        }

        private fun buildRailModel(
            state: ProductSyncState,
            coordinatorBusy: Boolean,
            awaitingEvidence: Boolean,
        ): SyncPipelineRailModel {
            val hcState = state.healthConnectStatus
            val phase = state.phase
            val confirmed = hcState == ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT
            val attention = hcState.requiresAttention()

            val huaweiState = SyncRailState.PENDING
            val syncState = when {
                attention -> SyncRailState.ATTENTION
                phase == ProductSyncPhase.PREFLIGHT && coordinatorBusy -> SyncRailState.ACTIVE_WRITE
                phase != ProductSyncPhase.IDLE || coordinatorBusy -> SyncRailState.ACTIVE_WRITE
                else -> SyncRailState.WAITING
            }
            val hcNodeState = when {
                attention -> SyncRailState.ATTENTION
                confirmed -> SyncRailState.CONFIRMED
                phase == ProductSyncPhase.VERIFICATION ||
                    hcState == ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK -> SyncRailState.ACTIVE_VERIFY
                phase == ProductSyncPhase.WRITE ||
                    phase == ProductSyncPhase.ACCEPTANCE ||
                    hcState == ProductHealthConnectStatus.WRITE_IN_PROGRESS -> SyncRailState.ACTIVE_WRITE
                coordinatorBusy -> SyncRailState.ACTIVE_WRITE
                else -> SyncRailState.WAITING
            }
            val gymRatsState = when {
                attention -> SyncRailState.PENDING
                confirmed -> SyncRailState.PENDING
                else -> SyncRailState.PENDING
            }

            val flow = when {
                awaitingEvidence -> SyncRailFlow.NONE
                hcNodeState == SyncRailState.ACTIVE_WRITE -> SyncRailFlow.FORWARD
                hcNodeState == SyncRailState.ACTIVE_VERIFY -> SyncRailFlow.REVERSE
                else -> SyncRailFlow.NONE
            }
            val activeNodeIndex = when {
                awaitingEvidence -> null
                hcNodeState == SyncRailState.ACTIVE_WRITE ||
                    hcNodeState == SyncRailState.ACTIVE_VERIFY -> HC_NODE_INDEX
                syncState == SyncRailState.ACTIVE_WRITE -> SYNC_NODE_INDEX
                else -> null
            }

            val nodes = listOf(
                SyncRailNode(
                    id = "huawei",
                    monogram = "H",
                    label = "HUAWEI",
                    state = huaweiState,
                    supportingText = "SOURCE",
                ),
                SyncRailNode(
                    id = "sync",
                    monogram = "S",
                    label = "SYNC",
                    state = syncState,
                    supportingText = "COORDINATOR",
                ),
                SyncRailNode(
                    id = "hc",
                    monogram = "HC",
                    label = "HEALTH",
                    state = hcNodeState,
                    supportingText = hcNodeState.railSupport(),
                    confirmedAcknowledgement = confirmed,
                ),
                SyncRailNode(
                    id = "gymrats",
                    monogram = "G",
                    label = "GYMRATS",
                    state = gymRatsState,
                    supportingText = "AWAITING G2",
                ),
            )

            return SyncPipelineRailModel(
                nodes = nodes,
                activeNodeIndex = activeNodeIndex,
                activeFlow = flow,
            )
        }
    }
}

private const val SYNC_NODE_INDEX = 1
private const val HC_NODE_INDEX = 2

private fun SyncRailState.railSupport(): String = when (this) {
    SyncRailState.WAITING -> "WAITING"
    SyncRailState.PENDING -> "PENDING"
    SyncRailState.ACTIVE_WRITE -> "WRITING"
    SyncRailState.ACTIVE_VERIFY -> "VERIFYING"
    SyncRailState.CONFIRMED -> "CONFIRMED"
    SyncRailState.ATTENTION -> "ATTENTION"
}

private val PipelineStep.label: String
    get() = when (this) {
        PipelineStep.PREFLIGHT -> "Preflight"
        PipelineStep.WRITE -> "Write"
        PipelineStep.ACCEPTANCE -> "Acceptance"
        PipelineStep.VERIFICATION -> "Readback verification"
        PipelineStep.RECONCILIATION -> "Reconciliation"
    }

private val PipelineStep.detail: String
    get() = when (this) {
        PipelineStep.PREFLIGHT -> "Check Health Connect availability and permission"
        PipelineStep.WRITE -> "Write one deterministic ExerciseSessionRecord"
        PipelineStep.ACCEPTANCE -> "Persist Health Connect acceptance in the ledger"
        PipelineStep.VERIFICATION -> "Read back the deterministic record and version"
        PipelineStep.RECONCILIATION -> "Resolve an uncertain prior write before retrying"
    }

private val PipelineStep.activeLabel: String
    get() = when (this) {
        PipelineStep.PREFLIGHT -> "Checking preflight evidence"
        PipelineStep.WRITE -> "Writing to Health Connect"
        PipelineStep.ACCEPTANCE -> "Recording Health Connect acceptance"
        PipelineStep.VERIFICATION -> "Checking Health Connect readback"
        PipelineStep.RECONCILIATION -> "Reconciling prior write evidence"
    }

private fun ProductSyncPhase.toPipelineStep(): PipelineStep? = when (this) {
    ProductSyncPhase.IDLE -> null
    ProductSyncPhase.PREFLIGHT -> PipelineStep.PREFLIGHT
    ProductSyncPhase.WRITE -> PipelineStep.WRITE
    ProductSyncPhase.ACCEPTANCE -> PipelineStep.ACCEPTANCE
    ProductSyncPhase.VERIFICATION -> PipelineStep.VERIFICATION
    ProductSyncPhase.RECONCILIATION -> PipelineStep.RECONCILIATION
}

private fun ProductSyncState.completedThrough(): Int {
    if (healthConnectStatus == ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT) {
        return if (phase == ProductSyncPhase.RECONCILIATION) {
            PipelineStep.RECONCILIATION.ordinal
        } else {
            PipelineStep.VERIFICATION.ordinal
        }
    }
    if (healthConnectStatus == ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK) {
        return PipelineStep.ACCEPTANCE.ordinal
    }

    val current = phase.toPipelineStep() ?: return -1
    return current.ordinal - 1
}

private fun ProductHealthConnectStatus.requiresAttention(): Boolean = when (this) {
    ProductHealthConnectStatus.UNAVAILABLE,
    ProductHealthConnectStatus.UPDATE_REQUIRED,
    ProductHealthConnectStatus.PERMISSION_REQUIRED,
    ProductHealthConnectStatus.RECONCILIATION_REQUIRED,
    ProductHealthConnectStatus.RETRY_REQUIRED,
    ProductHealthConnectStatus.ACTION_REQUIRED,
    ProductHealthConnectStatus.FAILED,
    -> true
    ProductHealthConnectStatus.READY_TO_SYNC,
    ProductHealthConnectStatus.WRITE_IN_PROGRESS,
    ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK,
    ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
    -> false
}
