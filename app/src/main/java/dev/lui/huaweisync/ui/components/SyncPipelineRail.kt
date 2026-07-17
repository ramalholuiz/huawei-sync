package dev.lui.huaweisync.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

/** Sync pipeline visual states. Motion, color, and shape all key off this. */
enum class SyncRailState {
    WAITING,
    ACTIVE_WRITE,
    ACTIVE_VERIFY,
    CONFIRMED,
    ATTENTION,
    PENDING,
}

/** Direction of the ticker motion on the active segment. */
enum class SyncRailFlow { NONE, FORWARD, REVERSE }

@Immutable
data class SyncRailNode(
    val id: String,
    val monogram: String,
    val label: String,
    val state: SyncRailState,
    val supportingText: String? = null,
    /** True triggers the confirmed one-shot exactly once per state entry. */
    val confirmedAcknowledgement: Boolean = false,
)

@Immutable
data class SyncPipelineRailModel(
    val nodes: List<SyncRailNode>,
    /** Index of the node the active flow terminates at, or null when nothing is active. */
    val activeNodeIndex: Int?,
    val activeFlow: SyncRailFlow,
) {
    init {
        require(nodes.size >= 2) { "Rail needs at least two nodes." }
        require(activeNodeIndex == null || activeNodeIndex in nodes.indices) {
            "activeNodeIndex must be a valid node index."
        }
    }
}

enum class SyncRailOrientation { HORIZONTAL, VERTICAL }

/**
 * Four-endpoint rail used across Dashboard, Sync-now modal, and Pipeline.
 * Motion is bounded by [HuaweiSyncMotion.current] — the reduced-motion branch draws no
 * ticker, no scale, no halo. State encoding remains legible without motion.
 */
@Composable
fun SyncPipelineRail(
    model: SyncPipelineRailModel,
    modifier: Modifier = Modifier,
    orientation: SyncRailOrientation = SyncRailOrientation.HORIZONTAL,
    nodeSize: Dp = 40.dp,
    showSupportingText: Boolean = true,
) {
    val policy = HuaweiSyncMotion.current
    when (orientation) {
        SyncRailOrientation.HORIZONTAL -> HorizontalRail(model, modifier, nodeSize, policy, showSupportingText)
        SyncRailOrientation.VERTICAL -> VerticalRail(model, modifier, nodeSize, policy, showSupportingText)
    }
}

@Composable
private fun HorizontalRail(
    model: SyncPipelineRailModel,
    modifier: Modifier,
    nodeSize: Dp,
    policy: HuaweiSyncMotionPolicy,
    showSupportingText: Boolean,
) {
    Column(modifier = modifier.testTag("sync-rail")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            model.nodes.forEachIndexed { index, node ->
                RailNode(
                    node = node,
                    size = nodeSize,
                    policy = policy,
                    modifier = Modifier.testTag("sync-rail-node-${node.id}"),
                )
                if (index < model.nodes.lastIndex) {
                    val active = model.activeNodeIndex != null &&
                        segmentIsActive(index, model.activeNodeIndex, model.activeFlow)
                    RailSegment(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = HuaweiSyncSpacing.xs)
                            .testTag("sync-rail-segment-$index"),
                        orientation = SyncRailOrientation.HORIZONTAL,
                        active = active,
                        flow = if (active) model.activeFlow else SyncRailFlow.NONE,
                        tone = segmentTone(model, index),
                        policy = policy,
                    )
                }
            }
        }
        Spacer(Modifier.height(HuaweiSyncSpacing.xs))
        Row(modifier = Modifier.fillMaxWidth()) {
            model.nodes.forEach { node ->
                RailLabel(
                    node = node,
                    modifier = Modifier.weight(1f),
                    showSupportingText = showSupportingText,
                )
            }
        }
    }
}

