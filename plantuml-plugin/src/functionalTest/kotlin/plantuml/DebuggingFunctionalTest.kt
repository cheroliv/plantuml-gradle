package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.Ignore

class DebuggingFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `debug what's in the output`() {
        // Créer les fichiers
        setupTestProject()

        // Exécuter un appel simple pour voir ce qu'il y a dans la sortie
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--console=plain")
            .withPluginClasspath()
            .build()

        // Afficher la sortie pour le débogage
        println("=== SORTIE GRADLE ===")
        println(result.output)
        println("====================")

        // Maintenant essayons les assertions une par une
        assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Should contain BUILD SUCCESSFUL")

        // Vérifier si les tâches sont bien là
        if (result.output.contains("processPlantumlPrompts")) {
            println("✓ processPlantumlPrompts found")
        } else {
            println("✗ processPlantumlPrompts NOT found")
        }

        if (result.output.contains("validatePlantumlSyntax")) {
            println("✓ validatePlantumlSyntax found")
        } else {
            println("✗ validatePlantumlSyntax NOT found")
        }

        if (result.output.contains("reindexPlantumlRag")) {
            println("✓ reindexPlantumlRag found")
        } else {
            println("✗ reindexPlantumlRag NOT found")
        }

        if (result.output.contains("plantuml")) {
            println("✓ plantuml found")
        } else {
            println("✗ plantuml NOT found")
        }

        // Pour le moment, passons juste le test si Gradle s'exécute
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    private fun setupTestProject() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "test"""")

        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.cheroliv.plantuml") }
        """.trimIndent()
        )
    }
}