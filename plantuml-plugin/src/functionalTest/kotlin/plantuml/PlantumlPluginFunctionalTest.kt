package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlantumlPluginFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `should apply plugin successfully`() {
        // Given
        writeBuildFile()
        writeSettingsFile()

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks")
            .withPluginClasspath()
            .withGradleVersion("9.4.0") // Spécifier explicitement la version de Gradle
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
    }

    @Test
    fun `should register all tasks`() {
        // Given
        writeBuildFile()
        writeSettingsFile()

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .withGradleVersion("9.4.0") // Spécifier explicitement la version de Gradle
            .build()

        // Then
        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax"))
        assertTrue(result.output.contains("reindexPlantumlRag"))
    }

    @Test
    fun `should configure extension properly`() {
        // Given
        writeBuildFileWithExtension()
        writeSettingsFile()
        val configFile = File(testProjectDir, "plantuml-context.yml").apply {
            writeText(
                """
                input:
                  prompts: "test-prompts"
                output:
                  images: "test-images"
            """.trimIndent()
            )
        }

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks")
            .withPluginClasspath()
            .withGradleVersion("9.4.0") // Spécifier explicitement la version de Gradle
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
    }

    private fun writeBuildFile() {
        val buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent()
        )
    }

    private fun writeBuildFileWithExtension() {
        val buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = file("plantuml-context.yml").absolutePath
            }
        """.trimIndent()
        )
    }

    private fun writeSettingsFile() {
        val settingsFile = File(testProjectDir, "settings.gradle.kts")
        settingsFile.writeText(
            """
            rootProject.name = "test-project"
        """.trimIndent()
        )
    }
}