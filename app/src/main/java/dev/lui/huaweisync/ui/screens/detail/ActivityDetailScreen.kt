package dev.lui.huaweisync.ui.screens.detail

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.HuaweiSyncMotion
import dev.lui.huaweisync.ui.components.HuaweiSyncMotionProvider
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.SyncPipelineRail
import dev.lui.huaweisync.ui.components.SyncPipelineRailModel
import dev.lui.huaweisync.ui.components.SyncRailFlow
import dev.lui.huaweisync.ui.components.SyncRailNode
import dev.lui.huaweisync.ui.components.SyncRailOrientation
import dev.lui.huaweisync.ui.components.SyncRailState
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.screens.history.readbackAccent
import dev.lui.huaweisync.ui.screens.history.readbackLabel
import dev.lui.huaweisync.ui.state.ActivityHistoryItem
import dev.lui.huaweisync.ui.state.ActivityReadbackState
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DetailTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)

@Composable
fun ActivityDetailScreen(
    activity: ActivityHistoryItem?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(HuaweiSyncTheme.colors.background)) {
        DetailTopBar(activity = activity, onBack = onBack)
        if (activity == null) {
            MissingActivity(onBack)
        } else {
            ActivityBody(activity)
        }
    }
}

@Composable
private fun DetailTopBar(activity: ActivityHistoryItem?, onBack: () -> Unit) {
    val label = activity
        ?.let { "Back to history from ${it.sourceProvider.uppercase()} activity" }
        ?: "Back to history"
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = HuaweiSyncSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("activity-detail-back")) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = label,
                tint = HuaweiSyncTheme.colors.ink,
            )
        }
        TechnicalMicrocopy("ACTIVITY RECORD")
    }
}

@Composable
private fun ActivityBody(activity: ActivityHistoryItem) {
    val motion = HuaweiSyncMotion.current
    val visibleState = remember(activity.clientRecordId) {
        MutableTransitionState(initialState = motion.reducedMotion)
    }
    LaunchedEffect(activity.clientRecordId, motion.reducedMotion) {
        visibleState.targetState = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = HuaweiSyncSpacing.xl,
                end = HuaweiSyncSpacing.xl,
                bottom = HuaweiSyncSpacing.xxl,
            )
            .testTag("activity-detail-${activity.clientRecordId}"),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = if (motion.reducedMotion) {
                fadeIn(tween(0))
            } else {
                fadeIn(tween(motion.standardMillis)) +
                    slideInVertically(tween(motion.standardMillis)) { height -> height / 4 }
            },
        ) {
            IdentityBand(activity)
        }

        SectionLabel("SYNC PATH")
        ModernistSurface(Modifier.fillMaxWidth()) {
            SyncPipelineRail(
                model = detailRailModel(activity),
                orientation = SyncRailOrientation.VERTICAL,
                nodeSize = 32.dp,
                showSupportingText = true,
            )
        }

        SectionLabel("LIFECYCLE")
        ModernistSurface(Modifier.fillMaxWidth()) {
            ActivityLifecycleTimeline(
                activity = activity,
                reducedMotion = motion.reducedMotion,
                standardMillis = motion.standardMillis,
                fastMillis = motion.fastMillis,
            )
        }

        TechnicalMicrocopy(
            "THIS VIEW REPORTS LEDGER AND READBACK FACTS ONLY. IT DOES NOT CLAIM DELIVERY TO ANOTHER APP.",
        )

        TechnicalDetailToggle(activity)
    }
}

@Composable
private fun IdentityBand(activity: ActivityHistoryItem) {
    val accent = readbackAccent(activity.readbackState)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HuaweiSyncTheme.colors.surface1)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line, RectangleShape)
            .testTag("activity-detail-identity"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(64.dp)
                .background(accent),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(HuaweiSyncSpacing.lg),
        ) {
            TechnicalMicrocopy(activity.sourceProvider.uppercase())
            Text(
                text = "Activity record",
                style = HuaweiSyncTheme.technicalTypography.value,
                color = HuaweiSyncTheme.colors.ink,
                fontWeight = FontWeight.Bold,
            )
        }
        ReadbackPill(activity.readbackState)
        Spacer(Modifier.width(HuaweiSyncSpacing.lg))
    }
}

