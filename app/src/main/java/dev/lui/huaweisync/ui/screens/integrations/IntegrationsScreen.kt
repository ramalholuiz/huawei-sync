package dev.lui.huaweisync.ui.screens.integrations

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.state.IntegrationEvidenceLevel
import dev.lui.huaweisync.ui.state.IntegrationItemState
import dev.lui.huaweisync.ui.state.IntegrationProvider
import dev.lui.huaweisync.ui.state.IntegrationState
import dev.lui.huaweisync.ui.state.IntegrationStatus
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

@Composable
fun IntegrationsScreen(
    state: IntegrationState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.canvasBackground),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = HuaweiSyncSpacing.lg,
            vertical = HuaweiSyncSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
    ) {
        item {
            SectionHeader(
                eyebrow = "EVIDENCE-BASED CONNECTIONS",
                title = "Integrations",
            )
        }
        item { EvidenceBanner() }
        item {
            TechnicalMicrocopy("CURRENT ROUTE")
            Spacer(Modifier.height(HuaweiSyncSpacing.sm))
            ActiveRoute(state)
        }
        item {
            SectionHeader(
                eyebrow = "ACTIVE PATH",
                title = "Android health exchange",
                trailing = { TechnicalMicrocopy("READBACK GATED") },
            )
        }
        items(state.activePath, key = { it.provider.name }) { item ->
            IntegrationCard(item = item, featured = item.provider == IntegrationProvider.HEALTH_CONNECT)
        }
        item {
            SectionHeader(
                eyebrow = "ROADMAP",
                title = "Future providers",
                trailing = { TechnicalMicrocopy("NO ACTIVE CONNECTION") },
            )
        }
        items(state.futureProviders.chunked(2), key = { row -> row.joinToString { it.provider.name } }) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            ) {
                row.forEach { item ->
                    IntegrationCard(
                        item = item,
                        compact = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item { Spacer(Modifier.height(HuaweiSyncSpacing.xxl)) }
    }
}

@Composable
private fun EvidenceBanner() {
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
            imageVector = Icons.Rounded.Hub,
            contentDescription = null,
            tint = HuaweiSyncTheme.colors.accentForeground,
            modifier = Modifier.size(24.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs)) {
            Text(
                text = "STATUS FOLLOWS EVIDENCE",
                color = HuaweiSyncTheme.colors.accentForeground,
                style = HuaweiSyncTheme.technicalTypography.label,
            )
            Text(
                text = "Health Connect confirmation requires official readback. Consumer apps remain ready-to-read until separately validated.",
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ActiveRoute(state: IntegrationState) {
    ModernistSurface(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = HuaweiSyncTheme.colors.surface2,
        contentPadding = HuaweiSyncSpacing.lg,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RouteStop("HUAWEI", "HW")
            RouteArrow()
            RouteStop("ANDROID HUB", "HC", emphasized = true)
            RouteArrow()
            RouteStop("CONSUMERS", "APP")
        }
        Spacer(Modifier.height(HuaweiSyncSpacing.lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TechnicalMicrocopy("SOURCE BOUNDARY")
            TechnicalMicrocopy(state.healthConnect.status.label.uppercase())
        }
    }
}

@Composable
private fun RouteStop(label: String, mark: String, emphasized: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm),
        modifier = Modifier.widthIn(min = 72.dp),
    ) {
        Box(
            modifier = Modifier
                .size(if (emphasized) 56.dp else 44.dp)
                .background(if (emphasized) HuaweiSyncTheme.colors.accentContainer else HuaweiSyncTheme.colors.surface1)
                .border(
                    HuaweiSyncGeometry.borderStrong,
                    if (emphasized) HuaweiSyncTheme.colors.accent else HuaweiSyncTheme.colors.lineStrong,
                    RectangleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = mark,
                color = if (emphasized) Color.White else HuaweiSyncTheme.colors.ink,
                style = HuaweiSyncTheme.technicalTypography.label,
            )
        }
        Text(
            text = label,
            color = HuaweiSyncTheme.colors.ink2,
            style = HuaweiSyncTheme.technicalTypography.microcopy,
            maxLines = 1,
        )
    }
}

@Composable
private fun RouteArrow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            Modifier
                .width(HuaweiSyncSpacing.sm)
                .height(HuaweiSyncGeometry.borderThin)
                .background(HuaweiSyncTheme.colors.lineStrong),
        )
        Icon(
            imageVector = Icons.Rounded.ArrowForward,
            contentDescription = null,
            tint = HuaweiSyncTheme.colors.ink2,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun IntegrationCard(
    item: IntegrationItemState,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    compact: Boolean = false,
) {
    val accent = statusColor(item.status)
    ModernistSurface(
        modifier = modifier.semantics {
            contentDescription = "${item.provider.displayName}. ${item.status.label}. ${item.detail}"
        },
        backgroundColor = if (featured) HuaweiSyncTheme.colors.surface2 else HuaweiSyncTheme.colors.surface1,
        borderColor = if (featured) accent else HuaweiSyncTheme.colors.line,
        contentPadding = if (compact) HuaweiSyncSpacing.md else HuaweiSyncSpacing.lg,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            ProviderMark(item.provider, accent, compact)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm),
            ) {
                Text(
                    text = item.provider.displayName,
                    color = HuaweiSyncTheme.colors.ink,
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IntegrationStatusChip(item.status)
                Text(
                    text = item.detail,
                    color = HuaweiSyncTheme.colors.ink2,
                    style = MaterialTheme.typography.bodySmall,
                )
                TechnicalMicrocopy(evidenceLabel(item.evidenceLevel))
            }
        }
    }
}

