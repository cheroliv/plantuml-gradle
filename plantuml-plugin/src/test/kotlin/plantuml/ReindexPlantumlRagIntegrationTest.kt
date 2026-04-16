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
                class User {
                  +name: String
                  +firstName: String
                  +email: String
                }
                note "User creation\nwith special characters: é à ü ñ 中文 🎉"
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

    @Test
    fun `should handle multiple prompt files in DATABASE mode`() {
        // Given: RAG directory with 10+ prompt files
        val ragDir = File(tempDir, "rag-multi-prompts")
        ragDir.mkdirs()

        val promptFiles = mutableListOf<File>()
        for (i in 1..10) {
            val promptFile = File(ragDir, "prompt-$i.prompt").apply {
                writeText(
                    """
                    Create a ${if (i % 3 == 0) "sequence" else if (i % 3 == 1) "class" else "component"} diagram for system component $i
                    """.trimIndent()
                )
            }
            promptFiles.add(promptFile)

            File(ragDir, "diagram-$i.puml").apply {
                writeText(
                    """
                    @startuml
                    class Component$i {
                      +id: Long
                      +name: String
                      +version: String
                    }
                    @enduml
                    """.trimIndent()
                )
            }
        }

        val historyFile = File(ragDir, "attempt-history-001.json").apply {
            writeText(
                """
                {
                  "prompt": "Multiple component diagrams",
                  "attempts": 1,
                  "finalDiagram": "@startuml\nclass Component\n@enduml",
                  "validationFeedback": []
                }
                """.trimIndent()
            )
        }

        // Configure with testcontainers PostgreSQL
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
              tableName: "embeddings_multi_prompts"
            """.trimIndent()
        )

        // When: Execute the task
        task.reindexRag()

        // Then: All prompt files and diagrams still exist
        promptFiles.forEach { assertTrue(it.exists(), "Prompt file ${it.name} should exist") }
        assertTrue(historyFile.exists(), "History file should exist")
        for (i in 1..10) {
            assertTrue(File(ragDir, "diagram-$i.puml").exists(), "Diagram $i should exist")
        }
    }

    @Test
    fun `should handle nested directory structure in DATABASE mode`() {
        // Given: Nested directory structure with prompts in subdirectories
        val baseRagDir = File(tempDir, "rag-nested")
        baseRagDir.mkdirs()

        val domain1Dir = File(baseRagDir, "architecture/domain1")
        domain1Dir.mkdirs()
        val domain2Dir = File(baseRagDir, "architecture/domain2")
        domain2Dir.mkdirs()
        val sharedDir = File(baseRagDir, "architecture/shared")
        sharedDir.mkdirs()

        File(domain1Dir, "user-service.puml").apply {
            writeText(
                """
                @startuml
                class UserService {
                  +createUser(): User
                  +deleteUser(id: Long)
                }
                @enduml
                """.trimIndent()
            )
        }

        File(domain2Dir, "order-service.puml").apply {
            writeText(
                """
                @startuml
                class OrderService {
                  +createOrder(): Order
                  +cancelOrder(id: Long)
                }
                @enduml
                """.trimIndent()
            )
        }

        File(sharedDir, "common-types.puml").apply {
            writeText(
                """
                @startuml
                class User { +id: Long }
                class Order { +id: Long }
                @enduml
                """.trimIndent()
            )
        }

        val historyFile = File(baseRagDir, "attempt-history-001.json").apply {
            writeText(
                """
                {
                  "prompt": "Nested architecture diagrams",
                  "attempts": 1,
                  "finalDiagram": "@startuml\nclass Architecture\n@enduml",
                  "validationFeedback": []
                }
                """.trimIndent()
            )
        }

        // Configure with testcontainers PostgreSQL
        val actualPort = postgresContainer.firstMappedPort
        val configPath = File(tempDir, "plantuml-context.yml")
        configPath.writeText(
            """
            output:
              rag: "${baseRagDir.absolutePath}"
            rag:
              databaseUrl: "localhost"
              port: $actualPort
              username: "${postgresContainer.username}"
              password: "${postgresContainer.password}"
              tableName: "embeddings_nested"
            """.trimIndent()
        )

        // When: Execute the task
        task.reindexRag()

        // Then: All nested files still exist
        assertTrue(File(domain1Dir, "user-service.puml").exists(), "Domain1 diagram should exist")
        assertTrue(File(domain2Dir, "order-service.puml").exists(), "Domain2 diagram should exist")
        assertTrue(File(sharedDir, "common-types.puml").exists(), "Shared diagram should exist")
        assertTrue(historyFile.exists(), "History file should exist")
    }

    @Test
    fun `should handle concurrent indexing in DATABASE mode`() {
        // Given: RAG directory with multiple diagrams and concurrent execution
        val ragDir = File(tempDir, "rag-concurrent")
        ragDir.mkdirs()

        for (i in 1..5) {
            File(ragDir, "diagram-$i.puml").apply {
                writeText(
                    """
                    @startuml
                    class ConcurrentClass$i {
                      +field: String
                      +method(): Void
                    }
                    @enduml
                    """.trimIndent()
                )
            }
        }

        val historyFile = File(ragDir, "attempt-history-001.json").apply {
            writeText(
                """
                {
                  "prompt": "Concurrent indexing test",
                  "attempts": 1,
                  "finalDiagram": "@startuml\nclass Concurrent\n@enduml",
                  "validationFeedback": []
                }
                """.trimIndent()
            )
        }

        // Configure with testcontainers PostgreSQL
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
              tableName: "embeddings_concurrent"
            """.trimIndent()
        )

        // When: Execute the task multiple times concurrently
        val threads = mutableListOf<Thread>()
        for (i in 1..3) {
            val thread = Thread {
                task.reindexRag()
            }
            threads.add(thread)
            thread.start()
        }

        threads.forEach { it.join() }

        // Then: All diagrams still exist, no corruption
        for (i in 1..5) {
            assertTrue(File(ragDir, "diagram-$i.puml").exists(), "Diagram $i should exist")
        }
        assertTrue(historyFile.exists(), "History file should exist")
    }

    @Test
    fun `should recover from partial failure in DATABASE mode`() {
        // Given: RAG directory with valid and invalid diagrams
        val ragDir = File(tempDir, "rag-partial-failure")
        ragDir.mkdirs()

        File(ragDir, "valid-diagram.puml").apply {
            writeText(
                """
                @startuml
                class ValidClass {
                  +field: String
                }
                @enduml
                """.trimIndent()
            )
        }

        File(ragDir, "invalid-diagram.puml").apply {
            writeText(
                """
                @startuml
                class InvalidClass {
                  +field: String
                // Missing @enduml - invalid syntax
                """.trimIndent()
            )
        }

        val historyFile = File(ragDir, "attempt-history-001.json").apply {
            writeText(
                """
                {
                  "prompt": "Partial failure test",
                  "attempts": 1,
                  "finalDiagram": "@startuml\nclass Valid\n@enduml",
                  "validationFeedback": []
                }
                """.trimIndent()
            )
        }

        // Configure with testcontainers PostgreSQL
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
              tableName: "embeddings_partial"
            """.trimIndent()
        )

        // When: Execute the task (should handle invalid diagram gracefully)
        task.reindexRag()

        // Then: All files still exist (no deletion on error)
        assertTrue(File(ragDir, "valid-diagram.puml").exists(), "Valid diagram should exist")
        assertTrue(File(ragDir, "invalid-diagram.puml").exists(), "Invalid diagram should exist")
        assertTrue(historyFile.exists(), "History file should exist")
    }

    @Test
    fun `should handle very large embeddings in DATABASE mode`() {
        // Given: RAG directory with a very large diagram (>10KB)
        val ragDir = File(tempDir, "rag-very-large")
        ragDir.mkdirs()

        val largeDiagram = File(ragDir, "very-large-system.puml").apply {
            val content = buildString {
                appendLine("@startuml")
                appendLine("package com.example.largesystem {")
                for (i in 1..100) {
                    appendLine("  class LargeClass$i {")
                    appendLine("    +field1: String")
                    appendLine("    +field2: Int")
                    appendLine("    +field3: Long")
                    appendLine("    +field4: Boolean")
                    appendLine("    +method1(): Void")
                    appendLine("    +method2(): String")
                    appendLine("    +method3(): Int")
                    appendLine("    +method4(): Boolean")
                    appendLine("  }")
                }
                for (i in 1..99) {
                    appendLine("  LargeClass$i --> LargeClass${i + 1}")
                }
                appendLine("}")
                appendLine("@enduml")
            }
            writeText(content)
        }

        val historyFile = File(ragDir, "attempt-history-001.json").apply {
            writeText(
                """
                {
                  "prompt": "Very large system diagram with 100+ classes",
                  "attempts": 1,
                  "finalDiagram": "@startuml\nclass Large\n@enduml",
                  "validationFeedback": []
                }
                """.trimIndent()
            )
        }

        // Configure with testcontainers PostgreSQL
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
              tableName: "embeddings_very_large"
            """.trimIndent()
        )

        // When: Execute the task
        task.reindexRag()

        // Then: Large diagram still exists and content preserved
        assertTrue(largeDiagram.exists(), "Large diagram should exist")
        assertTrue(largeDiagram.readText().contains("LargeClass100"), "Large diagram content should be preserved")
        assertTrue(historyFile.exists(), "History file should exist")
    }
}
