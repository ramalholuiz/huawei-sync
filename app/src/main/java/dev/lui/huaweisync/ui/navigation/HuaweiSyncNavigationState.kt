package dev.lui.huaweisync.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** Single owner for screen navigation, the compact menu, and the modal sync destination. */
@Stable
class HuaweiSyncNavigationState internal constructor(
    initialDestination: HuaweiSyncScreenDestination,
) {
    var currentDestination: HuaweiSyncScreenDestination by mutableStateOf(initialDestination)
        private set

    var overlayDestination: SyncNow? by mutableStateOf(null)
        private set

    var compactMenuVisible: Boolean by mutableStateOf(false)
        private set

    fun navigateTo(destination: HuaweiSyncScreenDestination) {
        currentDestination = destination
        compactMenuVisible = false
        overlayDestination = null
    }

    fun showSyncOverlay() {
        compactMenuVisible = false
        overlayDestination = SyncNow
    }

    fun dismissSyncOverlay() {
        overlayDestination = null
    }

    fun showCompactMenu() {
        compactMenuVisible = true
    }

    fun dismissCompactMenu() {
        compactMenuVisible = false
    }

    companion object {
        val Saver: Saver<HuaweiSyncNavigationState, String> = Saver(
            save = { it.currentDestination.route },
            restore = { route ->
                val destination = HuaweiSyncDestination.all
                    .filterIsInstance<HuaweiSyncScreenDestination>()
                    .singleOrNull { it.route == route }
                    ?: Dashboard
                HuaweiSyncNavigationState(destination)
            },
        )
    }
}

@Composable
fun rememberHuaweiSyncNavigationState(
    initialDestination: HuaweiSyncScreenDestination = Dashboard,
): HuaweiSyncNavigationState = rememberSaveable(
    saver = HuaweiSyncNavigationState.Saver,
) {
    HuaweiSyncNavigationState(initialDestination)
}
