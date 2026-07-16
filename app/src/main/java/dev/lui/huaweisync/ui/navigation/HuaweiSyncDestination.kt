package dev.lui.huaweisync.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

/** A typed entry in the preserved ten-destination prototype inventory. */
sealed interface HuaweiSyncDestination {
    val route: String
    val label: String

    companion object {
        val all: List<HuaweiSyncDestination> = listOf(
            Onboarding,
            Dashboard,
            Pipeline,
            SyncNow,
            Integrations,
            Diagnostics,
            Automation,
            History,
            ActivityDetail,
            AiAssistant,
        )
    }
}

/** Destinations rendered in the shell's content region. */
sealed interface HuaweiSyncScreenDestination : HuaweiSyncDestination {
    val icon: ImageVector
}

data object Onboarding : HuaweiSyncScreenDestination {
    override val route = "onboarding"
    override val label = "Setup"
    override val icon = Icons.Rounded.List
}

data object Dashboard : HuaweiSyncScreenDestination {
    override val route = "dashboard"
    override val label = "Dashboard"
    override val icon = Icons.Rounded.Home
}

data object Pipeline : HuaweiSyncScreenDestination {
    override val route = "pipeline"
    override val label = "Pipeline"
    override val icon = Icons.Rounded.Timeline
}

/** Modal-only destination; it can never replace the current screen. */
data object SyncNow : HuaweiSyncDestination {
    override val route = "sync-now"
    override val label = "Sync now"
}

data object Integrations : HuaweiSyncScreenDestination {
    override val route = "integrations"
    override val label = "Integrations"
    override val icon = Icons.Rounded.Settings
}

data object Diagnostics : HuaweiSyncScreenDestination {
    override val route = "diagnostics"
    override val label = "Diagnostics"
    override val icon = Icons.Rounded.Info
}

data object Automation : HuaweiSyncScreenDestination {
    override val route = "automation"
    override val label = "Automation"
    override val icon = Icons.Rounded.Build
}

data object History : HuaweiSyncScreenDestination {
    override val route = "history"
    override val label = "History"
    override val icon = Icons.Rounded.History
}

data object ActivityDetail : HuaweiSyncScreenDestination {
    override val route = "activity-detail"
    override val label = "Activity detail"
    override val icon = Icons.Rounded.Timeline
}

data object AiAssistant : HuaweiSyncScreenDestination {
    override val route = "ai-assistant"
    override val label = "AI Assistant"
    override val icon = Icons.Rounded.Info
}

val PrimaryDestinations: List<HuaweiSyncScreenDestination> = listOf(
    Dashboard,
    Pipeline,
    Integrations,
    Diagnostics,
    Automation,
    History,
    AiAssistant,
)

val CompactDestinations: List<HuaweiSyncScreenDestination> = listOf(
    Dashboard,
    Pipeline,
    History,
)