@Composable
private fun ReadbackPill(state: ActivityReadbackState) {
    val color = readbackAccent(state)
    val label = readbackLabel(state).uppercase()
    Text(
        text = label,
        modifier = Modifier
            .background(color.copy(alpha = 0.14f))
            .border(HuaweiSyncGeometry.borderThin, color, RectangleShape)
            .padding(horizontal = HuaweiSyncSpacing.sm, vertical = HuaweiSyncSpacing.xs)
            .semantics { stateDescription = readbackLabel(state) },
        style = HuaweiSyncTheme.technicalTypography.label,
        color = color,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SectionLabel(label: String) {
    TechnicalMicrocopy(label)
}

@Composable
private fun ActivityLifecycleTimeline(
    activity: ActivityHistoryItem,
    reducedMotion: Boolean,
    standardMillis: Int,
    fastMillis: Int,
) {
    val tones = TimelineTones(
        neutral = HuaweiSyncTheme.colors.ink2,
        info = HuaweiSyncTheme.colors.info,
        ok = HuaweiSyncTheme.colors.ok,
        warning = HuaweiSyncTheme.colors.warning,
        error = HuaweiSyncTheme.colors.accentForeground,
        pending = HuaweiSyncTheme.colors.lineStrong,
    )
    val entries = remember(activity, tones) { lifecycleEntries(activity, tones) }
    Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md)) {
        entries.forEachIndexed { index, entry ->
            TimelineEntry(
                entry = entry,
                showConnector = index < entries.lastIndex,
                reducedMotion = reducedMotion,
                standardMillis = standardMillis,
                enterDelayMillis = if (reducedMotion) 0 else index * (fastMillis / 2),
            )
        }
    }
}

@Composable
private fun TimelineEntry(
    entry: TimelineEntryModel,
    showConnector: Boolean,
    reducedMotion: Boolean,
    standardMillis: Int,
    enterDelayMillis: Int,
) {
    val visibleState = remember(entry.key) {
        MutableTransitionState(initialState = reducedMotion)
    }
    LaunchedEffect(entry.key, reducedMotion) {
        if (reducedMotion) {
            visibleState.targetState = true
        } else {
            if (enterDelayMillis > 0) {
                kotlinx.coroutines.delay(enterDelayMillis.toLong())
            }
            visibleState.targetState = true
        }
    }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = if (reducedMotion) {
            fadeIn(tween(0))
        } else {
            fadeIn(tween(standardMillis)) +
                slideInVertically(tween(standardMillis)) { height -> height / 4 }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("activity-lifecycle-${entry.key}"),
        ) {
            TimelineRail(color = entry.tone, showConnector = showConnector)
            Spacer(Modifier.width(HuaweiSyncSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = HuaweiSyncTheme.technicalTypography.value,
                    color = HuaweiSyncTheme.colors.ink,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(HuaweiSyncSpacing.xs))
                Text(
                    text = entry.detail,
                    style = HuaweiSyncTheme.technicalTypography.value,
                    color = HuaweiSyncTheme.colors.ink2,
                )
                entry.timestamp?.let {
                    Spacer(Modifier.height(HuaweiSyncSpacing.xs))
                    TechnicalMicrocopy(it)
                }
                entry.technical?.let {
                    Spacer(Modifier.height(HuaweiSyncSpacing.xs))
                    TechnicalMicrocopy(it)
                }
            }
        }
    }
}

@Composable
private fun TimelineRail(color: Color, showConnector: Boolean) {
    Column(
        modifier = Modifier.width(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(color)
                .border(HuaweiSyncGeometry.borderThin, color, RectangleShape),
        )
        if (showConnector) {
            Box(
                Modifier
                    .width(2.dp)
                    .height(32.dp)
                    .background(HuaweiSyncTheme.colors.lineStrong),
            )
        }
    }
}

@Composable
private fun TechnicalDetailToggle(activity: ActivityHistoryItem) {
    val motion = HuaweiSyncMotion.current
    var expanded by rememberSaveable(activity.clientRecordId) { mutableStateOf(false) }
    val toggleLabel = if (expanded) "Hide technical detail" else "Show technical detail"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (motion.reducedMotion) Modifier
                else Modifier.animateContentSize(tween(motion.standardMillis)),
            )
            .testTag("activity-detail-technical"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HuaweiSyncTheme.colors.surface1)
                .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line, RectangleShape)
                .clickable(role = Role.Button, onClickLabel = toggleLabel) { expanded = !expanded }
                .padding(HuaweiSyncSpacing.md)
                .semantics {
                    role = Role.Button
                    stateDescription = if (expanded) "Expanded" else "Collapsed"
                    contentDescription = toggleLabel
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = toggleLabel.uppercase(),
                style = HuaweiSyncTheme.technicalTypography.label,
                color = HuaweiSyncTheme.colors.ink,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (expanded) "−" else "+",
                style = HuaweiSyncTheme.technicalTypography.value,
                color = HuaweiSyncTheme.colors.ink,
            )
        }
        if (expanded) {
            Spacer(Modifier.height(HuaweiSyncSpacing.sm))
            ModernistSurface(Modifier.fillMaxWidth().testTag("activity-detail-technical-body")) {
                Fact("Client record ID", activity.clientRecordId, allowEllipsis = false)
                Fact("Record version", activity.clientRecordVersion.toString())
                Fact("Attempt count", activity.attemptCount.toString())
                Fact("Source provider", activity.sourceProvider)
                activity.safeErrorCode?.let { Fact("Safe error code", it) }
            }
        }
    }
}

