package dev.lui.huaweisync.ui.screens.history

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.state.ActivityHistoryItem
import dev.lui.huaweisync.ui.state.ActivityReadbackState
import dev.lui.huaweisync.ui.state.HistoryState
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val RowTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DayFallbackFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val WeekdayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE dd MMM", Locale.ENGLISH)

@Composable
fun HistoryScreen(
    state: HistoryState,
    onRetry: () -> Unit,
    onSelectActivity: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(HuaweiSyncTheme.colors.background)) {
        when (state) {
            HistoryState.Loading -> HistoryEmptyStateSurface(
                title = "Loading ledger",
                message = "Reading durable sync facts.",
                loading = true,
                modifier = Modifier.testTag("history-loading"),
            )
            HistoryState.Empty -> HistoryEmptyStateSurface(
                title = "No ledger activity",
                message = "A workout appears here only after a durable ledger row exists.",
                modifier = Modifier.testTag("history-empty"),
            )
            is HistoryState.RetryableError -> HistoryEmptyStateSurface(
                title = "History unavailable",
                message = "The read-only ledger could not be loaded. No sync result was inferred.",
                actionLabel = "Retry read",
                onAction = onRetry,
                code = state.safeCode,
                modifier = Modifier.testTag("history-error"),
            )
            is HistoryState.Content -> HistoryContent(state, onSelectActivity)
        }
    }
}

