package dev.lui.huaweisync.ui.screens.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.screens.history.LedgerStateLabel
import dev.lui.huaweisync.ui.screens.history.readbackLabel
import dev.lui.huaweisync.ui.state.ActivityHistoryItem
import dev.lui.huaweisync.ui.state.ActivityReadbackState
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = HuaweiSyncSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("activity-detail-back")) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back to history",
                    tint = HuaweiSyncTheme.colors.ink,
                )
            }
            TechnicalMicrocopy("DETERMINISTIC LEDGER DETAIL")
        }
        if (activity == null) {
            MissingActivity(onBack)
        } else {
            ActivityFacts(activity)
        }
    }
}

@Composable
private fun ActivityFacts(activity: ActivityHistoryItem) {
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
        SectionHeader(
            eyebrow = activity.sourceProvider.uppercase(),
            title = "Activity record",
        )
        ModernistSurface(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    TechnicalMicrocopy("READBACK STATE")
                    Text(
                        readbackLabel(activity.readbackState),
                        style = HuaweiSyncTheme.technicalTypography.value,
                        color = HuaweiSyncTheme.colors.ink,
                        fontWeight = FontWeight.Bold,
                    )
                }
                LedgerStateLabel(activity.readbackState)
            }
            Spacer(Modifier.height(HuaweiSyncSpacing.md))
            Text(
                stateExplanation(activity.readbackState),
                style = HuaweiSyncTheme.technicalTypography.value,
                color = HuaweiSyncTheme.colors.ink2,
            )
        }

        FactSection("DETERMINISTIC IDENTITY") {
            Fact("Client record ID", activity.clientRecordId, allowEllipsis = false)
            Fact("Record version", activity.clientRecordVersion.toString())
        }
        FactSection("WRITE ATTEMPTS") {
            Fact("Attempt count", activity.attemptCount.toString())
            Fact(
                "Acceptance",
                activity.acceptedAtEpochMillis?.let { "Recorded · ${formatTime(it)}" }
                    ?: "Not recorded",
            )
        }
        FactSection("READBACK VERIFICATION") {
            Fact(
                "Verification",
                activity.confirmedAtEpochMillis?.let { "Verified · ${formatTime(it)}" }
                    ?: verificationFact(activity.readbackState),
            )
            Fact("Last ledger update", formatTime(activity.updatedAtEpochMillis))
            activity.safeErrorCode?.let { Fact("Safe error code", it) }
        }
        TechnicalMicrocopy(
            "THIS VIEW REPORTS LEDGER AND READBACK FACTS ONLY. IT DOES NOT CLAIM DELIVERY TO ANOTHER APP.",
        )
    }
}

@Composable
private fun FactSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm)) {
        TechnicalMicrocopy(label)
        ModernistSurface(Modifier.fillMaxWidth()) { content() }
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
    ModernistSurface(
        modifier = Modifier.fillMaxWidth().padding(HuaweiSyncSpacing.xl).testTag("activity-detail-missing"),
    ) {
        Text(
            "Activity unavailable",
            style = HuaweiSyncTheme.technicalTypography.value,
            color = HuaweiSyncTheme.colors.ink,
        )
        Spacer(Modifier.height(HuaweiSyncSpacing.sm))
        Text(
            "No ledger row matches the selected deterministic client identity.",
            style = HuaweiSyncTheme.technicalTypography.value,
            color = HuaweiSyncTheme.colors.ink2,
        )
        Spacer(Modifier.height(HuaweiSyncSpacing.lg))
        StraightEdgeButton("Back to history", onClick = onBack)
    }
}

private fun stateExplanation(state: ActivityReadbackState): String = when (state) {
    ActivityReadbackState.PREPARED -> "The ledger row is prepared. No write acceptance is recorded."
    ActivityReadbackState.RECONCILIATION_REQUIRED ->
        "The durable state is uncertain and must be reconciled before another write."
    ActivityReadbackState.PENDING_READBACK ->
        "Write acceptance is recorded; deterministic readback verification is still pending."
    ActivityReadbackState.VERIFIED ->
        "Deterministic readback matched the accepted client identity and record version."
    ActivityReadbackState.RETRYABLE_ERROR ->
        "A sanitized retryable failure is recorded. No acceptance or verification is inferred."
    ActivityReadbackState.ACTION_REQUIRED ->
        "The ledger records a blocked or stopped state. No acceptance or verification is inferred."
}

private fun verificationFact(state: ActivityReadbackState): String = when (state) {
    ActivityReadbackState.VERIFIED -> "Verified"
    ActivityReadbackState.PENDING_READBACK -> "Pending deterministic readback"
    ActivityReadbackState.RECONCILIATION_REQUIRED -> "Reconciliation required"
    else -> "Not recorded"
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
    HuaweiSyncTheme { ActivityDetailScreen(detailPreviewItem(ActivityReadbackState.VERIFIED), {}) }
}

@Preview(name = "Detail pending readback", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun PendingDetailPreview() {
    HuaweiSyncTheme { ActivityDetailScreen(detailPreviewItem(ActivityReadbackState.PENDING_READBACK), {}) }
}

@Preview(name = "Detail reconciliation", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun ReconciliationDetailPreview() {
    HuaweiSyncTheme {
        ActivityDetailScreen(detailPreviewItem(ActivityReadbackState.RECONCILIATION_REQUIRED), {})
    }
}
