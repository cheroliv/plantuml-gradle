package plantuml.service

import org.junit.jupiter.api.Test
import plantuml.PlantumlConfig
import kotlin.test.assertNotNull

class LlmServiceTest {

    @Test
    fun `should create Ollama chat model`() {
        // Given
        val config = PlantumlConfig()
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
    }

    @Test
    fun `should create OpenAI chat model when configured`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "openai",
                openai = plantuml.ApiKeyConfig("test-key")
            )
        )
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
    }

    @Test
    fun `should create Gemini chat model when configured`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "gemini",
                gemini = plantuml.ApiKeyConfig("test-key")
            )
        )
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
    }

    @Test
    fun `should create Mistral chat model when configured`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "mistral",
                mistral = plantuml.ApiKeyConfig("test-key")
            )
        )
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
    }

    @Test
    fun `should create Claude chat model when configured`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "claude",
                claude = plantuml.ApiKeyConfig("test-key")
            )
        )
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
    }

    @Test
    fun `should create HuggingFace chat model when configured`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "huggingface",
                huggingface = plantuml.ApiKeyConfig("test-key")
            )
        )
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
    }
}