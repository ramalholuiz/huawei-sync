package dev.lui.huaweisync.ui.theme

import androidx.compose.ui.unit.dp

/** App-wide spacing scale. Screen structure remains unchanged while surfaces adopt this rhythm. */
object HuaweiSyncSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val gutter = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val section = 40.dp
}

/** Radius roles are keyed to the size and purpose of a surface, not to individual screens. */
object HuaweiSyncRadius {
    val chart = 8.dp
    val control = 12.dp
    val card = 20.dp
    val hero = 28.dp
    val sheet = 28.dp
    val chip = 999.dp
}

/** Hairlines remain available for controls; card depth is tonal and borderless by default. */
object HuaweiSyncGeometry {
    val borderThin = 1.dp
    val borderStrong = 2.dp
}

/** Elevation is intentionally sparse: structural cards use tone, overlays use one soft lift. */
object HuaweiSyncElevation {
    val none = 0.dp
    val overlay = 3.dp
    val fab = 3.dp
}
