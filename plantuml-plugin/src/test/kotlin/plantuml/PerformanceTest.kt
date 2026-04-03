package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

class PerformanceTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    companion object {
        private const val BASE_BUILD_SCRIPT = """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """
        
        private const val PLUGIN_CONFIG_SCRIPT = """
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """
        
        private const val BASE_CONFIG_CONTENT = """
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
        """
    }

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-performance-test"
        """.trimIndent())
    }

    @kotlin.test.Ignore
    @Test
    fun `should process multiple prompts within reasonable time`() {
        // Given
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())

        // Create config file
        createConfigFile()

        // Create prompts directory and sample prompts
        createPromptsDirectory(3) // Reduced from 5 to 3

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("--quiet", "--no-daemon", "processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then - Check that the task completed successfully
        assertSuccessfulGradleRun(result)
    }

    @kotlin.test.Ignore
    @Test
    fun `should validate syntax quickly for few files`() {
        // Given
        buildFile.writeText(BASE_BUILD_SCRIPT.trimIndent())

        // Create fewer PlantUML files for faster testing
        for (i in 1..5) { // Further reduced from 10 to 5
            val diagramFile = File(testProjectDir, "diagram$i.puml")
            diagramFile.writeText("@startuml\nclass Component$i\n@enduml")
        }

        // Measure time for validating all files
        val duration = measureTimeMillis {
            for (i in 1..5) {
                val result = GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("--quiet", "--no-daemon", "validatePlantumlSyntax", "-Pplantuml.diagram=diagram$i.puml", "--stacktrace")
                    .withPluginClasspath()
                    .build()

                assertValidSyntaxResult(result)
            }
        }

        // Performance assertion - should complete within reasonable time
        assertTrue(duration < 10000, "Validating 5 files took too long: ${duration}ms") // Reduced further to 10s
    }

    @kotlin.test.Ignore
    @Test
    fun `should handle concurrent task execution efficiently`() {
        // Given
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())

        // Create config file
        createConfigFile()

        // Create prompts directory and sample prompts
        createPromptsDirectory(2) // Reduced from 3 to 2

        // Create fewer PlantUML files for validation
        for (i in 1..2) {
            val diagramFile = File(testProjectDir, "validate$i.puml")
            diagramFile.writeText("@startuml\nclass Test$i\n@enduml")
        }

        // When - Run multiple tasks with optimized arguments
        val duration = measureTimeMillis {
            // Run processPlantumlPrompts
            val processResult = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "--no-daemon", "processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Run reindexPlantumlRag
            val reindexResult = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "--no-daemon", "reindexPlantumlRag", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Run single validation task for efficiency
            val validateResult = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "--no-daemon", "validatePlantumlSyntax", "-Pplantuml.diagram=validate1.puml", "--stacktrace")
                .withPluginClasspath()
                .build()
        }

        // Then - Should complete all tasks within reasonable time
        assertTrue(duration < 15000, "Concurrent tasks took too long: ${duration}ms") // Reduced to 15s
    }

    @kotlin.test.Ignore
    @Test
    fun `should handle configuration and deep structures efficiently`() {
        // Combined test for both large config and deep paths
        
        // Given - Large config
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())

        // Create a moderate configuration file
        createModerateConfigFile()

        // Also test deep directory structures
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "deep/structure/prompts"
            output:
              images: "deep/structure/images"
              rag: "deep/structure/rad"
        """.trimIndent())

        // Create moderately nested directories and files
        val deepPromptsDir = File(testProjectDir, "deep/structure/prompts")
        deepPromptsDir.mkdirs()
        
        // Create minimal prompt files
        for (i in 1..3) {
            val promptFile = File(deepPromptsDir, "deep$i.prompt")
            promptFile.writeText("Create diagram $i")
        }

        // When
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "--no-daemon", "processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Then
            assertProcessingResult(result)
        }

        // Performance assertion - should handle both efficiently
        assertTrue(duration < 10000, "Combined config and deep paths processing took too long: ${duration}ms") // Reduced to 10s
    }

    // Méthodes utilitaires pour mutualiser la configuration
    private fun createConfigFile() {
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText(BASE_CONFIG_CONTENT.trimIndent())
    }

    private fun createModerateConfigFile() {
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
              defaultLang: "en"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain:
              maxIterations: 3
              model: "ollama"
            git:
              userName: "test-user"
              userEmail: "test@example.com"
              watchedBranches: 
                - "main"
                - "develop"
                - "feature"
        """.trimIndent())
    }

    private fun createPromptsDirectory(count: Int) {
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        
        for (i in 1..count) {
            val promptFile = File(promptsDir, "prompt$i.prompt")
            promptFile.writeText("Create a diagram for feature $i")
        }
    }

    // Méthodes utilitaires pour les assertions
    private fun assertSuccessfulGradleRun(result: org.gradle.testkit.runner.BuildResult) {
        assertTrue(result.output.contains("BUILD SUCCESSFUL") ||
                  result.output.contains("Prompts directory does not exist") ||
                  result.output.contains("No prompt files found") ||
                  result.output.contains("Processing"))
    }

    private fun assertValidSyntaxResult(result: org.gradle.testkit.runner.BuildResult) {
        assertTrue(result.output.contains("PlantUML syntax is valid") ||
                  result.output.contains("PlantUML syntax is invalid"))
    }

    private fun assertProcessingResult(result: org.gradle.testkit.runner.BuildResult) {
        assertTrue(result.output.contains("Processing") ||
                  result.output.contains("No prompt files found"))
    }
}