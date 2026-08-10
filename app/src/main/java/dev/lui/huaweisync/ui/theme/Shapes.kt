package dev.lui.huaweisync.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/** Material defaults mapped onto the app radius scale; no shipping default is square. */
val HuaweiSyncShapes = Shapes(
    extraSmall = RoundedCornerShape(HuaweiSyncRadius.control),
    small = RoundedCornerShape(HuaweiSyncRadius.control),
    medium = RoundedCornerShape(HuaweiSyncRadius.card),
    large = RoundedCornerShape(HuaweiSyncRadius.card),
    extraLarge = RoundedCornerShape(HuaweiSyncRadius.hero),
)

val HuaweiSyncSheetShape = RoundedCornerShape(
    topStart = HuaweiSyncRadius.sheet,
    topEnd = HuaweiSyncRadius.sheet,
)
