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
    fun `prototype inventory has ten unique typed destinations`() {
        assertEquals(10, HuaweiSyncDestination.all.size)
        assertEquals(10, HuaweiSyncDestination.all.map { it.route }.toSet().size)
        assertEquals(10, HuaweiSyncDestination.all.map { it.label }.toSet().size)
        assertSame(SyncNow, HuaweiSyncDestination.all.single { it.route == "sync-now" })
    }

    @Test
    fun `one state owner separates screen navigation from sync overlay`() {
        val state = HuaweiSyncNavigationState(Dashboard)

        state.navigateTo(Pipeline)
        assertSame(Pipeline, state.currentDestination)
        assertNull(state.overlayDestination)

        state.showSyncOverlay()
        assertSame(Pipeline, state.currentDestination)
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

        composeRule.onNodeWithContentDescription("Pipeline").performClick()
        composeRule.onNodeWithText("Live sync pipeline").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithTag("compact-more-menu").assertIsDisplayed()
        composeRule.onNodeWithTag("nav-diagnostics").performClick()
        composeRule.onNodeWithText("Gate 1 diagnostics content").assertIsDisplayed()

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
    fun `responsive policy selects wide primary navigation at the audited breakpoint`() {
        assertFalse(usesWidePrimaryNavigation(759.dp))
        assertTrue(usesWidePrimaryNavigation(760.dp))
        assertEquals(
            listOf(
                Dashboard,
                Pipeline,
                Integrations,
                Diagnostics,
                Automation,
                History,
                AiAssistant,
            ),
            PrimaryDestinations,
        )
    }
}
