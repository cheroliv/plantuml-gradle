package plantuml.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import plantuml.PlantumlCode
import plantuml.PlantumlDiagram
import plantuml.ValidationFeedback
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiagramProcessorTest {

    private lateinit var mockPlantumlService: PlantumlService
    private lateinit var diagramProcessor: DiagramProcessor

    @BeforeEach
    fun setUp() {
        mockPlantumlService = mock(PlantumlService::class.java)
        // Pour les tests, nous utilisons l'implémentation simulée qui ne nécessite pas de ChatModel
        diagramProcessor = DiagramProcessor(mockPlantumlService, null, null)
    }

    @Test
    fun `should process prompt with mock llm response`() {
        // Given
        val prompt = "Create a user diagram"
        `when`(mockPlantumlService.validateSyntax(anyString()))
            .thenReturn(PlantumlService.SyntaxValidationResult.Valid)

        // When
        val result = diagramProcessor.processPrompt(prompt)

        // Then
        assertNotNull(result)
        // Vérifier que la conversation contient au moins une entrée
        assertTrue(result.conversation.isNotEmpty())
        // Vérifier que le code PlantUML contient les éléments attendus
        assertTrue(result.plantuml.code.contains("@startuml"))
        assertTrue(result.plantuml.code.contains("@enduml"))
        assertTrue(result.plantuml.description.contains("Auto-generated diagram based on prompt: $prompt"))
    }

    @Test
    fun `should iterate on syntax errors`() {
        // Given
        val prompt = "Create a user diagram"
        // Simuler une erreur de syntaxe suivie d'une correction
        `when`(mockPlantumlService.validateSyntax(anyString()))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Test error", "Stack trace"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Valid)

        // When
        val result = diagramProcessor.processPrompt(prompt, maxIterations = 2)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `should validate diagram quality`() {
        // Given
        val diagram = PlantumlDiagram(
            conversation = listOf("Test conversation"),
            plantuml = PlantumlCode(
                code = "@startuml\nactor User\n@enduml",
                description = "Test diagram"
            )
        )

        // When
        val result = diagramProcessor.validateDiagram(diagram)

        // Then
        assertEquals(8, result.score)
        assertNotNull(result.feedback)
        assertEquals(3, result.recommendations.size)
    }

    @Test
    fun `should save for rag training`() {
        // Given
        val diagram = PlantumlDiagram(
            conversation = listOf("Test conversation"),
            plantuml = PlantumlCode(
                code = "@startuml\nactor User\n@enduml",
                description = "Test diagram"
            )
        )
        val validation = ValidationFeedback(
            score = 8,
            feedback = "Good diagram",
            recommendations = listOf("Add more details")
        )

        // When
        diagramProcessor.saveForRagTraining(diagram, validation)

        // Then
        // Le test passe si aucune exception n'est levée
        // La méthode print dans l'implémentation est normalement exécutée
    }
    
    @Test
    fun `should return null when max iterations exceeded`() {
        // Given
        val prompt = "Create a user diagram"
        `when`(mockPlantumlService.validateSyntax(anyString()))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Test error", "Stack trace"))

        // When
        val result = diagramProcessor.processPrompt(prompt, maxIterations = 1)

        // Then
        assertNull(result)
    }
    
    @Test
    fun `should track attempt history in conversation`() {
        // Given
        val prompt = "Create a user diagram"
        // Simuler une erreur de syntaxe suivie d'une correction
        `when`(mockPlantumlService.validateSyntax(anyString()))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Test error", "Stack trace"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Valid)

        // When
        val result = diagramProcessor.processPrompt(prompt, maxIterations = 2)

        // Then
        assertNotNull(result)
        // Vérifier que l'historique des tentatives est inclus dans la conversation
        assertTrue(result.conversation.size > 1)
        assertTrue(result.conversation.any { it.contains("->") })
    }
    
    @Test
    fun `should maintain proper conversation history on failure`() {
        // Given
        val prompt = "Create a user diagram"
        `when`(mockPlantumlService.validateSyntax(anyString()))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Test error", "Stack trace"))

        // When
        val result = diagramProcessor.processPrompt(prompt, maxIterations = 2)

        // Then
        assertNull(result)
        // Même si le résultat est null, nous voulons vérifier que l'historique est bien géré en interne
        verify(mockPlantumlService, times(2)).validateSyntax(anyString())
    }
}