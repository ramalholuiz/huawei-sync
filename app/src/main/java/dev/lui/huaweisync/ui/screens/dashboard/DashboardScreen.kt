package dev.lui.huaweisync.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.ModernistBottomNavigation
import dev.lui.huaweisync.ui.components.ModernistNavigationItem
import dev.lui.huaweisync.ui.components.ModernistProgress
import dev.lui.huaweisync.ui.components.ModernistStatus
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StatusLabel
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.SyncFab
import dev.lui.huaweisync.ui.components.SyncFabState
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.state.ProductVerificationEvidence
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

@Immutable
sealed interface DashboardScreenState {
    data object Loading : DashboardScreenState
    data class Content(val sync: ProductSyncState) : DashboardScreenState
}

@Composable
fun DashboardScreen(
    state: DashboardScreenState,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSync: () -> Unit,
    onResolveHealthConnect: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    syncInProgress: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.background),
    ) {
        DashboardTopBar(darkTheme = darkTheme, onToggleTheme = onToggleTheme)
        Box(modifier = Modifier.weight(1f)) {
            when (state) {
                DashboardScreenState.Loading -> LoadingDashboard()
                is DashboardScreenState.Content -> DashboardContent(
                    sync = state.sync,
                    onResolveHealthConnect = onResolveHealthConnect,
                )
            }
            val fabState = if (syncInProgress) SyncFabState.Syncing else state.fabState()
            SyncFab(
                state = fabState,
                onClick = onSync,
                enabled = state !is DashboardScreenState.Loading && !syncInProgress,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(HuaweiSyncSpacing.xl),
            )
        }
        ModernistBottomNavigation(
            items = dashboardNavigationItems,
            selectedKey = "dashboard",
            onSelect = onNavigate,
        )
    }
}

private val dashboardNavigationItems = listOf(
    ModernistNavigationItem("dashboard", "Dashboard", Icons.Rounded.Home),
    ModernistNavigationItem("history", "History", Icons.Rounded.Timeline),
    ModernistNavigationItem("settings", "Settings", Icons.Rounded.Settings),
)

