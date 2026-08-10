package dev.lui.huaweisync.presentation.theme

import androidx.compose.runtime.Composable
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme as PrototypeTheme

/** Compatibility boundary retained so the M001 Gate 1 presentation entry stays unchanged. */
@Composable
fun HuaweiSyncTheme(content: @Composable () -> Unit) {
    PrototypeTheme(content = content)
}
