package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.BeforeEach
import java.io.File
import kotlin.test.assertTrue

class CachedPlantumlPluginFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `cached plugin initialization test`() {
        // Créer les fichiers de projet
        val settingsFile = File(testProjectDir, "settings.gradle.kts")
        val buildFile = File(testProjectDir, "build.gradle.kts")
        
        settingsFile.writeText("""rootProject.name = "test"""")
        
        buildFile.writeText("""
            plugins { id("com.cheroliv.plantuml") }
        """.trimIndent())

        // Premier appel avec cache
        val result1 = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("help", "--configuration-cache")
            .withPluginClasspath()
            .build()

        assertTrue(result1.output.contains("BUILD SUCCESSFUL"))
    }
}