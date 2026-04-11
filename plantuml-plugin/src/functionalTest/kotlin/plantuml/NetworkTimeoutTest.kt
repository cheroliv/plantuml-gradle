package plantuml

import org.gradle.testkit.runner.GradleRunner.create
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.ServerSocket
import kotlin.test.assertTrue

@Suppress("FunctionName")
class NetworkTimeoutTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `should handle network timeout gracefully with slow server`() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "plantuml-network-test" """)
        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.cheroliv.plantuml") }
            plantuml { configPath = "plantuml-context.yml" }
            """.trimIndent()
        )

        File(testProjectDir, "plantuml-context.yml").writeText(
            """
            input: { prompts: "test-prompts" }
            output: { images: "test-images", rag: "test-rag" }
            langchain4j: { model: "ollama", ollama: { baseUrl: "http://localhost:12345", modelName: "slow-model" } }
            """.trimIndent()
        )

        File(testProjectDir, "test-prompts/timeout.prompt").apply {
            parentFile.mkdirs()
            writeText("Create a simple class diagram")
        }

        val serverThread = Thread {
            try {
                ServerSocket(12345).use { server ->
                    server.accept().use { client ->
                        Thread.sleep(100)
                    }
                }
            } catch (_: Exception) { }
        }
        serverThread.start()

        try {
            Thread.sleep(100)
            val result = create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts")
                .withPluginClasspath()
                .buildAndFail()

            assertTrue(
                result.output.contains("timeout") ||
                        result.output.contains("TIMEOUT") ||
                        result.output.contains("Connection refused") ||
                        result.output.contains("Connect timed out") ||
                        result.output.contains("Read timed out")
            )
        } finally {
            serverThread.interrupt()
            try { serverThread.join(500) } catch (_: Exception) { }
        }
    }

    @Test
    fun `should handle connection refused gracefully`() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "plantuml-network-test" """)
        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.cheroliv.plantuml") }
            plantuml { configPath = "plantuml-context.yml" }
            """.trimIndent()
        )

        File(testProjectDir, "plantuml-context.yml").writeText(
            """
            input: { prompts: "test-prompts" }
            output: { images: "test-images", rag: "test-rag" }
            langchain4j: { model: "ollama", ollama: { baseUrl: "http://localhost:65000", modelName: "unreachable-model" } }
            """.trimIndent()
        )

        File(testProjectDir, "test-prompts/connection.prompt").apply {
            parentFile.mkdirs()
            writeText("Create a simple class diagram")
        }

        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts")
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(
            result.output.contains("Connection refused") ||
                    result.output.contains("connect") ||
                    result.output.contains("refused") ||
                    result.output.contains("UnknownHostException") ||
                    result.output.contains("Failed to connect")
        )
    }

    @Test
    fun `should handle DNS resolution failure gracefully`() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "plantuml-network-test" """)
        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.cheroliv.plantuml") }
            plantuml { configPath = "plantuml-context.yml" }
            """.trimIndent()
        )

        File(testProjectDir, "plantuml-context.yml").writeText(
            """
            input: { prompts: "test-prompts" }
            output: { images: "test-images", rag: "test-rag" }
            langchain4j: { model: "ollama", ollama: { baseUrl: "http://nonexistent.invalid.domain.local:11434", modelName: "dns-failure-model" } }
            """.trimIndent()
        )

        File(testProjectDir, "test-prompts/dns.prompt").apply {
            parentFile.mkdirs()
            writeText("Create a simple class diagram")
        }

        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(
            result.output.contains("UnresolvedAddressException") ||
                    result.output.contains("UnknownHostException") ||
                    result.output.contains("DNS") ||
                    result.output.contains("resolution") ||
                    result.output.contains("connect") ||
                    result.output.contains("nonexistent") ||
                    result.output.contains("Failed to connect") ||
                    result.output.contains("java.net"),
            "La sortie ne contient aucun des mots attendus. Contenu de la sortie:\n${result.output}"
        )
    }

    @Test
    fun `should degrade gracefully with network issues`() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "plantuml-network-test" """)
        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.cheroliv.plantuml") }
            """.trimIndent()
        )

        File(testProjectDir, "local.puml").writeText(
            """
            @startuml
            class Test { - String field; + void method() }
            @enduml
            """.trimIndent()
        )

        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=local.puml")
            .withPluginClasspath()
            .build()

        assertTrue(result.output.contains("PlantUML syntax is valid"))
    }
}