@Composable
private fun HistoryContent(
    state: HistoryState.Content,
    onSelectActivity: (String) -> Unit,
) {
    val zone = remember { ZoneId.systemDefault() }
    val attentionCount = remember(state.activities) {
        state.activities.count { it.readbackState.needsAttention() }
    }
    var attentionOnly by rememberSaveable { mutableStateOf(false) }
    val filterEnabled = attentionCount > 0

    val visible = remember(state.activities, attentionOnly, filterEnabled) {
        if (attentionOnly && filterEnabled) {
            state.activities.filter { it.readbackState.needsAttention() }
        } else {
            state.activities
        }
    }
    val groups = remember(visible, zone) { groupByDay(visible, zone) }

    Column(Modifier.fillMaxSize()) {
        SectionHeader(
            eyebrow = "READ-ONLY LEDGER",
            title = "Activity history",
            modifier = Modifier.padding(
                start = HuaweiSyncSpacing.xl,
                top = HuaweiSyncSpacing.xl,
                end = HuaweiSyncSpacing.xl,
                bottom = HuaweiSyncSpacing.sm,
            ),
        )
        HistorySummaryRow(
            totalCount = state.activities.size,
            attentionCount = attentionCount,
            attentionOnly = attentionOnly && filterEnabled,
            filterEnabled = filterEnabled,
            onToggleAttentionOnly = { attentionOnly = !attentionOnly },
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("history-content"),
            contentPadding = PaddingValues(
                start = HuaweiSyncSpacing.xl,
                end = HuaweiSyncSpacing.xl,
                top = HuaweiSyncSpacing.md,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
        ) {
            if (visible.isEmpty()) {
                item {
                    FilterEmptyStrip()
                }
            }
            groups.forEach { group ->
                item(key = "day-${group.dayLabel}") {
                    DayHeader(
                        label = group.dayLabel,
                        count = group.activities.size,
                    )
                }
                items(group.activities, key = { it.clientRecordId }) { activity ->
                    ActivityHistoryRow(
                        activity = activity,
                        zone = zone,
                        onSelectActivity = onSelectActivity,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySummaryRow(
    totalCount: Int,
    attentionCount: Int,
    attentionOnly: Boolean,
    filterEnabled: Boolean,
    onToggleAttentionOnly: () -> Unit,
) {
    val recordWord = if (totalCount == 1) "RECORD" else "RECORDS"
    val counter = buildString {
        append("$totalCount DURABLE $recordWord")
        if (attentionOnly) append(" · FILTERED TO ATTENTION")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = HuaweiSyncSpacing.xl,
                end = HuaweiSyncSpacing.xl,
                top = HuaweiSyncSpacing.xs,
                bottom = HuaweiSyncSpacing.xs,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TechnicalMicrocopy(counter, modifier = Modifier.weight(1f))
        AttentionFilterChip(
            attentionCount = attentionCount,
            enabled = filterEnabled,
            active = attentionOnly,
            onToggle = onToggleAttentionOnly,
        )
    }
}

@Composable
private fun AttentionFilterChip(
    attentionCount: Int,
    enabled: Boolean,
    active: Boolean,
    onToggle: () -> Unit,
) {
    val border = when {
        !enabled -> HuaweiSyncTheme.colors.lineStrong
        active -> HuaweiSyncTheme.colors.accentForeground
        else -> HuaweiSyncTheme.colors.lineStrong
    }
    val background = when {
        !enabled -> HuaweiSyncTheme.colors.surface1
        active -> HuaweiSyncTheme.colors.accentSoft
        else -> HuaweiSyncTheme.colors.surface2
    }
    val foreground = when {
        !enabled -> HuaweiSyncTheme.colors.ink3
        active -> HuaweiSyncTheme.colors.accentForeground
        else -> HuaweiSyncTheme.colors.ink2
    }
    val label = if (enabled) "NEEDS ATTENTION ×$attentionCount" else "NO ATTENTION NEEDED"
    val stateLabel = if (active) "Filter active" else "Filter inactive"
    Row(
        modifier = Modifier
            .testTag("history-attention-filter")
            .background(background)
            .border(HuaweiSyncGeometry.borderThin, border, RectangleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onToggle)
            .padding(horizontal = HuaweiSyncSpacing.sm, vertical = HuaweiSyncSpacing.xs)
            .semantics {
                role = Role.Button
                selected = active
                contentDescription = if (enabled) "$label. $stateLabel" else label
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = HuaweiSyncTheme.technicalTypography.label,
            color = foreground,
        )
    }
}

@Composable
private fun DayHeader(label: String, count: Int) {
    val recordWord = if (count == 1) "RECORD" else "RECORDS"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HuaweiSyncTheme.colors.background)
            .padding(vertical = HuaweiSyncSpacing.xs)
            .semantics { contentDescription = "$label, $count $recordWord" }
            .testTag("history-day-header-$label"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = HuaweiSyncTheme.technicalTypography.label,
            color = HuaweiSyncTheme.colors.ink,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        TechnicalMicrocopy("$count $recordWord")
    }
}

@Composable
private fun FilterEmptyStrip() {
    ModernistSurface(Modifier.fillMaxWidth().testTag("history-filter-empty")) {
        Text(
            text = "No workouts currently require attention.",
            style = HuaweiSyncTheme.technicalTypography.value,
            color = HuaweiSyncTheme.colors.ink,
        )
        Spacer(Modifier.height(HuaweiSyncSpacing.xs))
        Text(
            text = "Clear the filter to see every durable ledger row.",
            style = HuaweiSyncTheme.technicalTypography.value,
            color = HuaweiSyncTheme.colors.ink2,
        )
    }
}

@Composable
private fun HistoryEmptyStateSurface(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    code: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(HuaweiSyncSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionHeader(
            eyebrow = "READ-ONLY LEDGER",
            title = "Activity history",
            modifier = Modifier.padding(bottom = HuaweiSyncSpacing.lg),
        )
        ModernistSurface(Modifier.fillMaxWidth()) {
            if (loading) {
                CircularProgressIndicator(
                    color = HuaweiSyncTheme.colors.accent,
                    modifier = Modifier.padding(bottom = HuaweiSyncSpacing.lg),
                )
            }
            Text(title, style = HuaweiSyncTheme.technicalTypography.value, color = HuaweiSyncTheme.colors.ink)
            Spacer(Modifier.height(HuaweiSyncSpacing.sm))
            Text(message, style = HuaweiSyncTheme.technicalTypography.value, color = HuaweiSyncTheme.colors.ink2)
            code?.let {
                Spacer(Modifier.height(HuaweiSyncSpacing.md))
                TechnicalMicrocopy(it)
            }
            actionLabel?.let {
                Spacer(Modifier.height(HuaweiSyncSpacing.lg))
                StraightEdgeButton(label = actionLabel, onClick = onAction)
            }
        }
    }
}

@Composable
private fun ActivityHistoryRow(
    activity: ActivityHistoryItem,
    zone: ZoneId,
    onSelectActivity: (String) -> Unit,
) {
    val color = readbackAccent(activity.readbackState)
    val timeText = Instant.ofEpochMilli(activity.updatedAtEpochMillis)
        .atZone(zone)
        .toLocalTime()
        .format(RowTimeFormatter)
    val label = readbackLabel(activity.readbackState)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HuaweiSyncTheme.colors.surface1)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line, RectangleShape)
            .clickable { onSelectActivity(activity.clientRecordId) }
            .testTag("history-item-${activity.clientRecordId}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(color)
                .semantics { contentDescription = "$label state marker" },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = HuaweiSyncSpacing.lg,
                    top = HuaweiSyncSpacing.md,
                    bottom = HuaweiSyncSpacing.md,
                    end = HuaweiSyncSpacing.md,
                ),
        ) {
            TechnicalMicrocopy(activity.sourceProvider.uppercase())
            Text(
                text = label,
                style = HuaweiSyncTheme.technicalTypography.value,
                color = HuaweiSyncTheme.colors.ink,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.padding(
                end = HuaweiSyncSpacing.lg,
                top = HuaweiSyncSpacing.md,
                bottom = HuaweiSyncSpacing.md,
            ),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = timeText,
                style = HuaweiSyncTheme.technicalTypography.value,
                color = HuaweiSyncTheme.colors.ink,
            )
            if (activity.attemptCount > 1) {
                Spacer(Modifier.height(HuaweiSyncSpacing.xs))
                Text(
                    text = "×${activity.attemptCount}",
                    style = HuaweiSyncTheme.technicalTypography.label,
                    color = HuaweiSyncTheme.colors.ink2,
                    modifier = Modifier.semantics {
                        contentDescription = "${activity.attemptCount} attempts"
                    },
                )
            }
        }
    }
}

internal fun readbackLabel(state: ActivityReadbackState): String = when (state) {
    ActivityReadbackState.PREPARED -> "Prepared"
    ActivityReadbackState.RECONCILIATION_REQUIRED -> "Reconciliation required"
    ActivityReadbackState.PENDING_READBACK -> "Pending readback"
    ActivityReadbackState.VERIFIED -> "Verified"
    ActivityReadbackState.RETRYABLE_ERROR -> "Retry available"
    ActivityReadbackState.ACTION_REQUIRED -> "Action required"
}

@Composable
internal fun readbackAccent(state: ActivityReadbackState): Color = when (state) {
    ActivityReadbackState.VERIFIED -> HuaweiSyncTheme.colors.ok
    ActivityReadbackState.PENDING_READBACK -> HuaweiSyncTheme.colors.info
    ActivityReadbackState.PREPARED -> HuaweiSyncTheme.colors.ink2
    ActivityReadbackState.RECONCILIATION_REQUIRED,
    ActivityReadbackState.RETRYABLE_ERROR,
    -> HuaweiSyncTheme.colors.warning
    ActivityReadbackState.ACTION_REQUIRED -> HuaweiSyncTheme.colors.accentForeground
}

internal fun ActivityReadbackState.needsAttention(): Boolean = when (this) {
    ActivityReadbackState.RECONCILIATION_REQUIRED,
    ActivityReadbackState.RETRYABLE_ERROR,
    ActivityReadbackState.ACTION_REQUIRED,
    -> true
    ActivityReadbackState.VERIFIED,
    ActivityReadbackState.PENDING_READBACK,
    ActivityReadbackState.PREPARED,
    -> false
}

internal data class HistoryDayGroup(
    val dayLabel: String,
    val activities: List<ActivityHistoryItem>,
)

internal fun groupByDay(
    activities: List<ActivityHistoryItem>,
    zone: ZoneId,
): List<HistoryDayGroup> {
    if (activities.isEmpty()) return emptyList()
    val today = LocalDate.now(zone)
    val yesterday = today.minus(1, ChronoUnit.DAYS)
    return activities
        .groupBy {
            Instant.ofEpochMilli(it.updatedAtEpochMillis)
                .atZone(zone)
                .toLocalDate()
        }
        .toSortedMap(compareByDescending { it })
        .map { (day, dayActivities) ->
            HistoryDayGroup(
                dayLabel = dayLabelFor(day, today, yesterday),
                activities = dayActivities.sortedByDescending { it.updatedAtEpochMillis },
            )
        }
}

internal fun dayLabelFor(
    day: LocalDate,
    today: LocalDate,
    yesterday: LocalDate,
): String = when (day) {
    today -> "TODAY"
    yesterday -> "YESTERDAY"
    else -> {
        val recent = day.isAfter(today.minus(7, ChronoUnit.DAYS))
        if (recent) day.format(WeekdayFormatter).uppercase(Locale.ENGLISH)
        else day.format(DayFallbackFormatter)
    }
}

private fun previewItem(state: ActivityReadbackState, suffix: String = state.name) = ActivityHistoryItem(
    clientRecordId = "huawei-sync:v1:$suffix:67b91d2d4a1f",
    clientRecordVersion = 2,
    sourceProvider = "synthetic",
    attemptCount = 3,
    acceptedAtEpochMillis = 1_750_000_000_000,
    confirmedAtEpochMillis = if (state == ActivityReadbackState.VERIFIED) 1_750_000_020_000 else null,
    updatedAtEpochMillis = 1_750_000_020_000,
    readbackState = state,
    safeErrorCode = null,
)

@Preview(name = "History facts", showBackground = true, widthDp = 412, heightDp = 892)
@Preview(name = "History facts dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 892)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun HistoryContentPreview() {
    HuaweiSyncTheme {
        HistoryScreen(
            state = HistoryState.Content(
                listOf(
                    previewItem(ActivityReadbackState.VERIFIED),
                    previewItem(ActivityReadbackState.PENDING_READBACK),
                    previewItem(ActivityReadbackState.RECONCILIATION_REQUIRED),
                    previewItem(ActivityReadbackState.RETRYABLE_ERROR),
                ),
            ),
            onRetry = {},
            onSelectActivity = {},
        )
    }
}

@Preview(name = "History empty", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun HistoryEmptyPreview() {
    HuaweiSyncTheme { HistoryScreen(HistoryState.Empty, {}, {}) }
}

@Preview(name = "History loading", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun HistoryLoadingPreview() {
    HuaweiSyncTheme { HistoryScreen(HistoryState.Loading, {}, {}) }
}

@Preview(name = "History error", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun HistoryErrorPreview() {
    HuaweiSyncTheme { HistoryScreen(HistoryState.RetryableError(), {}, {}) }
}
