package plantuml.task

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("rag-heavy")
class ReindexPlantumlRagTaskTest {

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
            rootProject.name = "plantuml-rag-test"
        """.trimIndent()
        )

        // Basic configuration for all tests
        createBasicBuildFile()
        createBasicConfigFile()
    }

    @Ignore("Tests trop lents - chargement du modèle d'embedding ML")
    @ParameterizedTest
    @ValueSource(strings = ["empty", "invalid_syntax", "subdirs", "empty_files"])
    fun `should handle various RAG scenarios`(scenario: String) {
        when (scenario) {
            "empty" -> testEmptyDirectory()
            "invalid_syntax" -> testInvalidPlantUmlSyntax()
            "subdirs" -> testSubdirectories()
            "empty_files" -> testEmptyFiles()
        }
    }

    @Ignore
    @Test
    fun `should handle moderate number of diagrams gracefully`() {
        // Given
        createRagDirectory()

        // Create reduced number of diagram files (5 instead of 50)
        for (i in 1..5) {
            createDiagramFile("diagram$i.puml", "@startuml\nclass Class$i\n@enduml")
        }

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        assertTrue(result.output.contains("→ Found 5 PlantUML diagrams and 0 training histories for indexing"))
    }

    private fun testEmptyDirectory() {
        // When - use --info to get more output and reduce timeout
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--info")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        assertTrue(
            result.output.contains("RAG") ||
                    result.output.contains("No PlantUML") ||
                    result.output.contains("Created")
        )
    }

    private fun testInvalidPlantUmlSyntax() {
        // Create RAG directory with mixed valid/invalid diagrams
        @Suppress("UnusedVariable")
        val ragDir = createRagDirectory()

        // Create valid diagram
        createDiagramFile("valid.puml", "@startuml\nclass ValidClass\n@enduml")

        // Create invalid diagram (missing @enduml)
        createDiagramFile("invalid.puml", "@startuml\nclass InvalidClass\n# This is invalid PlantUML syntax")

        // When - use --info for more output
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--info")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        // Then - just verify task completed
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
    }

    private fun testSubdirectories() {
        // Create RAG directory structure with subdirectories
        val ragDir = createRagDirectory()

        // Create subdirectories
        val subdir1 = File(ragDir, "subdir1")
        subdir1.mkdirs()
        val subdir2 = File(ragDir, "subdir2")
        subdir2.mkdirs()

        // Create diagram files in various locations
        createDiagramFile("root.puml", "@startuml\nclass Root\n@enduml")
        File(subdir1, "sub1.puml").writeText("@startuml\nclass Sub1\n@enduml")
        File(subdir2, "sub2.puml").writeText("@startuml\nclass Sub2\n@enduml")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--info")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
    }

    private fun testEmptyFiles() {
        // Create RAG directory with empty files
        createRagDirectory()

        // Create empty diagram file
        createDiagramFile("empty.puml", "")

        // Create valid diagram file
        createDiagramFile("valid.puml", "@startuml\nclass Valid\n@enduml")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--info")
            .withPluginClasspath()
            .withGradleVersion("9.4.0")
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
    }

    // Méthodes utilitaires pour mutualiser la configuration
    private fun createBasicBuildFile() {
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
    }

    private fun createBasicConfigFile() {
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText(
            """
            output:
              rag: "generated/rag"
        """.trimIndent()
        )
    }

    private fun createRagDirectory(): File {
        val ragDir = File(testProjectDir, "generated/rag")
        ragDir.mkdirs()
        return ragDir
    }

    private fun createDiagramFile(name: String, content: String) {
        val file = File(File(testProjectDir, "generated/rag"), name)
        file.writeText(content)
    }
}