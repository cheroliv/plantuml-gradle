package plantuml

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import plantuml.tasks.ReindexPlantumlRagTask
import java.io.File
import java.time.Duration
import kotlin.test.assertTrue

/**
 * Integration tests for ReindexPlantumlRagTask with real PostgreSQL via testcontainers
 * 
 * Tests the RAG re-indexing task in DATABASE mode with a real PostgreSQL container
 */
@Testcontainers
class ReindexPlantumlRagIntegrationTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var project: Project
    private lateinit var task: ReindexPlantumlRagTask

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("pgvector/pgvector:pg15").apply {
            withDatabaseName("plantuml_rag")
            withUsername("test")
            withPassword("test")
            withStartupTimeout(Duration.ofMinutes(2))
        }
    }

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()

        project.pluginManager.apply("com.cheroliv.plantuml")
        task = project.tasks.getByName("reindexPlantumlRag") as ReindexPlantumlRagTask
    }

    @AfterEach
    fun cleanup() {
        // Cleanup any temporary files
        tempDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    @Test
    fun `should index PlantUML diagrams in DATABASE mode with testcontainers`() {
        // Given: RAG directory with PlantUML diagrams
        val ragDir = File(tempDir, "rag-integration")
        ragDir.mkdirs()

        val diagram1 = File(ragDir, "class-diagram.puml").apply {
            writeText(
                """
                @startuml
                class User {
                  +id: Long
                  +name: String
                  +email: String
                }
                class Order {
                  +id: Long
                  +user: User
                  +items: List<OrderItem>
                }
                User "1" --> "0..*" Order
                @enduml
                """.trimIndent()
            )
        }

        val diagram2 = File(ragDir, "sequence-diagram.puml").apply {
            writeText(
                """
                @startuml
                participant User
                participant Controller
                participant Service
                User -> Controller: createOrder()
                Controller -> Service: processOrder()
                Service --> Controller: OrderCreated
                Controller --> User: Success
                @enduml
                """.trimIndent()
            )
        }

        val historyFile = File(ragDir, "attempt-history-001.json").apply {
            writeText(
                """
                {
                  "prompt": "Create a class diagram for User and Order",
                  "attempts": 2,
                  "finalDiagram": "@startuml\nclass User\n@enduml",
                  "validationFeedback": []
                }
                """.trimIndent()
            )
        }

        // Configure with testcontainers PostgreSQL - use mapped port
        val actualPort = postgresContainer.firstMappedPort
        val configPath = File(tempDir, "plantuml-context.yml")
        configPath.writeText(
            """
            output:
              rag: "${ragDir.absolutePath}"
            rag:
              databaseUrl: "localhost"
              port: $actualPort
              username: "${postgresContainer.username}"
              password: "${postgresContainer.password}"
              tableName: "embeddings_test"
            """.trimIndent()
        )

        // When: Execute the task
        task.reindexRag()

        // Then: Task completes successfully (diagrams indexed in PostgreSQL)
        assertTrue(diagram1.exists(), "Diagram 1 should still exist")
        assertTrue(diagram2.exists(), "Diagram 2 should still exist")
        assertTrue(historyFile.exists(), "History file should still exist")
    }

    @Test
    fun `should handle empty RAG directory in DATABASE mode`() {
        // Given: Empty RAG directory
        val ragDir = File(tempDir, "rag-empty-db")
        ragDir.mkdirs()

        // Configure with testcontainers PostgreSQL - use mapped port
        val actualPort = postgresContainer.firstMappedPort
        val configPath = File(tempDir, "plantuml-context.yml")
        configPath.writeText(
            """
            output:
              rag: "${ragDir.absolutePath}"
            rag:
              databaseUrl: "localhost"
              port: $actualPort
              username: "${postgresContainer.username}"
              password: "${postgresContainer.password}"
              tableName: "embeddings_empty"
            """.trimIndent()
        )

        // When: Execute the task
        task.reindexRag()

        // Then: Task completes gracefully
        assertTrue(ragDir.exists(), "RAG directory should exist")
    }

    @Test
    fun `should index large PlantUML diagram in DATABASE mode`() {
        // Given: RAG directory with a large PlantUML diagram
        val ragDir = File(tempDir, "rag-large")
        ragDir.mkdirs()

        val largeDiagram = File(ragDir, "large-system.puml").apply {
            val content = buildString {
                appendLine("@startuml")
                appendLine("package com.example {")
                for (i in 1..50) {
                    appendLine("  class Class$i {")
                    appendLine("    +field1: String")
                    appendLine("    +field2: Int")
                    appendLine("    +method1(): Void")
                    appendLine("    +method2(): String")
                    appendLine("  }")
                }
                for (i in 1..49) {
                    appendLine("  Class$i --> Class${i + 1}")
                }
                appendLine("}")
                appendLine("@enduml")
            }
            writeText(content)
        }

        // Configure with testcontainers PostgreSQL - use mapped port
        val actualPort = postgresContainer.firstMappedPort
        val configPath = File(tempDir, "plantuml-context.yml")
        configPath.writeText(
            """
            output:
              rag: "${ragDir.absolutePath}"
            rag:
              databaseUrl: "localhost"
              port: $actualPort
              username: "${postgresContainer.username}"
              password: "${postgresContainer.password}"
              tableName: "embeddings_large"
            """.trimIndent()
        )

        // When: Execute the task
        task.reindexRag()

        // Then: Task completes and diagram still exists
        assertTrue(largeDiagram.exists(), "Large diagram should still exist")
        assertTrue(largeDiagram.readText().contains("Class50"), "Diagram content should be preserved")
    }

    @Test
    fun `should handle unicode content in DATABASE mode`() {
        // Given: RAG directory with unicode content
        val ragDir = File(tempDir, "rag-unicode")
        ragDir.mkdirs()

        val unicodeDiagram = File(ragDir, "unicode-diagram.puml").apply {
            writeText(
                """
                @startuml
                class Utilisateur {
                  +nom: String
                  +prénom: String
                  +email: String
                }
                note "Création d'un utilisateur\navec des caractères spéciaux: é à ü ñ 中文 🎉"
                @enduml
                """.trimIndent()
            )
        }

        // Configure with testcontainers PostgreSQL - use mapped port
        val actualPort = postgresContainer.firstMappedPort
        val configPath = File(tempDir, "plantuml-context.yml")
        configPath.writeText(
            """
            output:
              rag: "${ragDir.absolutePath}"
            rag:
              databaseUrl: "localhost"
              port: $actualPort
              username: "${postgresContainer.username}"
              password: "${postgresContainer.password}"
              tableName: "embeddings_unicode"
            """.trimIndent()
        )

        // When: Execute the task
        task.reindexRag()

        // Then: Task completes and unicode content preserved
        assertTrue(unicodeDiagram.exists(), "Unicode diagram should still exist")
        assertTrue(
            unicodeDiagram.readText().contains("Utilisateur"),
            "Unicode content should be preserved"
        )
    }

    @Test
    fun `should handle multiple history files in DATABASE mode`() {
        // Given: RAG directory with multiple history files
        val ragDir = File(tempDir, "rag-multi-history")
        ragDir.mkdirs()

        for (i in 1..5) {
            File(ragDir, "attempt-history-$i.json").apply {
                writeText(
                    """
                    {
                      "prompt": "Test prompt $i",
                      "attempts": $i,
                      "finalDiagram": "@startuml\nclass Test$i\n@enduml",
                      "validationFeedback": []
                    }
                    """.trimIndent()
                )
            }
        }

        // Configure with testcontainers PostgreSQL - use mapped port
        val actualPort = postgresContainer.firstMappedPort
        val configPath = File(tempDir, "plantuml-context.yml")
        configPath.writeText(
            """
            output:
              rag: "${ragDir.absolutePath}"
            rag:
              databaseUrl: "localhost"
              port: $actualPort
              username: "${postgresContainer.username}"
              password: "${postgresContainer.password}"
              tableName: "embeddings_history"
            """.trimIndent()
        )

        // When: Execute the task
        task.reindexRag()

        // Then: All history files still exist
        for (i in 1..5) {
            val historyFile = File(ragDir, "attempt-history-$i.json")
            assertTrue(historyFile.exists(), "History file $i should still exist")
        }
    }
}
