package dev.lui.huaweisync.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import dev.lui.huaweisync.ui.components.HuaweiSyncMotionProvider
import dev.lui.huaweisync.ui.components.ModernistBottomNavigation
import dev.lui.huaweisync.ui.components.ModernistNavigationItem
import dev.lui.huaweisync.ui.components.ModernistSurface
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.TechnicalMicrocopy
import dev.lui.huaweisync.ui.navigation.ActivityDetail
import dev.lui.huaweisync.ui.navigation.AiAssistant
import dev.lui.huaweisync.ui.navigation.Automation
import dev.lui.huaweisync.ui.navigation.CompactDestinations
import dev.lui.huaweisync.ui.navigation.Dashboard
import dev.lui.huaweisync.ui.navigation.Diagnostics
import dev.lui.huaweisync.ui.navigation.History
import dev.lui.huaweisync.ui.navigation.HuaweiSyncNavigationState
import dev.lui.huaweisync.ui.navigation.HuaweiSyncScreenDestination
import dev.lui.huaweisync.ui.navigation.Integrations
import dev.lui.huaweisync.ui.navigation.Onboarding
import dev.lui.huaweisync.ui.navigation.Pipeline
import dev.lui.huaweisync.ui.navigation.PrimaryDestinations
import dev.lui.huaweisync.ui.navigation.rememberHuaweiSyncNavigationState
import dev.lui.huaweisync.ui.screens.assistant.AssistantScreen
import dev.lui.huaweisync.ui.screens.automation.AutomationScreen
import dev.lui.huaweisync.ui.screens.dashboard.DashboardScreen
import dev.lui.huaweisync.ui.screens.dashboard.DashboardScreenState
import dev.lui.huaweisync.ui.screens.integrations.IntegrationsScreen
import dev.lui.huaweisync.ui.screens.onboarding.OnboardingScreen
import dev.lui.huaweisync.ui.screens.detail.ActivityDetailScreen
import dev.lui.huaweisync.ui.screens.history.HistoryScreen
import dev.lui.huaweisync.ui.screens.onboarding.OnboardingScreenState
import dev.lui.huaweisync.ui.screens.pipeline.PipelineScreen
import dev.lui.huaweisync.ui.screens.sync.SyncNowModal
import dev.lui.huaweisync.ui.state.AssistantLocalContext
import dev.lui.huaweisync.ui.state.HistoryState
import dev.lui.huaweisync.ui.state.HistoryStateMapper
import dev.lui.huaweisync.ui.state.IntegrationState
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.theme.HuaweiSyncGeometry
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

internal val WideNavigationBreakpoint = 760.dp
private const val MoreNavigationKey = "more"

internal fun usesWidePrimaryNavigation(width: androidx.compose.ui.unit.Dp): Boolean =
    width >= WideNavigationBreakpoint

/**
 * Complete prototype navigation shell. The diagnostics slot preserves the proven Gate 1 runtime
 * boundary; every other destination is explicitly labeled as unavailable or preview-only.
 */
@Composable
fun HuaweiSyncRoot(
    modifier: Modifier = Modifier,
    initialDestination: HuaweiSyncScreenDestination = Dashboard,
    productSyncState: ProductSyncState? = null,
    historyState: HistoryState = HistoryState.Loading,
    syncInProgress: Boolean = false,
    onHistoryRetry: () -> Unit = {},
    onSync: () -> Unit = {},
    gate1Entry: @Composable () -> Unit,
) {
    val navigationState = rememberHuaweiSyncNavigationState(initialDestination)
    val systemDarkTheme = isSystemInDarkTheme()
    var darkTheme by rememberSaveable { mutableStateOf(systemDarkTheme) }
    var selectedActivityClientRecordId by rememberSaveable { mutableStateOf<String?>(null) }
    HuaweiSyncMotionProvider {
        HuaweiSyncTheme(darkTheme = darkTheme) {
            Surface(
                modifier = Modifier.fillMaxSize().then(modifier),
                color = HuaweiSyncTheme.colors.background,
            ) {
                HuaweiSyncNavigationShell(
                    navigationState = navigationState,
                    productSyncState = productSyncState,
                    historyState = historyState,
                    selectedActivityClientRecordId = selectedActivityClientRecordId,
                    syncInProgress = syncInProgress,
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onHistoryRetry = onHistoryRetry,
                    onSelectActivity = { clientRecordId ->
                        selectedActivityClientRecordId = clientRecordId
                        navigationState.navigateTo(ActivityDetail)
                    },
                    onSync = onSync,
                    gate1Entry = gate1Entry,
                )
            }
        }
    }
}

