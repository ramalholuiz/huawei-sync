package dev.lui.huaweisync.ui.prototypes

import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductVerificationEvidence

/**
 * The seven canonical states Cycle 3 concepts must render. Each fixture is a real
 * `ProductSyncState`, so no concept can invent a value the production coordinator
 * could not produce.
 */
enum class PrototypeState(val label: String) {
    EMPTY("Empty"),
    WAITING("Waiting"),
    SYNCING("Syncing"),
    SUCCESS_CONFIRMED("Success"),
    ERROR("Error"),
    GYMRATS_AVAILABLE("GymRats"),
    COMPOSED_REPRESENTATIVE("Composed"),
}

object PrototypeGalleryFixtures {
    fun stateFor(fixture: PrototypeState): ProductSyncState = when (fixture) {
        PrototypeState.EMPTY -> baseState(
            status = ProductHealthConnectStatus.READY_TO_SYNC,
        )

        PrototypeState.WAITING -> baseState(
            status = ProductHealthConnectStatus.READY_TO_SYNC,
            phase = ProductSyncPhase.PREFLIGHT,
        )

        PrototypeState.SYNCING -> baseState(
            status = ProductHealthConnectStatus.WRITE_IN_PROGRESS,
            phase = ProductSyncPhase.WRITE,
            attemptCount = 1,
        )

        PrototypeState.SUCCESS_CONFIRMED -> baseState(
            status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
            phase = ProductSyncPhase.IDLE,
            ledgerWorkoutCount = 1,
            attemptCount = 1,
            confirmed = true,
        )

        PrototypeState.ERROR -> baseState(
            status = ProductHealthConnectStatus.FAILED,
            phase = ProductSyncPhase.IDLE,
            attemptCount = 1,
            failure = "Health Connect write failed. Retry after reviewing availability and permission.",
        )

        PrototypeState.GYMRATS_AVAILABLE -> baseState(
            status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
            phase = ProductSyncPhase.IDLE,
            ledgerWorkoutCount = 1,
            attemptCount = 1,
            confirmed = true,
        )

        PrototypeState.COMPOSED_REPRESENTATIVE -> baseState(
            status = ProductHealthConnectStatus.WRITE_IN_PROGRESS,
            phase = ProductSyncPhase.ACCEPTANCE,
            ledgerWorkoutCount = 3,
            attemptCount = 2,
            confirmed = false,
        )
    }

    private fun baseState(
        status: ProductHealthConnectStatus,
        phase: ProductSyncPhase = ProductSyncPhase.IDLE,
        ledgerWorkoutCount: Int = 0,
        attemptCount: Int = 0,
        confirmed: Boolean = false,
        failure: String? = null,
    ): ProductSyncState = ProductSyncState(
        healthConnectStatus = status,
        gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
        phase = phase,
        attemptCount = attemptCount,
        ledgerWorkoutCount = ledgerWorkoutCount,
        verification = ProductVerificationEvidence(
            diagnosticEvidence = null,
            realReadbackConfirmed = confirmed,
            healthConnectMatchCount = if (confirmed) 1 else null,
            expectedVersionMatchCount = if (confirmed) 1 else null,
            versionMatch = if (confirmed) true else null,
        ),
        sanitizedFailureSummary = failure,
    )
}
