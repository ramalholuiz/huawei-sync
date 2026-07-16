package dev.lui.huaweisync.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Screenshot matrix shared by every destination in the imported ten-screen prototype.
 *
 * The large-font entries deliberately combine font scaling with both navigation breakpoints so a
 * preview review catches clipping and accidental tablet-only assumptions without multiplying each
 * screen's preview boilerplate.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "01 compact light",
    group = "Screenshot matrix",
    widthDp = 390,
    heightDp = 844,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "02 compact dark large text",
    group = "Screenshot matrix",
    widthDp = 390,
    heightDp = 844,
    fontScale = 1.3f,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Preview(
    name = "03 expanded light",
    group = "Screenshot matrix",
    widthDp = 1000,
    heightDp = 700,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "04 expanded dark maximum text",
    group = "Screenshot matrix",
    widthDp = 1000,
    heightDp = 700,
    fontScale = 2f,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
annotation class HuaweiSyncScreenshotPreviews
