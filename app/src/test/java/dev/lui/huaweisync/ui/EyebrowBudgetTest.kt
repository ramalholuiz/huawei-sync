package dev.lui.huaweisync.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Direction A enforces "one eyebrow per screen, at the top". Diagnostics is exempt because it
 * is the deliberately evidence-heavy "prove it to me" drawer; SyncNowModal has a single
 * status-driven eyebrow. Every other screen composes at most one non-null eyebrow argument
 * in its own source file.
 */
class EyebrowBudgetTest {

    private val eyebrowPattern = Regex("""eyebrow\s*=\s*["\$]""")

    // Screen path → max eyebrows in the source. Most screens are 1. History exposes 2 because
    // its empty/loading/error branch and its content branch each carry the same page-top
    // eyebrow; only one branch composes at a time, so the user still sees one on screen.
    private val screenBudgets = mapOf(
        "onboarding/OnboardingScreen.kt" to 0,
        "dashboard/DashboardScreen.kt" to 0,
        "pipeline/PipelineScreen.kt" to 1,
        "integrations/IntegrationsScreen.kt" to 1,
        "automation/AutomationScreen.kt" to 1,
        "history/HistoryScreen.kt" to 2,
        "detail/ActivityDetailScreen.kt" to 1,
        "assistant/AssistantScreen.kt" to 1,
    )

    @Test
    fun `each dashboard-tier screen composes at most its allowed eyebrows`() {
        val sourceRoot = File("src/main/java/dev/lui/huaweisync/ui/screens")
        val violations = mutableListOf<String>()
        screenBudgets.forEach { (relative, budget) ->
            val file = File(sourceRoot, relative)
            val hits = eyebrowPattern.findAll(file.readText()).count()
            if (hits > budget) violations += "$relative uses $hits eyebrows (budget: $budget)"
        }
        assertTrue(
            "Screens over the eyebrow budget:\n  " + violations.joinToString("\n  "),
            violations.isEmpty(),
        )
    }
}
