package dev.lui.huaweisync.ui.screens.automation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

private enum class PreviewRunState {
    READY,
    CHECKED,
}

@Composable
fun AutomationScreen(
    state: AutomationScreenState = AutomationScreenState(),
    modifier: Modifier = Modifier,
) {
    var previewRunState by remember { mutableStateOf(PreviewRunState.READY) }

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
                eyebrow = "${state.experienceLabel.uppercase()} · LOCAL SESSION",
                title = "Automation",
                trailing = { TechnicalMicrocopy("NO BACKGROUND JOB") },
            )
        }
        item { PreviewNotice() }
        item { AutomationOverview(state) }
        item {
            SectionHeader(
                title = "Choose how sync could start",
                trailing = { TechnicalMicrocopy("${state.triggers.size.toString().padStart(2, '0')} CONCEPTS") },
            )
        }
        items(state.triggers, key = AutomationTrigger::title) { trigger ->
            TriggerCard(trigger)
        }
        item {
            PreviewFlowCard(
                checked = previewRunState == PreviewRunState.CHECKED,
                onRunPreview = { previewRunState = PreviewRunState.CHECKED },
                onReset = { previewRunState = PreviewRunState.READY },
            )
        }
        item { Spacer(Modifier.height(HuaweiSyncSpacing.xxl)) }
    }
}

@Composable
private fun PreviewNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HuaweiSyncTheme.colors.accentSoft)
            .border(HuaweiSyncGeometry.borderStrong, HuaweiSyncTheme.colors.accent, RectangleShape)
            .padding(HuaweiSyncSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Rounded.CloudOff,
            contentDescription = null,
            tint = HuaweiSyncTheme.colors.accentForeground,
            modifier = Modifier.size(24.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs)) {
            Text(
                text = "PREVIEW ONLY",
                color = HuaweiSyncTheme.colors.accentForeground,
                style = HuaweiSyncTheme.technicalTypography.microcopy,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Explore the intended automation experience without enabling automation.",
                style = MaterialTheme.typography.titleMedium,
                color = HuaweiSyncTheme.colors.ink,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Nothing here is saved. No background job is created. Leaving this screen resets the preview.",
                style = MaterialTheme.typography.bodyMedium,
                color = HuaweiSyncTheme.colors.ink2,
            )
        }
    }
}

@Composable
private fun AutomationOverview(state: AutomationScreenState) {
    ModernistSurface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = if (state.hasEnabledSchedulingBehavior) "Enabled" else "Preview only"
            },
        backgroundColor = HuaweiSyncTheme.colors.surface2,
        borderColor = HuaweiSyncTheme.colors.lineStrong,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs),
            ) {
                TechnicalMicrocopy("MASTER CONTROL")
                Text(
                    text = "Automatic sync",
                    style = MaterialTheme.typography.titleLarge,
                    color = HuaweiSyncTheme.colors.ink,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Unavailable in this preview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HuaweiSyncTheme.colors.ink2,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                ComingSoonLabel()
                Switch(
                    checked = false,
                    onCheckedChange = null,
                    enabled = false,
                    modifier = Modifier.semantics {
                        contentDescription = "Automatic sync"
                        stateDescription = "Coming soon"
                        disabled()
                    },
                )
            }
        }
    }
}

@Composable
private fun TriggerCard(trigger: AutomationTrigger) {
    val icon = when (trigger.title) {
        "Scheduled sync" -> Icons.Rounded.AccessTime
        "After a Huawei Health update" -> Icons.Rounded.Refresh
        "While charging" -> Icons.Rounded.Bolt
        else -> Icons.Rounded.Wifi
    }

    ModernistSurface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${trigger.title}. ${trigger.availabilityLabel}"
                stateDescription = trigger.availabilityLabel
                disabled()
            },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TriggerIcon(icon)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs),
            ) {
                Text(
                    text = trigger.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = HuaweiSyncTheme.colors.ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = trigger.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HuaweiSyncTheme.colors.ink2,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                ComingSoonLabel(trigger.availabilityLabel)
                Switch(
                    checked = trigger.enabled,
                    onCheckedChange = null,
                    enabled = false,
                )
            }
        }
    }
}

@Composable
private fun TriggerIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(HuaweiSyncTheme.colors.surface3)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.lineStrong, RectangleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HuaweiSyncTheme.colors.accentForeground,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun ComingSoonLabel(label: String = AutomationScreenState.COMING_SOON) {
    Text(
        text = label.uppercase(),
        modifier = Modifier
            .background(HuaweiSyncTheme.colors.surface3)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.lineStrong, RectangleShape)
            .padding(horizontal = HuaweiSyncSpacing.sm, vertical = HuaweiSyncSpacing.xs),
        style = MaterialTheme.typography.labelSmall,
        color = HuaweiSyncTheme.colors.ink2,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun PreviewFlowCard(
    checked: Boolean,
    onRunPreview: () -> Unit,
    onReset: () -> Unit,
) {
    ModernistSurface(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = HuaweiSyncTheme.colors.surface2,
        borderColor = if (checked) HuaweiSyncTheme.colors.ok else HuaweiSyncTheme.colors.lineStrong,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = if (checked) Icons.Rounded.AutoAwesome else Icons.Rounded.LockClock,
                contentDescription = null,
                tint = if (checked) HuaweiSyncTheme.colors.ok else HuaweiSyncTheme.colors.info,
                modifier = Modifier.size(24.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm),
            ) {
                TechnicalMicrocopy("LOCAL INTERACTION · NOT A SYNC")
                Text(
                    text = if (checked) "Preview check complete" else "Inspect the preview contract",
                    style = MaterialTheme.typography.titleMedium,
                    color = HuaweiSyncTheme.colors.ink,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (checked) {
                        "All concepts remain off. This confirmation exists only until the screen leaves composition."
                    } else {
                        "Run a local visual check that confirms every trigger remains unavailable."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = HuaweiSyncTheme.colors.ink2,
                )
                StraightEdgeButton(
                    label = if (checked) "Reset local preview" else "Run local preview check",
                    onClick = if (checked) onReset else onRunPreview,
                    accent = !checked,
                )
            }
        }
    }
}

@Preview(name = "Automation · Light", showBackground = true, widthDp = 412, heightDp = 1100)
@Preview(
    name = "Automation · Dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 1100,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun AutomationScreenPreview() {
    HuaweiSyncTheme(darkTheme = androidx.compose.foundation.isSystemInDarkTheme()) {
        AutomationScreen()
    }
}
