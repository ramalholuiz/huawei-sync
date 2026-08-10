package dev.lui.huaweisync.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

object ModernistComponentMetrics {
    val minimumTouchTarget = 48.dp
    val syncFabSize = 72.dp
    val progressRuleHeight = 6.dp
    val bottomBarTopRule = HuaweiSyncGeometry.borderThin
}

@Composable
fun ModernistSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = HuaweiSyncTheme.colors.surface1,
    borderColor: Color = HuaweiSyncTheme.colors.line,
    contentPadding: Dp = HuaweiSyncSpacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Temporary source-compatible alias while screen call sites migrate in later cycles.
    SurfaceCard(
        modifier = modifier,
        containerColor = backgroundColor,
        contentPadding = contentPadding,
        content = content,
    )
}

enum class ModernistStatus(val label: String, val spokenState: String) {
    Ready("READY", "Ready"),
    Syncing("SYNCING", "Sync in progress"),
    Complete("COMPLETE", "Complete"),
    Warning("ATTENTION", "Attention required"),
    Error("ERROR", "Error"),
}

@Composable
fun StatusLabel(
    status: ModernistStatus,
    modifier: Modifier = Modifier,
) {
    // Temporary source-compatible alias while status call sites migrate to semantic chips.
    val visualStatus = when (status) {
        ModernistStatus.Ready -> VisualStatus.Ready
        ModernistStatus.Syncing -> VisualStatus.Pending
        ModernistStatus.Complete -> VisualStatus.Success
        ModernistStatus.Warning -> VisualStatus.Attention
        ModernistStatus.Error -> VisualStatus.Error
    }
    StatusChip(label = status.label, status = visualStatus, modifier = modifier)
}

@Composable
fun TechnicalMicrocopy(
    text: String,
    modifier: Modifier = Modifier,
) {
    // Temporary alias. Diagnostics may keep technical caps; product surfaces now get title styling.
    Eyebrow(text = text, modifier = modifier)
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HuaweiSyncSpacing.sm),
    ) {
        val stackTrailing = trailing != null &&
            (maxWidth < 420.dp || LocalDensity.current.fontScale >= 1.3f)
        if (stackTrailing) {
            Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm)) {
                SectionHeaderText(title = title, eyebrow = eyebrow)
                trailing?.invoke()
            }
        } else {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            ) {
                SectionHeaderText(
                    title = title,
                    eyebrow = eyebrow,
                    modifier = Modifier.weight(1f),
                )
                trailing?.invoke()
            }
        }
    }
}

@Composable
private fun SectionHeaderText(
    title: String,
    eyebrow: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (eyebrow != null) Eyebrow(eyebrow)
        Text(
            text = title,
            color = HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
fun StraightEdgeButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    // Temporary source-compatible alias; new code should choose PrimaryAction or SecondaryAction.
    if (accent) {
        PrimaryAction(
            label = label,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            leadingIcon = leadingIcon,
        )
    } else {
        SecondaryAction(
            label = label,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            leadingIcon = leadingIcon,
        )
    }
}

@Immutable
data class ModernistNavigationItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun ModernistBottomNavigation(
    items: List<ModernistNavigationItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(items.isNotEmpty()) { "Bottom navigation requires at least one item" }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HuaweiSyncTheme.colors.background)
            .border(
                width = ModernistComponentMetrics.bottomBarTopRule,
                color = HuaweiSyncTheme.colors.line,
                shape = RectangleShape,
            )
            .padding(horizontal = HuaweiSyncSpacing.xs, vertical = HuaweiSyncSpacing.sm),
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                item = item,
                selected = item.key == selectedKey,
                onClick = { onSelect(item.key) },
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavigationItem(
    item: ModernistNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) HuaweiSyncTheme.colors.accentForeground else HuaweiSyncTheme.colors.ink2
    Column(
        modifier = Modifier
            .weight(1f)
            .testTag("bottom-nav-${item.key}")
            .defaultMinSize(minHeight = ModernistComponentMetrics.minimumTouchTarget)
            .clickable(role = Role.Tab, onClickLabel = item.label, onClick = onClick)
            .semantics {
                this.selected = selected
                role = Role.Tab
                contentDescription = item.label
            }
            .padding(vertical = HuaweiSyncSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs),
    ) {
        Icon(item.icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(
            text = item.label.uppercase(),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = HuaweiSyncTheme.technicalTypography.label,
        )
    }
}

enum class SyncFabState(val spokenState: String, val contentLabel: String) {
    Idle(spokenState = "Ready to sync", contentLabel = "Sync now"),
    Syncing(spokenState = "Sync in progress", contentLabel = "Sync in progress"),
    Complete(spokenState = "Sync complete", contentLabel = "Synced — sync again"),
}

@Composable
fun SyncFab(
    state: SyncFabState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = state != SyncFabState.Syncing,
) {
    val label = state.contentLabel
    Box(
        modifier = modifier
            .size(ModernistComponentMetrics.syncFabSize)
            .clip(RectangleShape)
            .background(HuaweiSyncTheme.colors.accentContainer)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = label
                stateDescription = state.spokenState
            },
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            SyncFabState.Idle -> Icon(Icons.Rounded.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            SyncFabState.Syncing -> SyncingTreatment()
            SyncFabState.Complete -> Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun SyncingTreatment() {
    if (HuaweiSyncMotion.current.reducedMotion) {
        Icon(Icons.Rounded.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
    } else {
        CircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            color = Color.White,
            strokeWidth = HuaweiSyncGeometry.borderStrong,
        )
    }
}

@Composable
fun ModernistProgress(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
) {
    val target = progress.coerceIn(0f, 1f)
    val policy = HuaweiSyncMotion.current
    val animated = animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = policy.standardMillis),
        label = "modernist-progress",
    ).value
    Column(
        modifier = modifier.semantics {
            contentDescription = label
            progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f)
        },
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ModernistComponentMetrics.progressRuleHeight)
                .background(HuaweiSyncTheme.colors.surface3),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animated)
                    .height(ModernistComponentMetrics.progressRuleHeight)
                    .background(HuaweiSyncTheme.colors.accent),
            )
        }
        TechnicalMicrocopy("${(target * 100).toInt()}% · ${label.uppercase()}")
    }
}
