package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BaselineFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Suppress("FunctionName")
    @Test
    fun `baseline test - traditional approach`() {
        // Setup comme dans les tests traditionnels
        val settingsFile = File(testProjectDir, "settings.gradle.kts")
        val buildFile = File(testProjectDir, "build.gradle.kts")

        settingsFile.writeText(
            """
            rootProject.name = "test-project"
        """.trimIndent()
        )

        buildFile.writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent()
        )

        // Test 1: plugin application
        val result1 = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--console=plain")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result1.task(":tasks")?.outcome)

        // Test 2: task registration 
        assertTrue(result1.output.contains("processPlantumlPrompts"))
        assertTrue(result1.output.contains("validatePlantumlSyntax"))
        assertTrue(result1.output.contains("reindexPlantumlRag"))
    }
}