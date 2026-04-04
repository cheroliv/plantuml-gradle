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
                "tasks",          // Liste les tâches (ce qu'on veut vérifier)
                "--console=plain" // Sortie simple
            )
            .withPluginClasspath()
            .build()

        // Vérifier tout dans un seul appel
        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax")) 
        assertTrue(result.output.contains("reindexPlantumlRag"))
        assertTrue(result.output.contains("plantuml"))
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
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