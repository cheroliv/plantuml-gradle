package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlantumlPluginFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `should apply plugin successfully`() {
        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent()
        )
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"test-project\"")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
    }

    @Test
    fun `should register all tasks`() {
        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent()
        )
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"test-project\"")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax"))
        assertTrue(result.output.contains("reindexPlantumlRag"))
    }

    @Test
    fun `should configure extension properly`() {
        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = file("plantuml-context.yml").absolutePath
            }
        """.trimIndent()
        )
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"test-project\"")
        File(testProjectDir, "plantuml-context.yml").writeText(
            """
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
    }
}