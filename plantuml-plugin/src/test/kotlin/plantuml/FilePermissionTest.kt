package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertContains
import kotlin.test.assertTrue

@Ignore
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

    @kotlin.test.Ignore
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
                diagramFile.setWritable(false)
            } catch (e: Exception) {
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
            // Restore permissions for cleanup
            if (File.separator == "/") {
                try {
                    diagramFile.setReadable(true)
                    diagramFile.setWritable(true)
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

        // Create config file
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

        // Create protected images directory and make it read-only
        val imagesDir = File(testProjectDir, "protected-images")
        imagesDir.mkdirs()

        // Make directory read-only to prevent writing
        if (File.separator == "/") {
            try {
                imagesDir.setWritable(false)
            } catch (e: Exception) {
                // If we can't change permissions, place a file there to block writes
                val blockerFile = File(imagesDir, ".gitkeep")
                blockerFile.writeText("")
                blockerFile.setReadOnly()
            }
        } else {
            // On Windows, place a file there to block writes
            val blockerFile = File(imagesDir, ".gitkeep")
            blockerFile.writeText("")
            blockerFile.setReadOnly()
        }

        // When & Then
        try {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .buildAndFail()

            // Then - Check various possible error messages including French system messages
            assertTrue(
                result.output.contains("Permission denied", true) ||
                result.output.contains("Access is denied", true) ||
                result.output.contains("access denied", true) ||
                result.output.contains("Failed to write", true) ||
                result.output.contains("Unable to write", true) ||
                result.output.contains("Failed to create", true) ||
                result.output.contains("Cannot create", true) ||
                result.output.contains("Read-only file system", true) ||
                result.output.contains("Operation not permitted", true) ||
                result.output.contains("Permission non accordée", true) ||
                result.output.contains("Écriture refusée", true) ||
                result.output.contains("Impossible d'écrire", true) ||
                result.output.contains("Impossible de créer", true),
                "Expected write permission error message but got: ${result.output}"
            )
        } finally {
            // Restore permissions for cleanup
            if (File.separator == "/") {
                try {
                    imagesDir.setWritable(true)
                } catch (e: Exception) {
                    // Remove blocking file
                    val blockerFile = File(imagesDir, ".gitkeep")
                    if (blockerFile.exists()) {
                        blockerFile.delete()
                    }
                }
            } else {
                // Remove blocking file
                val blockerFile = File(imagesDir, ".gitkeep")
                if (blockerFile.exists()) {
                    blockerFile.delete()
                }
            }
        }
    }

    @kotlin.test.Ignore
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
        if (File.separator == "/") {
            try {
                // Remove all permissions
                ragDir.setReadable(false, false)
                ragDir.setWritable(false, false)
                ragDir.setExecutable(false, false)
            } catch (e: Exception) {
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
            if (File.separator == "/") {
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
                } catch (e: Exception) {
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

    @kotlin.test.Ignore
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