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
    fun createChatModel(): ChatModel = when (config.langchain4j.model.lowercase()) {
        "ollama" -> createOllamaModel()
        "openai" -> createOpenAiModel()
        "gemini" -> createGeminiModel()
        "mistral" -> createMistralModel()
        "claude" -> createClaudeModel()
        "huggingface" -> createHuggingFaceModel()
        "groq" -> createGroqModel()
        else -> createOllamaModel() // Default to Ollama
    }

    private fun createOllamaModel(): ChatModel = OllamaChatModel.builder()
        .baseUrl(config.langchain4j.ollama.baseUrl)
        .modelName(config.langchain4j.ollama.modelName)
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    private fun createOpenAiModel(): ChatModel = OpenAiChatModel.builder()
        .apiKey(config.langchain4j.openai.apiKey)
        .modelName("gpt-4") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    private fun createGeminiModel(): ChatModel = GoogleAiGeminiChatModel.builder()
        .apiKey(config.langchain4j.gemini.apiKey)
        .modelName("gemini-pro") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    private fun createMistralModel(): ChatModel = MistralAiChatModel.builder()
        .apiKey(config.langchain4j.mistral.apiKey)
        .modelName("mistral-large-latest") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    private fun createClaudeModel(): ChatModel = AnthropicChatModel.builder()
        .apiKey(config.langchain4j.claude.apiKey)
        .modelName("claude-3-opus-20240229") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    private fun createHuggingFaceModel(): ChatModel {
        return OpenAiChatModel.builder()
            .apiKey(config.langchain4j.huggingface.apiKey)
            .baseUrl("https://api-inference.huggingface.co")
            .modelName("gpt2") // Default model, can be made configurable
            .temperature(0.7)
            .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
            .build()
    }

    private fun createGroqModel(): ChatModel = OpenAiChatModel.builder()
        .apiKey(config.langchain4j.groq.apiKey)
        .baseUrl("https://api.groq.com/openai/v1")
        .modelName("llama3-8b-8192") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()
    
    private fun getTimeoutInSeconds(): Long {
        // In test environments, use shorter timeouts to avoid hanging tests
        return if (System.getenv("TEST_ENV") == "true") 5L else 60L
    }
}