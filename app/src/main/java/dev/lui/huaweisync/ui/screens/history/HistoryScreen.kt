package dev.lui.huaweisync.ui.screens.history

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.state.ActivityHistoryItem
import dev.lui.huaweisync.ui.state.ActivityReadbackState
import dev.lui.huaweisync.ui.state.HistoryState
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val LedgerTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm 'UTC'").withZone(ZoneOffset.UTC)

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
            is HistoryState.Content -> Column(Modifier.fillMaxSize()) {
                SectionHeader(
                    eyebrow = "READ-ONLY LEDGER",
                    title = "Activity history",
                    modifier = Modifier.padding(
                        start = HuaweiSyncSpacing.xl,
                        top = HuaweiSyncSpacing.xl,
                        end = HuaweiSyncSpacing.xl,
                        bottom = HuaweiSyncSpacing.md,
                    ),
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("history-content"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = HuaweiSyncSpacing.xl,
                        end = HuaweiSyncSpacing.xl,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
                ) {
                    item {
                        TechnicalMicrocopy("${state.activities.size} DURABLE ${if (state.activities.size == 1) "RECORD" else "RECORDS"}")
                        Spacer(Modifier.height(HuaweiSyncSpacing.xs))
                    }
                    items(state.activities, key = ActivityHistoryItem::clientRecordId) { activity ->
                        ActivityHistoryRow(activity, onSelectActivity)
                    }
                }
            }
        }
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
    onSelectActivity: (String) -> Unit,
) {
    ModernistSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectActivity(activity.clientRecordId) }
            .testTag("history-item-${activity.clientRecordId}"),
        borderColor = stateColor(activity.readbackState),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                TechnicalMicrocopy(activity.sourceProvider.uppercase())
                Text(
                    text = readbackLabel(activity.readbackState),
                    style = HuaweiSyncTheme.technicalTypography.value,
                    color = HuaweiSyncTheme.colors.ink,
                    fontWeight = FontWeight.Bold,
                )
            }
            LedgerStateLabel(activity.readbackState)
        }
        Spacer(Modifier.height(HuaweiSyncSpacing.lg))
        Text(
            text = LedgerTimeFormatter.format(Instant.ofEpochMilli(activity.updatedAtEpochMillis)),
            style = HuaweiSyncTheme.technicalTypography.value,
            color = HuaweiSyncTheme.colors.ink2,
        )
        Spacer(Modifier.height(HuaweiSyncSpacing.sm))
        Text(
            text = activity.clientRecordId,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = HuaweiSyncTheme.technicalTypography.label,
            color = HuaweiSyncTheme.colors.ink2,
        )
        Spacer(Modifier.height(HuaweiSyncSpacing.md))
        TechnicalMicrocopy("ATTEMPTS ${activity.attemptCount}  ·  RECORD VERSION ${activity.clientRecordVersion}")
    }
}

@Composable
internal fun LedgerStateLabel(state: ActivityReadbackState) {
    Text(
        text = readbackLabel(state).uppercase(),
        modifier = Modifier
            .background(stateColor(state).copy(alpha = 0.14f))
            .padding(horizontal = HuaweiSyncSpacing.sm, vertical = HuaweiSyncSpacing.xs),
        style = HuaweiSyncTheme.technicalTypography.label,
        color = stateColor(state),
        fontWeight = FontWeight.Bold,
    )
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
private fun stateColor(state: ActivityReadbackState): Color = when (state) {
    ActivityReadbackState.VERIFIED -> HuaweiSyncTheme.colors.ok
    ActivityReadbackState.PENDING_READBACK -> HuaweiSyncTheme.colors.info
    ActivityReadbackState.PREPARED -> HuaweiSyncTheme.colors.ink2
    ActivityReadbackState.RECONCILIATION_REQUIRED,
    ActivityReadbackState.RETRYABLE_ERROR,
    -> HuaweiSyncTheme.colors.warning
    ActivityReadbackState.ACTION_REQUIRED -> HuaweiSyncTheme.colors.accentForeground
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
