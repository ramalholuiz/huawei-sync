package dev.lui.huaweisync.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HuaweiSyncThemeOptionContractTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `theme model exposes exactly the four approved options in cycle order`() {
        assertEquals(
            listOf(
                HuaweiSyncThemeOption.SageLight,
                HuaweiSyncThemeOption.SageDark,
                HuaweiSyncThemeOption.EmberLight,
                HuaweiSyncThemeOption.EmberDark,
            ),
            HuaweiSyncThemeOption.entries,
        )
        assertEquals(HuaweiSyncThemeOption.SageDark, HuaweiSyncThemeOption.sageForSystemDark(true))
        assertEquals(HuaweiSyncThemeOption.SageLight, HuaweiSyncThemeOption.sageForSystemDark(false))
        assertEquals(HuaweiSyncThemeOption.SageDark, HuaweiSyncThemeOption.SageLight.next())
        assertEquals(HuaweiSyncThemeOption.EmberLight, HuaweiSyncThemeOption.SageDark.next())
        assertEquals(HuaweiSyncThemeOption.EmberDark, HuaweiSyncThemeOption.EmberLight.next())
        assertEquals(HuaweiSyncThemeOption.SageLight, HuaweiSyncThemeOption.EmberDark.next())
    }

    @Test
    fun `each option selects its own semantic and Material tokens`() {
        val observed = mutableMapOf<HuaweiSyncThemeOption, Triple<HuaweiSyncColors, Color, Color>>()

        compose.setContent {
            HuaweiSyncThemeOption.entries.forEach { option ->
                HuaweiSyncTheme(themeOption = option) {
                    val colors = HuaweiSyncTheme.colors
                    val primary = MaterialTheme.colorScheme.primary
                    val onPrimary = MaterialTheme.colorScheme.onPrimary
                    SideEffect {
                        observed[option] = Triple(colors, primary, onPrimary)
                    }
                }
            }
        }
        compose.waitForIdle()

        HuaweiSyncThemeOption.entries.forEach { option ->
            val (semanticColors, materialPrimary, materialOnPrimary) = observed.getValue(option)
            assertEquals(option.colors, semanticColors)
            assertEquals(option.colors.accent, materialPrimary)
            assertEquals(expectedActionForeground(option), materialOnPrimary)
        }
    }

    @Test
    fun `Sage palettes preserve the current Cycle 5 values`() {
        assertPalette(
            HuaweiSyncThemeOption.SageLight.colors,
            background = 0xFFF7F7F5,
            surface1 = 0xFFFFFFFF,
            surface2 = 0xFFF1F1EE,
            surface3 = 0xFFE8E8E4,
            accent = 0xFF2E7D5B,
            accentForeground = 0xFF2E7D5B,
            accentContainer = 0xFFC7E4D4,
            success = 0xFF2E7D5B,
            warning = 0xFF8A5200,
            info = 0xFF315F8E,
        )
        assertPalette(
            HuaweiSyncThemeOption.SageDark.colors,
            background = 0xFF0F1210,
            surface1 = 0xFF171A18,
            surface2 = 0xFF1F2321,
            surface3 = 0xFF262A28,
            accent = 0xFF7CCFA5,
            accentForeground = 0xFF7CCFA5,
            accentContainer = 0xFF244B39,
            success = 0xFF7CCFA5,
            warning = 0xFFE7A356,
            info = 0xFF9FC9FF,
        )
    }

    @Test
    fun `Ember palettes restore the original red identity and black dark base`() {
        assertPalette(
            HuaweiSyncThemeOption.EmberLight.colors,
            background = 0xFFF3F2F2,
            surface1 = 0xFFFFFFFF,
            surface2 = 0xFFEAE9E9,
            surface3 = 0xFFD7D3D3,
            accent = 0xFFEC3013,
            accentForeground = 0xFFA61B12,
            accentContainer = 0xFFDF2B10,
            success = 0xFF1B5E20,
            warning = 0xFF8A5200,
            info = 0xFF315F8E,
        )
        assertPalette(
            HuaweiSyncThemeOption.EmberDark.colors,
            background = 0xFF0B0A09,
            surface1 = 0xFF131211,
            surface2 = 0xFF1C1A18,
            surface3 = 0xFF26231F,
            accent = 0xFFEC3013,
            accentForeground = 0xFFFF6B55,
            accentContainer = 0xFFDF2B10,
            success = 0xFF4ADE80,
            warning = 0xFFF5A524,
            info = 0xFF6EA8FF,
        )
        assertEquals(0xFF050403.toInt(), HuaweiSyncThemeOption.EmberDark.colors.canvasBackground.toArgb())
    }

    @Test
    fun `text actions state colors and accent foreground meet WCAG AA in all themes`() {
        HuaweiSyncThemeOption.entries.forEach { option ->
            val colors = option.colors
            listOf(colors.surface1, colors.surface2, colors.surface3).forEach { surface ->
                assertContrast("${option.label} ink on surface", colors.ink, surface)
                assertContrast("${option.label} secondary ink on surface", colors.ink2, surface)
            }
            foregroundRoleSurfaces(option).forEach { surface ->
                assertContrast("${option.label} accent foreground on neutral surface", colors.accentForeground, surface)
                assertContrast("${option.label} success on neutral surface", colors.success, surface)
                assertContrast("${option.label} warning on neutral surface", colors.warning, surface)
                assertContrast("${option.label} attention on neutral surface", colors.attention, surface)
                assertContrast("${option.label} info on neutral surface", colors.info, surface)
                assertContrast("${option.label} error on neutral surface", colors.error, surface)
            }
            assertContrast("${option.label} primary action", expectedActionForeground(option), colors.accent)
        }
    }

    private fun foregroundRoleSurfaces(option: HuaweiSyncThemeOption): List<Color> = when (option) {
        HuaweiSyncThemeOption.SageLight -> listOf(option.colors.surface1)
        HuaweiSyncThemeOption.SageDark -> listOf(option.colors.surface1, option.colors.surface2)
        HuaweiSyncThemeOption.EmberLight -> listOf(option.colors.surface1, option.colors.surface2)
        HuaweiSyncThemeOption.EmberDark -> listOf(option.colors.surface1, option.colors.surface2)
    }

    private fun expectedActionForeground(option: HuaweiSyncThemeOption): Color = when (option) {
        HuaweiSyncThemeOption.SageLight -> Color.White
        HuaweiSyncThemeOption.SageDark -> option.colors.background
        HuaweiSyncThemeOption.EmberLight -> Color.Black
        HuaweiSyncThemeOption.EmberDark -> option.colors.background
    }

    private fun assertPalette(
        colors: HuaweiSyncColors,
        background: Long,
        surface1: Long,
        surface2: Long,
        surface3: Long,
        accent: Long,
        accentForeground: Long,
        accentContainer: Long,
        success: Long,
        warning: Long,
        info: Long,
    ) {
        assertEquals(background.toInt(), colors.background.toArgb())
        assertEquals(surface1.toInt(), colors.surface1.toArgb())
        assertEquals(surface2.toInt(), colors.surface2.toArgb())
        assertEquals(surface3.toInt(), colors.surface3.toArgb())
        assertEquals(accent.toInt(), colors.accent.toArgb())
        assertEquals(accentForeground.toInt(), colors.accentForeground.toArgb())
        assertEquals(accentContainer.toInt(), colors.accentContainer.toArgb())
        assertEquals(success.toInt(), colors.success.toArgb())
        assertEquals(warning.toInt(), colors.warning.toArgb())
        assertEquals(info.toInt(), colors.info.toArgb())
    }

    private fun assertContrast(name: String, foreground: Color, background: Color) {
        val contrast = contrastRatio(foreground, background)
        assertTrue("$name contrast $contrast must be at least 4.5", contrast >= 4.5)
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val light = maxOf(first.relativeLuminance(), second.relativeLuminance())
        val dark = minOf(first.relativeLuminance(), second.relativeLuminance())
        return (light + 0.05) / (dark + 0.05)
    }

    private fun Color.relativeLuminance(): Double {
        fun channel(value: Float): Double {
            val normalized = value.toDouble()
            return if (normalized <= 0.03928) {
                normalized / 12.92
            } else {
                Math.pow((normalized + 0.055) / 1.055, 2.4)
            }
        }
        return 0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)
    }
}
