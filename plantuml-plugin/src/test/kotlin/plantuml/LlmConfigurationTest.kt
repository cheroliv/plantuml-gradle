package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class LlmConfigurationTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-llm-config-test"
        """.trimIndent())
    }

    @kotlin.test.Ignore
    @Test
    fun `should handle Ollama configuration correctly`() {
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
            langchain:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "llama3"
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
            .buildAndFail()

        // Then - Should not crash with Ollama configuration (model might not be found, but plugin should load config)
        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("model") ||
                  result.output.contains("not found"))
    }

    @kotlin.test.Ignore
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
            langchain:
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

    @kotlin.test.Ignore
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
            langchain:
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

    @kotlin.test.Ignore
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
            langchain:
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

    @kotlin.test.Ignore
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
            langchain:
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

    @kotlin.test.Ignore
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
            langchain:
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

    @kotlin.test.Ignore
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
            langchain:
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
            langchain:
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