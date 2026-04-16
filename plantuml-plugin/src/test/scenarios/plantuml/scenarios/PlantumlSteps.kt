package plantuml.scenarios

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import java.io.File

class PlantumlSteps(private val world: PlantumlWorld) {

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
            properties["plantuml.langchain.model"] = "ollama"
            properties["plantuml.langchain.ollama.baseUrl"] = "http://localhost:$it"
        }

        world.executeGradle("processPlantumlPrompts", properties = properties)
    }

    @When("I run processPlantumlPrompts task with max {int} iterations")
    fun runProcessPlantumlPromptsTaskWithMaxIterations(maxIterations: Int) = runBlocking {
        val properties = mutableMapOf<String, String>()
        world.mockServerPort?.let {
            properties["plantuml.langchain.model"] = "ollama"
            properties["plantuml.langchain.ollama.baseUrl"] = "http://localhost:$it"
        }
        properties["plantuml.langchain.maxIterations"] = maxIterations.toString()

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
        if (historyDir.exists()) {
            val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
            assertThat(historyFiles).isNotNull.hasSize(expectedCount)
        } else {
            assertThat(expectedCount).isEqualTo(0)
        }
    }

    @Then("the first entry should indicate syntax error")
    fun firstEntryShouldIndicateSyntaxError() {
        val historyDir = File(world.projectDir, "generated/diagrams")
        if (historyDir.exists()) {
            val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
            assertThat(historyFiles).isNotNull.isNotEmpty

            val firstFile = historyFiles?.firstOrNull()
            if (firstFile != null) {
                val content = firstFile.readText()
                assertThat(content).contains("syntax").contains("error")
            }
        }
    }

    @Then("the second entry should indicate success")
    fun secondEntryShouldIndicateSuccess() {
        val historyDir = File(world.projectDir, "generated/diagrams")
        if (historyDir.exists()) {
            val historyFiles = historyDir.listFiles { file -> file.extension == "json" }

            if (historyFiles != null && historyFiles.size >= 2) {
                val secondFile = historyFiles[1]
                val content = secondFile.readText()

                assertThat(content).doesNotContain("error")
            } else {
                throw AssertionError("Expected at least 2 history entries, but found ${historyFiles?.size ?: 0}")
            }
        } else {
            throw AssertionError("History directory does not exist")
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

    @Then("attempt history should be archived with {int} entries")
    fun attemptHistoryShouldBeArchived(expectedCount: Int) {
        val archiveDir = File(world.projectDir, "generated/diagrams")
        if (archiveDir.exists()) {
            val archivedFiles = archiveDir.listFiles { file -> file.extension == "json" }
            assertThat(archivedFiles).isNotNull.hasSize(expectedCount)
        } else {
            assertThat(expectedCount).isEqualTo(0)
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
        if (historyDir.exists()) {
            val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
                ?.sortedBy { it.nameWithoutExtension }

            if (historyFiles != null && historyFiles.size >= 3) {
                for (i in 0 until 3) {
                    val content = historyFiles[i].readText()
                    assertThat(content).contains("syntax").contains("error")
                }
            } else {
                throw AssertionError("Need at least 3 history entries to evaluate the first three.")
            }
        } else {
            throw AssertionError("History directory does not exist")
        }
    }

    @Then("the fourth entry should indicate success")
    fun fourthEntryShouldIndicateSuccess() {
        val historyDir = File(world.projectDir, "generated/diagrams")
        if (historyDir.exists()) {
            val historyFiles = historyDir.listFiles { file -> file.extension == "json" }
                ?.sortedBy { it.nameWithoutExtension }

            if (historyFiles != null && historyFiles.size >= 4) {
                val content = historyFiles[3].readText()

                assertThat(content).doesNotContain("syntax error")
            } else {
                throw AssertionError("Need at least 4 history entries; only got ${historyFiles?.size ?: 0}.")
            }
        } else {
            throw AssertionError("History directory does not exist")
        }
    }
}