@Composable
internal fun HuaweiSyncNavigationShell(
    navigationState: HuaweiSyncNavigationState,
    productSyncState: ProductSyncState?,
    historyState: HistoryState = HistoryState.Loading,
    selectedActivityClientRecordId: String? = null,
    syncInProgress: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onHistoryRetry: () -> Unit = {},
    onSelectActivity: (String) -> Unit = {},
    onSync: () -> Unit,
    gate1Entry: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val wide = usesWidePrimaryNavigation(maxWidth)
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                WidePrimaryNavigation(
                    navigationState = navigationState,
                    modifier = Modifier.width(224.dp).fillMaxHeight(),
                )
                DestinationContent(
                    navigationState = navigationState,
                    productSyncState = productSyncState,
                    historyState = historyState,
                    selectedActivityClientRecordId = selectedActivityClientRecordId,
                    syncInProgress = syncInProgress,
                    darkTheme = darkTheme,
                    onToggleTheme = onToggleTheme,
                    onHistoryRetry = onHistoryRetry,
                    onSelectActivity = onSelectActivity,
                    onSync = onSync,
                    gate1Entry = gate1Entry,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                DestinationContent(
                    navigationState = navigationState,
                    productSyncState = productSyncState,
                    historyState = historyState,
                    selectedActivityClientRecordId = selectedActivityClientRecordId,
                    syncInProgress = syncInProgress,
                    darkTheme = darkTheme,
                    onToggleTheme = onToggleTheme,
                    onHistoryRetry = onHistoryRetry,
                    onSelectActivity = onSelectActivity,
                    onSync = onSync,
                    gate1Entry = gate1Entry,
                    modifier = Modifier.weight(1f),
                )
                CompactPrimaryNavigation(navigationState)
            }
        }

        if (navigationState.compactMenuVisible) {
            CompactMoreMenu(navigationState)
        }
        LaunchedEffect(navigationState.overlayDestination) {
            if (navigationState.overlayDestination != null) {
                onSync()
            }
        }
        if (navigationState.overlayDestination != null) {
            SyncNowModal(
                state = productSyncState,
                coordinatorBusy = syncInProgress,
                onDismiss = navigationState::dismissSyncOverlay,
            )
        }
    }
}

@Composable
private fun WidePrimaryNavigation(
    navigationState: HuaweiSyncNavigationState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(HuaweiSyncTheme.colors.surface1)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line, RectangleShape)
            .padding(HuaweiSyncSpacing.lg)
            .testTag("wide-primary-navigation")
            .semantics { contentDescription = "Primary navigation" },
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm),
    ) {
        TechnicalMicrocopy("HUAWEI SYNC")
        Text("Workout bridge", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.size(HuaweiSyncSpacing.sm))
        PrimaryDestinations.forEach { destination ->
            WideNavigationItem(
                destination = destination,
                selected = navigationState.currentDestination == destination,
                onClick = { navigationState.navigateTo(destination) },
            )
        }
        Spacer(Modifier.weight(1f))
        StraightEdgeButton(
            label = "Setup",
            onClick = { navigationState.navigateTo(Onboarding) },
            modifier = Modifier.fillMaxWidth().testTag("nav-onboarding"),
        )
        StraightEdgeButton(
            label = "Sync now",
            onClick = navigationState::showSyncOverlay,
            modifier = Modifier.fillMaxWidth().testTag("nav-sync-now"),
            accent = true,
        )
    }
}

@Composable
private fun WideNavigationItem(
    destination: HuaweiSyncScreenDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) HuaweiSyncTheme.colors.accentSoft else HuaweiSyncTheme.colors.surface1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nav-${destination.route}")
            .background(background)
            .border(HuaweiSyncGeometry.borderThin, HuaweiSyncTheme.colors.line, RectangleShape)
            .defaultMinSize(minHeight = 48.dp)
            .clickable(role = Role.Tab, onClickLabel = destination.label, onClick = onClick)
            .semantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = "Navigate to ${destination.label}"
            }
            .padding(HuaweiSyncSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(destination.icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(destination.label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun CompactPrimaryNavigation(navigationState: HuaweiSyncNavigationState) {
    val items = CompactDestinations.map {
        ModernistNavigationItem(key = it.route, label = it.label, icon = it.icon)
    } + ModernistNavigationItem(
        key = MoreNavigationKey,
        label = "More",
        icon = Icons.Rounded.MoreHoriz,
    )
    val selectedKey = navigationState.currentDestination.route
        .takeIf { route -> CompactDestinations.any { it.route == route } }
        ?: MoreNavigationKey

    ModernistBottomNavigation(
        items = items,
        selectedKey = selectedKey,
        onSelect = { route ->
            if (route == MoreNavigationKey) {
                navigationState.showCompactMenu()
            } else {
                CompactDestinations.single { it.route == route }.let(navigationState::navigateTo)
            }
        },
        modifier = Modifier.testTag("compact-primary-navigation"),
    )
}

@Composable
private fun CompactMoreMenu(navigationState: HuaweiSyncNavigationState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.background.copy(alpha = 0.94f))
            .clickable(
                role = Role.Button,
                onClickLabel = "Dismiss navigation menu",
                onClick = navigationState::dismissCompactMenu,
            )
            .semantics { contentDescription = "Dismiss navigation menu" },
        contentAlignment = Alignment.BottomCenter,
    ) {
        ModernistSurface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("compact-more-menu")
                .clickable(enabled = false, onClick = {}),
            backgroundColor = HuaweiSyncTheme.colors.surface1,
        ) {
            SectionHeader(title = "All destinations", eyebrow = "NAVIGATION")
            (PrimaryDestinations.filterNot { it in CompactDestinations } + Onboarding).forEach { destination ->
                StraightEdgeButton(
                    label = destination.label,
                    onClick = { navigationState.navigateTo(destination) },
                    modifier = Modifier.fillMaxWidth().testTag("nav-${destination.route}"),
                )
            }
            StraightEdgeButton(
                label = "Sync now",
                onClick = navigationState::showSyncOverlay,
                modifier = Modifier.fillMaxWidth().testTag("nav-sync-now"),
                accent = true,
            )
        }
    }
}

