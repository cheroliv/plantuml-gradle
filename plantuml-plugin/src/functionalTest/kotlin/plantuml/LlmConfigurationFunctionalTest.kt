package plantuml

// WireMock for mocking LLM calls
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertTrue

//slow test 37sec
class LlmConfigurationFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File
    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setup() {
        // Start WireMock server for mocking LLM calls with dynamic port and load mappings from classpath
        wireMockServer = WireMockServer(
            WireMockConfiguration.options()
                .dynamicPort()
                .usingFilesUnderClasspath("wiremock")
        )
        wireMockServer.start()
        WireMock.configureFor("localhost", wireMockServer.port())
        
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-llm-config-test"
        """.trimIndent())
    }

    @AfterEach
    fun tearDown() {
        // Stop WireMock server after tests
        if (::wireMockServer.isInitialized) {
            wireMockServer.stop()
        }
    }

    @Ignore
    @Test
    fun `should handle Ollama configuration correctly`() {
        // Check if we should use real LLM or mock
        val useRealLlm = System.getProperty("test.use.real.llm", "false").toBoolean()
        val baseUrl = if (useRealLlm) "http://localhost:11434" else "http://localhost:${wireMockServer.port()}"
        
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with Ollama configuration
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "$baseUrl"
                modelName: "smollm:135m"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "ollama.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then - Should not crash with Ollama configuration (model might not be found, but plugin should load config)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("model") ||
                  result.output.contains("not found") ||
                  result.output.contains("Completed processing"))
    }

    @Test
    fun `should handle Gemini configuration correctly`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with Gemini configuration
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "gemini"
              gemini:
                apiKey: "fake-gemini-key-for-testing"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "gemini.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then - Should not crash with Gemini configuration (API key is fake, but plugin should load config)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication"))
    }

    @Test
    fun `should handle Mistral configuration correctly`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with Mistral configuration
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "mistral"
              mistral:
                apiKey: "fake-mistral-key-for-testing"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "mistral.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then - Should not crash with Mistral configuration (API key is fake, but plugin should load config)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication"))
    }

    @Test
    fun `should handle OpenAI configuration correctly`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with OpenAI configuration
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "openai"
              openai:
                apiKey: "fake-openai-key-for-testing"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "openai.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then - Should not crash with OpenAI configuration (API key is fake, but plugin should load config)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication"))
    }

    @Test
    fun `should handle Claude configuration correctly`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with Claude configuration
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "claude"
              claude:
                apiKey: "fake-claude-key-for-testing"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "claude.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then - Should not crash with Claude configuration (API key is fake, but plugin should load config)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication"))
    }

    @Test
    fun `should handle HuggingFace configuration correctly`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with HuggingFace configuration
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "huggingface"
              huggingface:
                apiKey: "fake-huggingface-key-for-testing"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "huggingface.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then - Should not crash with HuggingFace configuration (API key is fake, but plugin should load config)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication") ||
                  result.output.contains("router.huggingface.co"))
    }

    @Test
    fun `should handle Groq configuration correctly`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with Groq configuration
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "groq"
              groq:
                apiKey: "fake-groq-key-for-testing"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "groq.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then - Should not crash with Groq configuration (API key is fake, but plugin should load config)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication"))
    }

    @Test
    fun `should handle mixed provider configurations correctly`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with all providers configured
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
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

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "mixed.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then - Should not crash with mixed configuration (some configs may fail, but plugin should load)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("model") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication"))
    }
}