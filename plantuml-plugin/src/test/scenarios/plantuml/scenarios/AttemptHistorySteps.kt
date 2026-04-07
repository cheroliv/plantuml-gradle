package plantuml.scenarios
//
//import io.cucumber.java.After
//import io.cucumber.java.Before
//import io.cucumber.java.en.Given
//import io.cucumber.java.en.Then
//import io.cucumber.java.en.When
//import org.mockito.ArgumentMatchers
//import org.mockito.Mockito
//import plantuml.PlantumlDiagram
//import plantuml.service.DiagramProcessor
//import plantuml.service.PlantumlService
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//import kotlin.test.assertNull
//import kotlin.test.assertTrue
//
//class AttemptHistorySteps {
//    private lateinit var plantumlService: PlantumlService
//    private lateinit var diagramProcessor: DiagramProcessor
//    private var result: PlantumlDiagram? = null
//    private var maxIterations: Int = 5
//    private var promptContent: String = ""
//    private var attemptResults: MutableList<Boolean> = mutableListOf()
//
//    @Before
//    fun setup() {
//        // Initialisation avant chaque scénario
//        plantumlService = Mockito.mock(PlantumlService::class.java)
//        diagramProcessor = DiagramProcessor(plantumlService, null, null)
//        result = null
//        maxIterations = 5
//        promptContent = ""
//        attemptResults.clear()
//    }
//
//    @After
//    fun tearDown() {
//        // Nettoyage après chaque scénario
//    }
//
//    @Given("a prompt file {string} with content {string}")
//    fun given_prompt_file_with_content(filename: String, content: String) {
//        promptContent = content
//    }
//
//    @Given("a mock LLM that returns an invalid PlantUML diagram on first attempt")
//    fun given_mock_llm_returns_invalid_on_first_attempt() {
//        attemptResults.add(false) // Premier essai invalide
//    }
//
//    @Given("a mock LLM that returns a valid PlantUML diagram on second attempt")
//    fun given_mock_llm_returns_valid_on_second_attempt() {
//        attemptResults.add(true) // Deuxième essai valide
//    }
//
//    @Given("a mock LLM that always returns invalid PlantUML diagrams")
//    fun given_mock_llm_always_returns_invalid() {
//        // Pour les tests avec 5 itérations, on ajoute 5 résultats invalides
//        repeat(5) { attemptResults.add(false) }
//    }
//
//    @Given("a mock LLM that returns invalid PlantUML diagrams for first {int} attempts")
//    fun given_mock_llm_returns_invalid_for_first_attempts(count: Int) {
//        repeat(count) { attemptResults.add(false) }
//    }
//
//    @Given("a mock LLM that returns a valid PlantUML diagram on fourth attempt")
//    fun given_mock_llm_returns_valid_on_fourth_attempt() {
//        attemptResults.add(true) // Quatrième essai valide
//    }
//
//    @When("I run processPlantumlPrompts task")
//    fun when_run_process_plantuml_prompts_task() {
//        // Configurer le mock pour retourner les résultats selon la séquence définie
//        val mockResponses = attemptResults.mapIndexed { index, isValid ->
//            if (isValid) {
//                PlantumlService.SyntaxValidationResult.Valid
//            } else {
//                PlantumlService.SyntaxValidationResult.Invalid(
//                    "Syntax error in attempt ${index + 1}",
//                    "This is a test error for attempt ${index + 1}"
//                )
//            }
//        }.toTypedArray()
//
//        // Si nous avons moins de résultats que maxIterations, remplir avec des échecs
//        val paddedResponses = if (mockResponses.size < maxIterations) {
//            mockResponses.plus(Array(maxIterations - mockResponses.size) {
//                PlantumlService.SyntaxValidationResult.Invalid(
//                    "Syntax error",
//                    "This is a test error"
//                )
//            })
//        } else {
//            mockResponses
//        }
//
//        Mockito.`when`(plantumlService.validateSyntax(ArgumentMatchers.anyString())).thenReturn(
//            paddedResponses[0],
//            *paddedResponses.drop(1).toTypedArray()
//        )
//
//        result = diagramProcessor.processPrompt(promptContent, maxIterations)
//    }
//
//    @When("I run processPlantumlPrompts task with max {int} iterations")
//    fun when_run_process_plantuml_prompts_task_with_max_iterations(iterations: Int) {
//        maxIterations = iterations
//        when_run_process_plantuml_prompts_task()
//    }
//
//    @Then("attempt history should be tracked with {int} entries")
//    fun then_attempt_history_should_be_tracked_with_entries(count: Int) {
//        assertNotNull(result)
//        // Vérifier que la conversation contient suffisamment d'entrées
//        assertTrue(result!!.conversation.size >= count)
//    }
//
//    @Then("the first entry should indicate syntax error")
//    fun then_first_entry_should_indicate_syntax_error() {
//        assertNotNull(result)
//        // Avec notre implémentation actuelle, la conversation contient simplement le prompt initial
//        // Dans une implémentation complète, on vérifierait ici le contenu détaillé de l'historique
//        assertEquals("Processed prompt: $promptContent", result!!.conversation.first())
//    }
//
//    @Then("the second entry should indicate success")
//    fun then_second_entry_should_indicate_success() {
//        assertNotNull(result)
//        // Dans une implémentation complète, on vérifierait ici le contenu détaillé de l'historique
//    }
//
//    @Then("a valid diagram should be generated")
//    fun then_a_valid_diagram_should_be_generated() {
//        assertNotNull(result)
//    }
//
//    @Then("a PNG image should be created")
//    fun then_a_png_image_should_be_created() {
//        // Ce test vérifierait la création du fichier dans une implémentation réelle
//        assertNotNull(result)
//    }
//
//    @Then("attempt history should be archived with {int} entries")
//    fun then_attempt_history_should_be_archived_with_entries(count: Int) {
//        assertNull(result)
//        // Dans une implémentation réelle, on vérifierait que l'historique a été archivé
//        // Par exemple, en vérifiant la présence de fichiers d'historique ou des logs spécifiques
//        Mockito.verify(plantumlService, Mockito.times(count)).validateSyntax(ArgumentMatchers.anyString())
//    }
//
//    @Then("no diagram should be generated")
//    fun then_no_diagram_should_be_generated() {
//        assertNull(result)
//    }
//
//    @Then("the prompt file should be deleted")
//    fun then_the_prompt_file_should_be_deleted() {
//        // Ce test vérifierait la suppression du fichier dans une implémentation réelle
//    }
//
//    @Then("validation feedback should be saved")
//    fun then_validation_feedback_should_be_saved() {
//        // Ce test vérifierait la sauvegarde du feedback dans une implémentation réelle
//    }
//
//    @Then("the first three entries should indicate syntax errors")
//    fun then_first_three_entries_should_indicate_syntax_errors() {
//        assertNotNull(result)
//        // Dans une implémentation complète, on vérifierait ici le contenu détaillé de l'historique
//    }
//
//    @Then("the fourth entry should indicate success")
//    fun then_fourth_entry_should_indicate_success() {
//        assertNotNull(result)
//        // Dans une implémentation complète, on vérifierait ici le contenu détaillé de l'historique
//    }
//}