@Composable
private fun DestinationContent(
    navigationState: HuaweiSyncNavigationState,
    productSyncState: ProductSyncState?,
    historyState: HistoryState,
    selectedActivityClientRecordId: String?,
    syncInProgress: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onHistoryRetry: () -> Unit,
    onSelectActivity: (String) -> Unit,
    onSync: () -> Unit,
    gate1Entry: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().testTag("destination-${navigationState.currentDestination.route}")) {
        when (navigationState.currentDestination) {
            Onboarding -> OnboardingScreen(
                state = OnboardingScreenState(productSyncState?.healthConnectStatus),
                darkTheme = darkTheme,
                onStartSetup = {
                    onSync()
                    navigationState.navigateTo(Dashboard)
                },
                onContinueExistingSetup = {
                    onSync()
                    navigationState.navigateTo(Dashboard)
                },
                onToggleTheme = onToggleTheme,
            )
            Dashboard -> DashboardScreen(
                state = productSyncState?.let(DashboardScreenState::Content)
                    ?: DashboardScreenState.Loading,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                onSync = onSync,
                onResolveHealthConnect = onSync,
                onNavigate = { route ->
                    navigationState.navigateTo(
                        PrimaryDestinations.firstOrNull { it.route == route } ?: Dashboard,
                    )
                },
                syncInProgress = syncInProgress,
            )
            Pipeline -> productSyncState?.let { state ->
                PipelineScreen(
                    state = state,
                    coordinatorBusy = syncInProgress,
                )
            } ?: PlaceholderDestination(
                eyebrow = "READ-ONLY",
                title = "Live sync pipeline",
                message = "Loading product and coordinator evidence. No phase progress is available yet.",
            )
            Integrations -> IntegrationsScreen(
                state = IntegrationState.from(productState = productSyncState),
            )
            Diagnostics -> Column(Modifier.fillMaxSize()) {
                SectionHeader(
                    title = "Diagnostics center",
                    eyebrow = "PROVEN GATE 1 SURFACE",
                    modifier = Modifier.padding(horizontal = HuaweiSyncSpacing.lg),
                )
                Box(Modifier.weight(1f)) { gate1Entry() }
            }
            Automation -> AutomationScreen()
            History -> HistoryScreen(
                state = historyState,
                onRetry = onHistoryRetry,
                onSelectActivity = onSelectActivity,
            )
            ActivityDetail -> ActivityDetailScreen(
                activity = selectedActivityClientRecordId?.let { clientRecordId ->
                    HistoryStateMapper.select(historyState, clientRecordId)
                },
                onBack = { navigationState.navigateTo(History) },
            )
            AiAssistant -> AssistantScreen(
                context = AssistantLocalContext.from(productSyncState),
            )
        }
    }
}

@Composable
private fun PlaceholderDestination(
    eyebrow: String,
    title: String,
    message: String,
    primaryLabel: String? = null,
    onPrimary: (() -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(HuaweiSyncSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
    ) {
        SectionHeader(title = title, eyebrow = eyebrow)
        ModernistSurface(modifier = Modifier.fillMaxWidth()) {
            TechnicalMicrocopy("CURRENT STATUS")
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
        if (primaryLabel != null && onPrimary != null) {
            StraightEdgeButton(
                label = primaryLabel,
                onClick = onPrimary,
                modifier = Modifier.fillMaxWidth(),
                accent = title == "Dashboard",
            )
        }
        if (secondaryLabel != null && onSecondary != null) {
            StraightEdgeButton(
                label = secondaryLabel,
                onClick = onSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Compact navigation", widthDp = 390, heightDp = 844, showBackground = true)
@Preview(
    name = "Compact navigation light",
    widthDp = 390,
    heightDp = 844,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Composable
private fun CompactNavigationPreview() {
    HuaweiSyncRoot(gate1Entry = { Text("Gate 1 diagnostics runtime") })
}

@Preview(name = "Wide navigation", widthDp = 1000, heightDp = 700, showBackground = true)
@Composable
private fun WideNavigationPreview() {
    HuaweiSyncRoot(gate1Entry = { Text("Gate 1 diagnostics runtime") })
}
