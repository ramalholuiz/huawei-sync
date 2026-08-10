package dev.lui.huaweisync.ui.screens.automation

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationStateTest {
    @Test
    fun `default automation experience is preview only with every trigger unavailable`() {
        val state = AutomationScreenState()

        assertTrue(state.triggers.isNotEmpty())
        assertTrue(state.triggers.all { it.availabilityLabel == AutomationScreenState.COMING_SOON })
        assertTrue(state.triggers.none(AutomationTrigger::enabled))
        assertFalse(state.createsBackgroundWork)
        assertFalse(state.persistsConfiguration)
        assertFalse(state.hasEnabledSchedulingBehavior)
    }

    @Test
    fun `project contains no scheduling library dependency or source reference`() {
        val projectRoot = findProjectRoot()
        val forbiddenType = "Work" + "Manager"
        val forbiddenPackage = "androidx." + "work"
        val filesToInspect = buildList {
            add(projectRoot.resolve("build.gradle.kts"))
            add(projectRoot.resolve("settings.gradle.kts"))
            add(projectRoot.resolve("gradle/libs.versions.toml"))
            Files.walk(projectRoot.resolve("app/src/main")).use { paths ->
                paths.filter(Files::isRegularFile).forEach(::add)
            }
        }

        val violations = filesToInspect.filter { file ->
            val source = file.readText()
            forbiddenType in source || forbiddenPackage in source
        }

        assertTrue("Scheduling implementation references found: $violations", violations.isEmpty())
    }

    @Test
    fun `screen keeps its only interactive preview state local to composition`() {
        val projectRoot = findProjectRoot()
        val screenSource = projectRoot
            .resolve("app/src/main/java/dev/lui/huaweisync/ui/screens/automation/AutomationScreen.kt")
            .readText()

        assertTrue("Expected local Compose state", "remember {" in screenSource)
        assertFalse("Preview state must not survive navigation or recreation", "rememberSaveable" in screenSource)
        assertTrue("The no-background disclaimer must stay visible", "No background job is created" in screenSource)
        assertTrue("The no-persistence disclaimer must stay visible", "Nothing here is saved" in screenSource)
    }

    private fun findProjectRoot(): Path {
        var candidate = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (candidate.parent != null) {
            if (candidate.resolve("settings.gradle.kts").toFile().isFile &&
                candidate.resolve("app").isDirectory()
            ) {
                return candidate
            }
            candidate = candidate.parent
        }
        error("Could not locate project root from ${System.getProperty("user.dir")}")
    }
}
