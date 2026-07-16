package dev.lui.huaweisync.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.lui.huaweisync.ui.HuaweiSyncRoot
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppShellNavigationRegressionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `app shell renders exactly one global bottom navigation`() {
        composeRule.setContent { AppUnderTest() }

        composeRule.onAllNodesWithTag("global-bottom-navigation")
            .assertCountEquals(1)
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    @Test
    fun `global selection follows the current route`() {
        composeRule.setContent { AppUnderTest() }

        composeRule.onNodeWithTag("bottom-nav-dashboard").assertIsSelected()
        composeRule.onNodeWithContentDescription("Pipeline").performClick()
        composeRule.onNodeWithTag("bottom-nav-pipeline").assertIsSelected()
        composeRule.onNodeWithContentDescription("History").performClick()
        composeRule.onNodeWithTag("bottom-nav-history").assertIsSelected()
    }

    @Test
    fun `integrations remains accessible through More under one consistent label`() {
        composeRule.setContent { AppUnderTest() }

        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithTag("nav-integrations").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Integrations").assertIsDisplayed()
        composeRule.onAllNodesWithTag("global-bottom-navigation")
            .assertCountEquals(1)
        composeRule.onNodeWithTag("bottom-nav-more").assertIsSelected()
    }

    @Test
    fun `more menu drops the settings relabel so integrations shows one name`() {
        composeRule.setContent { AppUnderTest() }

        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithTag("nav-settings").assertDoesNotExist()
        composeRule.onNodeWithTag("nav-integrations").assertIsDisplayed()
    }

    @Test
    fun `back navigation closes transient UI then follows screen hierarchy`() {
        val state = HuaweiSyncNavigationState(Dashboard)

        assertFalse(state.navigateBack())
        state.navigateTo(Pipeline)
        assertTrue(state.navigateBack())
        assertSame(Dashboard, state.currentDestination)

        state.navigateTo(ActivityDetail)
        assertTrue(state.navigateBack())
        assertSame(History, state.currentDestination)

        state.showCompactMenu()
        assertTrue(state.navigateBack())
        assertFalse(state.compactMenuVisible)

        state.showSyncOverlay()
        assertTrue(state.navigateBack())
        assertSame(History, state.currentDestination)
        assertTrue(state.navigateBack())
        assertSame(Dashboard, state.currentDestination)
        assertFalse(state.canNavigateBack)
    }

    @Test
    fun `nav shell contains no residual settings label for the integrations route`() {
        val projectRoot = findProjectRoot()
        val shell = projectRoot.resolve("app/src/main/java/dev/lui/huaweisync/ui/HuaweiSyncRoot.kt")
        val text = shell.readText()
        assertFalse(
            "Settings relabel resurfaced in ${shell.fileName}",
            text.contains("\"Settings\"") || text.contains("nav-settings"),
        )
    }

    @Test
    fun `screen implementations do not own bottom navigation copies`() {
        val projectRoot = findProjectRoot()
        val screens = projectRoot.resolve("app/src/main/java/dev/lui/huaweisync/ui/screens")
        val offenders = Files.walk(screens).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".kt") }
                .filter { it.readText().contains("ModernistBottomNavigation") }
                .map { screens.relativize(it).toString() }
                .toList()
        }

        assertTrue("Screen-owned bottom navigation found in $offenders", offenders.isEmpty())
    }

    @Test
    fun `compact app shell padding keeps content and fab above global navigation`() {
        composeRule.setContent { AppUnderTest() }

        val contentBounds = composeRule.onNodeWithTag("app-shell-content")
            .fetchSemanticsNode().boundsInRoot
        val navigationBounds = composeRule.onNodeWithTag("global-bottom-navigation")
            .fetchSemanticsNode().boundsInRoot
        val fabBounds = composeRule.onNodeWithContentDescription("Sync now")
            .fetchSemanticsNode().boundsInRoot

        assertTrue(contentBounds.bottom <= navigationBounds.top)
        assertTrue(fabBounds.bottom <= navigationBounds.top)
    }

    @androidx.compose.runtime.Composable
    private fun AppUnderTest() {
        HuaweiSyncRoot(gate1Entry = { Text("Gate 1 diagnostics content") })
    }

    private fun findProjectRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (!current.resolve("settings.gradle.kts").toFile().isFile) {
            current = current.parent ?: error("Could not locate project root")
        }
        check(current.isDirectory())
        return current
    }
}
