package dev.lui.huaweisync.ui.screens.automation

/** Presentation-only trigger inventory. No item in this model can schedule work. */
data class AutomationTrigger(
    val title: String,
    val detail: String,
) {
    val availabilityLabel: String
        get() = AutomationScreenState.COMING_SOON

    val enabled: Boolean
        get() = false
}

data class AutomationScreenState(
    val triggers: List<AutomationTrigger> = defaultTriggers,
) {
    val experienceLabel: String
        get() = PREVIEW

    val createsBackgroundWork: Boolean
        get() = false

    val persistsConfiguration: Boolean
        get() = false

    val hasEnabledSchedulingBehavior: Boolean
        get() = false

    companion object {
        const val PREVIEW = "Preview"
        const val COMING_SOON = "Coming soon"

        val defaultTriggers = listOf(
            AutomationTrigger(
                title = "Scheduled sync",
                detail = "Choose a recurring time window for future automatic imports.",
            ),
            AutomationTrigger(
                title = "After a Huawei Health update",
                detail = "React when a new source workout becomes available.",
            ),
            AutomationTrigger(
                title = "While charging",
                detail = "Wait for a power-friendly device state before syncing.",
            ),
            AutomationTrigger(
                title = "On unmetered network",
                detail = "Limit future automatic sync to an unmetered connection.",
            ),
        )
    }
}
