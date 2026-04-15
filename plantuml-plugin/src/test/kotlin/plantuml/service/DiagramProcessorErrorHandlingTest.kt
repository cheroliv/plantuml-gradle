package plantuml.service

import dev.langchain4j.model.chat.ChatModel
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import plantuml.PlantumlConfig
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests pour couvrir les branches error handling de DiagramProcessor
 * Cible : boucle do-while avec vrai ChatModel (lignes 220-251)
 * 
 * Couvre les branches non couvertes :
 * - if (validationResult is SyntaxValidationResult.Invalid) — branche true
 * - correctionPrompt + chatModel.chat()
 * - while (iterations < maxIterations)
 */
class DiagramProcessorErrorHandlingTest {

    @Test
    fun `should handle invalid response from ChatModel and retry with correction prompt`() {
        val mockPlantumlService = mock(PlantumlService::class.java)
        val mockChatModel = mock(ChatModel::class.java)
        val mockConfig = mock(PlantumlConfig::class.java)
        val mockLangchainConfig = mock(plantuml.LangchainConfig::class.java)

        // Setup : première réponse invalide, deuxième réponse valide
        `when`(mockChatModel.chat(anyString()))
            .thenReturn("@startuml\nInvalid syntax")  // Première réponse invalide
            .thenReturn("@startuml\nFixed\n@enduml")  // Deuxième réponse valide

        // Setup validation : d'abord invalide, puis valide
        `when`(mockPlantumlService.validateSyntax("@startuml\nInvalid syntax"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Syntax error", "stack"))
        `when`(mockPlantumlService.validateSyntax("@startuml\nFixed\n@enduml"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Valid)

        // Setup config
        `when`(mockConfig.langchain4j).thenReturn(mockLangchainConfig)
        `when`(mockLangchainConfig.validationPrompt).thenReturn("Validate this PlantUML")

        val processor = DiagramProcessor(mockPlantumlService, mockChatModel, mockConfig)
        val result = processor.processPrompt("Test prompt with error handling")

        // Verify : le ChatModel a été appelé 2 fois (initial + correction)
        verify(mockChatModel, times(2)).chat(anyString())
        
        // Verify : la validation a été appelée 2 fois
        verify(mockPlantumlService, times(2)).validateSyntax(anyString())
        
        assertNotNull(result)
        assertTrue(result.conversation.isNotEmpty())
    }

    @Test
    fun `should handle multiple correction attempts before success`() {
        val mockPlantumlService = mock(PlantumlService::class.java)
        val mockChatModel = mock(ChatModel::class.java)
        val mockConfig = mock(PlantumlConfig::class.java)
        val mockLangchainConfig = mock(plantuml.LangchainConfig::class.java)

        // Setup : 2 réponses invalides, puis valide
        `when`(mockChatModel.chat(anyString()))
            .thenReturn("@startuml\nInvalid 1")
            .thenReturn("@startuml\nInvalid 2")
            .thenReturn("@startuml\nValid\n@enduml")

        `when`(mockPlantumlService.validateSyntax("@startuml\nInvalid 1"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Error 1", "stack1"))
        `when`(mockPlantumlService.validateSyntax("@startuml\nInvalid 2"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Error 2", "stack2"))
        `when`(mockPlantumlService.validateSyntax("@startuml\nValid\n@enduml"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Valid)

        `when`(mockConfig.langchain4j).thenReturn(mockLangchainConfig)
        `when`(mockLangchainConfig.validationPrompt).thenReturn("Validate this")

        val processor = DiagramProcessor(mockPlantumlService, mockChatModel, mockConfig)
        val result = processor.processPrompt("Test with multiple corrections")

        // Verify : le ChatModel a été appelé 3 fois
        verify(mockChatModel, times(3)).chat(anyString())
        
        assertNotNull(result)
        assertTrue(result.conversation.size >= 3)
    }

    @Test
    fun `should return null when ChatModel always returns invalid responses`() {
        val mockPlantumlService = mock(PlantumlService::class.java)
        val mockChatModel = mock(ChatModel::class.java)
        val mockConfig = mock(PlantumlConfig::class.java)
        val mockLangchainConfig = mock(plantuml.LangchainConfig::class.java)

        // Setup : ChatModel retourne toujours des réponses invalides
        `when`(mockChatModel.chat(anyString()))
            .thenReturn("@startuml\nAlways invalid")

        `when`(mockPlantumlService.validateSyntax(anyString()))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Always error", "stack"))

        `when`(mockConfig.langchain4j).thenReturn(mockLangchainConfig)
        `when`(mockLangchainConfig.validationPrompt).thenReturn("Validate this")

        val processor = DiagramProcessor(mockPlantumlService, mockChatModel, mockConfig)
        val result = processor.processPrompt("Test that always fails", maxIterations = 2)

        // Verify : le ChatModel a été appelé 3 fois (1 initial + 2 retries)
        verify(mockChatModel, times(3)).chat(anyString())
        
        assertNull(result, "Devrait retourner null après maxIterations")
    }

    @Test
    fun `should build history context for correction prompt`() {
        val mockPlantumlService = mock(PlantumlService::class.java)
        val mockChatModel = mock(ChatModel::class.java)
        val mockConfig = mock(PlantumlConfig::class.java)
        val mockLangchainConfig = mock(plantuml.LangchainConfig::class.java)

        // Setup pour forcer l'appel à buildHistoryContext
        `when`(mockChatModel.chat(anyString()))
            .thenReturn("@startuml\nFirst attempt")
            .thenReturn("@startuml\nSecond attempt")
            .thenReturn("@startuml\nThird attempt\n@enduml")

        `when`(mockPlantumlService.validateSyntax("@startuml\nFirst attempt"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Error 1", "stack1"))
        `when`(mockPlantumlService.validateSyntax("@startuml\nSecond attempt"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Error 2", "stack2"))
        `when`(mockPlantumlService.validateSyntax("@startuml\nThird attempt\n@enduml"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Valid)

        `when`(mockConfig.langchain4j).thenReturn(mockLangchainConfig)
        `when`(mockLangchainConfig.validationPrompt).thenReturn("Validate this")

        val processor = DiagramProcessor(mockPlantumlService, mockChatModel, mockConfig)
        val result = processor.processPrompt("Test with history context")

        // Verify : le ChatModel a été appelé 3 fois, incluant les correction prompts avec history
        verify(mockChatModel, times(3)).chat(anyString())
        
        assertNotNull(result)
        assertTrue(result.conversation.size >= 3)
    }

    @Test
    fun `should archive attempt history after successful correction`() {
        val mockPlantumlService = mock(PlantumlService::class.java)
        val mockChatModel = mock(ChatModel::class.java)
        val mockConfig = mock(PlantumlConfig::class.java)
        val mockLangchainConfig = mock(plantuml.LangchainConfig::class.java)

        // Setup : échec initial puis succès
        `when`(mockChatModel.chat(anyString()))
            .thenReturn("@startuml\nInitial invalid")
            .thenReturn("@startuml\nCorrected\n@enduml")

        `when`(mockPlantumlService.validateSyntax("@startuml\nInitial invalid"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Invalid("Initial error", "stack"))
        `when`(mockPlantumlService.validateSyntax("@startuml\nCorrected\n@enduml"))
            .thenReturn(PlantumlService.SyntaxValidationResult.Valid)

        `when`(mockConfig.langchain4j).thenReturn(mockLangchainConfig)
        `when`(mockLangchainConfig.validationPrompt).thenReturn("Validate this")

        val processor = DiagramProcessor(mockPlantumlService, mockChatModel, mockConfig)
        val result = processor.processPrompt("Test with archiving")

        assertNotNull(result)
        // L'historique devrait être archivé car il y a eu plusieurs tentatives
        assertTrue(result.conversation.size >= 2)
    }

    @Test
    fun `should handle validationPrompt from config in validateDiagram`() {
        val mockPlantumlService = mock(PlantumlService::class.java)
        val mockChatModel = mock(ChatModel::class.java)
        val mockConfig = mock(PlantumlConfig::class.java)
        val mockLangchainConfig = mock(plantuml.LangchainConfig::class.java)

        `when`(mockConfig.langchain4j).thenReturn(mockLangchainConfig)
        `when`(mockLangchainConfig.validationPrompt).thenReturn("Custom validation prompt")
        
        val processor = DiagramProcessor(mockPlantumlService, mockChatModel, mockConfig)
        
        val diagram = plantuml.PlantumlDiagram(
            conversation = listOf("Test"),
            plantuml = plantuml.PlantumlCode(
                code = "@startuml\nactor User\n@enduml",
                description = "Test"
            )
        )
        
        val result = processor.validateDiagram(diagram)
        
        assertNotNull(result)
    }
}
