package dev.lui.huaweisync.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Static guardrail for M006's honesty invariants. Any string listed here would overpromise
 * beyond the current gate — see docs/design/m005-design-directions.md § Shared constraints.
 * The rule is compile-time-cheap and independent of composition, so it catches regressions
 * from copy edits before any UI test runs.
 */
class HonestyCopyGuardTest {

    private val forbiddenStrings = listOf(
        "Synced!",
        "Sincronizado com o GymRats",
        "Todos os aplicativos atualizados",
        "Confirmed everywhere",
        "Imported by GymRats",
        "Delivered to GymRats",
        "All apps up to date",
    )

    @Test
    fun `no user-visible source string overpromises delivery`() {
        val roots = listOf(
            File("src/main/java"),
            File("src/main/res"),
        ).filter { it.isDirectory }
        val extensions = setOf("kt", "xml")

        val violations = mutableListOf<String>()
        roots.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && it.extension in extensions }
                .forEach { file ->
                    val text = file.readText()
                    forbiddenStrings.forEach { needle ->
                        if (text.contains(needle)) {
                            violations += "${file.relativeTo(File("."))}: contains forbidden \"$needle\""
                        }
                    }
                }
        }

        assertTrue(
            "Overpromising strings found:\n  " + violations.joinToString("\n  "),
            violations.isEmpty(),
        )
    }
}