@Composable
private fun Fact(label: String, value: String, allowEllipsis: Boolean = true) {
    Column(Modifier.fillMaxWidth().padding(vertical = HuaweiSyncSpacing.sm)) {
        Text(
            label,
            style = HuaweiSyncTheme.technicalTypography.label,
            color = HuaweiSyncTheme.colors.ink2,
        )
        Text(
            value,
            maxLines = if (allowEllipsis) 2 else Int.MAX_VALUE,
            overflow = if (allowEllipsis) TextOverflow.Ellipsis else TextOverflow.Clip,
            style = HuaweiSyncTheme.technicalTypography.value,
            color = HuaweiSyncTheme.colors.ink,
        )
    }
}

@Composable
private fun MissingActivity(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HuaweiSyncSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionHeader(
            eyebrow = "READ-ONLY LEDGER",
            title = "Activity unavailable",
            modifier = Modifier.padding(bottom = HuaweiSyncSpacing.lg),
        )
        ModernistSurface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("activity-detail-missing"),
        ) {
            Text(
                "No ledger row matches the selected deterministic client identity.",
                style = HuaweiSyncTheme.technicalTypography.value,
                color = HuaweiSyncTheme.colors.ink2,
            )
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
            StraightEdgeButton("Back to history", onClick = onBack)
        }
    }
}

private data class TimelineEntryModel(
    val key: String,
    val title: String,
    val detail: String,
    val tone: Color,
    val timestamp: String?,
    val technical: String?,
)

private data class TimelineTones(
    val neutral: Color,
    val info: Color,
    val ok: Color,
    val warning: Color,
    val error: Color,
    val pending: Color,
)

private fun lifecycleEntries(
    activity: ActivityHistoryItem,
    tones: TimelineTones,
): List<TimelineEntryModel> {
    val entries = mutableListOf<TimelineEntryModel>()
    val neutral = tones.neutral
    val info = tones.info
    val ok = tones.ok
    val warning = tones.warning
    val error = tones.error
    val pending = tones.pending

    entries += TimelineEntryModel(
        key = "prepared",
        title = "Prepared",
        detail = "The ledger recorded a durable row with the deterministic client identity.",
        tone = neutral,
        timestamp = formatTime(activity.updatedAtEpochMillis),
        technical = null,
    )

    activity.acceptedAtEpochMillis?.let { acceptedAt ->
        entries += TimelineEntryModel(
            key = "accepted",
            title = "Health Connect accepted the write",
            detail = "Persisted acceptance is preserved even if verification later needs another pass.",
            tone = info,
            timestamp = formatTime(acceptedAt),
            technical = null,
        )
    }

    activity.confirmedAtEpochMillis?.let { confirmedAt ->
        entries += TimelineEntryModel(
            key = "verified",
            title = "Readback verified",
            detail = "A Health Connect readback matched the deterministic client identity and version.",
            tone = ok,
            timestamp = formatTime(confirmedAt),
            technical = null,
        )
    }

    when (activity.readbackState) {
        ActivityReadbackState.RECONCILIATION_REQUIRED -> entries += TimelineEntryModel(
            key = "reconciliation",
            title = "Reconciliation required",
            detail = "The durable state is uncertain. The coordinator will reconcile before the next write.",
            tone = warning,
            timestamp = null,
            technical = activity.safeErrorCode?.let { "SAFE ERROR CODE $it" },
        )
        ActivityReadbackState.RETRYABLE_ERROR -> entries += TimelineEntryModel(
            key = "retryable",
            title = "Retry available",
            detail = "A sanitized retryable failure is recorded. No acceptance or verification is inferred.",
            tone = warning,
            timestamp = null,
            technical = activity.safeErrorCode?.let { "SAFE ERROR CODE $it" },
        )
        ActivityReadbackState.ACTION_REQUIRED -> entries += TimelineEntryModel(
            key = "action",
            title = "Action required",
            detail = "A blocked or permanent state is recorded. Resolve the block before another attempt.",
            tone = error,
            timestamp = null,
            technical = activity.safeErrorCode?.let { "SAFE ERROR CODE $it" },
        )
        ActivityReadbackState.PENDING_READBACK -> if (activity.confirmedAtEpochMillis == null) {
            entries += TimelineEntryModel(
                key = "pending-readback",
                title = "Pending readback",
                detail = "Write acceptance is recorded; deterministic readback verification is still pending.",
                tone = info,
                timestamp = null,
                technical = null,
            )
        }
        ActivityReadbackState.PREPARED,
        ActivityReadbackState.VERIFIED,
        -> Unit
    }

    entries += TimelineEntryModel(
        key = "gymrats",
        title = "Available for GymRats to import",
        detail = "GymRats can read the record once its own import runs. Delivery has not been claimed.",
        tone = pending,
        timestamp = null,
        technical = null,
    )
    return entries
}

