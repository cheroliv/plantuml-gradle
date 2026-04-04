package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.BeforeEach
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OptimizedPlantumlPluginFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File
    
    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        // Configuration commune pour tous les tests
        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())
    }

    @Test
    fun `should apply plugin and register all tasks`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())
        
        val configFile = File(testProjectDir, "plantuml-context.yml").apply {
            writeText("""
                input:
                  prompts: "test-prompts"
                output:
                  images: "test-images"
            """.trimIndent())
        }

        // When - exécuter plusieurs tâches dans un seul appel Gradle
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all", "--console=plain")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        // Then - vérifier plusieurs aspects dans un seul test
        assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax"))
        assertTrue(result.output.contains("reindexPlantumlRag"))
    }

    @Test
    fun `should handle basic plugin configuration`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = file("plantuml-context.yml").absolutePath
            }
        """.trimIndent())
        
        val configFile = File(testProjectDir, "plantuml-context.yml").apply {
            writeText("""
                input:
                  prompts: "test-prompts"
                output:
                  images: "test-images"
            """.trimIndent())
        }

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("help", "--console=plain")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome)
    }
}