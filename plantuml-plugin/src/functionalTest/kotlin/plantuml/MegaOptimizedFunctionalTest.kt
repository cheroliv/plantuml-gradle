package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class MegaOptimizedFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `mega optimized single test`() {
        // Créer tous les fichiers nécessaires
        setupTestProject()
        
        // Appel Gradle minimal mais efficace
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "help",          // Vérifie que le plugin fonctionne
                "--console=plain" // Sortie simple
            )
            .withPluginClasspath()
            .build()

        // Vérifier le succès de l'application du plugin
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
        
        // Vérifier la présence des tâches avec une commande différente
        val tasksResult = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all", "--console=plain")
            .withPluginClasspath()
            .build()
            
        assertTrue(tasksResult.output.contains("processPlantumlPrompts"))
        assertTrue(tasksResult.output.contains("validatePlantumlSyntax")) 
        assertTrue(tasksResult.output.contains("reindexPlantumlRag"))
    }

    private fun setupTestProject() {
        // Settings minimal
        File(testProjectDir, "settings.gradle.kts").writeText("""
            rootProject.name = "test"
        """.trimIndent())
        
        // Build file avec plugin et config
        File(testProjectDir, "build.gradle.kts").writeText("""
            plugins { id("com.cheroliv.plantuml") }
            plantuml { configPath = "config.yml" }
        """.trimIndent())
        
        // Config minimale
        File(testProjectDir, "config.yml").writeText("""
            input: { prompts: "p" }
            output: { images: "i" }
        """.trimIndent())
    }
}