package dev.lui.huaweisync.ui.screens.assistant

import dev.lui.huaweisync.ui.state.AssistantAction
import dev.lui.huaweisync.ui.state.AssistantLocalContext
import dev.lui.huaweisync.ui.state.AssistantMessageAuthor
import dev.lui.huaweisync.ui.state.AssistantPrompt
import dev.lui.huaweisync.ui.state.AssistantReducer
import dev.lui.huaweisync.ui.state.AssistantState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantReducerTest {
    private val context = AssistantLocalContext(
        healthConnectStatus = "Health Connect permission required",
        gymRatsStatus = "Available for GymRats to import",
        ledgerWorkoutCount = 2,
        attemptCount = 3,
        sanitizedFailureSummary = "Permission is required before sync can continue.",
    )

    @Test
    fun `same local state and prompt always produce the same response`() {
        val initial = AssistantState(context)
        val action = AssistantAction.SelectPrompt(AssistantPrompt.SYNC_STATUS)

        val first = AssistantReducer.reduce(initial, action)
        val second = AssistantReducer.reduce(initial, action)

        assertEquals(first, second)
        assertEquals(3, first.messages.size)
        assertEquals(AssistantMessageAuthor.YOU, first.messages[1].author)
        assertEquals(AssistantMessageAuthor.ASSISTANT, first.messages[2].author)
        assertTrue(first.messages.last().text.contains(context.healthConnectStatus))
        assertTrue(first.messages.last().text.contains("2 workouts"))
        assertTrue(first.messages.last().text.contains("3 recorded attempts"))
    }

    @Test
    fun `permission guidance uses only the supplied local status`() {
        val result = AssistantReducer.reduce(
            AssistantState(context),
            AssistantAction.SelectPrompt(AssistantPrompt.MISSING_PERMISSIONS),
        )

        val answer = result.messages.last().text
        assertTrue(answer.contains("The app currently reports: ${context.healthConnectStatus}"))
        assertTrue(answer.contains("Only the app's permission status is available here"))
    }

    @Test
    fun `gymrats guidance refuses to claim import success`() {
        val result = AssistantReducer.reduce(
            AssistantState(context),
            AssistantAction.SelectPrompt(AssistantPrompt.GYMRATS_VISIBILITY),
        )

        val answer = result.messages.last().text
        assertTrue(answer.contains("has not verified that GymRats imported a workout"))
        assertTrue(answer.contains(context.healthConnectStatus))
        assertTrue(answer.contains(context.gymRatsStatus))
        assertFalse(answer.contains("successfully imported", ignoreCase = true))
        assertFalse(answer.contains("sent to GymRats", ignoreCase = true))
    }

    @Test
    fun `assistant contract is experimental local only and canned prompt only`() {
        val state = AssistantState(context)

        assertEquals(AssistantState.EXPERIMENTAL, state.experienceLabel)
        assertTrue(state.isLocalOnly)
        assertFalse(state.acceptsFreeFormInput)
        assertFalse(state.contactsRemoteService)
        assertTrue(AssistantPrompt.entries.isNotEmpty())
        assertTrue(state.messages.single().text.contains("do not contact an AI service"))
    }

    @Test
    fun `clear conversation retains local context and safe welcome`() {
        val answered = AssistantReducer.reduce(
            AssistantState(context),
            AssistantAction.SelectPrompt(AssistantPrompt.SYNC_STATUS),
        )

        val cleared = AssistantReducer.reduce(answered, AssistantAction.ClearConversation)

        assertEquals(context, cleared.context)
        assertEquals(listOf(AssistantState.welcomeMessage), cleared.messages)
    }
}
