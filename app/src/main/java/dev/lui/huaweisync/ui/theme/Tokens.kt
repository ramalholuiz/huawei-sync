package dev.lui.huaweisync.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Modular spacing values copied from the preserved Modernist design-system bundle. */
object HuaweiSyncSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/** Flat geometry preserves the prototype's rules-first layout rather than generic rounded cards. */
object HuaweiSyncGeometry {
    val cornerRadius = 0.dp
    val borderThin = 1.dp
    val borderStrong = 2.dp
}

/**
 * CSS shadow y-offsets translated to Compose elevations.
 *
 * Blur and color remain component responsibilities because Material elevation cannot reproduce CSS
 * shadows exactly. Most structural surfaces should stay at none and use rules instead.
 */
object HuaweiSyncElevation {
    val none = 0.dp
    val small = 1.dp
    val medium = 3.dp
    val large = 12.dp
}

private val ZeroRadiusShape = RoundedCornerShape(HuaweiSyncGeometry.cornerRadius)

val HuaweiSyncShapes = Shapes(
    extraSmall = ZeroRadiusShape,
    small = ZeroRadiusShape,
    medium = ZeroRadiusShape,
    large = ZeroRadiusShape,
    extraLarge = ZeroRadiusShape,
)
