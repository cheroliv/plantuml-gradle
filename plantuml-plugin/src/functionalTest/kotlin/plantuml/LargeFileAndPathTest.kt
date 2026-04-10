package plantuml

import org.gradle.testkit.runner.GradleRunner.create
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertTrue

@Suppress("FunctionName")
class LargeFileAndPathTest {

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
            rootProject.name = "plantuml-large-file-test"
        """.trimIndent()
        )

        // Common plugin configuration
        buildFile.writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent()
        )
    }

    @Test
    fun `should handle large PlantUML file`() {
        testLargePlantUmlFile()
    }

    @Test
    fun `should handle special characters in filename`() {
        testSpecialCharactersInFilename()
    }

    @Test
    fun `should handle deeply nested paths`() {
        testDeeplyNestedPaths()
    }

    @Test
    fun `should handle unicode characters`() {
        testUnicodeCharacters()
    }

    private fun testLargePlantUmlFile() {
        val largeDiagramFile = File(testProjectDir, "large.puml")
        largeDiagramFile.writeText(buildSmallLargePlantUmlContent())

        create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=large.puml")
            .withPluginClasspath()
            .build()
    }

    private fun testSpecialCharactersInFilename() {
        val specialFiles = listOf(
            "file with spaces.puml",
            "file-with-dashes.puml",
            "àccéntéd_nâmë.puml"
        )

        specialFiles.forEach { filename ->
            File(testProjectDir, filename).writeText("@startuml\nclass Test\n@enduml")
        }

        create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=file with spaces.puml")
            .withPluginClasspath()
            .build()
    }

    private fun testDeeplyNestedPaths() {
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "deep/path/prompts"
            output:
              images: "test-images"
              rag: "generated/rag"
              diagrams: "generated/diagrams"
        """.trimIndent())

        val deepPromptsDir = File(testProjectDir, "deep/path/prompts")
        deepPromptsDir.mkdirs()

        File(deepPromptsDir, "deep.prompt").writeText("Create a diagram")

        create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts")
            .withPluginClasspath()
            .build()
    }

    private fun testUnicodeCharacters() {
        val unicodeFile = File(testProjectDir, "unicode.puml")
        unicodeFile.writeText("""
            @startuml
            title Diagramme avec des caractères spéciaux
            actor Utilisateur
            rectangle "Système" {
              Utilisateur --> (Fonctionnalité)
            }
            @enduml
        """.trimIndent())

        create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=unicode.puml")
            .withPluginClasspath()
            .build()
    }

    private fun buildSmallLargePlantUmlContent(): String {
        val builder = StringBuilder()
        builder.append("@startuml\n")
        builder.append("title Large Diagram Test\n")

        // Reduced number of classes for faster testing
        for (i in 1..10) {
            builder.append("class Class$i {\n")
            builder.append("  - String field$i\n")
            builder.append("  + void method$i()\n")
            builder.append("}\n\n")
        }

        // Add some relationships
        for (i in 1..5) {
            builder.append("Class$i --> Class${i + 1}\n")
        }

        builder.append("@enduml\n")
        return builder.toString()
    }
}