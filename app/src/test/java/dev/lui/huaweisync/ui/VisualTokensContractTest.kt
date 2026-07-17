package dev.lui.huaweisync.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.theme.DarkHuaweiSyncColors
import dev.lui.huaweisync.ui.theme.HuaweiSyncElevation
import dev.lui.huaweisync.ui.theme.HuaweiSyncRadius
import dev.lui.huaweisync.ui.theme.HuaweiSyncShapes
import dev.lui.huaweisync.ui.theme.HuaweiSyncSpacing
import dev.lui.huaweisync.ui.theme.LightHuaweiSyncColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class VisualTokensContractTest {
    @Test
    fun `radius spacing and elevation roles expose the approved scale`() {
        assertEquals(8.dp, HuaweiSyncRadius.chart)
        assertEquals(12.dp, HuaweiSyncRadius.control)
        assertEquals(20.dp, HuaweiSyncRadius.card)
        assertEquals(28.dp, HuaweiSyncRadius.hero)
        assertEquals(28.dp, HuaweiSyncRadius.sheet)
        assertEquals(999.dp, HuaweiSyncRadius.chip)
        assertEquals(20.dp, HuaweiSyncSpacing.gutter)
        assertEquals(40.dp, HuaweiSyncSpacing.section)
        assertEquals(0.dp, HuaweiSyncElevation.none)
        assertEquals(3.dp, HuaweiSyncElevation.overlay)
        assertEquals(3.dp, HuaweiSyncElevation.fab)
    }

    @Test
    fun `material defaults resolve to control card or hero shapes`() {
        assertEquals(RoundedCornerShape(HuaweiSyncRadius.control), HuaweiSyncShapes.extraSmall)
        assertEquals(RoundedCornerShape(HuaweiSyncRadius.control), HuaweiSyncShapes.small)
        assertEquals(RoundedCornerShape(HuaweiSyncRadius.card), HuaweiSyncShapes.medium)
        assertEquals(RoundedCornerShape(HuaweiSyncRadius.card), HuaweiSyncShapes.large)
        assertEquals(RoundedCornerShape(HuaweiSyncRadius.hero), HuaweiSyncShapes.extraLarge)
        assertNotEquals(RoundedCornerShape(0.dp), HuaweiSyncShapes.medium)
    }

    @Test
    fun `both palettes expose distinct semantic and tonal roles`() {
        listOf(DarkHuaweiSyncColors, LightHuaweiSyncColors).forEach { palette ->
            assertNotEquals(palette.surface1, palette.surface2)
            assertNotEquals(palette.surface2, palette.surface3)
            assertNotEquals(palette.accent, palette.attention)
            assertNotEquals(palette.accent, palette.error)
            assertNotEquals(palette.success, palette.attention)
            assertNotEquals(palette.neutralHigh, palette.neutralLow)
            assertNotEquals(palette.surfaceAccentWash, palette.surfaceAttentionWash)
            assertEquals(0x14, (palette.accentSubtle.alpha * 255).toInt())
        }
    }
}