@Composable
private fun ProviderMark(provider: IntegrationProvider, accent: Color, compact: Boolean) {
    Box(
        modifier = Modifier
            .size(if (compact) 42.dp else 52.dp)
            .clip(RectangleShape)
            .background(accent.copy(alpha = 0.12f))
            .border(HuaweiSyncGeometry.borderStrong, accent, RectangleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = provider.monogram,
            color = accent,
            style = HuaweiSyncTheme.technicalTypography.label,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun IntegrationStatusChip(status: IntegrationStatus) {
    val color = statusColor(status)
    Text(
        text = status.label.uppercase(),
        modifier = Modifier
            .border(HuaweiSyncGeometry.borderThin, color, RectangleShape)
            .padding(horizontal = HuaweiSyncSpacing.sm, vertical = HuaweiSyncSpacing.xs)
            .semantics { stateDescription = status.label },
        color = color,
        style = HuaweiSyncTheme.technicalTypography.microcopy,
    )
}

@Composable
private fun statusColor(status: IntegrationStatus): Color = when (status) {
    IntegrationStatus.CONFIRMED_IN_HEALTH_CONNECT,
    IntegrationStatus.AVAILABLE_THROUGH_HEALTH_CONNECT,
    -> HuaweiSyncTheme.colors.ok
    IntegrationStatus.READY_FOR_GYMRATS_TO_READ,
    IntegrationStatus.READY_TO_WRITE,
    IntegrationStatus.WRITE_IN_PROGRESS,
    IntegrationStatus.AWAITING_READBACK,
    -> HuaweiSyncTheme.colors.info
    IntegrationStatus.PREVIEW,
    IntegrationStatus.COMING_SOON,
    IntegrationStatus.NOT_CONFIGURED,
    -> HuaweiSyncTheme.colors.ink2
    IntegrationStatus.ACTION_REQUIRED,
    IntegrationStatus.RECONCILIATION_REQUIRED,
    IntegrationStatus.RETRY_REQUIRED,
    IntegrationStatus.UNAVAILABLE,
    -> HuaweiSyncTheme.colors.warning
}

private fun evidenceLabel(level: IntegrationEvidenceLevel): String = when (level) {
    IntegrationEvidenceLevel.READBACK -> "EVIDENCE / OFFICIAL READBACK"
    IntegrationEvidenceLevel.CONFIGURATION -> "EVIDENCE / DEVICE CONFIGURATION"
    IntegrationEvidenceLevel.LOCAL_STATE -> "EVIDENCE / LOCAL STATE"
    IntegrationEvidenceLevel.ROADMAP_ONLY -> "EVIDENCE / ROADMAP ONLY"
}

@Preview(name = "Integrations Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Integrations Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun IntegrationsScreenPreview() {
    HuaweiSyncTheme(darkTheme = true) {
        IntegrationsScreen(IntegrationState.from(productState = null))
    }
}
