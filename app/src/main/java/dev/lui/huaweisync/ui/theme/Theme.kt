package dev.lui.huaweisync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private fun materialColorsFor(themeOption: HuaweiSyncThemeOption): ColorScheme {
    val colors = themeOption.colors
    return if (themeOption.isDark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.background,
            primaryContainer = colors.accentContainer,
            onPrimaryContainer = colors.ink,
            secondary = colors.info,
            onSecondary = colors.background,
            tertiary = colors.success,
            onTertiary = colors.background,
            background = colors.background,
            onBackground = colors.ink,
            surface = colors.surface1,
            onSurface = colors.ink,
            surfaceVariant = colors.surface2,
            onSurfaceVariant = colors.ink2,
            error = colors.error,
            onError = colors.background,
            outline = colors.lineStrong,
            outlineVariant = colors.line,
            scrim = colors.canvasBackground,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = if (themeOption == HuaweiSyncThemeOption.EmberLight) Color.Black else Color.White,
            primaryContainer = colors.accentContainer,
            onPrimaryContainer = colors.ink,
            secondary = colors.info,
            onSecondary = colors.ink,
            tertiary = colors.success,
            onTertiary = colors.ink,
            background = colors.background,
            onBackground = colors.ink,
            surface = colors.surface1,
            onSurface = colors.ink,
            surfaceVariant = colors.surface2,
            onSurfaceVariant = colors.ink2,
            error = colors.error,
            onError = Color.White,
            outline = colors.lineStrong,
            outlineVariant = colors.line,
            scrim = colors.canvasBackground,
        )
    }
}

private val LocalHuaweiSyncColors = staticCompositionLocalOf { DarkHuaweiSyncColors }
private val LocalHuaweiSyncTechnicalTypography =
    staticCompositionLocalOf { HuaweiSyncTechnicalTypography }

/** Access to app-specific semantic tokens that have no one-to-one Material role. */
object HuaweiSyncTheme {
    val colors: HuaweiSyncColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHuaweiSyncColors.current

    val technicalTypography: HuaweiSyncTechnicalType
        @Composable
        @ReadOnlyComposable
        get() = LocalHuaweiSyncTechnicalTypography.current
}

/**
 * App theme for the approved product surface.
 * Dynamic color stays disabled because it would replace the approved semantic hue map.
 */
@Composable
fun HuaweiSyncTheme(
    themeOption: HuaweiSyncThemeOption = HuaweiSyncThemeOption.sageForSystemDark(isSystemInDarkTheme()),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalHuaweiSyncColors provides themeOption.colors,
        LocalHuaweiSyncTechnicalTypography provides HuaweiSyncTechnicalTypography,
    ) {
        MaterialTheme(
            colorScheme = materialColorsFor(themeOption),
            typography = HuaweiSyncTypography,
            shapes = HuaweiSyncShapes,
            content = content,
        )
    }
}

@Composable
fun HuaweiSyncTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    HuaweiSyncTheme(
        themeOption = HuaweiSyncThemeOption.sageForSystemDark(darkTheme),
        content = content,
    )
}
