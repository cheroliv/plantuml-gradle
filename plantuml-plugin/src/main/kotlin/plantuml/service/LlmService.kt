package plantuml.service

import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.mistralai.MistralAiChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import plantuml.PlantumlConfig
import java.time.Duration

/**
 * Service responsible for managing LLM models through LangChain4j.
 */
class LlmService(private val config: PlantumlConfig) {

    /**
     * Creates and returns the appropriate ChatModel based on configuration.
     */
    fun createChatModel(): ChatModel {
        return when (config.langchain.model.lowercase()) {
            "ollama" -> createOllamaModel()
            "openai" -> createOpenAiModel()
            "gemini" -> createGeminiModel()
            "mistral" -> createMistralModel()
            "claude" -> createClaudeModel()
            "huggingface" -> createHuggingFaceModel()
            "groq" -> TODO("Groq model implementation")
            else -> createOllamaModel() // Default to Ollama
        }
    }

    private fun createOllamaModel(): ChatModel {
        return OllamaChatModel.builder()
            .baseUrl(config.langchain.ollama.baseUrl)
            .modelName(config.langchain.ollama.modelName)
            .temperature(0.7)
            .timeout(Duration.ofSeconds(60))
            .build()
    }

    private fun createOpenAiModel(): ChatModel {
        return OpenAiChatModel.builder()
            .apiKey(config.langchain.openai.apiKey)
            .modelName("gpt-4") // Default model, can be made configurable
            .temperature(0.7)
            .timeout(Duration.ofSeconds(60))
            .build()
    }

    private fun createGeminiModel(): ChatModel {
        return GoogleAiGeminiChatModel.builder()
            .apiKey(config.langchain.gemini.apiKey)
            .modelName("gemini-pro") // Default model, can be made configurable
            .temperature(0.7)
            .timeout(Duration.ofSeconds(60))
            .build()
    }

    private fun createMistralModel(): ChatModel {
        return MistralAiChatModel.builder()
            .apiKey(config.langchain.mistral.apiKey)
            .modelName("mistral-large-latest") // Default model, can be made configurable
            .temperature(0.7)
            .timeout(Duration.ofSeconds(60))
            .build()
    }

    private fun createClaudeModel(): ChatModel {
        return AnthropicChatModel.builder()
            .apiKey(config.langchain.claude.apiKey)
            .modelName("claude-3-opus-20240229") // Default model, can be made configurable
            .temperature(0.7)
            .timeout(Duration.ofSeconds(60))
            .build()
    }

    private fun createHuggingFaceModel(): ChatModel {
        return OpenAiChatModel.builder()
            .apiKey(config.langchain.huggingface.apiKey)
            .baseUrl("https://api-inference.huggingface.co")
            .modelName("gpt2") // Default model, can be made configurable
            .temperature(0.7)
            .timeout(Duration.ofSeconds(60))
            .build()
    }
}