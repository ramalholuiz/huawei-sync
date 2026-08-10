package dev.lui.huaweisync.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.HuaweiSyncRoot
import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductVerificationEvidence
import dev.lui.huaweisync.ui.usesWidePrimaryNavigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HuaweiSyncNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `active inventory has unique typed sync destinations`() {
        assertEquals(6, HuaweiSyncDestination.all.size)
        assertEquals(6, HuaweiSyncDestination.all.map { it.route }.toSet().size)
        assertEquals(6, HuaweiSyncDestination.all.map { it.label }.toSet().size)
        assertSame(SyncNow, HuaweiSyncDestination.all.single { it.route == "sync-now" })
    }

    @Test
    fun `one state owner separates screen navigation from sync overlay`() {
        val state = HuaweiSyncNavigationState(Dashboard)

        state.navigateTo(Diagnostics)
        assertSame(Diagnostics, state.currentDestination)
        assertNull(state.overlayDestination)

        state.showSyncOverlay()
        assertSame(Diagnostics, state.currentDestination)
        assertSame(SyncNow, state.overlayDestination)

        state.dismissSyncOverlay()
        assertNull(state.overlayDestination)
        assertFalse(state.compactMenuVisible)
    }

    @Test
    fun `compact primary navigation reaches destinations and routes sync through runtime callback`() {
        var syncRequests = 0
        composeRule.setContent {
            HuaweiSyncRoot(
                onSync = { syncRequests += 1 },
                gate1Entry = { Text("Gate 1 diagnostics content") },
            )
        }

        composeRule.onNodeWithContentDescription("Diagnostics").performClick()
        composeRule.onNodeWithText("Gate 1 diagnostics content").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithTag("compact-more-menu").assertIsDisplayed()
        composeRule.onNodeWithTag("nav-onboarding").performClick()
        composeRule.onNodeWithText("Your workouts, available through Health Connect.").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Dashboard").performClick()
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithTag("nav-sync-now").performClick()
        composeRule.runOnIdle { assertEquals(1, syncRequests) }
        composeRule.onNodeWithTag("sync-now-overlay").assertIsDisplayed()
        composeRule.onNodeWithText("No product or coordinator phase evidence is available yet.")
            .assertIsDisplayed()
    }

    @Test
    fun `dashboard fab tap opens sync overlay and triggers runtime sync exactly once`() {
        var syncRequests = 0
        composeRule.setContent {
            HuaweiSyncRoot(
                productSyncState = readyProductSyncState(),
                onSync = { syncRequests += 1 },
                gate1Entry = { Text("Gate 1 diagnostics content") },
            )
        }

        composeRule.onNodeWithTag("dashboard-sync-fab").performClick()
        composeRule.onNodeWithTag("sync-now-overlay").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(1, syncRequests) }
    }

    private fun readyProductSyncState(): ProductSyncState = ProductSyncState(
        healthConnectStatus = ProductHealthConnectStatus.READY_TO_SYNC,
        gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
        phase = ProductSyncPhase.IDLE,
        attemptCount = 0,
        ledgerWorkoutCount = 0,
        verification = ProductVerificationEvidence(
            diagnosticEvidence = null,
            realReadbackConfirmed = false,
            healthConnectMatchCount = null,
            expectedVersionMatchCount = null,
            versionMatch = null,
        ),
        sanitizedFailureSummary = null,
    )

    @Test
    fun `responsive policy selects wide primary navigation at the audited breakpoint`() {
        assertFalse(usesWidePrimaryNavigation(759.dp))
        assertTrue(usesWidePrimaryNavigation(760.dp))
        assertEquals(
            listOf(
                Dashboard,
                Diagnostics,
                History,
            ),
            PrimaryDestinations,
        )
    }
}
