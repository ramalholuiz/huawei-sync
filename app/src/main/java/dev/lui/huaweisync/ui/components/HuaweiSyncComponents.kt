package dev.lui.huaweisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncRadius
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

private val CardShape = RoundedCornerShape(HuaweiSyncRadius.card)
private val HeroShape = RoundedCornerShape(HuaweiSyncRadius.hero)
private val ControlShape = RoundedCornerShape(HuaweiSyncRadius.control)
private val ChipShape = RoundedCornerShape(HuaweiSyncRadius.chip)

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    containerColor: Color = HuaweiSyncTheme.colors.surface1,
    contentPadding: Dp = HuaweiSyncSpacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(containerColor)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun RaisedCard(
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    contentPadding: Dp = HuaweiSyncSpacing.xl,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(if (hero) HeroShape else CardShape)
            .background(HuaweiSyncTheme.colors.surface2)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun AttentionCard(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    contentPadding: Dp = HuaweiSyncSpacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    val borderColor = if (isError) HuaweiSyncTheme.colors.error else HuaweiSyncTheme.colors.attention
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(HuaweiSyncTheme.colors.surface1)
            .border(HuaweiSyncGeometry.borderStrong, borderColor, CardShape)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun AccentWashCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = HuaweiSyncSpacing.xl,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(HeroShape)
            .background(HuaweiSyncTheme.colors.surface2)
            .background(
                Brush.verticalGradient(
                    listOf(HuaweiSyncTheme.colors.surfaceAccentWash, Color.Transparent),
                ),
            )
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun PrimaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    ActionButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        containerColor = HuaweiSyncTheme.colors.accent,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        bordered = false,
    )
}

@Composable
fun SecondaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    ActionButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        containerColor = HuaweiSyncTheme.colors.surface1,
        contentColor = HuaweiSyncTheme.colors.ink,
        bordered = true,
    )
}

@Composable
private fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    containerColor: Color,
    contentColor: Color,
    bordered: Boolean,
) {
    val alpha = if (enabled) 1f else 0.45f
    val base = modifier
        .defaultMinSize(minWidth = 96.dp, minHeight = 48.dp)
        .clip(ControlShape)
        .background(containerColor.copy(alpha = containerColor.alpha * alpha))
    val chrome = if (bordered) {
        base.border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line, ControlShape)
    } else {
        base
    }
    Row(
        modifier = chrome
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = HuaweiSyncSpacing.lg, vertical = HuaweiSyncSpacing.md),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = contentColor)
            Spacer(Modifier.width(HuaweiSyncSpacing.sm))
        }
        Text(label, color = contentColor.copy(alpha = alpha), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Text(
        text = label,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(ControlShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = HuaweiSyncSpacing.sm, vertical = HuaweiSyncSpacing.md),
        color = HuaweiSyncTheme.colors.accentForeground.copy(alpha = if (enabled) 1f else 0.45f),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
fun FabAction(
    contentDescription: String,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(HuaweiSyncTheme.colors.accent)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onPrimary)
    }
}

@Immutable
enum class VisualStatus(val spokenState: String) {
    Ready("Ready"),
    Pending("In progress"),
    Success("Complete"),
    Attention("Attention required"),
    Error("Error"),
}

@Composable
fun StatusChip(
    label: String,
    status: VisualStatus,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val color = when (status) {
        VisualStatus.Ready -> HuaweiSyncTheme.colors.ink2
        VisualStatus.Pending -> HuaweiSyncTheme.colors.info
        VisualStatus.Success -> HuaweiSyncTheme.colors.success
        VisualStatus.Attention -> HuaweiSyncTheme.colors.attention
        VisualStatus.Error -> HuaweiSyncTheme.colors.error
    }
    Row(
        modifier = modifier
            .height(24.dp)
            .clip(ChipShape)
            .background(color.copy(alpha = 0.10f))
            .border(HuaweiSyncGeometry.borderThin, color.copy(alpha = 0.55f), ChipShape)
            .padding(horizontal = HuaweiSyncSpacing.sm)
            .semantics(mergeDescendants = true) { stateDescription = status.spokenState },
        horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
        Text(label, color = color, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) HuaweiSyncTheme.colors.accentSoft else HuaweiSyncTheme.colors.neutralLow
    val foreground = if (selected) HuaweiSyncTheme.colors.accentForeground else HuaweiSyncTheme.colors.ink2
    Text(
        text = label,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(ChipShape)
            .background(background)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line, ChipShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = HuaweiSyncSpacing.lg, vertical = HuaweiSyncSpacing.sm),
        color = foreground,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text = text, modifier = modifier, color = HuaweiSyncTheme.colors.ink2, style = MaterialTheme.typography.titleSmall)
}

@Composable
fun EmptyStateSection(
    title: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    RaisedCard(modifier = modifier, hero = true) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = HuaweiSyncTheme.colors.accentForeground)
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
        }
        Text(title, color = HuaweiSyncTheme.colors.ink, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(HuaweiSyncSpacing.sm))
        Text(supportingText, color = HuaweiSyncTheme.colors.ink2, style = MaterialTheme.typography.bodyMedium)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
            PrimaryAction(label = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun ErrorStateSection(
    title: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    AttentionCard(modifier = modifier, isError = true) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = HuaweiSyncTheme.colors.error)
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
        }
        Text(title, color = HuaweiSyncTheme.colors.ink, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(HuaweiSyncSpacing.sm))
        Text(
            supportingText,
            color = HuaweiSyncTheme.colors.ink2,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
            PrimaryAction(label = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm),
    ) {
        listOf(0.55f, 0.88f, 0.72f).forEachIndexed { index, fraction ->
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(if (index == 0) 20.dp else 14.dp)
                    .clip(RoundedCornerShape(HuaweiSyncRadius.chart))
                    .background(HuaweiSyncTheme.colors.neutralLow),
            )
        }
    }
}
