package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertTrue

class LargeFileAndPathTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-large-file-test"
        """.trimIndent())
    }

    @Test
    fun `should handle large PlantUML files gracefully`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create a large PlantUML file (simulate with repeated content)
        val largeDiagramFile = File(testProjectDir, "large.puml")
        val largeContent = buildLargePlantUmlContent()
        largeDiagramFile.writeText(largeContent)

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=large.puml", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertTrue(result.output.contains("PlantUML syntax is valid") ||
                  result.output.contains("PlantUML syntax is invalid"))
    }

    @Test
    fun `should handle files with special characters in filename`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create files with special characters in filename
        val specialFiles = listOf(
            "file with spaces.puml",
            "file-with-dashes.puml",
            "file_with_underscores.puml",
            "file.with.dots.puml",
            "àccéntéd_nâmë.puml",
            "文件名.puml", // Chinese characters
            "файл.puml"    // Cyrillic characters
        )

        specialFiles.forEach { filename ->
            val file = File(testProjectDir, filename)
            file.writeText("""
                @startuml
                class Test
                @enduml
            """.trimIndent())
        }

        // Test one of the special files
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=file with spaces.puml", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertTrue(result.output.contains("PlantUML syntax is valid") ||
                  result.output.contains("PlantUML syntax is invalid"))
    }

    @Test
    fun `should handle deeply nested directory paths`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with deep path
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "very/long/path/to/prompts/directory/with/many/subdirectories"
            output:
              images: "another/very/long/path/to/images/directory/with/many/subdirectories"
              rag: "yet/another/very/long/path/to/rad/directory/with/many/subdirectories"
        """.trimIndent())

        // Create deeply nested directories and files
        val deepPromptsDir = File(testProjectDir, "very/long/path/to/prompts/directory/with/many/subdirectories")
        deepPromptsDir.mkdirs()
        
        val promptFile = File(deepPromptsDir, "deep.prompt")
        promptFile.writeText("Create a diagram")
        
        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        // Should not crash on deep paths
        assertTrue(result.output.contains("Processing") || 
                  result.output.contains("No prompt files found"))
    }

    @Test
    fun `should handle unicode characters in file content`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create a PlantUML file with unicode characters
        val unicodeFile = File(testProjectDir, "unicode.puml")
        unicodeFile.writeText("""
            @startuml
            title Diagramme avec des caractères spéciaux
            actor Utilisateur
            rectangle "Système" {
              Utilisateur --> (Fonctionnalité)
            }
            note right of Utilisateur
              Ceci est une note avec des caractères accentués: àáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ
            end note
            @enduml
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=unicode.puml", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertTrue(result.output.contains("PlantUML syntax is valid") ||
                  result.output.contains("PlantUML syntax is invalid"))
    }

    private fun buildLargePlantUmlContent(): String {
        val builder = StringBuilder()
        builder.append("@startuml\n")
        builder.append("title Large Diagram Test\n")
        
        // Add many classes to make it large
        for (i in 1..100) {
            builder.append("class Class$i {\n")
            builder.append("  - String field$i\n")
            builder.append("  + void method$i()\n")
            builder.append("}\n\n")
        }
        
        // Add some relationships
        for (i in 1..50) {
            builder.append("Class$i --> Class${i + 1}\n")
        }
        
        builder.append("@enduml\n")
        return builder.toString()
    }
}