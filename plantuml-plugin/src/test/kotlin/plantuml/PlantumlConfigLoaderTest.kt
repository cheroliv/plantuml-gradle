package plantuml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PlantumlConfigLoaderTest {

    companion object {
        private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    }

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `should load default configuration when no config file exists`() {
        // Given
        val configFile = File(tempDir, "nonexistent.yml")
        
        // When
        val config = PlantumlConfig()
        
        // Then
        assertDefaultConfigValues(config)
    }

    @Test
    fun `should load configuration from valid YAML file`() {
        // Given
        val configFile = File(tempDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "custom-prompts"
              defaultLang: "fr"
              
            output:
              images: "custom-images"
              format: "svg"
              theme: "blue"
              
            langchain:
              maxIterations: 3
              model: "gemini"
              validation: false
              
              gemini:
                apiKey: "custom-gemini-key"
                
            git:
              userName: "custom-user"
              userEmail: "custom@example.com"
              commitMessage: "custom commit message"
              watchedBranches: 
                - "feature"
                - "release"
        """.trimIndent())

        // When
        val config = mapper.readValue(configFile, PlantumlConfig::class.java)

        // Then
        assertNotNull(config)
        assertEquals("custom-prompts", config.input.prompts)
        assertEquals("fr", config.input.defaultLang)
        assertEquals("custom-images", config.output.images)
        assertEquals("svg", config.output.format)
        assertEquals("blue", config.output.theme)
        assertEquals(3, config.langchain.maxIterations)
        assertEquals("gemini", config.langchain.model)
        assertEquals(false, config.langchain.validation)
        assertEquals("custom-gemini-key", config.langchain.gemini.apiKey)
        assertEquals("custom-user", config.git.userName)
        assertEquals("custom@example.com", config.git.userEmail)
        assertEquals("custom commit message", config.git.commitMessage)
        assertEquals(listOf("feature", "release"), config.git.watchedBranches)
    }

    @Test
    fun `should handle all LLM provider configurations`() {
        // Given
        val configFile = File(tempDir, "plantuml-context.yml")
        configFile.writeText("""
            langchain:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11435"
                modelName: "llama3:8b"
              gemini:
                apiKey: "fake-gemini-key"
              mistral:
                apiKey: "fake-mistral-key"
              openai:
                apiKey: "fake-openai-key"
              claude:
                apiKey: "fake-claude-key"
              huggingface:
                apiKey: "fake-huggingface-key"
              groq:
                apiKey: "fake-groq-key"
        """.trimIndent())

        // When
        val config = mapper.readValue(configFile, PlantumlConfig::class.java)

        // Then
        assertEquals("ollama", config.langchain.model)
        assertEquals("http://localhost:11435", config.langchain.ollama.baseUrl)
        assertEquals("llama3:8b", config.langchain.ollama.modelName)
        assertEquals("fake-gemini-key", config.langchain.gemini.apiKey)
        assertEquals("fake-mistral-key", config.langchain.mistral.apiKey)
        assertEquals("fake-openai-key", config.langchain.openai.apiKey)
        assertEquals("fake-claude-key", config.langchain.claude.apiKey)
        assertEquals("fake-huggingface-key", config.langchain.huggingface.apiKey)
        assertEquals("fake-groq-key", config.langchain.groq.apiKey)
    }

    @Test
    fun `should use default values for missing configuration`() {
        // Given
        val configFile = File(tempDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "only-this-is-specified"
        """.trimIndent())

        // When
        val config = mapper.readValue(configFile, PlantumlConfig::class.java)

        // Then
        assertEquals("only-this-is-specified", config.input.prompts)
        // All other values should use defaults
        assertDefaultConfigValues(config, excludeInputPrompts = true)
    }

    private fun assertDefaultConfigValues(config: PlantumlConfig, excludeInputPrompts: Boolean = false) {
        if (!excludeInputPrompts) {
            assertEquals("prompts", config.input.prompts)
        }
        assertEquals("en", config.input.defaultLang)
        assertEquals("generated/images", config.output.images)
        assertEquals("png", config.output.format)
        assertEquals("default", config.output.theme)
        assertEquals(5, config.langchain.maxIterations)
        assertEquals("ollama", config.langchain.model)
        assertEquals(true, config.langchain.validation)
        assertEquals("", config.langchain.gemini.apiKey)
        assertEquals("", config.langchain.mistral.apiKey)
        assertEquals("", config.langchain.openai.apiKey)
        assertEquals("", config.langchain.claude.apiKey)
        assertEquals("", config.langchain.huggingface.apiKey)
        assertEquals("", config.langchain.groq.apiKey)
        assertEquals("github-actions[bot]", config.git.userName)
        assertEquals("github-actions[bot]@users.noreply.github.com", config.git.userEmail)
        assertEquals("chore: update PlantUML diagrams [skip ci]", config.git.commitMessage)
        assertEquals(listOf("main", "develop"), config.git.watchedBranches)
    }
}