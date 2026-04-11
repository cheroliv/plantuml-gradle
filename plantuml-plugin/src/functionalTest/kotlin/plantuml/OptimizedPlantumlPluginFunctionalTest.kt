package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

/**
 * Test fonctionnel optimisé pour le plugin PlantUML.
 */
@Disabled
class OptimizedPlantumlPluginFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `plugin loads and registers all tasks`() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "test"""")
        
        File(testProjectDir, "build.gradle.kts").writeText("""
            plugins { 
                id("com.cheroliv.plantuml") 
            }
        """.trimIndent())
        
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--console=plain")
            .withPluginClasspath()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Plugin should load successfully")
        assertTrue(result.output.contains("processPlantumlPrompts"), "Task processPlantumlPrompts should be registered")
        assertTrue(result.output.contains("validatePlantumlSyntax"), "Task validatePlantumlSyntax should be registered") 
        assertTrue(result.output.contains("reindexPlantumlRag"), "Task reindexPlantumlRag should be registered")
    }
}