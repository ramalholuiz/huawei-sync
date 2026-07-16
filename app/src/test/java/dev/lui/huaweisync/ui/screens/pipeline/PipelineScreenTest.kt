package dev.lui.huaweisync.ui.screens.pipeline

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import dev.lui.huaweisync.ui.components.HuaweiSyncMotionProvider
import dev.lui.huaweisync.ui.screens.sync.SyncNowModal
import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductVerificationEvidence
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class PipelineScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `pipeline renders actual evidence and no prototype progress claims`() {
        compose.setContent {
            HuaweiSyncTheme {
                HuaweiSyncMotionProvider(reducedMotion = true) {
                    PipelineScreen(
                        state = state(
                            status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                            phase = ProductSyncPhase.VERIFICATION,
                            confirmed = true,
                            attempts = 2,
                            workouts = 1,
                        ),
                        coordinatorBusy = false,
                    )
                }
            }
        }

        compose.onNodeWithTag("pipeline-screen").assertIsDisplayed()
        compose.onAllNodesWithText("Confirmed in Health Connect", substring = true).assertCountEquals(2)
        compose.onNodeWithTag("pipeline-screen").performScrollToIndex(4)
        compose.onNodeWithText("Available for GymRats to import").assertIsDisplayed()
        compose.onAllNodesWithText("68%", substring = true).assertCountEquals(0)
        compose.onAllNodesWithText("remaining", substring = true, ignoreCase = true).assertCountEquals(0)
    }

    @Test
    fun `reduced motion modal stays static while awaiting real phase evidence`() {
        compose.setContent {
            HuaweiSyncTheme {
                HuaweiSyncMotionProvider(reducedMotion = true) {
                    SyncNowModal(
                        state = state(
                            status = ProductHealthConnectStatus.READY_TO_SYNC,
                            phase = ProductSyncPhase.IDLE,
                        ),
                        coordinatorBusy = true,
                        onDismiss = {},
                    )
                }
            }
        }

        compose.onNodeWithTag("sync-now-overlay").assertIsDisplayed()
        compose.onNodeWithText("Waiting for coordinator evidence").assertIsDisplayed()
        compose.onNodeWithContentDescription("No active coordinator phase observed").assertIsDisplayed()
        compose.onNodeWithText(
            "The request is running, but the coordinator has not exposed a newer phase. No progress has been inferred from elapsed time.",
        ).assertIsDisplayed()
    }

    private fun state(
        status: ProductHealthConnectStatus,
        phase: ProductSyncPhase,
        confirmed: Boolean = false,
        attempts: Int = 0,
        workouts: Int = 0,
    ) = ProductSyncState(
        healthConnectStatus = status,
        gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
        phase = phase,
        attemptCount = attempts,
        ledgerWorkoutCount = workouts,
        verification = ProductVerificationEvidence(
            diagnosticEvidence = null,
            realReadbackConfirmed = confirmed,
            healthConnectMatchCount = null,
            expectedVersionMatchCount = null,
            versionMatch = null,
        ),
        sanitizedFailureSummary = null,
    )
}
