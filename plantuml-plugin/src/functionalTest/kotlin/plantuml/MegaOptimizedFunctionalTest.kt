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
        // Setup inline
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"test\"")
        File(testProjectDir, "build.gradle.kts").writeText("plugins { id(\"com.cheroliv.plantuml\") }")
        File(testProjectDir, "config.yml").writeText("input: { prompts: \"p\" }\noutput: { images: \"i\" }")
        
        // Single Gradle call with tasks --all
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all", "--console=plain")
            .withPluginClasspath()
            .build()

        // Verify plugin tasks are registered
        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax"))
        assertTrue(result.output.contains("reindexPlantumlRag"))
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}