package plantuml.task

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReindexPlantumlRagTaskTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-rag-test"
        """.trimIndent())
    }

    @Test
    fun `should run reindexPlantumlRag task successfully with empty directory`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file to ensure correct RAG directory
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            output:
              rag: "generated/rag"
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        assertTrue(result.output.contains("→ Created RAG directory") ||
                   result.output.contains("→ No PlantUML diagrams or training data found in RAG directory"))
    }

    @Test
    fun `should handle invalid PlantUML syntax in RAG directory gracefully`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file to ensure correct RAG directory
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            output:
              rag: "generated/rag"
        """.trimIndent())

        // Create RAG directory with mixed valid/invalid diagrams
        val ragDir = File(testProjectDir, "generated/rag")
        ragDir.mkdirs()
        
        // Create valid diagram
        val validDiagram = File(ragDir, "valid.puml")
        validDiagram.writeText("""
            @startuml
            class ValidClass
            @enduml
        """.trimIndent())

        // Create invalid diagram (missing @enduml)
        val invalidDiagram = File(ragDir, "invalid.puml")
        invalidDiagram.writeText("""
            @startuml
            class InvalidClass
            # This is invalid PlantUML syntax
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        assertTrue(result.output.contains("→ Found 2 PlantUML diagrams and 0 training histories for indexing") ||
                   result.output.contains("→ Found 1 PlantUML diagrams and 0 training histories for indexing"))
    }

    @Test
    fun `should handle large number of diagrams gracefully`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file to ensure correct RAG directory
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            output:
              rag: "generated/rag"
        """.trimIndent())

        // Create RAG directory with many diagrams
        val ragDir = File(testProjectDir, "generated/rag")
        ragDir.mkdirs()
        
        // Create 50 diagram files
        for (i in 1..50) {
            val diagramFile = File(ragDir, "diagram$i.puml")
            diagramFile.writeText("""
                @startuml
                class Class$i
                @enduml
            """.trimIndent())
        }

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        assertTrue(result.output.contains("→ Found 50 PlantUML diagrams and 0 training histories for indexing"))
    }

    @Test
    fun `should handle RAG directory with subdirectories`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file to ensure correct RAG directory
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            output:
              rag: "generated/rag"
        """.trimIndent())

        // Create RAG directory structure with subdirectories
        val ragDir = File(testProjectDir, "generated/rag")
        ragDir.mkdirs()
        
        // Create subdirectories
        val subdir1 = File(ragDir, "subdir1")
        subdir1.mkdirs()
        val subdir2 = File(ragDir, "subdir2")
        subdir2.mkdirs()
        
        // Create diagram files in various locations
        val rootDiagram = File(ragDir, "root.puml")
        rootDiagram.writeText("@startuml\nclass Root\n@enduml")
        
        val sub1Diagram = File(subdir1, "sub1.puml")
        sub1Diagram.writeText("@startuml\nclass Sub1\n@enduml")
        
        val sub2Diagram = File(subdir2, "sub2.puml")
        sub2Diagram.writeText("@startuml\nclass Sub2\n@enduml")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then - Only files in the root RAG directory should be processed
        // (the task currently only looks at the root directory, not subdirectories)
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        // The task processes subdirectories recursively, so we expect 3 diagrams
        assertTrue(result.output.contains("→ Found 3 PlantUML diagrams and 0 training histories for indexing"))
    }

    @Test
    fun `should handle empty files in RAG directory`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config file to ensure correct RAG directory
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            output:
              rag: "generated/rag"
        """.trimIndent())

        // Create RAG directory with empty files
        val ragDir = File(testProjectDir, "generated/rag")
        ragDir.mkdirs()
        
        // Create empty diagram file
        val emptyDiagram = File(ragDir, "empty.puml")
        emptyDiagram.writeText("")

        // Create valid diagram file
        val validDiagram = File(ragDir, "valid.puml")
        validDiagram.writeText("@startuml\nclass Valid\n@enduml")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        assertTrue(result.output.contains("→ Found 2 PlantUML diagrams and 0 training histories for indexing") ||
                  result.output.contains("→ Found 1 PlantUML diagrams and 0 training histories for indexing"))
    }
}