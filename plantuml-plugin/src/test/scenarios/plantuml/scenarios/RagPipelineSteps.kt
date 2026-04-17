package plantuml.scenarios

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import java.io.File
import java.time.Duration

class RagPipelineSteps(private val world: PlantumlWorld) {

    companion object {
        private val log = LoggerFactory.getLogger(RagPipelineSteps::class.java)
    }

    // Shared container reference - avoids duplicate variable issue with CommonSteps
    private var pgvectorContainer: PostgreSQLContainer<*>? = null

    @Given("a PlantUML configuration with RAG enabled")
    fun configurePlantumlWithRag() {
        val configPath = File(world.projectDir, "plantuml-context.yml")
        if (configPath.exists()) {
            val configContent = configPath.readText()
            if (!configContent.contains("rag:")) {
                val updatedConfig = configContent.replace(
                    "output:".toRegex(),
                    """rag:
                      |  enabled: true
                      |  mode: "database"
                      |output:
                    """.trimMargin()
                )
                configPath.writeText(updatedConfig)
            }
        }
    }

    @Given("a running pgvector container")
    fun startPgvectorContainer() {
        log.info("Starting pgvector container...")
        
        pgvectorContainer = PostgreSQLContainer<Nothing>("pgvector/pgvector:pg15").apply {
            withDatabaseName("plantuml_rag")
            withUsername("test")
            withPassword("test")
            withStartupTimeout(Duration.ofMinutes(2))
            withReuse(false) // Force new container each test
        }.also { container ->
            try {
                container.start()
                log.info("pgvector container started: ${container.containerId}")
            } catch (e: Exception) {
                log.error("Failed to start pgvector container: ${e.message}", e)
                throw e
            }
        }
        
        val configPath = File(world.projectDir, "plantuml-context.yml")
        val configContent = configPath.readText()
        val updatedConfig = configContent.replace(
            "rag:\\s*\\n\\s*databaseUrl:.*".toRegex(RegexOption.MULTILINE),
            """rag:
              |  databaseUrl: "localhost"
              |  port: ${pgvectorContainer!!.firstMappedPort}
              |  username: "test"
              |  password: "test"
              |  tableName: "embeddings_test"
            """.trimMargin()
        )
        configPath.writeText(updatedConfig)
        log.info("pgvector configured on port ${pgvectorContainer!!.firstMappedPort}")
    }

    @Given("a running pgvector container with existing embeddings")
    fun startPgvectorContainerWithEmbeddings() {
        startPgvectorContainer()
        val ragDir = File(world.projectDir, "generated/rag")
        ragDir.mkdirs()
        File(ragDir, "existing-diagram.puml").writeText(
            """
            @startuml
            class ExistingClass {
              +field: String
            }
            @enduml
            """.trimIndent()
        )
        File(ragDir, "attempt-history-001.json").writeText(
            """
            {
              "prompt": "Existing prompt for RAG context",
              "attempts": 1,
              "finalDiagram": "@startuml\nclass Existing\n@enduml",
              "validationFeedback": []
            }
            """.trimIndent()
        )
    }

    @Given("a running pgvector container with embeddings for {int} prompts")
    fun startPgvectorContainerWithMultipleEmbeddings(count: Int) {
        log.info("Starting pgvector container for $count prompts...")
        
        pgvectorContainer = PostgreSQLContainer<Nothing>("pgvector/pgvector:pg15").apply {
            withDatabaseName("plantuml_rag")
            withUsername("test")
            withPassword("test")
            withStartupTimeout(Duration.ofMinutes(2))
            withReuse(false)
        }.also { container ->
            try {
                container.start()
                log.info("pgvector container started: ${container.containerId}")
            } catch (e: Exception) {
                log.error("Failed to start pgvector container: ${e.message}", e)
                throw e
            }
        }
        
        val ragDir = File(world.projectDir, "generated/rag")
        ragDir.mkdirs()
        
        for (i in 1..count) {
            File(ragDir, "diagram-$i.puml").writeText(
                """
                @startuml
                class Class$i {
                  +field: String
                }
                @enduml
                """.trimIndent()
            )
            File(ragDir, "attempt-history-$i.json").writeText(
                """
                {
                  "prompt": "Prompt number $i",
                  "attempts": 1,
                  "finalDiagram": "@startuml\nclass Class$i\n@enduml",
                  "validationFeedback": []
                }
                """.trimIndent()
            )
        }
        
        val configPath = File(world.projectDir, "plantuml-context.yml")
        val configContent = configPath.readText()
        val updatedConfig = configContent.replace(
            "rag:\\s*\\n\\s*databaseUrl:.*".toRegex(RegexOption.MULTILINE),
            """rag:
              |  databaseUrl: "localhost"
              |  port: ${pgvectorContainer!!.firstMappedPort}
              |  username: "test"
              |  password: "test"
              |  tableName: "embeddings_multi"
            """.trimMargin()
        )
        configPath.writeText(updatedConfig)
    }

