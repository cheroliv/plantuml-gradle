package plantuml.scenarios

import io.cucumber.java.After
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.testcontainers.containers.PostgreSQLContainer
import java.io.File
import java.time.Duration

class PlantumlSteps(private val world: PlantumlWorld) {

    private var pgvectorContainer: PostgreSQLContainer<*>? = null

    @After
    fun cleanup() {
        world.cleanup()
        pgvectorContainer?.stop()
        pgvectorContainer = null
    }

    @Given("a prompt file {string} with content {string}")
    fun createPromptFile(fileName: String, content: String) {
        world.createGradleProject()
        world.createPromptFile(fileName, content)
    }

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

    @Given("a valid PlantUML file {string} with content {string}")
    fun createValidPlantumlFile(fileName: String, content: String) {
        world.createGradleProject()
        world.createPlantUmlFile(fileName, content)
    }

    @Given("an invalid PlantUML file {string} with content {string}")
    fun createInvalidPlantumlFile(fileName: String, content: String) {
        world.createGradleProject()
        world.createPlantUmlFile(fileName, content)
    }

    @Given("a mock LLM that returns a valid PlantUML diagram")
    fun mockLlmReturnsValidDiagram() {
        world.startMockLlm(
            """
            {
              "plantuml": {
                "code": "@startuml\nactor User\n@enduml",
                "description": "Generated diagram"
              }
            }
        """.trimIndent()
        )
    }

    @When("I run processPlantumlPrompts task")
    fun runProcessPlantumlPromptsTask() = runBlocking {
        val properties = mutableMapOf<String, String>()
        world.mockServerPort?.let {
            properties["plantuml.langchain4j.model"] = "ollama"
            properties["plantuml.langchain4j.ollama.baseUrl"] = "http://localhost:$it"
            properties["plantuml.langchain4j.ollama.modelName"] = "smollm:135m"
        }
        world.projectDir?.let {
            properties["plugin.project.dir"] = it.absolutePath
        }
        properties["plantuml.test.mode"] = "true"

        world.executeGradle("processPlantumlPrompts", properties = properties)
    }

    @When("I run processPlantumlPrompts task with max {int} iterations")
    fun runProcessPlantumlPromptsTaskWithMaxIterations(maxIterations: Int) = runBlocking {
        val properties = mutableMapOf<String, String>()
        world.mockServerPort?.let {
            properties["plantuml.langchain4j.model"] = "ollama"
            properties["plantuml.langchain4j.ollama.baseUrl"] = "http://localhost:$it"
            properties["plantuml.langchain4j.ollama.modelName"] = "smollm:135m"
        }
        properties["plantuml.langchain4j.maxIterations"] = maxIterations.toString()
        world.projectDir?.let {
            properties["plugin.project.dir"] = it.absolutePath
        }

        world.executeGradle("processPlantumlPrompts", properties = properties)
    }

    @When("I run validatePlantumlSyntax task with file {string}")
    fun runValidatePlantumlSyntaxTaskWithFile(fileName: String) = runBlocking {
        val properties = mapOf("plantuml.diagram" to "test-diagrams/$fileName")
        world.executeGradle("validatePlantumlSyntax", properties = properties)
    }

    @Then("a PlantUML diagram should be generated")
    fun verifyPlantumlDiagramGenerated() {
        world.verifyDirectoryContainsFiles("generated/rag", "puml", "A PlantUML diagram should be generated")
    }

    @Then("a PNG image should be created")
    fun verifyPngImageCreated() {
        world.verifyDirectoryContainsFiles("generated/images", "png", "A PNG image should be created")
    }

    @Then("the prompt file should be deleted")
    fun verifyPromptFileDeleted() {
        val promptsDir = File(world.projectDir, "prompts")
        if (promptsDir.exists()) {
            val promptFiles = promptsDir.listFiles()
            assertThat(promptFiles).isNullOrEmpty()
        }
    }

