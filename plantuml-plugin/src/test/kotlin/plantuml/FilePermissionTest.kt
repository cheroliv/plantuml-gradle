package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertTrue

class FilePermissionTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-permission-test"
        """.trimIndent())
    }

    @Test
    fun `should handle read permission denied gracefully`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create a sample PlantUML file
        val diagramFile = File(testProjectDir, "protected.puml")
        diagramFile.writeText("""
            @startuml
            class Car {
              - String brand
            }
            @enduml
        """.trimIndent())

        // Make file unreadable (Unix-like systems)
        if (File.separator == "/") {
            try {
                diagramFile.setReadable(false)
            } catch (e: Exception) {
                // Ignore if permission change fails
            }
        }

        // When & Then
        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=protected.puml", "--stacktrace")
                .withPluginClasspath()
                .buildAndFail()

            // Then
            assertTrue(result.output.contains("Diagram file does not exist") ||
                      result.output.contains("Permission denied") ||
                      result.output.contains("Access is denied"))
        } finally {
            // Restore permissions for cleanup
            if (File.separator == "/") {
                try {
                    diagramFile.setReadable(true)
                } catch (e: Exception) {
                    // Ignore if permission change fails
                }
            }
        }
    }

    @Test
    fun `should handle write permission denied gracefully`() {
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
              images: "protected-images"
              rag: "test-rag"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "test.prompt")
        promptFile.writeText("Create a simple class diagram")

        // Create protected images directory
        val imagesDir = File(testProjectDir, "protected-images")
        imagesDir.mkdirs()

        // Make directory unwritable (Unix-like systems)
        if (File.separator == "/") {
            try {
                imagesDir.setWritable(false)
            } catch (e: Exception) {
                // Ignore if permission change fails
            }
        }

        // When & Then
        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .buildAndFail()

            // Then
            assertTrue(result.output.contains("Permission denied") ||
                      result.output.contains("Access is denied") ||
                      result.output.contains("Failed to write"))
        } finally {
            // Restore permissions for cleanup
            if (File.separator == "/") {
                try {
                    imagesDir.setWritable(true)
                } catch (e: Exception) {
                    // Ignore if permission change fails
                }
            }
        }
    }

    @Test
    fun `should handle directory permission denied gracefully`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create RAG directory with restricted permissions
        val ragDir = File(testProjectDir, "restricted-rag")
        ragDir.mkdirs()

        // Create a sample diagram
        val diagramFile = File(ragDir, "sample.puml")
        diagramFile.writeText("""
            @startuml
            class Car {
              - String brand
            }
            @enduml
        """.trimIndent())

        // Make directory inaccessible (Unix-like systems)
        if (File.separator == "/") {
            try {
                ragDir.setExecutable(false)
            } catch (e: Exception) {
                // Ignore if permission change fails
            }
        }

        // When & Then
        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("reindexPlantumlRag", "--stacktrace")
                .withPluginClasspath()
                .buildAndFail()

            // Then
            assertTrue(result.output.contains("Permission denied") ||
                      result.output.contains("Access is denied") ||
                      result.output.contains("Directory not found"))
        } finally {
            // Restore permissions for cleanup
            if (File.separator == "/") {
                try {
                    ragDir.setExecutable(true)
                } catch (e: Exception) {
                    // Ignore if permission change fails
                }
            }
        }
    }

    @Test
    fun `should handle nonexistent directory gracefully`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertContains(result.output, "No RAG directory found")
    }
}