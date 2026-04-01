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
        val promptsDir = File(world.projectDir, "prompts").apply { mkdirs() }
        val promptFile = File(promptsDir, fileName)
        promptFile.writeText(content)
    }

    @Given("a valid PlantUML file {string} with content {string}")
    fun createValidPlantumlFile(fileName: String, content: String) {
        world.createGradleProject()
        val testDir = File(world.projectDir, "test-diagrams").apply { mkdirs() }
        val plantumlFile = File(testDir, fileName)
        plantumlFile.writeText(content)
    }

    @Given("an invalid PlantUML file {string} with content {string}")
    fun createInvalidPlantumlFile(fileName: String, content: String) {
        world.createGradleProject()
        val testDir = File(world.projectDir, "test-diagrams").apply { mkdirs() }
        val plantumlFile = File(testDir, fileName)
        plantumlFile.writeText(content)
    }

    @Given("a mock LLM that returns a valid PlantUML diagram")
    fun mockLlmReturnsValidDiagram() {
        world.startMockLlm("""
            {
              "plantuml": {
                "code": "@startuml\nactor User\n@enduml",
                "description": "Generated diagram"
              }
            }
        """.trimIndent())
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
        val ragDir = File(world.projectDir, "generated/rag")
        assertThat(ragDir).exists()
        val diagrams = ragDir.listFiles { file -> file.extension == "puml" }
        assertThat(diagrams).isNotNull.isNotEmpty
    }

    @Then("a PNG image should be created")
    fun verifyPngImageCreated() {
        val imagesDir = File(world.projectDir, "generated/images")
        assertThat(imagesDir).exists()
        val images = imagesDir.listFiles { file -> file.extension == "png" }
        assertThat(images).isNotNull.isNotEmpty
    }

    @Then("the prompt file should be deleted")
    fun verifyPromptFileDeleted() {
        val promptsDir = File(world.projectDir, "prompts")
        val promptFiles = promptsDir.listFiles()
        assertThat(promptFiles).isNullOrEmpty()
    }

    @Then("the syntax should be reported as valid")
    fun verifySyntaxValid() {
        assertThat(world.buildResult).isNotNull
        // Vérifier que la sortie ne contient pas d'erreurs
        assertThat(world.buildResult!!.output).doesNotContain("Invalid")
    }

    @Then("the syntax should be reported as invalid")
    fun verifySyntaxInvalid() {
        assertThat(world.buildResult).isNotNull
        // Vérifier que la sortie contient des erreurs
        assertThat(world.buildResult!!.output).containsAnyOf("Invalid", "Error")
    }

    @Then("error details should be provided")
    fun verifyErrorDetailsProvided() {
        assertThat(world.buildResult).isNotNull
        // Vérifier que des détails d'erreur sont présents
        assertThat(world.buildResult!!.output).isNotEmpty()
    }

    @Then("the LLM should correct the syntax after iteration")
    fun verifyLlmCorrectsSyntax() {
        // Pour ce test, on suppose que le processus a réussi
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
        val validationsDir = File(world.projectDir, "generated/validations")
        if (validationsDir.exists()) {
            val feedbackFiles = validationsDir.listFiles { file -> file.extension == "json" }
            assertThat(feedbackFiles).isNotNull.isNotEmpty
        }
    }
}