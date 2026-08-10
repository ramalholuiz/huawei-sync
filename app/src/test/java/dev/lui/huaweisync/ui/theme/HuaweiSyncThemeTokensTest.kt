package dev.lui.huaweisync.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HuaweiSyncThemeTokensTest {
    @Test
    fun `dark palette matches approved Calm Health Companion roles`() {
        assertEquals(Color(0xFF0F1210), DarkHuaweiSyncColors.background)
        assertEquals(Color(0xFF171A18), DarkHuaweiSyncColors.surface1)
        assertEquals(Color(0xFF1F2321), DarkHuaweiSyncColors.surface2)
        assertEquals(Color(0xFF262A28), DarkHuaweiSyncColors.surface3)
        assertEquals(Color(0xFFF4F5F1), DarkHuaweiSyncColors.ink)
        assertEquals(Color(0xFFB8BAB5), DarkHuaweiSyncColors.ink2)
        assertEquals(Color(0xFF7CCFA5), DarkHuaweiSyncColors.accent)
        assertEquals(Color(0xFFE7A356), DarkHuaweiSyncColors.attention)
        assertEquals(Color(0xFFF2B8B5), DarkHuaweiSyncColors.error)
    }

    @Test
    fun `light palette matches approved Calm Health Companion roles`() {
        assertEquals(Color(0xFFF7F7F5), LightHuaweiSyncColors.background)
        assertEquals(Color(0xFFFFFFFF), LightHuaweiSyncColors.surface1)
        assertEquals(Color(0xFFF1F1EE), LightHuaweiSyncColors.surface2)
        assertEquals(Color(0xFFE8E8E4), LightHuaweiSyncColors.surface3)
        assertEquals(Color(0xFF12140F), LightHuaweiSyncColors.ink)
        assertEquals(Color(0xFF545651), LightHuaweiSyncColors.ink2)
        assertEquals(Color(0xFF2E7D5B), LightHuaweiSyncColors.accent)
        assertEquals(Color(0xFFB3261E), LightHuaweiSyncColors.error)
        assertNotEquals(LightHuaweiSyncColors.accent, LightHuaweiSyncColors.attention)
    }

    @Test
    fun `geometry exposes the rounded role scale and sparse elevation model`() {
        assertEquals(4.dp, HuaweiSyncSpacing.xs)
        assertEquals(8.dp, HuaweiSyncSpacing.sm)
        assertEquals(12.dp, HuaweiSyncSpacing.md)
        assertEquals(16.dp, HuaweiSyncSpacing.lg)
        assertEquals(20.dp, HuaweiSyncSpacing.gutter)
        assertEquals(24.dp, HuaweiSyncSpacing.xl)
        assertEquals(32.dp, HuaweiSyncSpacing.xxl)
        assertEquals(40.dp, HuaweiSyncSpacing.section)
        assertEquals(12.dp, HuaweiSyncRadius.control)
        assertEquals(20.dp, HuaweiSyncRadius.card)
        assertEquals(28.dp, HuaweiSyncRadius.hero)
        assertEquals(1.dp, HuaweiSyncGeometry.borderThin)
        assertEquals(2.dp, HuaweiSyncGeometry.borderStrong)
        assertEquals(0.dp, HuaweiSyncElevation.none)
        assertEquals(3.dp, HuaweiSyncElevation.overlay)
        assertEquals(3.dp, HuaweiSyncElevation.fab)
    }

    @Test
    fun `typography uses local fallbacks and the approved hierarchy`() {
        assertEquals(FontFamily.SansSerif, HuaweiSyncTypography.displayLarge.fontFamily)
        assertEquals(FontWeight.ExtraBold, HuaweiSyncTypography.displayLarge.fontWeight)
        assertEquals(56.sp, HuaweiSyncTypography.displayLarge.fontSize)
        assertEquals(36.sp, HuaweiSyncTypography.displayMedium.fontSize)
        assertEquals(28.sp, HuaweiSyncTypography.displaySmall.fontSize)
        assertEquals(FontFamily.Monospace, HuaweiSyncTechnicalTypography.label.fontFamily)
        assertEquals(FontWeight.SemiBold, HuaweiSyncTechnicalTypography.label.fontWeight)
        assertEquals(10.sp, HuaweiSyncTechnicalTypography.label.fontSize)
        assertEquals(FontFamily.Monospace, HuaweiSyncTechnicalTypography.value.fontFamily)
    }

    @Test
    fun `page title role keeps Direction A heading weight`() {
        assertEquals(28.sp, HuaweiSyncTypography.headlineLarge.fontSize)
        assertEquals(FontWeight.ExtraBold, HuaweiSyncTypography.headlineLarge.fontWeight)
    }
}
