package dev.lui.huaweisync.ui.screens.sync

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.lui.huaweisync.ui.components.HuaweiSyncMotion
import dev.lui.huaweisync.ui.components.HuaweiSyncMotionProvider
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.screens.pipeline.EvidenceTransition
import dev.lui.huaweisync.ui.screens.pipeline.PipelinePresentation
import dev.lui.huaweisync.ui.screens.pipeline.PipelineStepState
import dev.lui.huaweisync.ui.screens.pipeline.previewPipelineState
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

@Composable
fun SyncNowModal(
    state: ProductSyncState?,
    coordinatorBusy: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    surfaceLabel: String? = null,
) {
    val motion = HuaweiSyncMotion.current
    val presentation = state?.let { PipelinePresentation.from(it, coordinatorBusy) }

    Dialog(onDismissRequest = onDismiss) {
        ModernistSurface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("sync-now-overlay")
                .semantics { paneTitle = "Sync now status" },
            backgroundColor = HuaweiSyncTheme.colors.surface1,
        ) {
            SectionHeader(
                title = "Sync now",
                eyebrow = surfaceLabel ?: if (coordinatorBusy) "SYNC IN PROGRESS" else "SYNC STATUS",
            )
            PhaseSignal(
                active = presentation?.motion?.active == true,
                confirmed = state?.verification?.realReadbackConfirmed == true,
            )
            Spacer(Modifier.height(HuaweiSyncSpacing.md))
            EvidenceTransition(
                value = presentation?.statusLabel ?: "Loading coordinator evidence",
                motion = motion,
            ) { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag("sync-modal-status"),
                )
            }
            Text(
                text = modalExplanation(presentation, coordinatorBusy),
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("sync-modal-explanation"),
            )
            Spacer(Modifier.height(HuaweiSyncSpacing.md))
            EndpointStrip(
                healthConnectLabel = presentation?.healthConnectLabel ?: "Evidence unavailable",
                destinationLabel = presentation?.destinationLabel ?: "Ready state unavailable",
            )
            presentation?.let {
                Spacer(Modifier.height(HuaweiSyncSpacing.lg))
                TechnicalMicrocopy("PHASE CHECKS · NO ESTIMATED PERCENT OR TIME")
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("sync-modal-phase-checks"),
                    horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs),
                ) {
                    it.steps.filterNot { step -> step.step.name == "RECONCILIATION" }.forEach { step ->
                        val color = when (step.state) {
                            PipelineStepState.COMPLETE -> HuaweiSyncTheme.colors.ok
                            PipelineStepState.ACTIVE -> HuaweiSyncTheme.colors.accent
                            PipelineStepState.NEEDS_ATTENTION -> HuaweiSyncTheme.colors.warning
                            PipelineStepState.PENDING -> HuaweiSyncTheme.colors.lineStrong
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(color)
                                .testTag("sync-check-${step.step.name.lowercase()}"),
                        )
                    }
                }
            }
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
            StraightEdgeButton(
                label = if (coordinatorBusy) "Hide sync status" else "Close sync status",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PhaseSignal(active: Boolean, confirmed: Boolean) {
    val motion = HuaweiSyncMotion.current
    val rotation = if (active && !motion.reducedMotion) {
        val transition = rememberInfiniteTransition(label = "sync-phase-signal")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                tween(motion.syncRotationMillis, easing = LinearEasing),
            ),
            label = "sync-phase-rotation",
        )
        value
    } else {
        0f
    }
    val color = when {
        confirmed -> HuaweiSyncTheme.colors.ok
        active -> HuaweiSyncTheme.colors.accentForeground
        else -> HuaweiSyncTheme.colors.ink2
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.lineStrong)
            .background(HuaweiSyncTheme.colors.surface2),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(HuaweiSyncGeometry.borderThin, color)
                .graphicsLayer(rotationZ = rotation),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (confirmed) Icons.Rounded.Check else Icons.Rounded.Sync,
                contentDescription = when {
                    confirmed -> "Readback confirmed"
                    active && motion.reducedMotion -> "Coordinator phase active, static reduced-motion indicator"
                    active -> "Coordinator phase active"
                    else -> "No active coordinator phase observed"
                },
                tint = color,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun EndpointStrip(healthConnectLabel: String, destinationLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm),
    ) {
        Endpoint("G1", "Synthetic source", Modifier.weight(1f))
        Endpoint("HC", healthConnectLabel, Modifier.weight(1f))
        Endpoint("G", destinationLabel, Modifier.weight(1f))
    }
}

@Composable
private fun Endpoint(code: String, detail: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line)
            .padding(HuaweiSyncSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs),
    ) {
        Text(code, style = MaterialTheme.typography.titleMedium)
        Text(
            text = detail,
            color = HuaweiSyncTheme.colors.ink2,
            style = HuaweiSyncTheme.technicalTypography.microcopy,
            maxLines = 3,
        )
    }
}

private fun modalExplanation(
    presentation: PipelinePresentation?,
    coordinatorBusy: Boolean,
): String = when {
    presentation == null ->
        "No product or coordinator phase evidence is available yet."
    presentation.awaitingCoordinatorEvidence ->
        "The request is running, but the coordinator has not exposed a newer phase. No progress has been inferred from elapsed time."
    presentation.motion.active ->
        "The active indicator reflects the latest observed coordinator phase. Checks appear only after later evidence proves the transition."
    coordinatorBusy ->
        "The coordinator is running. The status below remains at the latest observed evidence until a newer result is available."
    else ->
        "This is the latest persisted sync outcome. The workout is available for GymRats to import; delivery has not been claimed."
}

@Preview(name = "Sync modal", showBackground = true, widthDp = 412, heightDp = 760)
@Preview(
    name = "Sync modal dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 760,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun SyncNowModalPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = false) {
            SyncNowModal(
                state = previewPipelineState(),
                coordinatorBusy = true,
                onDismiss = {},
                surfaceLabel = "PREVIEW · EVIDENCE SAMPLE",
            )
        }
    }
}

@Preview(name = "Sync modal reduced motion", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun SyncNowModalReducedMotionPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = true) {
            SyncNowModal(
                state = previewPipelineState(),
                coordinatorBusy = true,
                onDismiss = {},
                surfaceLabel = "PREVIEW · REDUCED MOTION",
            )
        }
    }
}
