package dev.lui.huaweisync.ui.screens.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import dev.lui.huaweisync.ui.state.ActivityHistoryItem
import dev.lui.huaweisync.ui.state.ActivityReadbackState
import dev.lui.huaweisync.ui.state.HistoryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class HistorySelectionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `history exposes retryable read failure without inventing activity`() {
        composeRule.setContent {
            HistoryScreen(HistoryState.RetryableError(), onRetry = {}, onSelectActivity = {})
        }

        composeRule.onNodeWithTag("history-error").assertIsDisplayed()
    }

    @Test
    fun `empty history surface renders near the vertical center of the viewport`() {
        composeRule.setContent {
            HistoryScreen(HistoryState.Empty, onRetry = {}, onSelectActivity = {})
        }

        val surfaceNode = composeRule.onNodeWithTag("history-empty").fetchSemanticsNode()
        val rootHeight = surfaceNode.root?.let { it.semanticsOwner.rootSemanticsNode.size.height.toFloat() }
            ?: surfaceNode.size.height.toFloat()
        val surfaceCenter = (surfaceNode.boundsInRoot.top + surfaceNode.boundsInRoot.bottom) / 2f
        val viewportCenter = rootHeight / 2f
        val tolerance = 0.15f * rootHeight
        assertTrue(
            "history-empty center=$surfaceCenter must be within ${tolerance}px of viewport center=$viewportCenter (viewport=$rootHeight)",
            kotlin.math.abs(surfaceCenter - viewportCenter) <= tolerance,
        )
    }

    @Test
    fun `selecting row emits its deterministic client identity`() {
        var selected: String? = null
        val activity = activity("client-deterministic-b")
        composeRule.setContent {
            HistoryScreen(
                state = HistoryState.Content(listOf(activity)),
                onRetry = {},
                onSelectActivity = { selected = it },
            )
        }

        composeRule.onNodeWithTag("history-item-${activity.clientRecordId}").performClick()

        assertEquals(activity.clientRecordId, selected)
    }

    private fun activity(clientRecordId: String) = ActivityHistoryItem(
        clientRecordId = clientRecordId,
        clientRecordVersion = 1,
        sourceProvider = "synthetic",
        attemptCount = 1,
        acceptedAtEpochMillis = 10,
        confirmedAtEpochMillis = 20,
        updatedAtEpochMillis = 20,
        readbackState = ActivityReadbackState.VERIFIED,
        safeErrorCode = null,
    )
}
