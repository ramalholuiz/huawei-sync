package dev.lui.huaweisync.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ModernistComponentsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `reduced motion policy disables every audited duration`() {
        val policy = HuaweiSyncMotionPolicy.forReducedMotion(reducedMotion = true)

        assertTrue(policy.reducedMotion)
        assertEquals(0, policy.fastMillis)
        assertEquals(0, policy.standardMillis)
        assertEquals(0, policy.deliberateMillis)
        assertEquals(0, policy.syncRotationMillis)
    }

    @Test
    fun `standard motion policy retains audited sync timing`() {
        val policy = HuaweiSyncMotionPolicy.forReducedMotion(reducedMotion = false)

        assertFalse(policy.reducedMotion)
        assertEquals(160, policy.fastMillis)
        assertEquals(240, policy.standardMillis)
        assertEquals(480, policy.deliberateMillis)
        assertEquals(1_000, policy.syncRotationMillis)
    }

    @Test
    fun `straight edge button exposes button semantics and 48dp target`() {
        compose.setContent {
            HuaweiSyncTheme {
                StraightEdgeButton(label = "Review", onClick = {})
            }
        }

        compose.onNodeWithText("REVIEW")
            .assertHasClickAction()
            .assertHeightIsAtLeast(ModernistComponentMetrics.minimumTouchTarget)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun `bottom navigation exposes selected tabs with 48dp targets`() {
        compose.setContent {
            HuaweiSyncTheme {
                ModernistBottomNavigation(
                    items = listOf(
                        ModernistNavigationItem("home", "Home", Icons.Rounded.Home),
                        ModernistNavigationItem("settings", "Settings", Icons.Rounded.Settings),
                    ),
                    selectedKey = "home",
                    onSelect = {},
                )
            }
        }

        compose.onNodeWithContentDescription("Home")
            .assertIsSelected()
            .assertHasClickAction()
            .assertHeightIsAtLeast(48.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
    }

    @Test
    fun `sync fab is square 72dp and announces its state`() {
        compose.setContent {
            HuaweiSyncTheme {
                SyncFab(state = SyncFabState.Idle, onClick = {})
            }
        }

        compose.onNodeWithContentDescription("Sync now")
            .assertHasClickAction()
            .assertWidthIsEqualTo(ModernistComponentMetrics.syncFabSize)
            .assertHeightIsAtLeast(ModernistComponentMetrics.syncFabSize)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    SyncFabState.Idle.spokenState,
                ),
            )
    }

    @Test
    fun `sync fab content description follows state so talkback does not double up`() {
        assertEquals("Sync now", SyncFabState.Idle.contentLabel)
        assertEquals("Sync in progress", SyncFabState.Syncing.contentLabel)
        assertEquals("Synced — sync again", SyncFabState.Complete.contentLabel)

        compose.setContent {
            HuaweiSyncTheme {
                SyncFab(state = SyncFabState.Complete, onClick = {})
            }
        }

        compose.onNodeWithContentDescription("Synced — sync again")
            .assertHasClickAction()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    SyncFabState.Complete.spokenState,
                ),
            )
    }

    @Test
    fun `status and progress expose nonvisual state`() {
        compose.setContent {
            HuaweiSyncTheme {
                StatusLabel(ModernistStatus.Warning)
                ModernistProgress(progress = 0.5f, label = "records prepared")
            }
        }

        compose.onNodeWithText("ATTENTION").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                ModernistStatus.Warning.spokenState,
            ),
        )
        compose.onNodeWithContentDescription("records prepared").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                androidx.compose.ui.semantics.ProgressBarRangeInfo(0.5f, 0f..1f),
            ),
        )
    }
}
