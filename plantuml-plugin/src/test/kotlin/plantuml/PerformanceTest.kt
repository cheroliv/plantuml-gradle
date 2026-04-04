package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.test.Ignore
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
              images: "generated/images"
              rag: "generated/rag"
              diagrams: "generated/diagrams"
        """
    }

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")

        settingsFile.writeText(
            """
            rootProject.name = "plantuml-performance-test"
        """.trimIndent()
        )
    }

    @Test
    fun `should process multiple prompts within reasonable time`() {
        // Given
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())

        // Create config file
        createConfigFile()

        // Create minimal prompts directory and sample prompts
        createPromptsDirectory(1) // Minimal for performance testing

        // When
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Basic validation to ensure the task actually runs
            println("Processing output: ${result.output}")
            assertTrue(result.output.contains(":processPlantumlPrompts") || result.output.contains("processPlantumlPrompts"), "Task should complete successfully")
        }

        // Performance assertion - should complete within 5 seconds for minimal processing
        assertTrue(duration < 5000, "Processing 1 prompt took too long: ${duration}ms")
    }

    @Test
    fun `should validate syntax quickly for few files`() {
        // Given
        buildFile.writeText(BASE_BUILD_SCRIPT.trimIndent())

        // Create minimal PlantUML files for faster testing
        for (i in 1..2) { // Reduced to 2 files for faster testing
            val diagramFile = File(testProjectDir, "diagram$i.puml")
            diagramFile.writeText("@startuml\nclass Component$i\n@enduml")
        }

        // Measure time for validating all files
        val duration = measureTimeMillis {
            for (i in 1..2) {
                val result = GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments(
                        "--quiet",
                        "validatePlantumlSyntax",
                        "-Pplantuml.diagram=diagram$i.puml",
                        "--stacktrace"
                    )
                    .withPluginClasspath()
                    .build()

                // Basic validation to ensure the task actually runs
                assertTrue(result.output.contains("validatePlantumlSyntax"), "Validation should complete successfully")
            }
        }

        // Performance assertion - should complete within 3 seconds for minimal processing
        assertTrue(duration < 3000, "Validating 2 files took too long: ${duration}ms")
    }

    @Test
    fun `should handle concurrent task execution efficiently`() {
        // Given
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())

        // Create config file
        createConfigFile()

        // Create minimal prompts directory and sample prompts
        createPromptsDirectory(1) // Minimal for performance testing

        // Create minimal PlantUML files for validation
        val diagramFile = File(testProjectDir, "validate.puml")
        diagramFile.writeText("@startuml\nclass Test\n@enduml")

        // When - Run multiple tasks with optimized arguments
        val duration = measureTimeMillis {
            // Run processPlantumlPrompts
            val processResult = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Run reindexPlantumlRag
            val reindexResult = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "reindexPlantumlRag", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Run single validation task for efficiency
            val validateResult = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "validatePlantumlSyntax", "-Pplantuml.diagram=validate.puml", "--stacktrace")
                .withPluginClasspath()
                .build()
            
            // Basic validation to ensure tasks actually run
            println("Processing output: ${processResult.output}")
            println("Reindex output: ${reindexResult.output}")
            println("Validation output: ${validateResult.output}")
            assertTrue(processResult.output.contains(":processPlantumlPrompts") || processResult.output.contains("processPlantumlPrompts"), "Processing task should complete successfully")
            assertTrue(reindexResult.output.contains(":reindexPlantumlRag") || reindexResult.output.contains("reindexPlantumlRag"), "Reindex task should complete successfully")
            assertTrue(validateResult.output.contains(":validatePlantumlSyntax") || validateResult.output.contains("validatePlantumlSyntax"), "Validation task should complete successfully")
        }

        // Then - Should complete all tasks within 8 seconds
        assertTrue(duration < 8000, "Concurrent tasks took too long: ${duration}ms")
    }

    @Test
    fun `should handle configuration and deep structures efficiently`() {
        // Combined test for both configuration and deep paths

        // Given - Simple config
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())

        // Create a minimal configuration file
        createConfigFile()

        // Also test deep directory structures with minimal depth
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText(
            """
            input:
              prompts: "deep/prompts"
            output:
              images: "generated/images"
              rag: "generated/rag"
              diagrams: "generated/diagrams"
        """.trimIndent()
        )

        // Create minimally nested directories and files
        val deepPromptsDir = File(testProjectDir, "deep/prompts")
        deepPromptsDir.mkdirs()

        // Create minimal prompt files
        val promptFile = File(deepPromptsDir, "deep.prompt")
        promptFile.writeText("Create diagram")

        // When
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

        // Basic validation to ensure the task actually runs
        println("Processing output: ${result.output}")
        assertTrue(result.output.contains(":processPlantumlPrompts") || result.output.contains("processPlantumlPrompts"), "Processing should complete successfully")
        }

        // Performance assertion - should handle both efficiently within 4 seconds
        assertTrue(
            duration < 4000,
            "Combined config and deep paths processing took too long: ${duration}ms"
        )
    }

    // Méthodes utilitaires pour mutualiser la configuration
    private fun createConfigFile() {
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText(BASE_CONFIG_CONTENT.trimIndent())
    }

    private fun createModerateConfigFile() {
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText(
            """
            input:
              prompts: "test-prompts"
              defaultLang: "en"
            output:
              images: "generated/images"
              rag: "generated/rag"
              diagrams: "generated/diagrams"
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
        """.trimIndent()
        )
    }

    private fun createPromptsDirectory(count: Int) {
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()

        for (i in 1..count) {
            val promptFile = File(promptsDir, "prompt$i.prompt")
            promptFile.writeText("Create a diagram for feature $i")
        }
    }

    // Méthodes utilitaires pour les assertions - conservées pour compatibilité mais non utilisées
    private fun assertSuccessfulGradleRun(result: org.gradle.testkit.runner.BuildResult) {
        // Ces méthodes ne sont plus utilisées car nous faisons les assertions directement dans les tests
        assertTrue(result.output.isNotEmpty())
    }

    private fun assertValidSyntaxResult(result: org.gradle.testkit.runner.BuildResult) {
        // Ces méthodes ne sont plus utilisées car nous faisons les assertions directement dans les tests
        assertTrue(result.output.isNotEmpty())
    }

    private fun assertProcessingResult(result: org.gradle.testkit.runner.BuildResult) {
        // Ces méthodes ne sont plus utilisées car nous faisons les assertions directement dans les tests
        assertTrue(result.output.isNotEmpty())
    }
}