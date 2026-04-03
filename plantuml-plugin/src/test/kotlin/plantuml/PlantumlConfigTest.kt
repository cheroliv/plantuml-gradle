package plantuml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PlantumlConfigTest {

    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `should load configuration from YAML file`() {
        // Given
        val configFile = File(tempDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "custom-prompts"
              defaultLang: "fr"
              
            output:
              diagrams: "custom-diagrams"
              images: "custom-images"
              validations: "custom-validations"
              rag: "custom-rag"
              format: "svg"
              theme: "blue"
              
            langchain:
              maxIterations: 3
              model: "gemini"
              validation: false
              validationPrompt: "Custom validation prompt"
              
              ollama:
                baseUrl: "http://custom-host:12345"
                modelName: "custom-model"
                
              gemini:
                apiKey: "custom-gemini-key"
                
              mistral:
                apiKey: "custom-mistral-key"
                
              openai:
                apiKey: "custom-openai-key"
                
              claude:
                apiKey: "custom-claude-key"
                
              huggingface:
                apiKey: "custom-huggingface-key"
                
              groq:
                apiKey: "custom-groq-key"
                
            git:
              userName: "custom-user"
              userEmail: "custom@example.com"
              commitMessage: "custom commit message"
              watchedBranches: 
                - "feature"
                - "release"
                
            rag:
              databaseUrl: "jdbc:postgresql://localhost:5432/custom_db"
              username: "custom_user"
              password: "custom_password"
              tableName: "custom_table"
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
        assertEquals("custom-mistral-key", config.langchain.mistral.apiKey)
        assertEquals("custom-openai-key", config.langchain.openai.apiKey)
        assertEquals("custom-claude-key", config.langchain.claude.apiKey)
        assertEquals("custom-huggingface-key", config.langchain.huggingface.apiKey)
        assertEquals("custom-groq-key", config.langchain.groq.apiKey)
        assertEquals("custom-user", config.git.userName)
        assertEquals("custom@example.com", config.git.userEmail)
        assertEquals("custom commit message", config.git.commitMessage)
        assertEquals(listOf("feature", "release"), config.git.watchedBranches)
        assertEquals("jdbc:postgresql://localhost:5432/custom_db", config.rag.databaseUrl)
        assertEquals("custom_user", config.rag.username)
        assertEquals("custom_password", config.rag.password)
        assertEquals("custom_table", config.rag.tableName)
    }

    @Test
    fun `should use default values when config file is missing`() {
        // Given
        val configFile = File(tempDir, "non-existent.yml")

        // When
        val config = PlantumlConfig()

        // Then
        assertNotNull(config)
        assertEquals("prompts", config.input.prompts)
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
        assertEquals("", config.rag.databaseUrl)
        assertEquals("", config.rag.username)
        assertEquals("", config.rag.password)
        assertEquals("embeddings", config.rag.tableName)
    }

    @Test
    fun `should use default values when config file is empty`() {
        // Given
        val configFile = File(tempDir, "empty.yml")
        configFile.writeText("")

        // When
        val config = PlantumlConfig()

        // Then
        assertNotNull(config)
        assertEquals("prompts", config.input.prompts)
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
        assertEquals("", config.rag.databaseUrl)
        assertEquals("", config.rag.username)
        assertEquals("", config.rag.password)
        assertEquals("embeddings", config.rag.tableName)
    }
}