private fun detailRailModel(activity: ActivityHistoryItem): SyncPipelineRailModel {
    val huaweiState = SyncRailState.PENDING
    val syncState = when (activity.readbackState) {
        ActivityReadbackState.RECONCILIATION_REQUIRED,
        ActivityReadbackState.RETRYABLE_ERROR,
        ActivityReadbackState.ACTION_REQUIRED,
        -> SyncRailState.ATTENTION
        ActivityReadbackState.PREPARED -> if (activity.acceptedAtEpochMillis == null) {
            SyncRailState.WAITING
        } else {
            SyncRailState.CONFIRMED
        }
        ActivityReadbackState.PENDING_READBACK,
        ActivityReadbackState.VERIFIED,
        -> SyncRailState.CONFIRMED
    }
    val hcState = when (activity.readbackState) {
        ActivityReadbackState.VERIFIED -> SyncRailState.CONFIRMED
        ActivityReadbackState.PENDING_READBACK -> SyncRailState.ACTIVE_VERIFY
        ActivityReadbackState.RECONCILIATION_REQUIRED,
        ActivityReadbackState.RETRYABLE_ERROR,
        ActivityReadbackState.ACTION_REQUIRED,
        -> SyncRailState.ATTENTION
        ActivityReadbackState.PREPARED -> SyncRailState.WAITING
    }
    return SyncPipelineRailModel(
        nodes = listOf(
            SyncRailNode(
                id = "huawei",
                monogram = "H",
                label = "HUAWEI",
                state = huaweiState,
                supportingText = "SOURCE",
            ),
            SyncRailNode(
                id = "sync",
                monogram = "S",
                label = "SYNC",
                state = syncState,
                supportingText = "LEDGER",
            ),
            SyncRailNode(
                id = "hc",
                monogram = "HC",
                label = "HEALTH",
                state = hcState,
                supportingText = readbackLabel(activity.readbackState).uppercase(),
            ),
            SyncRailNode(
                id = "gymrats",
                monogram = "G",
                label = "GYMRATS",
                state = SyncRailState.PENDING,
                supportingText = "AWAITING G2",
            ),
        ),
        activeNodeIndex = null,
        activeFlow = SyncRailFlow.NONE,
    )
}

private fun formatTime(epochMillis: Long): String =
    DetailTimeFormatter.format(Instant.ofEpochMilli(epochMillis))

private fun detailPreviewItem(state: ActivityReadbackState) = ActivityHistoryItem(
    clientRecordId = "hs:v1:9f1707728c79ea56ac10a92b8c78300e3e0a7d34f774c7508cb31db29ac73eed",
    clientRecordVersion = 2,
    sourceProvider = "synthetic",
    attemptCount = 3,
    acceptedAtEpochMillis = if (state == ActivityReadbackState.RETRYABLE_ERROR) null else 1_750_000_000_000,
    confirmedAtEpochMillis = if (state == ActivityReadbackState.VERIFIED) 1_750_000_020_000 else null,
    updatedAtEpochMillis = 1_750_000_020_000,
    readbackState = state,
    safeErrorCode = if (state == ActivityReadbackState.RETRYABLE_ERROR) "HC_WRITE_RETRYABLE" else null,
)

@Preview(name = "Detail verified", showBackground = true, widthDp = 412, heightDp = 892)
@Preview(name = "Detail verified dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 892)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun VerifiedDetailPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = false) {
            ActivityDetailScreen(detailPreviewItem(ActivityReadbackState.VERIFIED), {})
        }
    }
}

@Preview(name = "Detail pending readback", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun PendingDetailPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = false) {
            ActivityDetailScreen(detailPreviewItem(ActivityReadbackState.PENDING_READBACK), {})
        }
    }
}

@Preview(name = "Detail reconciliation", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun ReconciliationDetailPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = false) {
            ActivityDetailScreen(detailPreviewItem(ActivityReadbackState.RECONCILIATION_REQUIRED), {})
        }
    }
}

@Preview(name = "Detail reduced motion", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun ReducedMotionDetailPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = true) {
            ActivityDetailScreen(detailPreviewItem(ActivityReadbackState.VERIFIED), {})
        }
    }
}
