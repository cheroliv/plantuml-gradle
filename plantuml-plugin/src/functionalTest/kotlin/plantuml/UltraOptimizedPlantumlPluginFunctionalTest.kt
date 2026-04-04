package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.BeforeEach
import java.io.File
import kotlin.test.assertTrue

class UltraOptimizedPlantumlPluginFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File
    
    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""rootProject.name = "test"""")
        
        buildFile.writeText("""
            plugins { id("com.cheroliv.plantuml") }
            plantuml { configPath = "plantuml-context.yml" }
        """.trimIndent())
        
        // Créer un fichier de config minimal
        File(testProjectDir, "plantuml-context.yml").writeText("""
            input: { prompts: "test-prompts" }
            output: { images: "test-images" }
        """.trimIndent())
    }

    @Test
    fun `ultra optimized plugin test`() {
        // Un seul appel Gradle pour tester tout le fonctionnement
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("--info", "--stacktrace") // Moins verbeux que --debug
            .withPluginClasspath()
            .build()

        // Vérifier que le plugin s'est appliqué correctement
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}