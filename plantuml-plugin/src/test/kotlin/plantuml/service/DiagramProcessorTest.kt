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

class DiagramProcessorTest {

    private lateinit var mockPlantumlService: PlantumlService
    private lateinit var diagramProcessor: DiagramProcessor

    @BeforeEach
    fun setUp() {
        mockPlantumlService = mock(PlantumlService::class.java)
        diagramProcessor = DiagramProcessor(mockPlantumlService)
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
        assertEquals("Processed prompt: $prompt", result!!.conversation.first())
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
}