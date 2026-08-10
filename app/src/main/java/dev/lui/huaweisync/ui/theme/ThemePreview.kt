package dev.lui.huaweisync.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "HuaweiSync dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HuaweiSyncDarkThemePreview() {
    ThemeTokenPreview(darkTheme = true)
}

@Preview(name = "HuaweiSync light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun HuaweiSyncLightThemePreview() {
    ThemeTokenPreview(darkTheme = false)
}

@Composable
private fun ThemeTokenPreview(darkTheme: Boolean) {
    HuaweiSyncTheme(darkTheme = darkTheme) {
        Surface(color = HuaweiSyncTheme.colors.background) {
            Column(
                modifier = Modifier.padding(HuaweiSyncSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(HuaweiSyncSpacing.lg),
            ) {
                Text("HUAWEI SYNC", style = MaterialTheme.typography.displaySmall)
                Text(
                    "HEALTH CONNECT / READY",
                    color = HuaweiSyncTheme.colors.ink2,
                    style = HuaweiSyncTheme.technicalTypography.label,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HuaweiSyncTheme.colors.surface1)
                        .border(
                            width = HuaweiSyncGeometry.borderThin,
                            color = HuaweiSyncTheme.colors.lineStrong,
                        )
                        .padding(HuaweiSyncSpacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Last sync", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "08:42:16",
                        color = HuaweiSyncTheme.colors.ok,
                        style = HuaweiSyncTheme.technicalTypography.value,
                    )
                }
            }
        }
    }
}
