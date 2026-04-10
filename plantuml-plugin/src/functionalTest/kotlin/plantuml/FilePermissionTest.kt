package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.File.separator
import kotlin.test.assertTrue

@Suppress("FunctionName")
class FilePermissionTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File

    private val simpleDiagram = """
        @startuml
        left to right direction
        actor "Food Critic" as fc
        rectangle Restaurant {
          usecase "Eat Food" as UC1
          usecase "Pay for Food" as UC2
          usecase "Drink" as UC3
        }
        fc --> UC1
        fc --> UC2
        fc --> UC3
        @enduml
    """.trimIndent()

    @BeforeEach
    fun setup() {
        File(testProjectDir, "settings.gradle.kts").writeText(
            """rootProject.name = "plantuml-permission-test"""".trimIndent()
        )
        buildFile = File(testProjectDir, "build.gradle.kts")
    }

    private fun writeConfig(ragDir: String) {
        File(testProjectDir, "plantuml-context.yml").writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "$ragDir"
            rag:
              databaseUrl: ""
              username: ""
              password: ""
              tableName: "embeddings"
        """.trimIndent())
    }

    private fun assertContainsPermissionOrNotFoundMessage(output: String, message: String) {
        assertTrue(
            output.contains("Permission denied", true) ||
            output.contains("Access is denied", true) ||
            output.contains("access denied", true) ||
            output.contains("Permission non accordée", true) ||
            output.contains("Unable to read", true) ||
            output.contains("Failed to read", true) ||
            output.contains("Directory not found", true) ||
            output.contains("No such file or directory", true) ||
            output.contains("No PlantUML diagrams or training data found", true),
            message
        )
    }

    @Test
    fun `should handle read permission denied gracefully`() {
        buildFile.writeText("""plugins { id("com.cheroliv.plantuml") }""")

        val diagramFile = File(testProjectDir, "protected.puml")
        diagramFile.writeText(simpleDiagram)

        if (separator == "/") {
            diagramFile.setReadable(false)
            diagramFile.setWritable(false)
        } else {
            diagramFile.delete()
            diagramFile.mkdirs()
        }

        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=protected.puml")
                .withPluginClasspath()
                .buildAndFail()

            assertContainsPermissionOrNotFoundMessage(
                result.output,
                "Expected permission or access error but got: ${result.output}"
            )
        } finally {
            diagramFile.setReadable(true)
            diagramFile.setWritable(true)
        }
    }

    @Test
    fun `should handle write permission denied gracefully`() {
        buildFile.writeText("""plugins { id("com.cheroliv.plantuml") }""")

        val diagramFile = File(testProjectDir, "test.puml")
        diagramFile.writeText(simpleDiagram)
        diagramFile.setWritable(false)

        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=test.puml")
                .withPluginClasspath()
                .build()

            assertTrue(
                result.output.contains("valid", true),
                "Expected validation success but got: ${result.output}"
            )
        } finally {
            diagramFile.setWritable(true)
        }
    }

    @Test
    fun `should handle directory permission denied gracefully`() {
        buildFile.writeText("""
            plugins { id("com.cheroliv.plantuml") }
            plantuml { configPath = "plantuml-context.yml" }
        """.trimIndent())

        writeConfig("restricted-rag")

        val ragDir = File(testProjectDir, "restricted-rag")
        ragDir.mkdirs()
        File(ragDir, "sample.puml").writeText(simpleDiagram)

        if (separator == "/") {
            ragDir.setReadable(false, false)
            ragDir.setExecutable(false, false)
        } else {
            val tempDir = File(testProjectDir, "temp-rag")
            ragDir.renameTo(tempDir)
        }

        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("reindexPlantumlRag")
                .withPluginClasspath()
                .build()

            assertContainsPermissionOrNotFoundMessage(
                result.output,
                "Expected directory permission error but got: ${result.output}"
            )
        } finally {
            if (separator == "/") {
                ragDir.setReadable(true, false)
                ragDir.setExecutable(true, false)
            } else {
                val tempDir = File(testProjectDir, "temp-rag")
                if (tempDir.exists()) tempDir.renameTo(ragDir)
                if (ragDir.exists() && !ragDir.isDirectory) ragDir.delete()
            }
        }
    }

    @Test
    fun `should handle nonexistent directory gracefully`() {
        buildFile.writeText("""
            plugins { id("com.cheroliv.plantuml") }
            plantuml { configPath = "plantuml-context.yml" }
        """.trimIndent())

        writeConfig("nonexistent-rag")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag")
            .withPluginClasspath()
            .build()

        assertTrue(
            result.output.contains("No RAG directory found", true) ||
                    result.output.contains("No PlantUML diagrams or training data found", true) ||
                    result.output.contains("RAG reindexing complete with 0 diagrams", true),
            "Expected missing RAG directory message but got: ${result.output}"
        )
    }
}