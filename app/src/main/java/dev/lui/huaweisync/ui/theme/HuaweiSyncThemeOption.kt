package dev.lui.huaweisync.ui.theme

/** Single app theme model. Sage preserves the Cycle 5 green palettes; Ember restores the original red identity. */
enum class HuaweiSyncThemeOption(
    val label: String,
    val isDark: Boolean,
) {
    SageLight(label = "Sage Light", isDark = false),
    SageDark(label = "Sage Dark", isDark = true),
    EmberLight(label = "Ember Light", isDark = false),
    EmberDark(label = "Ember Dark", isDark = true),
    ;

    val colors: HuaweiSyncColors
        get() = when (this) {
            SageLight -> LightHuaweiSyncColors
            SageDark -> DarkHuaweiSyncColors
            EmberLight -> EmberLightHuaweiSyncColors
            EmberDark -> EmberDarkHuaweiSyncColors
        }

    fun next(): HuaweiSyncThemeOption = entries[(ordinal + 1) % entries.size]

    companion object {
        fun sageForSystemDark(isSystemDark: Boolean): HuaweiSyncThemeOption =
            if (isSystemDark) SageDark else SageLight
    }
}
