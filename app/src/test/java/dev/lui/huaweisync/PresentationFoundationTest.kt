package dev.lui.huaweisync

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.lui.huaweisync.diagnostics.HealthConnectPermission
import dev.lui.huaweisync.health.HealthConnectAvailability
import dev.lui.huaweisync.presentation.HuaweiSyncRoot
import dev.lui.huaweisync.ui.screens.diagnostics.DiagnosticsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PresentationFoundationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `existing Gate 1 entry remains constructible through presentation root`() {
        composeRule.setContent {
            HuaweiSyncRoot {
                DiagnosticsScreen(
                    availability = HealthConnectAvailability.Unavailable,
                    permission = HealthConnectPermission.NOT_REQUESTED,
                    diagnostic = null,
                    busy = false,
                    onCheckAvailability = {},
                    onRequestPermission = {},
                    onRun = {},
                    onConfirm = {},
                    onReconcile = {},
                    onRefresh = {},
                    onExport = {},
                )
            }
        }

        composeRule.onNodeWithText("Diagnostics").assertIsDisplayed()
        composeRule.onNodeWithText("Unavailable").assertIsDisplayed()
    }
}
