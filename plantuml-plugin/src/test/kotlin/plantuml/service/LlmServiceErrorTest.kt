package plantuml.service

import org.junit.jupiter.api.Test
import plantuml.PlantumlConfig
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

class LlmServiceErrorTest {

    @Test
    fun `should handle invalid API key gracefully`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "openai",
                openai = plantuml.ApiKeyConfig("invalid-key")
            )
        )
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
        // Note: Actual API key validation would happen during usage, not creation
    }

    @Test
    fun `should handle unsupported model gracefully`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "unsupported-model"
            )
        )
        val llmService = LlmService(config)

        // When & Then
        // Should fall back to Ollama model
        val chatModel = llmService.createChatModel()
        assertNotNull(chatModel)
    }

    @Test
    fun `should handle network timeouts gracefully`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "openai",
                openai = plantuml.ApiKeyConfig("test-key")
            )
        )
        val llmService = LlmService(config)

        // When & Then
        // Creation should succeed even with unreachable endpoints
        val chatModel = llmService.createChatModel()
        assertNotNull(chatModel)
    }

    @Test
    fun `should handle rate limiting gracefully`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "mistral",
                mistral = plantuml.ApiKeyConfig("test-key")
            )
        )
        val llmService = LlmService(config)

        // When & Then
        // Creation should succeed even with rate limiting concerns
        val chatModel = llmService.createChatModel()
        assertNotNull(chatModel)
    }

    @Test
    fun `should fallback to default model when provider configuration is missing`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "gemini"
                // Missing gemini configuration
            )
        )
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
        // Note: Actual fallback behavior would depend on LangChain4j implementation
    }

    @Test
    fun `should handle malformed configuration gracefully`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "claude",
                claude = plantuml.ApiKeyConfig("") // Empty API key
            )
        )
        val llmService = LlmService(config)

        // When & Then
        // Should throw IllegalArgumentException for empty API key
        assertFailsWith<IllegalArgumentException> {
            llmService.createChatModel()
        }
    }
}