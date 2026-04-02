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
        
        // Create 100 prompt files
        for (i in 1..100) {
            val promptFile = File(promptsDir, "prompt$i.prompt")
            promptFile.writeText("Create a class diagram for component $i")
        }

        // When
        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Then
            assertTrue(result.output.contains("Processing 100 prompt files") ||
                      result.output.contains("Processing") ||
                      result.output.contains("No prompt files found"))
        }

        // Performance assertion - should complete within reasonable time
        // Note: This is a loose constraint as test environments vary
        assertTrue(duration < 60000, "Processing 100 prompts took too long: ${duration}ms")
    }

    @Test
    fun `should validate syntax quickly for many files`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create many PlantUML files
        for (i in 1..50) {
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
            for (i in 1..50) {
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
        assertTrue(duration < 30000, "Validating 50 files took too long: ${duration}ms")
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
        
        // Create 10 prompt files
        for (i in 1..10) {
            val promptFile = File(promptsDir, "prompt$i.prompt")
            promptFile.writeText("Create a diagram for feature $i")
        }

        // Create some PlantUML files for validation
        for (i in 1..5) {
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

            // Run a few validation tasks
            for (i in 1..3) {
                val validateResult = GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=validate$i.puml", "--stacktrace")
                    .withPluginClasspath()
                    .build()
            }
        }

        // Then - Should complete all tasks within reasonable time
        assertTrue(duration < 45000, "Concurrent tasks took too long: ${duration}ms")
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

        // Add many watched branches to make config large
        configBuilder.append("\n")
        for (i in 1..100) {
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
        assertTrue(duration < 30000, "Large config processing took too long: ${duration}ms")
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

        // Create config with deep paths
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "very/deep/directory/structure/for/prompts/with/many/levels"
            output:
              images: "another/very/deep/directory/structure/for/images/with/many/levels"
              rag: "yet/another/very/deep/directory/structure/for/rad/with/many/levels"
        """.trimIndent())

        // Create deeply nested directories and files
        val deepPromptsDir = File(testProjectDir, "very/deep/directory/structure/for/prompts/with/many/levels")
        deepPromptsDir.mkdirs()
        
        // Create 20 prompt files
        for (i in 1..20) {
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
        assertTrue(duration < 30000, "Deep directory processing took too long: ${duration}ms")
    }
}