@Composable
private fun VerticalRail(
    model: SyncPipelineRailModel,
    modifier: Modifier,
    nodeSize: Dp,
    policy: HuaweiSyncMotionPolicy,
    showSupportingText: Boolean,
) {
    Row(modifier = modifier.testTag("sync-rail")) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            model.nodes.forEachIndexed { index, node ->
                RailNode(
                    node = node,
                    size = nodeSize,
                    policy = policy,
                    modifier = Modifier.testTag("sync-rail-node-${node.id}"),
                )
                if (index < model.nodes.lastIndex) {
                    val active = model.activeNodeIndex != null &&
                        segmentIsActive(index, model.activeNodeIndex, model.activeFlow)
                    RailSegment(
                        modifier = Modifier
                            .height(HuaweiSyncSpacing.md)
                            .padding(vertical = HuaweiSyncSpacing.xs)
                            .testTag("sync-rail-segment-$index"),
                        orientation = SyncRailOrientation.VERTICAL,
                        active = active,
                        flow = if (active) model.activeFlow else SyncRailFlow.NONE,
                        tone = segmentTone(model, index),
                        policy = policy,
                    )
                }
            }
        }
        Spacer(Modifier.width(HuaweiSyncSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.xs)) {
            model.nodes.forEachIndexed { index, node ->
                RailLabel(node = node, modifier = Modifier, showSupportingText = showSupportingText)
                if (index < model.nodes.lastIndex) {
                    Spacer(Modifier.height(HuaweiSyncSpacing.md))
                }
            }
        }
    }
}

