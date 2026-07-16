package dev.lui.huaweisync.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Prototype semantic palette, kept separate from Material roles so no audited token is lost. */
@Immutable
data class HuaweiSyncColors(
    val background: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val ink: Color,
    /** Exact imported secondary-ink token, retained for provenance and non-content rendering. */
    val prototypeInk2: Color,
    /** Accessible runtime secondary content color. */
    val ink2: Color,
    val ink3: Color,
    val ink4: Color,
    val line: Color,
    val lineStrong: Color,
    val canvasBackground: Color,
    /** Imported HTML accent retained for borders and non-text decoration. */
    val accent: Color = Color(0xFFEC3013),
    /** Theme-aware accent for text and icons that must meet 4.5:1 on content surfaces. */
    val accentForeground: Color,
    /** Slightly darkened accent for white content on primary interactive controls. */
    val accentContainer: Color = Color(0xFFDF2B10),
    val accentSoft: Color = Color(0x24EC3013),
    val ok: Color = Color(0xFF4ADE80),
    val warning: Color = Color(0xFFF5A524),
    val info: Color = Color(0xFF6EA8FF),
)

val DarkHuaweiSyncColors = HuaweiSyncColors(
    background = Color(0xFF0B0A09),
    surface1 = Color(0xFF131211),
    surface2 = Color(0xFF1C1A18),
    surface3 = Color(0xFF26231F),
    ink = Color(0xFFF3F2F2),
    prototypeInk2 = Color(0x9EF3F2F2),
    ink2 = Color(0xFFAAA8A8),
    ink3 = Color(0x61F3F2F2),
    ink4 = Color(0x2EF3F2F2),
    line = Color(0x1AF3F2F2),
    lineStrong = Color(0x38F3F2F2),
    canvasBackground = Color(0xFF050403),
    accentForeground = Color(0xFFFF6B55),
)

val LightHuaweiSyncColors = HuaweiSyncColors(
    background = Color(0xFFF3F2F2),
    surface1 = Color(0xFFFFFFFF),
    surface2 = Color(0xFFEAE9E9),
    surface3 = Color(0xFFD7D3D3),
    ink = Color(0xFF201E1D),
    prototypeInk2 = Color(0x9E201E1D),
    ink2 = Color(0xFF5B5755),
    ink3 = Color(0x6B201E1D),
    ink4 = Color(0x33201E1D),
    line = Color(0x1A201E1D),
    lineStrong = Color(0x38201E1D),
    canvasBackground = Color(0xFFE7E3DE),
    accentForeground = Color(0xFFA61B12),
)