@Composable
private fun DashboardTopBar(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line)
            .padding(horizontal = HuaweiSyncSpacing.lg, vertical = HuaweiSyncSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(HuaweiSyncTheme.colors.accentContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("H", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Column {
                Text("Dashboard", color = HuaweiSyncTheme.colors.ink, style = MaterialTheme.typography.titleMedium)
                TechnicalMicrocopy("LIVE PRODUCT STATE")
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
private fun LoadingDashboard() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(HuaweiSyncSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
    ) {
        item {
            ModernistSurface(modifier = Modifier.fillMaxWidth()) {
                StatusLabel(ModernistStatus.Syncing)
                Spacer(Modifier.height(HuaweiSyncSpacing.md))
                Text("Loading sync status", color = HuaweiSyncTheme.colors.ink, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(HuaweiSyncSpacing.sm))
                Text(
                    "Reading Health Connect availability, permissions, and the durable ledger.",
                    color = HuaweiSyncTheme.colors.ink2,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(HuaweiSyncSpacing.lg))
                ModernistProgress(progress = 0f, label = "Loading sync status")
            }
        }
    }
}

@Composable
private fun DashboardContent(sync: ProductSyncState, onResolveHealthConnect: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = HuaweiSyncSpacing.lg,
            top = HuaweiSyncSpacing.lg,
            end = HuaweiSyncSpacing.lg,
            bottom = 112.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
    ) {
        item { SyncHero(sync = sync, onResolveHealthConnect = onResolveHealthConnect) }
        item { LedgerSummary(sync) }
        item { ConnectedServices(sync) }
        item { VerificationSummary(sync) }
        sync.sanitizedFailureSummary?.let { summary ->
            item { FailureSummary(summary) }
        }
    }
}

@Composable
private fun SyncHero(sync: ProductSyncState, onResolveHealthConnect: () -> Unit) {
    val visualStatus = sync.healthConnectStatus.toModernistStatus()
    ModernistSurface(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = HuaweiSyncTheme.colors.surface2,
        borderColor = when (visualStatus) {
            ModernistStatus.Error -> HuaweiSyncTheme.colors.accent
            ModernistStatus.Warning -> HuaweiSyncTheme.colors.warning
            else -> HuaweiSyncTheme.colors.lineStrong
        },
        contentPadding = HuaweiSyncSpacing.xl,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusLabel(visualStatus)
            TechnicalMicrocopy(sync.phase.displayLabel())
        }
        Spacer(Modifier.height(HuaweiSyncSpacing.lg))
        Text(
            text = sync.healthConnectStatus.label,
            color = HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(HuaweiSyncSpacing.sm))
        Text(
            text = sync.healthConnectStatus.supportingText(),
            color = HuaweiSyncTheme.colors.ink2,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (sync.healthConnectStatus.needsHealthConnectAction()) {
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
            StraightEdgeButton(
                label = sync.healthConnectStatus.actionLabel(),
                onClick = onResolveHealthConnect,
                accent = sync.healthConnectStatus == ProductHealthConnectStatus.PERMISSION_REQUIRED,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (sync.phase != ProductSyncPhase.IDLE) {
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
            ModernistProgress(
                progress = sync.phase.progressFraction(),
                label = "Sync phase: ${sync.phase.displayLabel()}",
            )
        }
    }
}

@Composable
private fun LedgerSummary(sync: ProductSyncState) {
    Column {
        SectionHeader(title = "Recent activity", eyebrow = "DURABLE LEDGER")
        ModernistSurface(modifier = Modifier.fillMaxWidth()) {
            if (sync.ledgerWorkoutCount == 0) {
                Text("No workouts recorded yet", color = HuaweiSyncTheme.colors.ink, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(HuaweiSyncSpacing.sm))
                Text(
                    "Run a sync when Health Connect is ready. This screen will only count rows present in the local ledger.",
                    color = HuaweiSyncTheme.colors.ink2,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                MetricRow("Workouts tracked", sync.ledgerWorkoutCount.toString())
                Spacer(Modifier.height(HuaweiSyncSpacing.md))
                MetricRow("Current record attempts", sync.attemptCount.toString())
            }
        }
    }
}

@Composable
private fun ConnectedServices(sync: ProductSyncState) {
    Column {
        SectionHeader(title = "Connected services", eyebrow = "CURRENT FACTS")
        ModernistSurface(modifier = Modifier.fillMaxWidth()) {
            ServiceRow(
                monogram = "HC",
                title = "Health Connect",
                status = sync.healthConnectStatus.label,
            )
            Spacer(Modifier.height(HuaweiSyncSpacing.lg))
            ServiceRow(
                monogram = "GR",
                title = "GymRats",
                status = sync.gymRatsStatus.label,
            )
        }
    }
}

@Composable
private fun VerificationSummary(sync: ProductSyncState) {
    Column {
        SectionHeader(title = "Verification", eyebrow = "HEALTH CONNECT READBACK")
        ModernistSurface(modifier = Modifier.fillMaxWidth()) {
            val verification = sync.verification
            Text(
                text = if (verification.realReadbackConfirmed) "Readback confirmed" else "Readback not yet confirmed",
                color = if (verification.realReadbackConfirmed) HuaweiSyncTheme.colors.ok else HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(HuaweiSyncSpacing.md))
            verification.healthConnectMatchCount?.let { MetricRow("Deterministic ID matches", it.toString()) }
            verification.expectedVersionMatchCount?.let {
                Spacer(Modifier.height(HuaweiSyncSpacing.sm))
                MetricRow("Expected version matches", it.toString())
            }
            if (verification.healthConnectMatchCount == null && verification.expectedVersionMatchCount == null) {
                Text(
                    "No readback counts are available.",
                    color = HuaweiSyncTheme.colors.ink2,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun FailureSummary(summary: String) {
    ModernistSurface(
        modifier = Modifier.fillMaxWidth(),
        borderColor = HuaweiSyncTheme.colors.accent,
    ) {
        TechnicalMicrocopy("SANITIZED FAILURE SUMMARY")
        Spacer(Modifier.height(HuaweiSyncSpacing.sm))
        Text(summary, color = HuaweiSyncTheme.colors.ink, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(label, color = HuaweiSyncTheme.colors.ink2, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = HuaweiSyncTheme.colors.ink, style = HuaweiSyncTheme.technicalTypography.value)
    }
}

@Composable
private fun ServiceRow(monogram: String, title: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(HuaweiSyncTheme.colors.surface3)
                .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.lineStrong),
            contentAlignment = Alignment.Center,
        ) {
            Text(monogram, color = HuaweiSyncTheme.colors.ink, style = HuaweiSyncTheme.technicalTypography.label)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = HuaweiSyncTheme.colors.ink, style = MaterialTheme.typography.titleSmall)
            TechnicalMicrocopy(status.uppercase())
        }
    }
}

private fun DashboardScreenState.fabState(): SyncFabState = when (this) {
    DashboardScreenState.Loading -> SyncFabState.Idle
    is DashboardScreenState.Content -> when {
        sync.phase != ProductSyncPhase.IDLE -> SyncFabState.Syncing
        sync.healthConnectStatus == ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT -> SyncFabState.Complete
        else -> SyncFabState.Idle
    }
}

private fun ProductHealthConnectStatus.toModernistStatus(): ModernistStatus = when (this) {
    ProductHealthConnectStatus.READY_TO_SYNC -> ModernistStatus.Ready
    ProductHealthConnectStatus.WRITE_IN_PROGRESS,
    ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK,
    -> ModernistStatus.Syncing
    ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT -> ModernistStatus.Complete
    ProductHealthConnectStatus.FAILED -> ModernistStatus.Error
    else -> ModernistStatus.Warning
}

private fun ProductHealthConnectStatus.supportingText(): String = when (this) {
    ProductHealthConnectStatus.UNAVAILABLE -> "Health Connect is not available on this device. Sync cannot start."
    ProductHealthConnectStatus.UPDATE_REQUIRED -> "Update Health Connect before starting a sync."
    ProductHealthConnectStatus.PERMISSION_REQUIRED -> "Allow Huawei Sync to write exercise sessions in Health Connect."
    ProductHealthConnectStatus.READY_TO_SYNC -> "Health Connect is available and the required write permission is granted."
    ProductHealthConnectStatus.WRITE_IN_PROGRESS -> "A workout is being written using the existing sync coordinator."
    ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK -> "The write was accepted, but a matching Health Connect readback has not confirmed it yet."
    ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT -> "A matching deterministic record was read back from Health Connect."
    ProductHealthConnectStatus.RECONCILIATION_REQUIRED -> "The durable ledger needs reconciliation before another write can be trusted."
    ProductHealthConnectStatus.RETRY_REQUIRED -> "The last known outcome permits a retry. Review the status before trying again."
    ProductHealthConnectStatus.ACTION_REQUIRED -> "Health Connect requires action before sync can continue."
    ProductHealthConnectStatus.FAILED -> "The sync did not complete. The sanitized failure summary is shown below when available."
}

private fun ProductHealthConnectStatus.needsHealthConnectAction(): Boolean = when (this) {
    ProductHealthConnectStatus.UNAVAILABLE,
    ProductHealthConnectStatus.WRITE_IN_PROGRESS,
    ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK,
    ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
    -> false
    else -> this != ProductHealthConnectStatus.READY_TO_SYNC
}

private fun ProductHealthConnectStatus.actionLabel(): String = when (this) {
    ProductHealthConnectStatus.UPDATE_REQUIRED -> "Open Health Connect update"
    ProductHealthConnectStatus.PERMISSION_REQUIRED -> "Review permission"
    ProductHealthConnectStatus.RECONCILIATION_REQUIRED -> "Review reconciliation"
    ProductHealthConnectStatus.RETRY_REQUIRED -> "Review retry"
    ProductHealthConnectStatus.ACTION_REQUIRED -> "Review required action"
    ProductHealthConnectStatus.FAILED -> "Review Health Connect"
    else -> "Review Health Connect"
}

private fun ProductSyncPhase.displayLabel(): String = when (this) {
    ProductSyncPhase.IDLE -> "IDLE"
    ProductSyncPhase.PREFLIGHT -> "PREFLIGHT"
    ProductSyncPhase.WRITE -> "WRITE"
    ProductSyncPhase.ACCEPTANCE -> "ACCEPTANCE"
    ProductSyncPhase.VERIFICATION -> "VERIFICATION"
    ProductSyncPhase.RECONCILIATION -> "RECONCILIATION"
}

private fun ProductSyncPhase.progressFraction(): Float = when (this) {
    ProductSyncPhase.IDLE -> 0f
    ProductSyncPhase.PREFLIGHT -> 0.2f
    ProductSyncPhase.WRITE -> 0.4f
    ProductSyncPhase.ACCEPTANCE -> 0.6f
    ProductSyncPhase.VERIFICATION -> 0.8f
    ProductSyncPhase.RECONCILIATION -> 1f
}

private fun previewState(
    status: ProductHealthConnectStatus,
    phase: ProductSyncPhase = ProductSyncPhase.IDLE,
    ledgerWorkoutCount: Int = 0,
    attemptCount: Int = 0,
    confirmed: Boolean = false,
    failure: String? = null,
): ProductSyncState = ProductSyncState(
    healthConnectStatus = status,
    gymRatsStatus = ProductGymRatsStatus.READY_TO_READ,
    phase = phase,
    attemptCount = attemptCount,
    ledgerWorkoutCount = ledgerWorkoutCount,
    verification = ProductVerificationEvidence(
        diagnosticEvidence = null,
        realReadbackConfirmed = confirmed,
        healthConnectMatchCount = if (confirmed) 1 else null,
        expectedVersionMatchCount = if (confirmed) 1 else null,
        versionMatch = if (confirmed) true else null,
    ),
    sanitizedFailureSummary = failure,
)

@Preview(name = "Dashboard loading", showBackground = true)
@Composable
private fun DashboardLoadingPreview() {
    HuaweiSyncTheme(darkTheme = true) {
        DashboardScreen(DashboardScreenState.Loading, true, {}, {}, {}, {})
    }
}

@Preview(name = "Dashboard empty", showBackground = true)
@Composable
private fun DashboardEmptyPreview() {
    HuaweiSyncTheme(darkTheme = true) {
        DashboardScreen(
            DashboardScreenState.Content(previewState(ProductHealthConnectStatus.READY_TO_SYNC)),
            true, {}, {}, {}, {},
        )
    }
}

@Preview(name = "Dashboard blocked", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DashboardBlockedPreview() {
    HuaweiSyncTheme(darkTheme = false) {
        DashboardScreen(
            DashboardScreenState.Content(previewState(ProductHealthConnectStatus.PERMISSION_REQUIRED)),
            false, {}, {}, {}, {},
        )
    }
}

@Preview(name = "Dashboard error", showBackground = true)
@Composable
private fun DashboardErrorPreview() {
    HuaweiSyncTheme(darkTheme = true) {
        DashboardScreen(
            DashboardScreenState.Content(
                previewState(
                    status = ProductHealthConnectStatus.FAILED,
                    attemptCount = 1,
                    failure = "Health Connect write failed. Retry after reviewing availability and permission.",
                ),
            ),
            true, {}, {}, {}, {},
        )
    }
}

@Preview(name = "Dashboard verified", showBackground = true)
@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews
@Composable
private fun DashboardVerifiedPreview() {
    HuaweiSyncTheme(darkTheme = true) {
        DashboardScreen(
            DashboardScreenState.Content(
                previewState(
                    status = ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT,
                    ledgerWorkoutCount = 1,
                    attemptCount = 1,
                    confirmed = true,
                ),
            ),
            true, {}, {}, {}, {},
        )
    }
}
