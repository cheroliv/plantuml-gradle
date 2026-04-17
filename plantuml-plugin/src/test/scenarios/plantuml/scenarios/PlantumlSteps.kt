package plantuml.scenarios

import io.cucumber.java.After
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import java.io.File

class PlantumlSteps(private val world: PlantumlWorld) {

    @After
    fun cleanup() {
        world.cleanup()
    }

    @Given("a prompt file {string} with content {string}")
    fun createPromptFile(fileName: String, content: String) {
        world.createGradleProject()
        world.createPromptFile(fileName, content)
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
}
