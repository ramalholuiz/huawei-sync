package dev.lui.huaweisync.ui.screens.pipeline

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.HuaweiSyncMotion
import dev.lui.huaweisync.ui.components.HuaweiSyncMotionProvider
import dev.lui.huaweisync.ui.components.HuaweiSyncMotionPolicy
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductVerificationEvidence
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

@Composable
fun PipelineScreen(
    state: ProductSyncState,
    coordinatorBusy: Boolean,
    modifier: Modifier = Modifier,
    surfaceLabel: String = "LIVE · READ-ONLY",
) {
    val presentation = PipelinePresentation.from(state, coordinatorBusy)
    val motion = HuaweiSyncMotion.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.background)
            .testTag("pipeline-screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(HuaweiSyncSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
    ) {
        item {
            SectionHeader(title = "Live sync pipeline", eyebrow = surfaceLabel)
        }
        item {
            PipelineEndpointCard(
                eyebrow = "SOURCE",
                title = "Gate 1 synthetic workout",
                detail = "Huawei Health import is not connected yet.",
            )
        }
        item {
            PipelinePhaseCard(presentation = presentation, motion = motion)
        }
        item {
            PipelineEndpointCard(
                eyebrow = "ANDROID HUB",
                title = "Health Connect",
                detail = presentation.healthConnectLabel,
            )
        }
        item {
            PipelineEndpointCard(
                eyebrow = "DESTINATION CLAIM",
                title = "GymRats",
                detail = presentation.destinationLabel,
            )
        }
        item {
            ModernistSurface(modifier = Modifier.fillMaxWidth()) {
                TechnicalMicrocopy("ACTUAL LEDGER EVIDENCE")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    EvidenceCount("LEDGER WORKOUTS", presentation.ledgerWorkoutCount)
                    EvidenceCount("WRITE ATTEMPTS", presentation.writeAttemptCount)
                }
                presentation.safeFailureSummary?.let { summary ->
                    Text(
                        text = summary,
                        color = HuaweiSyncTheme.colors.warning,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("pipeline-failure-summary"),
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelineEndpointCard(eyebrow: String, title: String, detail: String) {
    ModernistSurface(modifier = Modifier.fillMaxWidth()) {
        TechnicalMicrocopy(eyebrow)
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(detail, color = HuaweiSyncTheme.colors.ink2, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PipelinePhaseCard(
    presentation: PipelinePresentation,
    motion: HuaweiSyncMotionPolicy,
) {
    ModernistSurface(modifier = Modifier.fillMaxWidth().testTag("pipeline-phases")) {
        TechnicalMicrocopy("COORDINATOR PHASE EVIDENCE")
        EvidenceTransition(
            value = presentation.statusLabel,
            motion = motion,
        ) { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("pipeline-status"),
            )
        }
        if (presentation.awaitingCoordinatorEvidence) {
            Text(
                "The coordinator is running; no newer phase has been observed yet.",
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("pipeline-awaiting-evidence"),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm)) {
            presentation.steps.forEachIndexed { index, step ->
                PipelineStepRow(step = step, motion = motion)
                if (index < presentation.steps.lastIndex) {
                    Box(
                        Modifier
                            .padding(start = 15.dp)
                            .size(width = HuaweiSyncGeometry.borderThin, height = HuaweiSyncSpacing.md)
                            .background(HuaweiSyncTheme.colors.lineStrong),
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelineStepRow(
    step: PipelineStepPresentation,
    motion: HuaweiSyncMotionPolicy,
) {
    val tone = when (step.state) {
        PipelineStepState.COMPLETE -> HuaweiSyncTheme.colors.ok
        PipelineStepState.ACTIVE -> HuaweiSyncTheme.colors.accentForeground
        PipelineStepState.NEEDS_ATTENTION -> HuaweiSyncTheme.colors.warning
        PipelineStepState.PENDING -> HuaweiSyncTheme.colors.ink2
    }
    Row(
        modifier = Modifier.fillMaxWidth().testTag("pipeline-step-${step.step.name.lowercase()}"),
        horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PipelineStepGlyph(step.state, tone, motion)
        Column(modifier = Modifier.weight(1f)) {
            Text(step.label, style = MaterialTheme.typography.titleSmall)
            Text(step.detail, color = HuaweiSyncTheme.colors.ink2, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = step.state.name.replace('_', ' '),
            color = tone,
            style = HuaweiSyncTheme.technicalTypography.microcopy,
        )
    }
}

@Composable
private fun PipelineStepGlyph(
    state: PipelineStepState,
    tone: Color,
    motion: HuaweiSyncMotionPolicy,
) {
    val rotation = if (state == PipelineStepState.ACTIVE && !motion.reducedMotion) {
        val transition = rememberInfiniteTransition(label = "pipeline-active")
        val degrees by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(motion.syncRotationMillis, easing = LinearEasing),
            ),
            label = "pipeline-active-rotation",
        )
        degrees
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .border(HuaweiSyncGeometry.borderThin, tone)
            .drawBehind { drawRect(tone.copy(alpha = 0.12f)) },
        contentAlignment = Alignment.Center,
    ) {
        EvidenceTransition(value = state, motion = motion) { visibleState ->
            val image = when (visibleState) {
                PipelineStepState.COMPLETE -> Icons.Rounded.Check
                PipelineStepState.ACTIVE -> Icons.Rounded.Sync
                PipelineStepState.NEEDS_ATTENTION -> Icons.Rounded.ErrorOutline
                PipelineStepState.PENDING -> Icons.Rounded.MoreHoriz
            }
            Icon(
                imageVector = image,
                contentDescription = visibleState.name.replace('_', ' ').lowercase(),
                tint = tone,
                modifier = Modifier.size(18.dp).graphicsLayer(rotationZ = rotation),
            )
        }
    }
}

@Composable
private fun EvidenceCount(label: String, count: Int) {
    Column {
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
        TechnicalMicrocopy(label)
    }
}

@Composable
internal fun <T> EvidenceTransition(
    value: T,
    motion: HuaweiSyncMotionPolicy,
    content: @Composable (T) -> Unit,
) {
    if (motion.reducedMotion) {
        content(value)
    } else {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(tween(motion.standardMillis)) togetherWith
                    fadeOut(tween(motion.fastMillis))
            },
            label = "evidence-transition",
        ) { target ->
            content(target)
        }
    }
}

@Preview(name = "Pipeline evidence", showBackground = true, widthDp = 412, heightDp = 860)
@Preview(
    name = "Pipeline evidence dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 860,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun PipelineScreenPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = false) {
            PipelineScreen(
                state = previewPipelineState(),
                coordinatorBusy = true,
                surfaceLabel = "PREVIEW · EVIDENCE SAMPLE",
            )
        }
    }
}

@Preview(name = "Pipeline reduced motion", showBackground = true, widthDp = 412, heightDp = 860)
@Composable
private fun PipelineReducedMotionPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = true) {
            PipelineScreen(
                state = previewPipelineState(),
                coordinatorBusy = true,
                surfaceLabel = "PREVIEW · REDUCED MOTION",
            )
        }
    }
}

internal fun previewPipelineState() = ProductSyncState(
    healthConnectStatus = ProductHealthConnectStatus.WRITE_IN_PROGRESS,
    gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
    phase = ProductSyncPhase.WRITE,
    attemptCount = 1,
    ledgerWorkoutCount = 1,
    verification = ProductVerificationEvidence(
        diagnosticEvidence = null,
        realReadbackConfirmed = false,
        healthConnectMatchCount = null,
        expectedVersionMatchCount = null,
        versionMatch = null,
    ),
    sanitizedFailureSummary = null,
)
