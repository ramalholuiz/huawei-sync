package dev.lui.huaweisync.ui.screens.dashboard

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductVerificationEvidence
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class DashboardScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `loading presents fact sources and disables sync`() {
        compose.setContent {
            HuaweiSyncTheme(darkTheme = true) {
                DashboardScreen(DashboardScreenState.Loading, true, {}, {}, {})
            }
        }

        compose.onNodeWithText("Loading sync status").assertExists()
        compose.onNodeWithText("Reading Health Connect availability, permissions, and the durable ledger.").assertExists()
        compose.onNodeWithContentDescription("Sync now").assertIsNotEnabled()
    }

    @Test
    fun `empty ledger does not invent activity totals`() {
        show(content(ProductHealthConnectStatus.READY_TO_SYNC))

        compose.onNodeWithText("No workouts recorded yet").assertExists()
        compose.onNodeWithText("143", substring = true).assertDoesNotExist()
        compose.onNodeWithText("6 apps", substring = true, ignoreCase = true).assertDoesNotExist()
        compose.onNodeWithText("up to date", substring = true, ignoreCase = true).assertDoesNotExist()
    }

    @Test
    fun `blocked state exposes permission action and callback`() {
        var resolveClicks = 0
        show(
            state = content(ProductHealthConnectStatus.PERMISSION_REQUIRED),
            onResolve = { resolveClicks++ },
        )

        compose.onNodeWithText("Health Connect permission required").assertExists()
        compose.onNodeWithText("Review permission", ignoreCase = true).performClick()

        assertEquals(1, resolveClicks)
    }

    @Test
    fun `error shows only sanitized failure supplied by product state`() {
        show(
            content(
                status = ProductHealthConnectStatus.FAILED,
                failure = "Health Connect write failed.",
            ),
        )

        compose.onNodeWithText("Health Connect write failed.").assertExists()
        compose.onNodeWithText("Sync failed").assertExists()
    }

    @Test
    fun `verified state shows exact ledger and readback facts without GymRats import claim`() {
        show(
            content(
                status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                ledgerCount = 7,
                attempts = 3,
                confirmed = true,
                healthConnectMatches = 1,
                versionMatches = 1,
            ),
        )

        compose.onNodeWithText("Confirmed in Health Connect").assertExists()
        compose.onNode(hasScrollAction()).performScrollToIndex(1)
        compose.onNodeWithText("Workouts tracked").assertExists()
        compose.onNodeWithText("7").assertExists()
        compose.onNodeWithText("Current record attempts").assertExists()
        compose.onNodeWithText("3").assertExists()
        compose.onNode(hasScrollAction()).performScrollToIndex(2)
        compose.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        compose.onNodeWithText("Available for GymRats to import", ignoreCase = true).assertExists()
        compose.onNode(hasScrollAction()).performScrollToIndex(3)
        compose.onNodeWithText("Readback confirmed").assertExists()
        compose.onNodeWithText("Imported by GymRats", substring = true, ignoreCase = true).assertDoesNotExist()
    }

    @Test
    fun `sanitized failure summary renders above the hero when a failure is set`() {
        show(
            content(
                status = ProductHealthConnectStatus.FAILED,
                failure = "Health Connect write failed. Retry after reviewing availability and permission.",
            ),
        )

        val failureTop = compose.onNodeWithTag("dashboard-failure-summary")
            .fetchSemanticsNode().boundsInRoot.top
        val heroTop = compose.onNodeWithTag("dashboard-sync-hero")
            .fetchSemanticsNode().boundsInRoot.top
        assert(failureTop < heroTop) {
            "failure summary top=$failureTop must be above hero top=$heroTop"
        }
    }

    @Test
    fun `failure summary stays hidden and hero renders when failure is null`() {
        show(content(status = ProductHealthConnectStatus.READY_TO_SYNC))

        compose.onNodeWithTag("dashboard-failure-summary").assertDoesNotExist()
        compose.onNodeWithTag("dashboard-sync-hero").assertExists()
    }

    @Test
    fun `confirmed hero body cites health connect and the gymrats next-open expectation`() {
        show(
            content(
                status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                ledgerCount = 1,
                attempts = 1,
                confirmed = true,
                healthConnectMatches = 1,
                versionMatches = 1,
            ),
        )

        compose.onNodeWithText(
            "Confirmed in Health Connect. GymRats can import it on next open.",
        ).assertExists()
    }

    @Test
    fun `list bottom padding keeps the last card visible above the sync fab`() {
        show(
            content(
                status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                ledgerCount = 1,
                attempts = 1,
                confirmed = true,
                healthConnectMatches = 1,
                versionMatches = 1,
            ),
        )

        compose.onNode(hasScrollAction()).performScrollToIndex(3)

        val fabTop = compose.onNodeWithTag("dashboard-sync-fab")
            .fetchSemanticsNode().boundsInRoot.top
        val verificationBottom = compose.onNodeWithText("Readback confirmed")
            .fetchSemanticsNode().boundsInRoot.bottom

        assert(verificationBottom < fabTop) {
            "Verification card ($verificationBottom) overlaps the FAB (top $fabTop)."
        }
    }

    @Test
    fun `sync and theme callbacks are passed through without local navigation`() {
        var syncClicks = 0
        var themeClicks = 0
        show(
            state = content(ProductHealthConnectStatus.READY_TO_SYNC),
            onSync = { syncClicks++ },
            onTheme = { themeClicks++ },
        )

        compose.onNodeWithContentDescription("Sync now").assertIsEnabled().performClick()
        compose.onNodeWithContentDescription("Switch theme").performClick()
        compose.onNodeWithContentDescription("History").assertDoesNotExist()
        compose.onNodeWithContentDescription("Settings").assertDoesNotExist()

        assertEquals(1, syncClicks)
        assertEquals(1, themeClicks)
    }

    private fun show(
        state: DashboardScreenState,
        onSync: () -> Unit = {},
        onResolve: () -> Unit = {},
        onTheme: () -> Unit = {},
    ) {
        compose.setContent {
            HuaweiSyncTheme(darkTheme = true) {
                DashboardScreen(
                    state = state,
                    darkTheme = true,
                    onToggleTheme = onTheme,
                    onSync = onSync,
                    onResolveHealthConnect = onResolve,
                )
            }
        }
    }

    private fun content(
        status: ProductHealthConnectStatus,
        ledgerCount: Int = 0,
        attempts: Int = 0,
        confirmed: Boolean = false,
        healthConnectMatches: Int? = null,
        versionMatches: Int? = null,
        failure: String? = null,
    ): DashboardScreenState = DashboardScreenState.Content(
        ProductSyncState(
            healthConnectStatus = status,
            gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
            phase = ProductSyncPhase.IDLE,
            attemptCount = attempts,
            ledgerWorkoutCount = ledgerCount,
            verification = ProductVerificationEvidence(
                diagnosticEvidence = null,
                realReadbackConfirmed = confirmed,
                healthConnectMatchCount = healthConnectMatches,
                expectedVersionMatchCount = versionMatches,
                versionMatch = if (confirmed) true else null,
            ),
            sanitizedFailureSummary = failure,
        ),
    )
}
