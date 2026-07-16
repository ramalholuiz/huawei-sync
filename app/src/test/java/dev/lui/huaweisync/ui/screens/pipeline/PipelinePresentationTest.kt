package dev.lui.huaweisync.ui.screens.pipeline

import dev.lui.huaweisync.ui.components.HuaweiSyncMotionPolicy
import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductVerificationEvidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PipelinePresentationTest {
    @Test
    fun `accepted outcome checks only phases proven before readback`() {
        val model = PipelinePresentation.from(
            state = state(
                status = ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK,
                phase = ProductSyncPhase.ACCEPTANCE,
            ),
            coordinatorBusy = false,
        )

        assertEquals(PipelineStepState.COMPLETE, model.step(PipelineStep.PREFLIGHT).state)
        assertEquals(PipelineStepState.COMPLETE, model.step(PipelineStep.WRITE).state)
        assertEquals(PipelineStepState.COMPLETE, model.step(PipelineStep.ACCEPTANCE).state)
        assertEquals(PipelineStepState.PENDING, model.step(PipelineStep.VERIFICATION).state)
        assertEquals(PipelineStepState.PENDING, model.step(PipelineStep.RECONCILIATION).state)
        assertNull(model.numericProgress)
        assertFalse(model.motion.active)
    }

    @Test
    fun `busy idle state waits for coordinator evidence without optimistic progress`() {
        val model = PipelinePresentation.from(
            state = state(
                status = ProductHealthConnectStatus.READY_TO_SYNC,
                phase = ProductSyncPhase.IDLE,
            ),
            coordinatorBusy = true,
        )

        assertTrue(model.awaitingCoordinatorEvidence)
        assertTrue(model.steps.all { it.state == PipelineStepState.PENDING })
        assertNull(model.numericProgress)
        assertFalse(model.motion.active)
    }

    @Test
    fun `actual write phase activates write and completes only preflight`() {
        val model = PipelinePresentation.from(
            state = state(
                status = ProductHealthConnectStatus.WRITE_IN_PROGRESS,
                phase = ProductSyncPhase.WRITE,
            ),
            coordinatorBusy = true,
        )

        assertEquals(PipelineStepState.COMPLETE, model.step(PipelineStep.PREFLIGHT).state)
        assertEquals(PipelineStepState.ACTIVE, model.step(PipelineStep.WRITE).state)
        assertEquals(PipelineStepState.PENDING, model.step(PipelineStep.ACCEPTANCE).state)
        assertTrue(model.motion.active)
        assertEquals(
            PipelineMotionPresentation.ANIMATED,
            model.motion.forPolicy(HuaweiSyncMotionPolicy.Standard),
        )
        assertEquals(
            PipelineMotionPresentation.STATIC,
            model.motion.forPolicy(HuaweiSyncMotionPolicy.Reduced),
        )
    }

    @Test
    fun `confirmed readback checks verification without claiming GymRats delivery`() {
        val model = PipelinePresentation.from(
            state = state(
                status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                phase = ProductSyncPhase.VERIFICATION,
                confirmed = true,
            ),
            coordinatorBusy = false,
        )

        assertEquals(PipelineStepState.COMPLETE, model.step(PipelineStep.VERIFICATION).state)
        assertEquals("Confirmed in Health Connect", model.healthConnectLabel)
        assertEquals("Available for GymRats to import", model.destinationLabel)
        assertFalse(model.destinationLabel.contains("delivered", ignoreCase = true))
        assertFalse(model.destinationLabel.contains("synced", ignoreCase = true))
    }

    @Test
    fun `write failure marks only evidenced phase as needing attention`() {
        val model = PipelinePresentation.from(
            state = state(
                status = ProductHealthConnectStatus.FAILED,
                phase = ProductSyncPhase.WRITE,
                failure = "WRITE_REJECTED",
            ),
            coordinatorBusy = false,
        )

        assertEquals(PipelineStepState.COMPLETE, model.step(PipelineStep.PREFLIGHT).state)
        assertEquals(PipelineStepState.NEEDS_ATTENTION, model.step(PipelineStep.WRITE).state)
        assertEquals(PipelineStepState.PENDING, model.step(PipelineStep.ACCEPTANCE).state)
        assertEquals("WRITE_REJECTED", model.safeFailureSummary)
    }

    private fun PipelinePresentation.step(step: PipelineStep): PipelineStepPresentation =
        steps.single { it.step == step }

    private fun state(
        status: ProductHealthConnectStatus,
        phase: ProductSyncPhase,
        confirmed: Boolean = false,
        failure: String? = null,
    ) = ProductSyncState(
        healthConnectStatus = status,
        gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
        phase = phase,
        attemptCount = 0,
        ledgerWorkoutCount = 0,
        verification = ProductVerificationEvidence(
            diagnosticEvidence = null,
            realReadbackConfirmed = confirmed,
            healthConnectMatchCount = null,
            expectedVersionMatchCount = null,
            versionMatch = null,
        ),
        sanitizedFailureSummary = failure,
    )
}
