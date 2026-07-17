package dev.lui.huaweisync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkMaterialColors = darkColorScheme(
    primary = DarkHuaweiSyncColors.accent,
    onPrimary = DarkHuaweiSyncColors.background,
    primaryContainer = DarkHuaweiSyncColors.accentContainer,
    onPrimaryContainer = DarkHuaweiSyncColors.ink,
    secondary = DarkHuaweiSyncColors.info,
    onSecondary = DarkHuaweiSyncColors.background,
    tertiary = DarkHuaweiSyncColors.success,
    onTertiary = DarkHuaweiSyncColors.background,
    background = DarkHuaweiSyncColors.background,
    onBackground = DarkHuaweiSyncColors.ink,
    surface = DarkHuaweiSyncColors.surface1,
    onSurface = DarkHuaweiSyncColors.ink,
    surfaceVariant = DarkHuaweiSyncColors.surface2,
    onSurfaceVariant = DarkHuaweiSyncColors.ink2,
    error = DarkHuaweiSyncColors.error,
    onError = DarkHuaweiSyncColors.background,
    outline = DarkHuaweiSyncColors.lineStrong,
    outlineVariant = DarkHuaweiSyncColors.line,
    scrim = DarkHuaweiSyncColors.canvasBackground,
)

private val LightMaterialColors = lightColorScheme(
    primary = LightHuaweiSyncColors.accent,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = LightHuaweiSyncColors.accentContainer,
    onPrimaryContainer = LightHuaweiSyncColors.ink,
    secondary = LightHuaweiSyncColors.info,
    onSecondary = LightHuaweiSyncColors.ink,
    tertiary = LightHuaweiSyncColors.success,
    onTertiary = LightHuaweiSyncColors.ink,
    background = LightHuaweiSyncColors.background,
    onBackground = LightHuaweiSyncColors.ink,
    surface = LightHuaweiSyncColors.surface1,
    onSurface = LightHuaweiSyncColors.ink,
    surfaceVariant = LightHuaweiSyncColors.surface2,
    onSurfaceVariant = LightHuaweiSyncColors.ink2,
    error = LightHuaweiSyncColors.error,
    onError = androidx.compose.ui.graphics.Color.White,
    outline = LightHuaweiSyncColors.lineStrong,
    outlineVariant = LightHuaweiSyncColors.line,
    scrim = LightHuaweiSyncColors.canvasBackground,
)

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
 * Native dark/light theme for the approved Calm Health Companion direction.
 * Dynamic color stays disabled because it would replace the approved semantic hue map.
 */
@Composable
fun HuaweiSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val prototypeColors = if (darkTheme) DarkHuaweiSyncColors else LightHuaweiSyncColors
    val materialColors = if (darkTheme) DarkMaterialColors else LightMaterialColors

    CompositionLocalProvider(
        LocalHuaweiSyncColors provides prototypeColors,
        LocalHuaweiSyncTechnicalTypography provides HuaweiSyncTechnicalTypography,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = HuaweiSyncTypography,
            shapes = HuaweiSyncShapes,
            content = content,
        )
    }
}
