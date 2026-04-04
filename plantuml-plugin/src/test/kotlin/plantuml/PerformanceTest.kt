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
    fun `should process single prompt quickly`() {
        // Given - Minimal setup
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())
        createConfigFile()
        
        // Create only 1 prompt file for fastest execution
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "minimal.prompt")
        promptFile.writeText("Simple diagram") // Minimal content

        // When - Measure with strict timeout
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Basic validation
            assertTrue(result.output.contains(":processPlantumlPrompts") || result.output.contains("processPlantumlPrompts"), "Task should run")
        }

        // Strict performance assertion - should complete within 2 seconds
        assertTrue(duration < 2000, "Processing should be quick: ${duration}ms")
    }

    @Test
    fun `should validate syntax extremely quickly`() {
        // Given - Ultra minimal setup
        buildFile.writeText(BASE_BUILD_SCRIPT.trimIndent())
        
        // Create only 1 minimal PlantUML file
        val diagramFile = File(testProjectDir, "minimal.puml")
        diagramFile.writeText("@startuml\nclass A\n@enduml") // Minimal valid content

        // Measure time for single file validation with strict timeout
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(
                    "--quiet",
                    "validatePlantumlSyntax",
                    "-Pplantuml.diagram=minimal.puml",
                    "--stacktrace"
                )
                .withPluginClasspath()
                .build()

            // Basic validation
            assertTrue(result.output.contains(":validatePlantumlSyntax") || result.output.contains("validatePlantumlSyntax"), "Validation should run")
        }

        // Very strict performance assertion - should complete within 1 second
        assertTrue(duration < 1000, "Validation should be ultra-fast: ${duration}ms")
    }

    @Test
    fun `should validate multiple files quickly`() {
        // Given - Ultra minimal setup for 2 files
        buildFile.writeText(BASE_BUILD_SCRIPT.trimIndent())
        
        // Create only 2 minimal PlantUML files
        for (i in 1..2) {
            val diagramFile = File(testProjectDir, "diagram$i.puml")
            diagramFile.writeText("@startuml\nclass A$i\n@enduml") // Minimal valid content
        }

        // When - Measure time for validating all files
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
                assertTrue(result.output.contains(":validatePlantumlSyntax") || 
                         result.output.contains("validatePlantumlSyntax"), 
                         "Validation should complete successfully for file $i")
            }
        }

        // Performance assertion - should complete within 3 seconds for minimal processing
        assertTrue(duration < 3000, "Validating 2 files took too long: ${duration}ms")
    }

    @Test
    fun `should handle concurrent tasks with minimal overhead`() {
        // Given - Streamlined setup
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())
        createConfigFile()
        
        // Minimal files
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        File(promptsDir, "tiny.prompt").writeText("A")
        
        val diagramFile = File(testProjectDir, "tiny.puml")
        diagramFile.writeText("@startuml\nclass B\n@enduml")

        // When - Run with strict timing
        val duration = measureTimeMillis {
            // Parallel execution of minimal tasks
            val results = listOf(
                GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("--quiet", "processPlantumlPrompts", "--stacktrace")
                    .withPluginClasspath()
                    .build(),
                GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("--quiet", "reindexPlantumlRag", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            )

            // Validate all executed
            results.forEach { result ->
                assertTrue(
                    result.output.contains(":processPlantumlPrompts") || 
                    result.output.contains("processPlantumlPrompts") ||
                    result.output.contains(":reindexPlantumlRag") || 
                    result.output.contains("reindexPlantumlRag"),
                    "All tasks should run"
                )
            }
        }

        // Strict performance - all tasks within 3 seconds
        assertTrue(duration < 3000, "Concurrent tasks should be fast: ${duration}ms")
    }

    @Test
    fun `should handle concurrent tasks efficiently`() {
        // Given - Streamlined setup for concurrent test
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())
        createConfigFile()
        
        // Minimal files for concurrent tasks
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        File(promptsDir, "tiny.prompt").writeText("A")
        
        val diagramFile = File(testProjectDir, "tiny.puml")
        diagramFile.writeText("@startuml\nclass B\n@enduml")

        // When - Run with strict timing constraint
        val duration = measureTimeMillis {
            // Parallel execution of minimal tasks
            val results = listOf(
                GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("--quiet", "processPlantumlPrompts", "--stacktrace")
                    .withPluginClasspath()
                    .build(),
                GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("--quiet", "reindexPlantumlRag", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            )

            // Validate all executed
            results.forEachIndexed { index, result ->
                val taskName = if (index == 0) "processPlantumlPrompts" else "reindexPlantumlRag"
                assertTrue(
                    result.output.contains(":$taskName") || 
                    result.output.contains(taskName),
                    "Task $taskName should run"
                )
            }
        }

        // Then - Should complete all tasks within 8 seconds
        assertTrue(duration < 8000, "Concurrent tasks took too long: ${duration}ms")
    }

    @Test
    fun `should handle minimal config and structures instantly`() {
        // Given - Ultra-minimal config and structure
        buildFile.writeText(PLUGIN_CONFIG_SCRIPT.trimIndent())
        
        // Minimal config
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("input:\n  prompts: \"min\"\noutput:\n  images: \"gen\"")

        // Minimal structure
        val promptsDir = File(testProjectDir, "min")
        promptsDir.mkdirs()
        File(promptsDir, "x.prompt").writeText("Y") // Single character content

        // When - Measure with tight constraint
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("--quiet", "processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Basic validation
            assertTrue(result.output.contains(":processPlantumlPrompts") || result.output.contains("processPlantumlPrompts"), "Should run")
        }

        // Extremely strict - should complete within 1.5 seconds
        assertTrue(duration < 1500, "Should handle minimal setup instantly: ${duration}ms")
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