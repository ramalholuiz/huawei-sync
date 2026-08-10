package dev.lui.huaweisync.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

@Preview(name = "Modernist primitives dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Modernist primitives light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ModernistPrimitivesPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = false) {
            Column(
                modifier = Modifier
                    .background(HuaweiSyncTheme.colors.background)
                    .padding(HuaweiSyncSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
            ) {
                SectionHeader(title = "Sync pipeline", eyebrow = "LIVE STATUS") {
                    StatusLabel(ModernistStatus.Ready)
                }
                ModernistSurface(modifier = Modifier.fillMaxWidth()) {
                    TechnicalMicrocopy("HC.WRITE / DETERMINISTIC-ID")
                    ModernistProgress(progress = 0.74f, label = "records prepared")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.md)) {
                    StraightEdgeButton(label = "Review", onClick = {})
                    SyncFab(state = SyncFabState.Idle, onClick = {})
                    SyncFab(state = SyncFabState.Complete, onClick = {})
                }
                ModernistBottomNavigation(
                    items = previewNavigationItems,
                    selectedKey = "home",
                    onSelect = {},
                )
            }
        }
    }
}

@Preview(name = "Reduced motion treatments", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReducedMotionPreview() {
    HuaweiSyncTheme {
        HuaweiSyncMotionProvider(reducedMotion = true) {
            Column(
                modifier = Modifier
                    .background(HuaweiSyncTheme.colors.background)
                    .padding(HuaweiSyncSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
            ) {
                StatusLabel(ModernistStatus.Syncing)
                SyncFab(state = SyncFabState.Syncing, onClick = {})
                ModernistProgress(progress = 0.42f, label = "writing")
            }
        }
    }
}

private val previewNavigationItems = listOf(
    ModernistNavigationItem("home", "Home", Icons.Rounded.Home),
    ModernistNavigationItem("pipeline", "Pipeline", Icons.Rounded.Timeline),
    ModernistNavigationItem("settings", "Settings", Icons.Rounded.Settings),
)
