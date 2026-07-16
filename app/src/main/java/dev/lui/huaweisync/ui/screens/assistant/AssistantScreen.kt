package dev.lui.huaweisync.ui.screens.assistant

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.state.AssistantAction
import dev.lui.huaweisync.ui.state.AssistantLocalContext
import dev.lui.huaweisync.ui.state.AssistantMessage
import dev.lui.huaweisync.ui.state.AssistantMessageAuthor
import dev.lui.huaweisync.ui.state.AssistantPrompt
import dev.lui.huaweisync.ui.state.AssistantReducer
import dev.lui.huaweisync.ui.state.AssistantState
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

@Composable
fun AssistantScreen(
    context: AssistantLocalContext,
    modifier: Modifier = Modifier,
) {
    var state by remember(context) { mutableStateOf(AssistantState(context)) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.canvasBackground),
        contentPadding = PaddingValues(
            horizontal = HuaweiSyncSpacing.lg,
            vertical = HuaweiSyncSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
    ) {
        item {
            SectionHeader(
                eyebrow = "${state.experienceLabel.uppercase()} · DETERMINISTIC LOCAL",
                title = "AI Assistant",
                trailing = { TechnicalMicrocopy("NO NETWORK") },
            )
        }
        item { LocalOnlyNotice() }
        item {
            SectionHeader(
                eyebrow = "ASK · LOCAL CONCEPT",
                title = "Sync assistant",
                trailing = {
                    if (state.messages.size > 1) {
                        StraightEdgeButton(
                            label = "Clear",
                            onClick = {
                                state = AssistantReducer.reduce(state, AssistantAction.ClearConversation)
                            },
                        )
                    }
                },
            )
        }
        items(state.messages) { message -> MessageBubble(message) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm)) {
                TechnicalMicrocopy("TRY A SAFE PROMPT")
                AssistantPrompt.entries.forEach { prompt ->
                    PromptButton(prompt) {
                        state = AssistantReducer.reduce(state, AssistantAction.SelectPrompt(prompt))
                    }
                }
            }
        }
        item { DisabledComposer() }
        item {
            Text(
                text = "Answers use only the local status displayed by Huawei Sync. They cannot inspect Huawei Health, Health Connect, GymRats, device logs, or external services.",
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LocalOnlyNotice() {
    ModernistSurface(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = HuaweiSyncTheme.colors.accentSoft,
        borderColor = HuaweiSyncTheme.colors.accent,
    ) {
        Row(
            modifier = Modifier.padding(HuaweiSyncSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(HuaweiSyncTheme.colors.accentContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    style = HuaweiSyncTheme.technicalTypography.label,
                    fontWeight = FontWeight.Black,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs)) {
                Text(
                    text = "EXPERIMENTAL · LOCAL ONLY",
                    color = HuaweiSyncTheme.colors.accentForeground,
                    style = HuaweiSyncTheme.technicalTypography.label,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "No model is connected. Prompts run through canned, deterministic rules and never leave this app.",
                    color = HuaweiSyncTheme.colors.ink2,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: AssistantMessage) {
    val assistant = message.author == AssistantMessageAuthor.ASSISTANT
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (assistant) Arrangement.Start else Arrangement.End,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(if (assistant) 0.92f else 0.82f)
                .background(
                    if (assistant) HuaweiSyncTheme.colors.surface1 else HuaweiSyncTheme.colors.accentSoft,
                )
                .border(
                    1.dp,
                    if (assistant) HuaweiSyncTheme.colors.line else HuaweiSyncTheme.colors.accent,
                    RectangleShape,
                )
                .padding(HuaweiSyncSpacing.md),
            verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm),
        ) {
            TechnicalMicrocopy(if (assistant) "ASSISTANT · LOCAL RULE" else "YOU · SAFE PROMPT")
            Text(
                text = message.text,
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PromptButton(prompt: AssistantPrompt, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .border(1.dp, HuaweiSyncTheme.colors.lineStrong, RectangleShape)
            .clickable(role = Role.Button, onClickLabel = prompt.label, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "Ask: ${prompt.label}"
            }
            .padding(HuaweiSyncSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prompt.label,
            color = HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(HuaweiSyncSpacing.md))
        Text(
            text = "RUN →",
            color = HuaweiSyncTheme.colors.accentForeground,
            style = HuaweiSyncTheme.technicalTypography.label,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DisabledComposer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, HuaweiSyncTheme.colors.line, RectangleShape)
            .background(HuaweiSyncTheme.colors.surface2)
            .semantics {
                disabled()
                contentDescription = AssistantState.INPUT_DISABLED_REASON
            }
            .padding(horizontal = HuaweiSyncSpacing.md),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = AssistantState.INPUT_DISABLED_REASON,
            color = HuaweiSyncTheme.colors.ink2,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private val previewContext = AssistantLocalContext(
    healthConnectStatus = "Confirmed in Health Connect",
    gymRatsStatus = "Available for GymRats to import",
    ledgerWorkoutCount = 3,
    attemptCount = 3,
    sanitizedFailureSummary = null,
)

@Preview(name = "Assistant local - light", showBackground = true, widthDp = 412, heightDp = 892)
@Preview(
    name = "Assistant local - dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 892,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun AssistantScreenPreview() {
    HuaweiSyncTheme { AssistantScreen(context = previewContext) }
}

@Preview(name = "Assistant answered", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun AssistantAnsweredPreview() {
    HuaweiSyncTheme {
        var state = AssistantState(previewContext)
        state = AssistantReducer.reduce(state, AssistantAction.SelectPrompt(AssistantPrompt.GYMRATS_VISIBILITY))
        AssistantPreviewContent(state)
    }
}

@Composable
private fun AssistantPreviewContent(state: AssistantState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.canvasBackground)
            .padding(HuaweiSyncSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
    ) {
        LocalOnlyNotice()
        state.messages.forEach { MessageBubble(it) }
        DisabledComposer()
    }
}
