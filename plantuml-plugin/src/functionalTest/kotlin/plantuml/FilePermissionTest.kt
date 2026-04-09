package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.File.separator
import java.io.File.createTempFile
import kotlin.test.assertTrue

@Suppress("FunctionName")
class FilePermissionTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    companion object {
        private var templateProjectDir: File? = null

        @BeforeAll
        @JvmStatic
        fun createTemplate() {
            if (templateProjectDir == null) {
                templateProjectDir = createBaseTemplateProject()
            }
        }

        private fun createBaseTemplateProject(): File {
            val templateDir = createTempFile("plantuml-permission-template-", "").apply {
                delete()
                mkdirs()
            }

            File(templateDir, "settings.gradle.kts").writeText(
                """rootProject.name = "plantuml-permission-test"""".trimIndent()
            )

            File(templateDir, "build.gradle.kts").writeText(
                """
                plugins {
                    id("com.cheroliv.plantuml")
                }
                """.trimIndent()
            )

            templateDir.deleteOnExit()
            return templateDir
        }
    }

    @BeforeEach
    fun setup() {
        val templateDir = templateProjectDir ?: createBaseTemplateProject().also { templateProjectDir = it }
        templateDir.copyRecursively(testProjectDir, overwrite = true)
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
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
        if (separator == "/") {
            try {
                diagramFile.setReadable(false)
                diagramFile.setWritable(false)
            } catch (_: Exception) {
                // If we can't change permissions, we'll test a different scenario
                // Create a directory instead of a file to simulate permission error
                diagramFile.delete()
                diagramFile.mkdirs()
            }
        } else {
            // On Windows or if permission change fails, make it a directory
            diagramFile.delete()
            diagramFile.mkdirs()
        }

        // When & Then
        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=protected.puml", "--stacktrace")
                .withPluginClasspath()
                .buildAndFail()

        // Then - Check various possible error messages including French system messages
            assertTrue(
                result.output.contains("Permission denied", true) ||
                        result.output.contains("Access is denied", true) ||
                        result.output.contains("access denied", true) ||
                        result.output.contains("Unable to read file", true) ||
                        result.output.contains("Failed to read", true) ||
                        result.output.contains("Diagram file does not exist", true) ||
                        result.output.contains("Is a directory", true) ||
                        result.output.contains("not a file", true) ||
                        result.output.contains("Permission non accordée", true) ||
                        result.output.contains("File not found", true) ||
                        result.output.contains("No such file or directory", true),
                "Expected permission or access error message but got: ${result.output}"
            )
        } finally {
            // Restore permissions for clean-up
            if (separator == "/") {
                try {
                    diagramFile.setReadable(true)
                    diagramFile.setWritable(true)
                } catch (_: Exception) {
                    // Ignore if permission change fails
                }
            }
        }
    }

    @Test
    fun `should handle write permission denied gracefully`() {
        // Given - Test write permission on diagram file for validation task
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create a PlantUML diagram file
        val diagramFile = File(testProjectDir, "test.puml")
        diagramFile.writeText("""
            @startuml
            class Car {
              - String brand
            }
            @enduml
        """.trimIndent())

        // Make file read-only to prevent any potential write operations
        if (separator == "/") {
            try {
                diagramFile.setWritable(false)
            } catch (_: Exception) {
                // Ignore if permission change fails
            }
        }

        // When & Then - Validation should succeed (it only reads)
        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=test.puml", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Validation task only reads, so it should succeed
            assertTrue(
                result.output.contains("valid", true) ||
                        result.output.contains("Valid", true),
                "Expected validation success but got: ${result.output}"
            )
        } finally {
            // Restore permissions for cleanup
            if (separator == "/") {
                try {
                    diagramFile.setWritable(true)
                } catch (_: Exception) {
                    // Ignore
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
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file with specific RAG directory
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "restricted-rag"
            rag:
              databaseUrl: ""
              username: ""
              password: ""
              tableName: "embeddings"
        """.trimIndent())

        // Create RAG directory and make it completely inaccessible
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

        // Make directory inaccessible - use a more reliable method
        if (separator == "/") {
            try {
                // Remove all permissions
                ragDir.setReadable(false, false)
                ragDir.setWritable(false, false)
                ragDir.setExecutable(false, false)
            } catch (_: Exception) {
                // If that fails, rename it temporarily to make it appear missing
                val tempDir = File(testProjectDir, "temp-rag")
                ragDir.renameTo(tempDir)

                // Create a file with the same name to block access
                ragDir.createNewFile()
                ragDir.setReadOnly()
            }
        } else {
            // On Windows, rename to make it appear missing
            val tempDir = File(testProjectDir, "temp-rag")
            ragDir.renameTo(tempDir)

            // Create a file with the same name to block access
            ragDir.createNewFile()
            ragDir.setReadOnly()
        }

        // When & Then
        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("reindexPlantumlRag", "--stacktrace")
                .withPluginClasspath()
                .build()

            // Then - Check that appropriate error/warning message is displayed
            assertTrue(
                result.output.contains("Permission denied", true) ||
                        result.output.contains("Access is denied", true) ||
                        result.output.contains("access denied", true) ||
                        result.output.contains("✗ Permission denied", true) ||
                        result.output.contains("✗ Error accessing RAG directory", true) ||
                        result.output.contains("No PlantUML diagrams or training data found", true) ||
                        result.output.contains("Failed to read", true) ||
                        result.output.contains("Unable to read", true) ||
                        result.output.contains("Directory not found", true) ||
                        result.output.contains("No such file or directory", true) ||
                        result.output.contains("The system cannot find the path specified", true),
                "Expected directory permission error or warning message but got: ${result.output}"
            )
        } finally {
            // Restore access for cleanup
            if (separator == "/") {
                try {
                    // Restore directory permissions
                    ragDir.setReadable(true, false)
                    ragDir.setWritable(true, false)
                    ragDir.setExecutable(true, false)

                    // Remove blocking file if exists
                    if (ragDir.exists() && !ragDir.isDirectory) {
                        ragDir.delete()
                    }

                    // Restore directory if renamed
                    val tempDir = File(testProjectDir, "temp-rag")
                    if (tempDir.exists()) {
                        tempDir.renameTo(ragDir)
                    }
                } catch (_: Exception) {
                    // Continue
                }
            } else {
                // Remove blocking file if exists
                if (ragDir.exists() && !ragDir.isDirectory) {
                    ragDir.delete()
                }

                // Restore directory if renamed
                val tempDir = File(testProjectDir, "temp-rag")
                if (tempDir.exists()) {
                    tempDir.renameTo(ragDir)
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
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file with specific RAG directory that doesn't exist
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "nonexistent-rag"
            rag:
              databaseUrl: ""
              username: ""
              password: ""
              tableName: "embeddings"
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then - Check that appropriate message is displayed
        assertTrue(
            result.output.contains("No RAG directory found", true) ||
                    result.output.contains("No PlantUML diagrams or training data found", true) ||
                    result.output.contains("RAG reindexing complete with 0 diagrams", true),
            "Expected message about missing RAG directory but got: ${result.output}"
        )
    }
}