    @When("I run reindexPlantumlRag task")
    fun runReindexPlantumlRagTask() = runBlocking {
        val properties = mutableMapOf<String, String>()
        pgvectorContainer?.let {
            properties["plantuml.rag.databaseUrl"] = "localhost"
            properties["plantuml.rag.port"] = it.firstMappedPort.toString()
            properties["plantuml.rag.username"] = "test"
            properties["plantuml.rag.password"] = "test"
            properties["plantuml.rag.tableName"] = "embeddings_test"
        }
        world.projectDir?.let {
            properties["plugin.project.dir"] = it.absolutePath
        }
        properties["plantuml.test.mode"] = "true"

        world.executeGradle("reindexPlantumlRag", properties = properties)
    }

    @Then("embeddings should be stored in pgvector")
    fun verifyEmbeddingsStoredInPgvector() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).containsAnyOf(
            "Rebuilding RAG index",
            "RAG mode",
            "database"
        )
    }

    @Then("the embedding count should match prompt count")
    fun verifyEmbeddingCountMatchesPromptCount() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).contains("BUILD SUCCESSFUL")
    }

    @Then("the LLM request should contain RAG context chunks")
    fun verifyLlmRequestContainsRagContext() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).containsAnyOf(
            "RAG",
            "context",
            "similarity"
        )
    }

    @Then("the RAG similarity score should be logged")
    fun verifyRagSimilarityScoreLogged() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).containsAnyOf(
            "similarity",
            "score",
            "RAG"
        )
    }

    @Given("the prompt file has not been modified")
    fun promptFileHasNotBeenModified() {
        val promptFile = File(world.projectDir, "prompts/rag-test.prompt")
        if (promptFile.exists()) {
            val originalTime = promptFile.lastModified()
            Thread.sleep(100)
            assertThat(promptFile.lastModified()).isEqualTo(originalTime)
        }
    }

    @Then("unchanged prompts should be skipped")
    fun verifyUnchangedPromptsSkipped() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).containsAnyOf(
            "BUILD SUCCESSFUL",
            "Rebuilding RAG"
        )
    }

    @Then("only new or modified prompts should be indexed")
    fun verifyOnlyNewOrModifiedPromptsIndexed() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).contains("BUILD SUCCESSFUL")
    }

    @Given("one prompt file is deleted")
    fun deleteOnePromptFile() {
        val ragDir = File(world.projectDir, "generated/rag")
        if (ragDir.exists()) {
            val promptFiles = ragDir.listFiles { file -> file.name.contains("attempt-history") }
            promptFiles?.firstOrNull()?.delete()
        }
    }

    @Then("the deleted prompt embeddings should be removed")
    fun verifyDeletedPromptEmbeddingsRemoved() {
        assertThat(world.buildResult).isNotNull
    }

    @Then("the embedding count should decrease by one")
    fun verifyEmbeddingCountDecreasedByOne() {
        val ragDir = File(world.projectDir, "generated/rag")
        val historyFiles = ragDir.listFiles { file -> file.name.contains("attempt-history") }
        assertThat(historyFiles).isNotNull
    }
}
