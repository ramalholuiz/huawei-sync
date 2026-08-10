package dev.lui.huaweisync.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import dev.lui.huaweisync.ui.components.SectionHeader
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.theme.DarkHuaweiSyncColors
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import dev.lui.huaweisync.ui.theme.LightHuaweiSyncColors
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h844dp")
class AccessibilityResponsiveRegressionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `large font stacks section action after heading instead of overlapping`() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                HuaweiSyncTheme(darkTheme = true) {
                    SectionHeader(
                        title = "A deliberately long destination heading",
                        modifier = Modifier,
                        eyebrow = "RESPONSIVE",
                        trailing = { StraightEdgeButton(label = "Action", onClick = {}) },
                    )
                }
            }
        }

        val heading = compose.onNodeWithText("A deliberately long destination heading")
            .fetchSemanticsNode().boundsInRoot
        val action = compose.onNodeWithText("Action", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        assertBelow(action = action, heading = heading)
    }

    @Test
    fun `runtime semantic foregrounds meet WCAG AA contrast`() {
        listOf(DarkHuaweiSyncColors, LightHuaweiSyncColors).forEach { palette ->
            listOf(palette.background, palette.surface1).forEach { surface ->
                assertTrue(
                    "secondary text contrast was ${contrastRatio(palette.ink2, surface)}",
                    contrastRatio(palette.ink2, surface) >= 4.5,
                )
            }
            listOf(palette.background, palette.surface1, palette.surface2, palette.surface3).forEach { surface ->
                assertTrue(
                    "accent foreground contrast was ${contrastRatio(palette.accentForeground, surface)}",
                    contrastRatio(palette.accentForeground, surface) >= 4.5,
                )
            }
            val onAccent = if (palette === DarkHuaweiSyncColors) palette.background else Color.White
            assertTrue(
                "primary action contrast was ${contrastRatio(onAccent, palette.accent)}",
                contrastRatio(onAccent, palette.accent) >= 4.5,
            )
            listOf(palette.success, palette.attention, palette.error, palette.info).forEach { status ->
                assertTrue(
                    "semantic status contrast was ${contrastRatio(status, palette.surface1)}",
                    contrastRatio(status, palette.surface1) >= 4.5,
                )
            }
        }
    }

    @Test
    fun `dark ink3 disclaimer contrast meets WCAG AA on every dark surface`() {
        val palette = DarkHuaweiSyncColors
        listOf(palette.background, palette.surface1, palette.surface2, palette.surface3).forEach { surface ->
            val ratio = contrastRatio(palette.ink3, surface)
            assertTrue(
                "dark ink3 body-text contrast was $ratio against $surface (needs >= 4.5)",
                ratio >= 4.5,
            )
        }
    }

    @Test
    fun `active destinations opt into the screenshot matrix`() {
        val sourceRoot = File("src/main/java/dev/lui/huaweisync/ui/screens")
        val screens = mapOf(
            "onboarding/OnboardingScreen.kt" to "OnboardingLoadingPreview",
            "dashboard/DashboardScreen.kt" to "DashboardVerifiedPreview",
            "sync/SyncNowModal.kt" to "SyncNowModalPreview",
            "diagnostics/DiagnosticsScreen.kt" to "VerifiedDiagnosticsPreview",
            "history/HistoryScreen.kt" to "HistoryContentPreview",
            "detail/ActivityDetailScreen.kt" to "VerifiedDetailPreview",
        )

        assertEquals(6, screens.size)
        screens.forEach { (relativePath, previewFunction) ->
            val source = File(sourceRoot, relativePath).readText()
            val marker = "@dev.lui.huaweisync.ui.preview.HuaweiSyncScreenshotPreviews\n@Composable\nprivate fun $previewFunction"
            assertTrue("$relativePath is missing the screenshot matrix", source.contains(marker))
        }
    }

    private fun assertBelow(action: Rect, heading: Rect) {
        assertTrue("action $action overlaps heading $heading", action.top >= heading.bottom)
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val opaque = composite(foreground, background)
        val lighter = max(luminance(opaque), luminance(background))
        val darker = min(luminance(opaque), luminance(background))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun composite(foreground: Color, background: Color): Color = Color(
        red = foreground.red * foreground.alpha + background.red * (1f - foreground.alpha),
        green = foreground.green * foreground.alpha + background.green * (1f - foreground.alpha),
        blue = foreground.blue * foreground.alpha + background.blue * (1f - foreground.alpha),
        alpha = 1f,
    )

    private fun luminance(color: Color): Double =
        0.2126 * linearize(color.red) +
            0.7152 * linearize(color.green) +
            0.0722 * linearize(color.blue)

    private fun linearize(channel: Float): Double {
        val value = channel.toDouble()
        return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
    }
}
