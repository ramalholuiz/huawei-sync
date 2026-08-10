package dev.lui.huaweisync.presentation

import androidx.compose.runtime.Composable
import dev.lui.huaweisync.ui.navigation.Diagnostics

internal const val GATE1_ROUTE = "gate1"

/**
 * Compatibility entry for the proven Gate 1 presentation tests. Product runtime uses the same
 * complete shell directly from MainActivity; this adapter starts on diagnostics for legacy callers.
 */
@Composable
fun HuaweiSyncRoot(gate1Entry: @Composable () -> Unit) {
    dev.lui.huaweisync.ui.HuaweiSyncRoot(
        gate1Entry = gate1Entry,
        initialDestination = Diagnostics,
    )
}
