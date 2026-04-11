package plantuml

import org.gradle.testkit.runner.GradleRunner.create
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import org.junit.jupiter.api.Disabled
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Tests are slow : ~46 sec
@Suppress("FunctionName")
class PlantumlPluginIntegrationTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")

        settingsFile.writeText(
            """
            rootProject.name = "plantuml-test-project"
        """.trimIndent()
        )
    }

    @Disabled
    @Test
    fun `should apply plugin and run processPlantumlPrompts task`() {
        buildFile.writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent()
        )

        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText(
            """
            input:
              prompts: "test-prompts"
            output:
              images: "generated/images"
              rag: "generated/rag"
              diagrams: "generated/diagrams"
              validations: "generated/validations"
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              maxIterations: 1
        """.trimIndent()
        )

        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        File(promptsDir, "test.prompt").writeText("Create a simple class diagram")

        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace", "-Dplantuml.test.mode=true")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":processPlantumlPrompts")?.outcome)
        assertTrue(result.output.contains("Processing 1 prompt files"))
    }

    @Disabled
    @Test
    fun `should run validatePlantumlSyntax task`() {
        buildFile.writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent()
        )

        File(testProjectDir, "sample.puml").writeText(
            """
            @startuml
            class Car {
              - String brand
              - String model
            }
            @enduml
        """.trimIndent()
        )

        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "validatePlantumlSyntax",
                "-Pplantuml.diagram=sample.puml",
                "--stacktrace",
                "-Dplantuml.test.mode=true"
            )
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":validatePlantumlSyntax")?.outcome)
        assertTrue(result.output.contains("PlantUML syntax is valid"))
    }

    @Disabled
    @Test
    fun `should run reindexPlantumlRag task`() {
        buildFile.writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent()
        )

        val ragDir = File(testProjectDir, "generated/rag")
        ragDir.mkdirs()
        File(ragDir, "sample.puml").writeText(
            """
            @startuml
            class Car {
              - String brand
              - String model
            }
            @enduml
        """.trimIndent()
        )

        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace", "-Dplantuml.test.mode=true")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        assertTrue(result.output.contains("Found") || result.output.contains("Indexing") || result.output.contains("Processed"))
    }
}