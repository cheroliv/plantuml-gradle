package plantuml.scenarios

import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat

class MinimalFeatureSteps(private val world: PlantumlWorld) {

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

    @Then("a PlantUML diagram should be generated")
    fun verifyPlantumlDiagramGenerated() {
        world.verifyDirectoryContainsFiles("generated/rag", "puml", "A PlantUML diagram should be generated")
    }
}
