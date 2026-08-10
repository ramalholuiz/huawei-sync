package dev.lui.huaweisync.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

/** Active product destinations. Prototype-era routes stay typed below, but are not surfaced. */
sealed interface HuaweiSyncDestination {
    val route: String
    val label: String

    companion object {
        val all: List<HuaweiSyncDestination> = listOf(
            Onboarding,
            Dashboard,
            SyncNow,
            Diagnostics,
            History,
            ActivityDetail,
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

/** Modal-only destination; it can never replace the current screen. */
data object SyncNow : HuaweiSyncDestination {
    override val route = "sync-now"
    override val label = "Sync now"
}

data object Diagnostics : HuaweiSyncScreenDestination {
    override val route = "diagnostics"
    override val label = "Diagnostics"
    override val icon = Icons.Rounded.Info
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

val PrimaryDestinations: List<HuaweiSyncScreenDestination> = listOf(
    Dashboard,
    Diagnostics,
    History,
)

val CompactDestinations: List<HuaweiSyncScreenDestination> = listOf(
    Dashboard,
    Diagnostics,
    History,
)
