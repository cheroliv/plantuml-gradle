package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlantumlPluginIntegrationTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-test-project"
        """.trimIndent())
    }

    @kotlin.test.Ignore
    @Test
    fun `should apply plugin and run processPlantumlPrompts task`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create minimal config file
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "test.prompt")
        promptFile.writeText("Create a simple class diagram")
        
        // Create minimal config with mock LLM settings to speed up test
        configFile.delete()
        configFile.createNewFile()
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-output/images"
              rag: "test-output/rag"
              validations: "test-output/validations"
            langchain:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              maxIterations: 1
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace", "-Dplantuml.test.mode=true")
            .withPluginClasspath()
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":processPlantumlPrompts")?.outcome)
        assertTrue(result.output.contains("Processing 1 prompt files"))
    }

    @kotlin.test.Ignore
    @Test
    fun `should run validatePlantumlSyntax task`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create a sample PlantUML file
        val diagramFile = File(testProjectDir, "sample.puml")
        diagramFile.writeText("""
            @startuml
            class Car {
              - String brand
              - String model
            }
            @enduml
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=sample.puml", "--stacktrace", "-Dplantuml.test.mode=true")
            .withPluginClasspath()
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":validatePlantumlSyntax")?.outcome)
        assertTrue(result.output.contains("PlantUML syntax is valid"))
    }

    @kotlin.test.Ignore
    @Test
    fun `should run reindexPlantumlRag task`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create RAG directory with a sample diagram
        val ragDir = File(testProjectDir, "test-rag")
        ragDir.mkdirs()
        val diagramFile = File(ragDir, "sample.puml")
        diagramFile.writeText("""
            @startuml
            class Car {
              - String brand
              - String model
            }
            @enduml
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace", "-Dplantuml.test.mode=true")
            .withPluginClasspath()
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        assertTrue(result.output.contains("Found 1 PlantUML diagrams for indexing"))
    }
}