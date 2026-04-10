package plantuml

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.test.assertTrue

class LlmConfigurationFunctionalTest {

    companion object {
        private lateinit var wireMockServer: WireMockServer
        private var wireMockPort: Int = 0

        @BeforeAll
        @JvmStatic
        fun setupAll() {
            wireMockServer = WireMockServer(
                WireMockConfiguration.options()
                    .dynamicPort()
                    .usingFilesUnderClasspath("wiremock")
            )
            wireMockServer.start()
            wireMockPort = wireMockServer.port()
            WireMock.configureFor("localhost", wireMockPort)
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            if (::wireMockServer.isInitialized) {
                wireMockServer.stop()
            }
        }
    }

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `should handle Ollama configuration correctly`() {
        setupTestProject()

        val useRealLlm = System.getProperty("test.use.real.llm", "false").toBoolean()
        val baseUrl = if (useRealLlm) "http://localhost:11434" else "http://localhost:$wireMockPort"

        File(testProjectDir, "plantuml-context.yml").writeText("""
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

        File(testProjectDir, "test-prompts").mkdirs()
        File(testProjectDir, "test-prompts/ollama.prompt").writeText("Create a simple class diagram")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts")
            .withPluginClasspath()
            .build()

        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("model") ||
                  result.output.contains("not found") ||
                  result.output.contains("Completed processing"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["gemini", "mistral", "openai", "claude", "huggingface", "groq"])
    fun `should handle API key provider configuration correctly`(provider: String) {
        setupTestProject()

        File(testProjectDir, "plantuml-context.yml").writeText("""
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "$provider"
              $provider:
                apiKey: "fake-$provider-key-for-testing"
        """.trimIndent())

        File(testProjectDir, "test-prompts").mkdirs()
        File(testProjectDir, "test-prompts/$provider.prompt").writeText("Create a simple class diagram")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts")
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication"))
    }

    @Test
    fun `should handle mixed provider configurations correctly`() {
        setupTestProject()

        File(testProjectDir, "plantuml-context.yml").writeText("""
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

        File(testProjectDir, "test-prompts").mkdirs()
        File(testProjectDir, "test-prompts/mixed.prompt").writeText("Create a simple class diagram")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts")
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(result.output.contains("Config loaded") ||
                  result.output.contains("model") ||
                  result.output.contains("API key") ||
                  result.output.contains("authentication"))
    }

    private fun setupTestProject() {
        File(testProjectDir, "settings.gradle.kts").writeText("""
            rootProject.name = "plantuml-llm-config-test"
        """.trimIndent())

        File(testProjectDir, "build.gradle.kts").writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }

            repositories {
                mavenCentral()
            }
        """.trimIndent())
    }
}
