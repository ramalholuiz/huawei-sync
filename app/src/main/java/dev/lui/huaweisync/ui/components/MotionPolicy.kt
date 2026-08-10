package dev.lui.huaweisync.ui.components

import android.animation.ValueAnimator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** Audited motion timings, with one explicit reduced-motion branch for every component. */
@Immutable
data class HuaweiSyncMotionPolicy(
    val reducedMotion: Boolean,
    val fastMillis: Int,
    val standardMillis: Int,
    val deliberateMillis: Int,
    val syncRotationMillis: Int,
) {
    init {
        require(fastMillis >= 0)
        require(standardMillis >= 0)
        require(deliberateMillis >= 0)
        require(syncRotationMillis >= 0)
        if (reducedMotion) {
            require(fastMillis == 0 && standardMillis == 0 && deliberateMillis == 0)
            require(syncRotationMillis == 0)
        }
    }

    companion object {
        val Standard = HuaweiSyncMotionPolicy(
            reducedMotion = false,
            fastMillis = 160,
            standardMillis = 240,
            deliberateMillis = 480,
            syncRotationMillis = 1_000,
        )

        val Reduced = HuaweiSyncMotionPolicy(
            reducedMotion = true,
            fastMillis = 0,
            standardMillis = 0,
            deliberateMillis = 0,
            syncRotationMillis = 0,
        )

        fun forReducedMotion(reducedMotion: Boolean): HuaweiSyncMotionPolicy =
            if (reducedMotion) Reduced else Standard
    }
}

private val LocalHuaweiSyncMotionPolicy = staticCompositionLocalOf {
    HuaweiSyncMotionPolicy.Standard
}

object HuaweiSyncMotion {
    val current: HuaweiSyncMotionPolicy
        @Composable
        @ReadOnlyComposable
        get() = LocalHuaweiSyncMotionPolicy.current
}

/**
 * Central motion boundary. By default it respects the Android animator-duration accessibility
 * setting; previews and tests can provide an explicit value without changing component code.
 */
@Composable
fun HuaweiSyncMotionProvider(
    reducedMotion: Boolean = !ValueAnimator.areAnimatorsEnabled(),
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalHuaweiSyncMotionPolicy provides HuaweiSyncMotionPolicy.forReducedMotion(reducedMotion),
        content = content,
    )
}
