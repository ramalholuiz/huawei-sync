package dev.lui.huaweisync.ui.screens.onboarding

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class OnboardingScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `shows live Health Connect permission status without delivery claims`() {
        compose.setContent {
            HuaweiSyncTheme(darkTheme = true) {
                OnboardingScreen(
                    state = OnboardingScreenState(ProductHealthConnectStatus.PERMISSION_REQUIRED),
                    darkTheme = true,
                    onStartSetup = {},
                    onContinueExistingSetup = {},
                    onToggleTheme = {},
                )
            }
        }

        compose.onNodeWithText("Health Connect permission required").assertExists()
        compose.onNodeWithText("delivered everywhere", substring = true, ignoreCase = true).assertDoesNotExist()
        compose.onNodeWithText("already synced", substring = true, ignoreCase = true).assertDoesNotExist()
    }

    @Test
    fun `loading state and all onboarding actions remain explicit`() {
        var setupClicks = 0
        var continueClicks = 0
        var themeClicks = 0
        compose.setContent {
            HuaweiSyncTheme(darkTheme = false) {
                OnboardingScreen(
                    state = OnboardingScreenState(),
                    darkTheme = false,
                    onStartSetup = { setupClicks++ },
                    onContinueExistingSetup = { continueClicks++ },
                    onToggleTheme = { themeClicks++ },
                )
            }
        }

        compose.onNodeWithText("Checking Health Connect").assertExists()
        compose.onNodeWithContentDescription("Switch theme").performClick()
        compose.onNode(hasScrollAction()).performTouchInput {
            swipeUp()
            swipeUp()
        }
        compose.onNodeWithText("Start setup", ignoreCase = true).performClick()
        compose.onNodeWithText("Continue existing setup", ignoreCase = true).performClick()

        assertEquals(1, setupClicks)
        assertEquals(1, continueClicks)
        assertEquals(1, themeClicks)
    }
}
