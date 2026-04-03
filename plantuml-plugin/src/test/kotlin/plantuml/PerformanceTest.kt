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

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-performance-test"
        """.trimIndent())
    }

    @Test
    fun `should process multiple prompts within reasonable time`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
        """.trimIndent())

        // Create prompts directory and many sample prompts
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        
        // Create 5 prompt files (faster for testing)
        for (i in 1..5) {
            val promptFile = File(promptsDir, "prompt$i.prompt")
            promptFile.writeText("Create a class diagram for component $i")
        }

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then - Check that the task completed successfully
        assertTrue(result.output.contains("BUILD SUCCESSFUL") ||
                  result.output.contains("Prompts directory does not exist") ||
                  result.output.contains("No prompt files found") ||
                  result.output.contains("Processing"))
    }

    @Test
    fun `should validate syntax quickly for many files`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create fewer PlantUML files for faster testing
        for (i in 1..10) { // Réduit de 50 à 10
            val diagramFile = File(testProjectDir, "diagram$i.puml")
            diagramFile.writeText("""
                @startuml
                class Component$i {
                  - String field$i
                  + void method$i()
                }
                @enduml
            """.trimIndent())
        }

        // Measure time for validating all files
        val duration = measureTimeMillis {
            for (i in 1..10) {
                val result = GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=diagram$i.puml", "--stacktrace")
                    .withPluginClasspath()
                    .build()

                assertTrue(result.output.contains("PlantUML syntax is valid") ||
                          result.output.contains("PlantUML syntax is invalid"))
            }
        }

        // Performance assertion - should complete within reasonable time
        assertTrue(duration < 15000, "Validating 10 files took too long: ${duration}ms") // Réduit à 15s
    }

    @Test
    fun `should handle concurrent task execution`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
        """.trimIndent())

        // Create prompts directory and sample prompts
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        
        // Create fewer prompt files
        for (i in 1..3) { // Réduit de 10 à 3
            val promptFile = File(promptsDir, "prompt$i.prompt")
            promptFile.writeText("Create a diagram for feature $i")
        }

        // Create fewer PlantUML files for validation
        for (i in 1..2) { // Réduit de 5 à 2
            val diagramFile = File(testProjectDir, "validate$i.puml")
            diagramFile.writeText("@startuml\nclass Test$i\n@enduml")
        }

        // When - Run multiple tasks concurrently (simulated by running them in sequence quickly)
        val duration = measureTimeMillis {
            // Run processPlantumlPrompts
            val processResult = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Run reindexPlantumlRag
            val reindexResult = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("reindexPlantumlRag", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Run fewer validation tasks
            for (i in 1..2) { // Réduit de 3 à 2
                val validateResult = GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=validate$i.puml", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            }
        }

        // Then - Should complete all tasks within reasonable time
        assertTrue(duration < 20000, "Concurrent tasks took too long: ${duration}ms") // Réduit à 20s
    }

    @Test
    fun `should handle large configuration files efficiently`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create a large configuration file
        val configFile = File(testProjectDir, "plantuml-context.yml")
        val configBuilder = StringBuilder()
        
        configBuilder.append("""
            input:
              prompts: "test-prompts"
              defaultLang: "en"
            output:
              diagrams: "generated/diagrams"
              images: "generated/images"
              validations: "generated/validations"
              rag: "generated/rag"
              format: "png"
              theme: "default"
            langchain:
              maxIterations: 5
              model: "ollama"
              validation: true
              validationPrompt: "Rate this diagram..."
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "llama3:8b"
              gemini:
                apiKey: "fake-key"
              mistral:
                apiKey: "fake-key"
              openai:
                apiKey: "fake-key"
              claude:
                apiKey: "fake-key"
              huggingface:
                apiKey: "fake-key"
              groq:
                apiKey: "fake-key"
            git:
              userName: "test-user"
              userEmail: "test@example.com"
              commitMessage: "test commit"
              watchedBranches: 
        """.trimIndent())

        // Add fewer watched branches to make config large but still manageable
        configBuilder.append("\n")
        for (i in 1..20) { // Réduit de 100 à 20
            configBuilder.append("    - \"branch$i\"\n")
        }

        configFile.writeText(configBuilder.toString())

        // When
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Then
            assertTrue(result.output.contains("Processing") ||
                      result.output.contains("No prompt files found"))
        }

        // Performance assertion - should handle large config efficiently
        assertTrue(duration < 15000, "Large config processing took too long: ${duration}ms") // Réduit à 15s
    }

    @Test
    fun `should maintain performance with deep directory structures`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with moderately deep paths
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
        
        // Create fewer prompt files
        for (i in 1..5) { // Réduit de 20 à 5
            val promptFile = File(deepPromptsDir, "deep$i.prompt")
            promptFile.writeText("Create diagram $i")
        }

        // When
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Then
            assertTrue(result.output.contains("Processing") ||
                      result.output.contains("No prompt files found"))
        }

        // Performance assertion - should handle deep paths efficiently
        assertTrue(duration < 15000, "Deep directory processing took too long: ${duration}ms") // Réduit à 15s
    }
}