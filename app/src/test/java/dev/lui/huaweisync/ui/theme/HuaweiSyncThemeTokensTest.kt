package dev.lui.huaweisync.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class HuaweiSyncThemeTokensTest {
    @Test
    fun `dark palette matches audited HTML tokens`() {
        assertEquals(Color(0xFF0B0A09), DarkHuaweiSyncColors.background)
        assertEquals(Color(0xFF131211), DarkHuaweiSyncColors.surface1)
        assertEquals(Color(0xFF1C1A18), DarkHuaweiSyncColors.surface2)
        assertEquals(Color(0xFF26231F), DarkHuaweiSyncColors.surface3)
        assertEquals(Color(0xFFF3F2F2), DarkHuaweiSyncColors.ink)
        assertEquals(Color(0x9EF3F2F2), DarkHuaweiSyncColors.prototypeInk2)
        assertEquals(Color(0xFFAAA8A8), DarkHuaweiSyncColors.ink2)
        assertEquals(Color(0x85F3F2F2), DarkHuaweiSyncColors.ink3)
        assertEquals(Color(0x2EF3F2F2), DarkHuaweiSyncColors.ink4)
        assertEquals(Color(0x1AF3F2F2), DarkHuaweiSyncColors.line)
        assertEquals(Color(0x38F3F2F2), DarkHuaweiSyncColors.lineStrong)
        assertEquals(Color(0xFF050403), DarkHuaweiSyncColors.canvasBackground)
    }

    @Test
    fun `light palette matches audited HTML overrides and preserves semantic colors`() {
        assertEquals(Color(0xFFF3F2F2), LightHuaweiSyncColors.background)
        assertEquals(Color(0xFFFFFFFF), LightHuaweiSyncColors.surface1)
        assertEquals(Color(0xFFEAE9E9), LightHuaweiSyncColors.surface2)
        assertEquals(Color(0xFFD7D3D3), LightHuaweiSyncColors.surface3)
        assertEquals(Color(0xFF201E1D), LightHuaweiSyncColors.ink)
        assertEquals(Color(0x9E201E1D), LightHuaweiSyncColors.prototypeInk2)
        assertEquals(Color(0xFF5B5755), LightHuaweiSyncColors.ink2)
        assertEquals(Color(0x6B201E1D), LightHuaweiSyncColors.ink3)
        assertEquals(Color(0x33201E1D), LightHuaweiSyncColors.ink4)
        assertEquals(Color(0x1A201E1D), LightHuaweiSyncColors.line)
        assertEquals(Color(0x38201E1D), LightHuaweiSyncColors.lineStrong)
        assertEquals(Color(0xFFE7E3DE), LightHuaweiSyncColors.canvasBackground)
        assertEquals(DarkHuaweiSyncColors.accent, LightHuaweiSyncColors.accent)
        assertEquals(DarkHuaweiSyncColors.ok, LightHuaweiSyncColors.ok)
        assertEquals(DarkHuaweiSyncColors.warning, LightHuaweiSyncColors.warning)
        assertEquals(DarkHuaweiSyncColors.info, LightHuaweiSyncColors.info)
    }

    @Test
    fun `geometry translates audited modular scale and modernist zero radius`() {
        assertEquals(4.dp, HuaweiSyncSpacing.xs)
        assertEquals(8.dp, HuaweiSyncSpacing.sm)
        assertEquals(12.dp, HuaweiSyncSpacing.md)
        assertEquals(16.dp, HuaweiSyncSpacing.lg)
        assertEquals(24.dp, HuaweiSyncSpacing.xl)
        assertEquals(32.dp, HuaweiSyncSpacing.xxl)
        assertEquals(0.dp, HuaweiSyncGeometry.cornerRadius)
        assertEquals(1.dp, HuaweiSyncGeometry.borderThin)
        assertEquals(2.dp, HuaweiSyncGeometry.borderStrong)
        assertEquals(1.dp, HuaweiSyncElevation.small)
        assertEquals(3.dp, HuaweiSyncElevation.medium)
        assertEquals(12.dp, HuaweiSyncElevation.large)
    }

    @Test
    fun `typography uses documented local system fallbacks and audited hierarchy`() {
        assertEquals(FontFamily.SansSerif, HuaweiSyncTypography.displayLarge.fontFamily)
        assertEquals(FontWeight.ExtraBold, HuaweiSyncTypography.displayLarge.fontWeight)
        assertEquals(56.sp, HuaweiSyncTypography.displayLarge.fontSize)
        assertEquals(57.sp, HuaweiSyncTypography.displayLarge.lineHeight)
        assertEquals(FontFamily.Monospace, HuaweiSyncTechnicalTypography.label.fontFamily)
        assertEquals(FontWeight.SemiBold, HuaweiSyncTechnicalTypography.label.fontWeight)
        assertEquals(10.sp, HuaweiSyncTechnicalTypography.label.fontSize)
        assertEquals(1.4.sp, HuaweiSyncTechnicalTypography.label.letterSpacing)
        assertEquals(FontFamily.Monospace, HuaweiSyncTechnicalTypography.value.fontFamily)
    }

    @Test
    fun `page title role hits Direction A's 28sp ExtraBold heading`() {
        assertEquals(28.sp, HuaweiSyncTypography.headlineLarge.fontSize)
        assertEquals(FontWeight.ExtraBold, HuaweiSyncTypography.headlineLarge.fontWeight)
    }
}
