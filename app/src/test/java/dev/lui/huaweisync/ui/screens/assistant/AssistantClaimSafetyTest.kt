package dev.lui.huaweisync.ui.screens.assistant

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantClaimSafetyTest {
    @Test
    fun `assistant production code and dependencies contain no remote ai integration`() {
        val projectRoot = findProjectRoot()
        val assistantSources = listOf(
            projectRoot.resolve("app/src/main/java/dev/lui/huaweisync/ui/state/AssistantState.kt"),
            projectRoot.resolve("app/src/main/java/dev/lui/huaweisync/ui/screens/assistant/AssistantScreen.kt"),
        )
        val dependencyFiles = listOf(
            projectRoot.resolve("app/build.gradle.kts"),
            projectRoot.resolve("gradle/libs.versions.toml"),
        )
        val forbiddenSourceTokens = listOf(
            "java." + "net",
            "android." + "net",
            "Http" + "Client",
            "Ok" + "Http",
            "Retro" + "fit",
            "io.ktor.client",
            "Open" + "AI",
            "Anth" + "ropic",
            "Gem" + "ini",
            "API" + "_KEY",
        )
        val forbiddenDependencies = listOf(
            "ok" + "http",
            "retro" + "fit",
            "ktor-client",
            "open" + "ai",
            "anth" + "ropic",
            "gem" + "ini",
            "lang" + "chain",
        )

        val sourceViolations = assistantSources.flatMap { file ->
            forbiddenSourceTokens.filter { token -> file.readText().contains(token, ignoreCase = true) }
                .map { token -> "${projectRoot.relativize(file)} contains $token" }
        }
        val dependencyViolations = dependencyFiles.flatMap { file ->
            forbiddenDependencies.filter { token -> file.readText().contains(token, ignoreCase = true) }
                .map { token -> "${projectRoot.relativize(file)} contains $token" }
        }

        assertTrue(
            "Remote assistant integration is forbidden:\n${(sourceViolations + dependencyViolations).joinToString("\n")}",
            sourceViolations.isEmpty() && dependencyViolations.isEmpty(),
        )
    }

    private fun findProjectRoot(): Path {
        var candidate = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (!candidate.resolve("settings.gradle.kts").toFile().exists()) {
            candidate = candidate.parent ?: error("Could not find project root")
        }
        check(candidate.resolve("app/src/main").isDirectory())
        return candidate
    }
}
