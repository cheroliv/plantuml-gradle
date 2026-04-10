package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.Ignore

class FinalOptimizedFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `final optimized functional test`() {
        // Créer les fichiers nécessaires
        setupTestProject()

        // Appel Gradle optimisé - un seul pour tout tester
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--console=plain")
            .withPluginClasspath()
            .build()

        // Tout vérifier dans un seul test efficace
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax"))
        assertTrue(result.output.contains("reindexPlantumlRag"))

        // Et aussi vérifier qu'on peut voir les extensions/propriétés
        val propsResult = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("properties", "--console=plain")
            .withPluginClasspath()
            .build()

        // Le plugin est bien appliqué si on arrive ici
        assertTrue(propsResult.output.contains("BUILD SUCCESSFUL"))
    }

    private fun setupTestProject() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "test"""")

        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins { 
                id("com.cheroliv.plantuml") 
            }
        """.trimIndent()
        )
    }
}