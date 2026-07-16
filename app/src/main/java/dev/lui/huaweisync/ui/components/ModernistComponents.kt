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
    Column(
        modifier = modifier
            .clip(RectangleShape)
            .background(backgroundColor)
            .border(HuaweiSyncGeometry.borderThin, borderColor, RectangleShape)
            .padding(contentPadding),
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
    val color = when (status) {
        ModernistStatus.Ready, ModernistStatus.Complete -> HuaweiSyncTheme.colors.ok
        ModernistStatus.Syncing -> HuaweiSyncTheme.colors.info
        ModernistStatus.Warning -> HuaweiSyncTheme.colors.warning
        ModernistStatus.Error -> HuaweiSyncTheme.colors.accentForeground
    }
    Text(
        text = status.label,
        modifier = modifier
            .border(HuaweiSyncGeometry.borderThin, color, RectangleShape)
            .padding(horizontal = HuaweiSyncSpacing.sm, vertical = HuaweiSyncSpacing.xs)
            .semantics { stateDescription = status.spokenState },
        color = color,
        style = HuaweiSyncTheme.technicalTypography.label,
    )
}

@Composable
fun TechnicalMicrocopy(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = HuaweiSyncTheme.colors.ink2,
        style = HuaweiSyncTheme.technicalTypography.microcopy,
    )
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
        if (eyebrow != null) TechnicalMicrocopy(eyebrow.uppercase())
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
    val background = if (accent) HuaweiSyncTheme.colors.accentContainer else HuaweiSyncTheme.colors.surface2
    val foreground = if (accent) Color.White else HuaweiSyncTheme.colors.ink
    Row(
        modifier = modifier
            .defaultMinSize(
                minWidth = ModernistComponentMetrics.minimumTouchTarget,
                minHeight = ModernistComponentMetrics.minimumTouchTarget,
            )
            .clip(RectangleShape)
            .background(background)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.lineStrong, RectangleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = HuaweiSyncSpacing.lg, vertical = HuaweiSyncSpacing.md),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = foreground)
            Spacer(Modifier.width(HuaweiSyncSpacing.sm))
        }
        Text(label.uppercase(), color = foreground, style = HuaweiSyncTheme.technicalTypography.label)
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

enum class SyncFabState(val spokenState: String) {
    Idle("Ready to sync"),
    Syncing("Sync in progress"),
    Complete("Sync complete"),
}

@Composable
fun SyncFab(
    state: SyncFabState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = state != SyncFabState.Syncing,
) {
    Box(
        modifier = modifier
            .size(ModernistComponentMetrics.syncFabSize)
            .clip(RectangleShape)
            .background(HuaweiSyncTheme.colors.accentContainer)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = "Sync now", onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "Sync now"
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
