package dev.lui.huaweisync.ui.state

/** Facts the local assistant may reference. Every value comes from the existing presentation state. */
data class AssistantLocalContext(
    val healthConnectStatus: String,
    val gymRatsStatus: String,
    val ledgerWorkoutCount: Int,
    val attemptCount: Int,
    val sanitizedFailureSummary: String?,
) {
    companion object {
        fun from(productState: ProductSyncState?): AssistantLocalContext = AssistantLocalContext(
            healthConnectStatus = productState?.healthConnectStatus?.label ?: "Status not loaded",
            gymRatsStatus = productState?.gymRatsStatus?.label ?: "Status not loaded",
            ledgerWorkoutCount = productState?.ledgerWorkoutCount ?: 0,
            attemptCount = productState?.attemptCount ?: 0,
            sanitizedFailureSummary = productState?.sanitizedFailureSummary,
        )
    }
}

enum class AssistantPrompt(val label: String) {
    SYNC_STATUS("What does my current sync status mean?"),
    MISSING_PERMISSIONS("What permissions might need attention?"),
    GYMRATS_VISIBILITY("Why might this workout not appear in GymRats?"),
}

enum class AssistantMessageAuthor { ASSISTANT, YOU }

data class AssistantMessage(
    val author: AssistantMessageAuthor,
    val text: String,
)

data class AssistantState(
    val context: AssistantLocalContext,
    val messages: List<AssistantMessage> = listOf(welcomeMessage),
) {
    val experienceLabel: String get() = EXPERIMENTAL
    val isLocalOnly: Boolean get() = true
    val acceptsFreeFormInput: Boolean get() = false
    val contactsRemoteService: Boolean get() = false

    companion object {
        const val EXPERIMENTAL = "Experimental"
        const val INPUT_DISABLED_REASON = "Free-form questions are unavailable. Choose a safe local prompt."
        val welcomeMessage = AssistantMessage(
            author = AssistantMessageAuthor.ASSISTANT,
            text = "I use only status already shown in this app. I do not contact an AI service. Choose a safe prompt below.",
        )
    }
}

sealed interface AssistantAction {
    data class SelectPrompt(val prompt: AssistantPrompt) : AssistantAction
    data object ClearConversation : AssistantAction
}

/** Pure reducer: the same local facts and canned prompt always produce the same conversation. */
object AssistantReducer {
    fun reduce(state: AssistantState, action: AssistantAction): AssistantState = when (action) {
        AssistantAction.ClearConversation -> state.copy(messages = listOf(AssistantState.welcomeMessage))
        is AssistantAction.SelectPrompt -> state.copy(
            messages = state.messages + listOf(
                AssistantMessage(AssistantMessageAuthor.YOU, action.prompt.label),
                AssistantMessage(AssistantMessageAuthor.ASSISTANT, answer(action.prompt, state.context)),
            ),
        )
    }

    private fun answer(prompt: AssistantPrompt, context: AssistantLocalContext): String = when (prompt) {
        AssistantPrompt.SYNC_STATUS -> buildString {
            append("Local status: ${context.healthConnectStatus}. ")
            append("The ledger currently shows ${context.ledgerWorkoutCount} workout")
            if (context.ledgerWorkoutCount != 1) append('s')
            append(" and ${context.attemptCount} recorded attempt")
            if (context.attemptCount != 1) append('s')
            append('.')
            context.sanitizedFailureSummary?.let { append(" Last safe error summary: $it") }
        }
        AssistantPrompt.MISSING_PERMISSIONS ->
            "The app currently reports: ${context.healthConnectStatus}. Only the app's permission status is available here; review the Permissions or Diagnostics screen for the next action."
        AssistantPrompt.GYMRATS_VISIBILITY ->
            "Known local facts: Health Connect is '${context.healthConnectStatus}' and GymRats is '${context.gymRatsStatus}'. This app has not verified that GymRats imported a workout; confirm that separately in GymRats."
    }
}
