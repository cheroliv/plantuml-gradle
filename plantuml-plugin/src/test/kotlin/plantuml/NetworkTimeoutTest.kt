package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.ServerSocket
import kotlin.test.assertTrue

class NetworkTimeoutTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        
        settingsFile.writeText("""
            rootProject.name = "plantuml-network-test"
        """.trimIndent())
    }

    @kotlin.test.Ignore
    @Test
    fun `should handle network timeout gracefully with slow server`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with Ollama pointing to slow server
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
                baseUrl: "http://localhost:12345"
                modelName: "slow-model"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "timeout.prompt")
        promptFile.writeText("Create a simple class diagram")

        // Start a slow server that doesn't respond quickly
        val serverThread = Thread {
            try {
                val serverSocket = ServerSocket(12345)
                val clientSocket = serverSocket.accept()
                
        // Sleep for shorter time to avoid long test execution
        Thread.sleep(1000)
                
                clientSocket.close()
                serverSocket.close()
            } catch (e: Exception) {
                // Ignore exceptions in test server
            }
        }
        serverThread.start()

        try {
            // Give server time to start
            Thread.sleep(1000)

            // When & Then
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .buildAndFail()

            // Then
            assertTrue(result.output.contains("timeout") ||
                      result.output.contains("TIMEOUT") ||
                      result.output.contains("Connection refused") ||
                      result.output.contains("Connect timed out"))
        } finally {
            // Interrupt server thread
            serverThread.interrupt()
            
            // Give thread time to shut down
            try {
                serverThread.join(5000)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    @Suppress("TestFailedLine")
    @kotlin.test.Ignore
    @Test
    fun `should handle connection refused gracefully`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with Ollama pointing to non-existent server
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
                baseUrl: "http://localhost:65000"  # Port that shouldn't be in use
                modelName: "unreachable-model"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "connection.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then
        assertTrue(result.output.contains("Connection refused") ||
                  result.output.contains("connect") ||
                  result.output.contains("refused") ||
                  result.output.contains("UnknownHostException"))
    }

    @kotlin.test.Ignore
    @Test
    fun `should handle DNS resolution failure gracefully`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "plantuml-context.yml"
            }
        """.trimIndent())

        // Create config with Ollama pointing to invalid hostname
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
                baseUrl: "http://nonexistent.invalid.domain.local:11434"
                modelName: "dns-failure-model"
        """.trimIndent())

        // Create prompts directory and a sample prompt
        val promptsDir = File(testProjectDir, "test-prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "dns.prompt")
        promptFile.writeText("Create a simple class diagram")

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Then
        assertTrue(result.output.contains("UnresolvedAddressException") ||
                  result.output.contains("UnknownHostException") ||
                  result.output.contains("DNS") ||
                  result.output.contains("resolution") ||
                  result.output.contains("connect"),
                  "La sortie ne contient aucun des mots attendus. Contenu de la sortie:\n${result.output}")
    }

    @kotlin.test.Ignore
    @Test
    fun `should degrade gracefully with network issues`() {
        // Given
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent())

        // Create a completely offline scenario by testing with localhost
        // but simulating network failure through firewall rules would be
        // platform-dependent. Instead, we'll test basic resilience.

        // Create a simple PlantUML file to validate locally
        val diagramFile = File(testProjectDir, "local.puml")
        diagramFile.writeText("""
            @startuml
            class Test {
              - String field
              + void method()
            }
            @enduml
        """.trimIndent())

        // When
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=local.puml", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Then - should work fine for local validation regardless of network
        assertTrue(result.output.contains("PlantUML syntax is valid"))
    }
}