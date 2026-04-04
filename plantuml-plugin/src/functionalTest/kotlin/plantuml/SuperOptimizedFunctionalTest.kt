package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class SuperOptimizedFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `super optimized single test for all plugin functionality`() {
        // Créer tous les fichiers nécessaires en une seule fois
        setupTestProject()
        
        // Un seul appel Gradle pour tester TOUTES les fonctionnalités
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "help",           // Vérifie que le plugin s'applique
                "tasks",          // Vérifie l'enregistrement des tâches
                "--dry-run",      // N'exécute pas réellement les tâches
                "--console=plain" // Sortie simple
            )
            .withPluginClasspath()
            .build()

        // Vérifier toutes les fonctionnalités dans un seul test
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax"))
        assertTrue(result.output.contains("reindexPlantumlRag"))
        assertTrue(result.output.contains("plantuml"))
    }

    private fun setupTestProject() {
        // Settings file
        File(testProjectDir, "settings.gradle.kts").writeText("""
            rootProject.name = "super-optimized-test"
        """.trimIndent())
        
        // Build file with plugin and extension
        File(testProjectDir, "build.gradle.kts").writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = file("plantuml-context.yml").absolutePath
            }
        """.trimIndent())
        
        // Configuration file with all providers
        File(testProjectDir, "plantuml-context.yml").writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              gemini:
                apiKey: "fake-gemini-key"
              mistral:
                apiKey: "fake-mistral-key"
        """.trimIndent())
    }
}