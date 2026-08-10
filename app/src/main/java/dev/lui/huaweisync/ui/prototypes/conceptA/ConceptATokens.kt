package dev.lui.huaweisync.ui.prototypes.conceptA

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

/**
 * Local tokens for Concept A (Soft Premium Health). Nothing here reads
 * `HuaweiSyncShapes` or `HuaweiSyncGeometry` — the concept is deliberately
 * rounded and calm, and must not inherit the production right-angle vocabulary.
 */
object ConceptATokens {
    val radiusChip = 12.dp
    val radiusInset = 16.dp
    val radiusCard = 20.dp
    val radiusHero = 28.dp

    val paddingCardInner = 24.dp
    val paddingHeroInner = 28.dp
    val paddingScreenH = 20.dp
    val paddingScreenV = 24.dp

    val gapCards = 20.dp
    val gapSections = 32.dp
    val gapContent = 12.dp

    val heroShape = RoundedCornerShape(radiusHero)
    val cardShape = RoundedCornerShape(radiusCard)
    val insetShape = RoundedCornerShape(radiusInset)
    val chipShape = RoundedCornerShape(radiusChip)
    val fabShape = RoundedCornerShape(28.dp)

    val fabDiameter = 56.dp

    val pulseDotSize = 10.dp

    @Composable
    @ReadOnlyComposable
    fun accentWash(): Brush = Brush.verticalGradient(
        colors = listOf(
            HuaweiSyncTheme.colors.ok.copy(alpha = 0.10f),
            Color.Transparent,
        ),
    )

    @Composable
    @ReadOnlyComposable
    fun waitingWash(): Brush = Brush.verticalGradient(
        colors = listOf(
            HuaweiSyncTheme.colors.info.copy(alpha = 0.10f),
            Color.Transparent,
        ),
    )

    @Composable
    @ReadOnlyComposable
    fun errorWash(): Brush = Brush.verticalGradient(
        colors = listOf(
            HuaweiSyncTheme.colors.accent.copy(alpha = 0.09f),
            Color.Transparent,
        ),
    )
}
