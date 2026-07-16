package dev.lui.huaweisync.ui.screens.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.ModernistStatus
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.StatusLabel
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

@Immutable
data class OnboardingScreenState(
    val healthConnectStatus: ProductHealthConnectStatus? = null,
)

@Composable
fun OnboardingScreen(
    state: OnboardingScreenState,
    darkTheme: Boolean,
    onStartSetup: () -> Unit,
    onContinueExistingSetup: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HuaweiSyncSpacing.xl, vertical = HuaweiSyncSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xl),
    ) {
        OnboardingHeader(darkTheme = darkTheme, onToggleTheme = onToggleTheme)
        ConnectionOrbit()
        Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md)) {
            TechnicalMicrocopy("FOR HUAWEI WATCH OWNERS")
            Text(
                text = "Your workouts, available through Health Connect.",
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = "Move workout sessions from Huawei Health into Android Health Connect. Apps you authorize can then read them from Health Connect.",
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        AvailabilityStrip(status = state.healthConnectStatus)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
        ) {
            StraightEdgeButton(
                label = "Start setup",
                onClick = onStartSetup,
                modifier = Modifier.fillMaxWidth(),
                accent = true,
                leadingIcon = Icons.Rounded.ArrowForward,
            )
            StraightEdgeButton(
                label = "Continue existing setup",
                onClick = onContinueExistingSetup,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        TechnicalMicrocopy(
            text = "Huawei Sync writes only the records you explicitly sync. Health Connect permissions remain under Android control.",
        )
        Spacer(Modifier.height(HuaweiSyncSpacing.lg))
    }
}

@Composable
private fun OnboardingHeader(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(HuaweiSyncTheme.colors.accentContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("H", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text("Huawei Sync", color = HuaweiSyncTheme.colors.ink, style = MaterialTheme.typography.titleMedium)
                TechnicalMicrocopy("STEP 01 / 04")
            }
        }
        StraightEdgeButton(
            label = if (darkTheme) "Light" else "Dark",
            onClick = onToggleTheme,
            modifier = Modifier.semantics { contentDescription = "Switch theme" },
        )
    }
}

@Composable
private fun ConnectionOrbit() {
    val lineColor = HuaweiSyncTheme.colors.lineStrong
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .semantics { contentDescription = "Huawei Health connects to Health Connect" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = lineColor, radius = size.minDimension * .42f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            drawCircle(color = lineColor, radius = size.minDimension * .28f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            drawLine(lineColor, Offset(size.width * .26f, center.y), Offset(size.width * .74f, center.y), strokeWidth = 1.dp.toPx())
        }
        OrbitNode("H", Modifier.align(Alignment.CenterStart))
        OrbitNode("HC", Modifier.align(Alignment.Center))
        OrbitNode("APPS", Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun OrbitNode(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(if (label == "APPS") 64.dp else 56.dp)
            .clip(CircleShape)
            .background(if (label == "HC") HuaweiSyncTheme.colors.accentContainer else HuaweiSyncTheme.colors.surface2)
            .border(HuaweiSyncGeometry.borderStrong, HuaweiSyncTheme.colors.lineStrong, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (label == "HC") Color.White else HuaweiSyncTheme.colors.ink,
            textAlign = TextAlign.Center,
            style = HuaweiSyncTheme.technicalTypography.label,
        )
    }
}

@Composable
private fun AvailabilityStrip(status: ProductHealthConnectStatus?) {
    val displayStatus = when (status) {
        null -> ModernistStatus.Syncing
        ProductHealthConnectStatus.READY_TO_SYNC,
        ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
        -> ModernistStatus.Ready
        ProductHealthConnectStatus.FAILED -> ModernistStatus.Error
        else -> ModernistStatus.Warning
    }
    ModernistSurface(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = HuaweiSyncTheme.colors.surface2,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusLabel(displayStatus)
            Column(modifier = Modifier.weight(1f)) {
                TechnicalMicrocopy("HEALTH CONNECT")
                Text(
                    text = status?.label ?: "Checking Health Connect",
                    color = HuaweiSyncTheme.colors.ink,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(name = "Onboarding loading", showBackground = true)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun OnboardingLoadingPreview() {
    HuaweiSyncTheme(darkTheme = true) {
        OnboardingScreen(OnboardingScreenState(), true, {}, {}, {})
    }
}

@Preview(name = "Onboarding blocked", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun OnboardingBlockedPreview() {
    HuaweiSyncTheme(darkTheme = false) {
        OnboardingScreen(
            state = OnboardingScreenState(ProductHealthConnectStatus.PERMISSION_REQUIRED),
            darkTheme = false,
            onStartSetup = {},
            onContinueExistingSetup = {},
            onToggleTheme = {},
        )
    }
}