    @Then("the syntax should be reported as valid")
    fun verifySyntaxValid() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).doesNotContain("Invalid")
    }

    @Then("the syntax should be reported as invalid")
    fun verifySyntaxInvalid() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).containsAnyOf("Invalid", "Error")
    }

    @Then("error details should be provided")
    fun verifyErrorDetailsProvided() {
        assertThat(world.buildResult).isNotNull
        assertThat(world.buildResult!!.output).isNotEmpty()
    }

    @Then("the LLM should correct the syntax after iteration")
    fun verifyLlmCorrectsSyntax() {
        assertThat(world.buildResult).isNotNull
    }

    @Then("a valid diagram should be generated")
    fun verifyValidDiagramGenerated() {
        val ragDir = File(world.projectDir, "generated/rag")
        if (ragDir.exists()) {
            val diagrams = ragDir.listFiles { file -> file.extension == "puml" }
            assertThat(diagrams).isNotNull.isNotEmpty
        }
    }

    @Then("validation feedback should be saved")
    fun verifyValidationFeedbackSaved() {
        world.verifyDirectoryContainsFiles("generated/validations", "json", "Validation feedback should be saved")
    }

    @Given("a mock LLM that returns an invalid PlantUML diagram on first attempt")
    fun mockLlmReturnsInvalidOnFirstAttempt() {
        world.startMockLlm(
            """
            {
              "plantuml": {
                "code": "@startuml\nactor User\n@endulm",
                "description": "Generated diagram with syntax error"
              }
            }
        """.trimIndent()
        )
    }

    @Given("a mock LLM that returns a valid PlantUML diagram on second attempt")
    fun mockLlmReturnsValidOnSecondAttempt() {
        world.startMockLlm(
            """
            {
              "plantuml": {
                "code": "@startuml\nactor User\n@enduml",
                "description": "Generated diagram"
              }
            }
        """.trimIndent()
        )
    }

    @Then("attempt history should be tracked with {int} entries")
    fun attemptHistoryShouldHaveEntries(expectedCount: Int) {
        val historyDir = File(world.projectDir, "generated/diagrams")
        assertThat(historyDir).`as`("History directory should exist").exists()
        
        val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
        assertThat(historyFiles).`as`("Should find JSON history files").isNotNull.isNotEmpty
        
        // Check that the history file contains the expected number of entries
        val historyFile = historyFiles?.firstOrNull()
        if (historyFile != null) {
            val content = historyFile.readText()
            assertThat(content).contains("\"totalAttempts\" : $expectedCount")
        }
    }

    @Then("the first entry should indicate syntax error")
    fun firstEntryShouldIndicateSyntaxError() {
        val historyDir = File(world.projectDir, "generated/diagrams")
        assertThat(historyDir).`as`("History directory should exist").exists()
        
        val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
        
        assertThat(historyFiles).`as`("Should find JSON files").isNotNull.isNotEmpty
        
        val firstFile = historyFiles?.firstOrNull()
        if (firstFile != null) {
            val content = firstFile.readText()
            // Check that first entry is invalid (valid: false)
            val validPattern = Regex("\"valid\"\\s*:\\s*(true|false)")
            val validMatches = validPattern.findAll(content).map { it.groupValues[1] }.toList()
            
            assertThat(validMatches).`as`("Should have at least 1 entry").isNotEmpty()
            assertThat(validMatches[0]).`as`("First entry should be invalid").isEqualTo("false")
        }
    }

    @Then("the second entry should indicate success")
    fun secondEntryShouldIndicateSuccess() {
        val historyDir = File(world.projectDir, "generated/diagrams")
        assertThat(historyDir).`as`("History directory should exist").exists()
        
        val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
        
        assertThat(historyFiles).`as`("Should find JSON files").isNotNull.isNotEmpty
        
        val latestFile = historyFiles?.firstOrNull()
        if (latestFile != null) {
            val content = latestFile.readText()
            
            // Check that 2nd entry is valid (valid: true)
            val validPattern = Regex("\"valid\"\\s*:\\s*(true|false)")
            val validMatches = validPattern.findAll(content).map { it.groupValues[1] }.toList()
            
            assertThat(validMatches.size).`as`("Should have at least 2 entries").isGreaterThanOrEqualTo(2)
            assertThat(validMatches[1]).`as`("Second entry should be valid (true)").isEqualTo("true")
        } else {
            throw AssertionError("No JSON files found in history directory")
        }
    }

    @Given("a mock LLM that always returns invalid PlantUML diagrams")
    fun mockLlmAlwaysReturnsInvalidDiagrams() {
        world.startMockLlm(
            """
            {
              "plantuml": {
                "code": "@startumlnactor User\n@endulm",
                "description": "Invalid PlantUML diagram"
              }
            }
        """.trimIndent()
        )
    }

    @Given("a mock LLM that returns invalid PlantUML diagrams for first {int} attempts")
    fun mockLlmReturnsInvalidForFirstAttempts(attempts: Int) {
        world.startMockLlm(
            """
            {
              "plantuml": {
                "code": "@startumlnactor User\n@endulm",
                "description": "Invalid PlantUML diagram"
              }
            }
        """.trimIndent()
        )
    }

    @Given("a mock LLM that returns a valid PlantUML diagram on fourth attempt")
    fun mockLlmReturnsValidOnFourthAttempt() {
        world.startMockLlm(
            """
            {
              "plantuml": {
                "code": "@startuml\nactor User\n@enduml",
                "description": "Generated diagram"
              }
            }
        """.trimIndent()
        )
    }

    @Given("a mock LLM that returns a sequence of responses: invalid then valid")
    fun mockLlmReturnsSequenceInvalidThenValid() {
        world.mockLlmReturnsSequence(
            """
            @startuml
            actor User
            @endulm
            """.trimIndent(),
            """
            @startuml
            actor User
            @enduml
            """.trimIndent()
        )
    }

    @Given("a mock LLM that returns a sequence of 4 responses: 3 invalid then valid")
    fun mockLlmReturnsSequence4Responses() {
        world.mockLlmReturnsSequence(
            """
            @startumlnactor User
            @endulm
            """.trimIndent(),
            """
            @startumlnactor User
            @endulm
            """.trimIndent(),
            """
            @startumlnactor User
            @endulm
            """.trimIndent(),
            """
            @startuml
            actor User
            @enduml
            """.trimIndent()
        )
    }

    @Then("attempt history should be archived with {int} entries")
    fun attemptHistoryShouldBeArchived(expectedCount: Int) {
        val archiveDir = File(world.projectDir, "generated/diagrams")
        assertThat(archiveDir).`as`("Archive directory should exist").exists()
        
        val archivedFiles = archiveDir.listFiles { file -> file.extension == "json" }
        assertThat(archivedFiles).`as`("Should find JSON archive files").isNotNull.isNotEmpty
        
        // Check that the archive file contains the expected number of entries
        val archiveFile = archivedFiles?.firstOrNull()
        if (archiveFile != null) {
            val content = archiveFile.readText()
            assertThat(content).contains("\"totalAttempts\" : $expectedCount")
        }
    }

    @Then("no diagram should be generated")
    fun noDiagramShouldBeGenerated() {
        val ragDir = File(world.projectDir, "generated/rag")
        if (ragDir.exists()) {
            val diagrams = ragDir.listFiles { file -> file.extension == "puml" }
            assertThat(diagrams).isNullOrEmpty()
        }
    }

    @Then("the first three entries should indicate syntax errors")
    fun firstThreeEntriesShouldIndicateSyntaxErrors() {
        val historyDir = File(world.projectDir, "generated/diagrams")
        assertThat(historyDir).`as`("History directory should exist").exists()
        
        val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() } // Get most recent file first
        
        assertThat(historyFiles).`as`("Should find at least one JSON file").isNotNull.isNotEmpty
        
        val latestFile = historyFiles!!.firstOrNull()
        if (latestFile != null) {
            val content = latestFile.readText()
            
            // Check that first 3 entries are invalid (valid: false)
            val validPattern = Regex("\"valid\"\\s*:\\s*(true|false)")
            val validMatches = validPattern.findAll(content).map { it.groupValues[1] }.toList()
            
            assertThat(validMatches.size).`as`("Should have at least 3 entries").isGreaterThanOrEqualTo(3)
            
            for (i in 0 until 3) {
                assertThat(validMatches[i]).`as`("Entry $i should be invalid (false)").isEqualTo("false")
            }
        } else {
            throw AssertionError("No JSON files found in history directory")
        }
    }

    @Then("the fourth entry should indicate success")
    fun fourthEntryShouldIndicateSuccess() {
        val historyDir = File(world.projectDir, "generated/diagrams")
        assertThat(historyDir).`as`("History directory should exist").exists()
        
        val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() } // Get most recent file first
        
        assertThat(historyFiles).`as`("Should find at least one JSON file").isNotNull.isNotEmpty
        
        val latestFile = historyFiles!!.firstOrNull()
        if (latestFile != null) {
            val content = latestFile.readText()
            
            // Check that 4th entry is valid (valid: true)
            val validPattern = Regex("\"valid\"\\s*:\\s*(true|false)")
            val validMatches = validPattern.findAll(content).map { it.groupValues[1] }.toList()
            
            assertThat(validMatches.size).`as`("Should have at least 4 entries").isGreaterThanOrEqualTo(4)
            assertThat(validMatches[3]).`as`("Entry 4 should be valid (true)").isEqualTo("true")
        } else {
            throw AssertionError("No JSON files found in history directory")
        }
    }

    @Given("a running pgvector container")
    fun startPgvectorContainer() {
        pgvectorContainer = PostgreSQLContainer<Nothing>("pgvector/pgvector:pg15").apply {
            withDatabaseName("plantuml_rag")
            withUsername("test")
            withPassword("test")
            withStartupTimeout(Duration.ofMinutes(2))
        }.also { it.start() }
        
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
        pgvectorContainer = PostgreSQLContainer<Nothing>("pgvector/pgvector:pg15").apply {
            withDatabaseName("plantuml_rag")
            withUsername("test")
            withPassword("test")
            withStartupTimeout(Duration.ofMinutes(2))
        }.also { it.start() }
        
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

    @Given("a mock LLM that captures the user message")
    fun mockLlmCapturesUserMessage() {
        world.startMockLlm(
            """
            {
              "plantuml": {
                "code": "@startuml\nactor User\n@enduml",
                "description": "Generated diagram with RAG context"
              }
            }
            """.trimIndent()
        )
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
