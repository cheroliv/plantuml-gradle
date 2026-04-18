package plantuml.scenarios

import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import java.io.File

class PlantUmlProcessingSteps(private val world: PlantumlWorld) {

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
        properties["plantuml.test.mode"] = "true"

        world.executeGradle("processPlantumlPrompts", properties = properties)
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
}
