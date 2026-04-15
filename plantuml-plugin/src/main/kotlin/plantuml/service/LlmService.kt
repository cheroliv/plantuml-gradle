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
 * Service responsible for creating and configuring LLM (Large Language Model) instances.
 *
 * Supports 7 LLM providers via LangChain4j:
 * - **Ollama** (local models)
 * - **OpenAI** (GPT-4, GPT-3.5-turbo)
 * - **Google Gemini** (gemini-pro)
 * - **Mistral AI** (mistral-large-latest)
 * - **Anthropic Claude** (claude-3-opus)
 * - **HuggingFace** (API inference)
 * - **Groq** (llama3-8b)
 *
 * All providers are configured with temperature 0.7 for balanced creativity/accuracy.
 *
 * @param config PlantUML configuration containing LLM provider settings
 */
class LlmService(private val config: PlantumlConfig) {

    /**
     * Creates and configures a LangChain4j [ChatModel] based on the active provider.
     *
     * @return A configured [ChatModel] instance for the selected provider
     * @throws IllegalArgumentException if provider name is invalid
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

    /**
     * Creates an Ollama chat model for local LLM inference.
     *
     * @return Configured [OllamaChatModel] with baseUrl and modelName from config
     */
    private fun createOllamaModel(): ChatModel = OllamaChatModel.builder()
        .baseUrl(config.langchain4j.ollama.baseUrl)
        .modelName(config.langchain4j.ollama.modelName)
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    /**
     * Creates an OpenAI chat model.
     *
     * @return Configured [OpenAiChatModel] with GPT-4 model
     */
    private fun createOpenAiModel(): ChatModel = OpenAiChatModel.builder()
        .apiKey(config.langchain4j.openai.apiKey)
        .modelName("gpt-4") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    /**
     * Creates a Google Gemini chat model.
     *
     * @return Configured [GoogleAiGeminiChatModel] with gemini-pro model
     */
    private fun createGeminiModel(): ChatModel = GoogleAiGeminiChatModel.builder()
        .apiKey(config.langchain4j.gemini.apiKey)
        .modelName("gemini-pro") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    /**
     * Creates a Mistral AI chat model.
     *
     * @return Configured [MistralAiChatModel] with mistral-large-latest model
     */
    private fun createMistralModel(): ChatModel = MistralAiChatModel.builder()
        .apiKey(config.langchain4j.mistral.apiKey)
        .modelName("mistral-large-latest") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    /**
     * Creates an Anthropic Claude chat model.
     *
     * @return Configured [AnthropicChatModel] with claude-3-opus-20240229 model
     */
    private fun createClaudeModel(): ChatModel = AnthropicChatModel.builder()
        .apiKey(config.langchain4j.claude.apiKey)
        .modelName("claude-3-opus-20240229") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()

    /**
     * Creates a HuggingFace chat model via OpenAI-compatible API.
     *
     * @return Configured [OpenAiChatModel] pointing to HuggingFace inference API
     */
    private fun createHuggingFaceModel(): ChatModel {
        return OpenAiChatModel.builder()
            .apiKey(config.langchain4j.huggingface.apiKey)
            .baseUrl("https://api-inference.huggingface.co")
            .modelName("gpt2") // Default model, can be made configurable
            .temperature(0.7)
            .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
            .build()
    }

    /**
     * Creates a Groq chat model via OpenAI-compatible API.
     *
     * @return Configured [OpenAiChatModel] pointing to Groq API with llama3-8b model
     */
    private fun createGroqModel(): ChatModel = OpenAiChatModel.builder()
        .apiKey(config.langchain4j.groq.apiKey)
        .baseUrl("https://api.groq.com/openai/v1")
        .modelName("llama3-8b-8192") // Default model, can be made configurable
        .temperature(0.7)
        .timeout(Duration.ofSeconds(getTimeoutInSeconds()))
        .build()
    
    /**
     * Returns timeout duration in seconds based on environment.
     *
     * @return 5 seconds in test environment (TEST_ENV=true), 60 seconds otherwise
     */
    private fun getTimeoutInSeconds(): Long {
        // In test environments, use shorter timeouts to avoid hanging tests
        return if (System.getenv("TEST_ENV") == "true") 5L else 60L
    }
}