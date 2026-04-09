package plantuml.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import plantuml.PlantumlConfig
import kotlin.test.assertNotNull

class LlmServiceTest {

    enum class LangchainModel {
        OLLAMA, OPENAI, GEMINI, MISTRAL, CLAUDE, HUGGINGFACE
    }

    private fun createConfigForModel(model: LangchainModel): PlantumlConfig {
        return when (model) {
            LangchainModel.OLLAMA -> PlantumlConfig()
            LangchainModel.OPENAI -> PlantumlConfig(
                langchain = plantuml.LangchainConfig(
                    model = "openai",
                    openai = plantuml.ApiKeyConfig("test-key")
                )
            )

            LangchainModel.GEMINI -> PlantumlConfig(
                langchain = plantuml.LangchainConfig(
                    model = "gemini",
                    gemini = plantuml.ApiKeyConfig("test-key")
                )
            )

            LangchainModel.MISTRAL -> PlantumlConfig(
                langchain = plantuml.LangchainConfig(
                    model = "mistral",
                    mistral = plantuml.ApiKeyConfig("test-key")
                )
            )

            LangchainModel.CLAUDE -> PlantumlConfig(
                langchain = plantuml.LangchainConfig(
                    model = "claude",
                    claude = plantuml.ApiKeyConfig("test-key")
                )
            )

            LangchainModel.HUGGINGFACE -> PlantumlConfig(
                langchain = plantuml.LangchainConfig(
                    model = "huggingface",
                    huggingface = plantuml.ApiKeyConfig("test-key")
                )
            )
        }
    }

    @ParameterizedTest
    @EnumSource(LangchainModel::class)
    fun `should create chat model for all supported providers`(model: LangchainModel) {
        // Given
        val config = createConfigForModel(model)
        val llmService = LlmService(config)

        // When
        val chatModel = llmService.createChatModel()

        // Then
        assertNotNull(chatModel)
    }

    @Test
    fun `createChatModel should throw NotImplementedError for groq`() {
        // Given
        val config = PlantumlConfig(
            langchain = plantuml.LangchainConfig(
                model = "groq"
            )
        )
        val llmService = LlmService(config)

        // When & Then
        try {
            llmService.createChatModel()
            throw AssertionError("Expected NotImplementedError was not thrown")
        } catch (e: NotImplementedError) {
            // Expected
        }
    }
}