@Composable
private fun RailNode(
    node: SyncRailNode,
    size: Dp,
    policy: HuaweiSyncMotionPolicy,
    modifier: Modifier = Modifier,
) {
    val tone = nodeTone(node.state)
    val scale = remember { Animatable(1f) }
    val halo = remember { Animatable(0f) }
    LaunchedEffect(node.state, node.confirmedAcknowledgement, node.id, policy.reducedMotion) {
        if (!policy.reducedMotion && node.confirmedAcknowledgement) {
            val duration = policy.deliberateMillis / 2
            scale.snapTo(1f)
            halo.snapTo(0f)
            scale.animateTo(1.08f, tween(duration, easing = FastOutSlowInEasing))
            halo.animateTo(0.8f, tween(duration, easing = LinearEasing))
            scale.animateTo(1f, tween(duration, easing = FastOutSlowInEasing))
            halo.animateTo(0f, tween(duration, easing = LinearEasing))
        } else {
            scale.snapTo(1f)
            halo.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = node.state.spokenState(node) },
        contentAlignment = Alignment.Center,
    ) {
        if (halo.value > 0f) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val inset = (-6).dp.toPx()
                val stroke = HuaweiSyncGeometry.borderThin.toPx()
                drawRect(
                    color = tone.copy(alpha = halo.value),
                    topLeft = Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(
                        this.size.width - 2 * inset,
                        this.size.height - 2 * inset,
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                .background(HuaweiSyncTheme.colors.surface1)
                .border(HuaweiSyncGeometry.borderThin, tone, RectangleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = node.monogram,
                color = HuaweiSyncTheme.colors.ink,
                style = HuaweiSyncTheme.technicalTypography.label,
            )
        }
    }
}

@Composable
private fun RailLabel(node: SyncRailNode, modifier: Modifier, showSupportingText: Boolean) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = node.label,
            color = HuaweiSyncTheme.colors.ink2,
            style = HuaweiSyncTheme.technicalTypography.label,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        if (showSupportingText) {
            node.supportingText?.let { support ->
                Text(
                    text = support,
                    color = HuaweiSyncTheme.colors.ink3,
                    style = HuaweiSyncTheme.technicalTypography.microcopy,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun RailSegment(
    modifier: Modifier,
    orientation: SyncRailOrientation,
    active: Boolean,
    flow: SyncRailFlow,
    tone: Color,
    policy: HuaweiSyncMotionPolicy,
) {
    val ruleThickness = 2.dp
    val tickerDot = 12.dp

    if (policy.reducedMotion || !active || flow == SyncRailFlow.NONE) {
        Box(
            modifier = modifier.then(
                when (orientation) {
                    SyncRailOrientation.HORIZONTAL -> Modifier.height(ruleThickness)
                    SyncRailOrientation.VERTICAL -> Modifier.width(ruleThickness)
                },
            ).background(tone.copy(alpha = if (active) 1f else 0.5f)),
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "sync-rail-ticker")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(policy.syncRotationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sync-rail-ticker-progress",
    )
    val dotAlpha = if (flow == SyncRailFlow.REVERSE) 0.55f else 1f

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val horizontal = orientation == SyncRailOrientation.HORIZONTAL
            val width = size.width
            val height = size.height
            if (horizontal) {
                val cy = height / 2f
                drawLine(
                    color = tone.copy(alpha = 0.5f),
                    start = Offset(0f, cy),
                    end = Offset(width, cy),
                    strokeWidth = ruleThickness.toPx(),
                )
                val direction: Float = if (flow == SyncRailFlow.FORWARD) progress else 1f - progress
                val x = direction * width
                drawCircle(
                    color = tone.copy(alpha = dotAlpha),
                    radius = tickerDot.toPx() / 2f,
                    center = Offset(x, cy),
                )
            } else {
                val cx = width / 2f
                drawLine(
                    color = tone.copy(alpha = 0.5f),
                    start = Offset(cx, 0f),
                    end = Offset(cx, height),
                    strokeWidth = ruleThickness.toPx(),
                )
                val direction: Float = if (flow == SyncRailFlow.FORWARD) progress else 1f - progress
                val y = direction * height
                drawCircle(
                    color = tone.copy(alpha = dotAlpha),
                    radius = tickerDot.toPx() / 2f,
                    center = Offset(cx, y),
                )
            }
        }
    }
}

private fun segmentIsActive(
    segmentIndex: Int,
    activeNodeIndex: Int,
    flow: SyncRailFlow,
): Boolean = when (flow) {
    SyncRailFlow.FORWARD, SyncRailFlow.REVERSE -> segmentIndex == activeNodeIndex - 1
    SyncRailFlow.NONE -> false
}

@Composable
private fun segmentTone(model: SyncPipelineRailModel, segmentIndex: Int): Color {
    val leftState = model.nodes[segmentIndex].state
    val rightState = model.nodes[segmentIndex + 1].state
    return when {
        rightState == SyncRailState.CONFIRMED -> HuaweiSyncTheme.colors.ok
        leftState == SyncRailState.CONFIRMED && rightState == SyncRailState.PENDING ->
            HuaweiSyncTheme.colors.ok
        rightState == SyncRailState.ACTIVE_WRITE -> HuaweiSyncTheme.colors.accentForeground
        rightState == SyncRailState.ACTIVE_VERIFY -> HuaweiSyncTheme.colors.info
        rightState == SyncRailState.ATTENTION -> HuaweiSyncTheme.colors.warning
        else -> HuaweiSyncTheme.colors.lineStrong
    }
}

@Composable
private fun nodeTone(state: SyncRailState): Color = when (state) {
    SyncRailState.WAITING, SyncRailState.PENDING -> HuaweiSyncTheme.colors.lineStrong
    SyncRailState.ACTIVE_WRITE -> HuaweiSyncTheme.colors.accentForeground
    SyncRailState.ACTIVE_VERIFY -> HuaweiSyncTheme.colors.info
    SyncRailState.CONFIRMED -> HuaweiSyncTheme.colors.ok
    SyncRailState.ATTENTION -> HuaweiSyncTheme.colors.warning
}

private fun SyncRailState.spokenState(node: SyncRailNode): String = when (this) {
    SyncRailState.WAITING -> "${node.label}, waiting"
    SyncRailState.PENDING -> "${node.label}, pending"
    SyncRailState.ACTIVE_WRITE -> "${node.label}, writing"
    SyncRailState.ACTIVE_VERIFY -> "${node.label}, verifying"
    SyncRailState.CONFIRMED -> "${node.label}, confirmed"
    SyncRailState.ATTENTION -> "${node.label}, attention required"
}
