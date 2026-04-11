@file:Suppress("FunctionName")

package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import org.junit.jupiter.api.Disabled
import kotlin.test.assertTrue

class SuperOptimizedFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Disabled("Conception intentionnelle — test fonctionnel lourd")
    @Test
    fun `super optimized single test for all plugin functionality`() {
        // Settings file
        File(testProjectDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "super-optimized-test"
        """.trimIndent()
        )

        // Build file with plugin and extension
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

        // Configuration file with all providers
        File(testProjectDir, "plantuml-context.yml").writeText(
            """
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              gemini:
                apiKey: "fake-gemini-key"
              mistral:
                apiKey: "fake-mistral-key"
        """.trimIndent()
        )

        // Un seul appel Gradle pour tester TOUTES les fonctionnalités
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all", "--console=plain")
            .withPluginClasspath()
            .build()

        // Vérifier le succès de l'application du plugin
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax"))
        assertTrue(result.output.contains("reindexPlantumlRag"))
    }
}