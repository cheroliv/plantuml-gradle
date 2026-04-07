package plantuml.scenarios

import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpServer.create
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.io.File
import java.io.File.createTempFile
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URI

class PlantumlWorld {
    val log: Logger = getLogger(PlantumlWorld::class.java)

    val scope = CoroutineScope(Default + SupervisorJob())

    var projectDir: File? = null
    var buildResult: BuildResult? = null
    var exception: Throwable? = null

    private val asyncJobs = mutableListOf<Deferred<BuildResult>>()

    companion object {
        private var templateProjectDir: File? = null

        @BeforeAll
        @JvmStatic
        fun createTemplateProject() {
            if (templateProjectDir == null) {
                templateProjectDir = createBaseTemplateProject()
            }
        }

        private fun createBaseTemplateProject(): File {
            val templateDir =
                File(System.getProperty("java.io.tmpdir"), "plantuml-test-template-${System.currentTimeMillis()}")
            templateDir.mkdirs()

            File(templateDir, "settings.gradle.kts").writeText(
                "pluginManagement.repositories.gradlePluginPortal()\n" + "rootProject.name = \"plantuml-test-template\""
            )

            File(templateDir, "build.gradle.kts").writeText(
                """
                plugins {
                    id("com.cheroliv.plantuml")
                }
                
                plantuml {
                    configPath = "plantuml-context.yml"
                }
                """.trimIndent()
            )

            File(templateDir, "plantuml-context.yml").writeText(
                """
                input:
                  prompts: "test-prompts"
                output:
                  images: "generated/images"
                  rag: "generated/rag"
                  diagrams: "generated/diagrams"
                  validations: "generated/validations"
                langchain:
                  model: "ollama"
                  ollama:
                    baseUrl: "http://localhost:11434"
                    modelName: "smollm:135m"
                  maxIterations: 1
                """.trimIndent()
            )

            val promptsDir = File(templateDir, "test-prompts")
            promptsDir.mkdirs()
            File(promptsDir, "test.prompt").writeText("Create a simple class diagram")

            templateDir.deleteOnExit()
            return templateDir
        }
    }

    /** Base URL of the Ollama instance available for this scenario. */
    var ollamaBaseUrl: String? = null
        private set

    /**
     * Base URL of the mock LLM server.
     */
    var mockServerPort: Int? = null
        private set

    private var mockServer: HttpServer? = null

    /**
     * Ensures an Ollama instance is available:
     * - If Ollama is running locally on port 11434, uses it directly.
     * - Otherwise starts a Testcontainers Ollama container.
     */
    fun ensureOllama() {
        if (isOllamaLocal()) {
            ollamaBaseUrl = "http://localhost:11434"
            log.info("Ollama detected locally at $ollamaBaseUrl")
        }
    }

    private fun isOllamaLocal(): Boolean = runCatching {
        val conn = URI("http://localhost:11434/api/tags").toURL().openConnection() as HttpURLConnection
        conn.connectTimeout = 1_000
        conn.readTimeout = 1_000
        conn.requestMethod = "GET"
        conn.responseCode == 200
    }.getOrDefault(false)

    /**
     * Starts a minimal Ollama-compatible HTTP server that always returns
     * [responseBody] as the assistant message content.
     */
    fun startMockLlm(responseBody: String) {
        val server = create(InetSocketAddress(0), 0)
        val port = server.address.port
        mockServerPort = port

        val ollamaResponse = """
            {
              "model": "smollm:135m",
              "message": { "role": "assistant", "content": ${escapeJson(responseBody)} },
              "done": true
            }
        """.trimIndent().toByteArray()

        server.createContext("/api/chat") { exchange ->
            exchange.sendResponseHeaders(200, ollamaResponse.size.toLong())
            exchange.responseBody.use { it.write(ollamaResponse) }
        }
        server.executor = null
        server.start()
        mockServer = server
        log.info("Mock LLM server started on port $port")
    }

    private fun escapeJson(value: String): String =
        "\"${value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""

    fun stopMockLlm() {
        mockServer?.stop(0)
        mockServer = null
        mockServerPort = null
    }

    /**
     * Executes a Gradle task asynchronously with optional Gradle properties.
     */
    fun executeGradleAsync(
        vararg tasks: String,
        properties: Map<String, String> = emptyMap(),
    ): Deferred<BuildResult> {
        require(projectDir != null) { "Project directory must be initialized" }
        val propArgs = properties.map { (k, v) -> "-P$k=$v" }
        val allArgs = tasks.toList() + propArgs + "--stacktrace"
        log.info("Starting async Gradle execution: $allArgs")
        return scope.async {
            try {
                GradleRunner.create().withProjectDir(projectDir!!).withArguments(allArgs).withPluginClasspath().build()
            } catch (e: Exception) {
                log.error("Gradle build failed", e)
                exception = e
                throw e
            }
        }.also { asyncJobs.add(it) }
    }

    suspend fun executeGradle(
        vararg tasks: String,
        properties: Map<String, String> = emptyMap(),
    ): BuildResult = executeGradleAsync(*tasks, properties = properties).await().also { buildResult = it }

    suspend fun awaitAll() {
        if (asyncJobs.isNotEmpty()) {
            log.info("Waiting for ${asyncJobs.size} async operations...")
            asyncJobs.awaitAll()
            log.info("All async operations completed")
        }
    }

    /**
     * Creates a temporary Gradle project for testing by copying from template.
     * This is faster than creating from scratch.
     */
    fun createGradleProject(configFileName: String = "plantuml-context.yml"): File {
        val templateDir = templateProjectDir ?: createBaseTemplateProject().also { templateProjectDir = it }

        val testDir = createTempFile("gradle-test-", "").apply {
            delete()
            mkdirs()
        }

        templateDir.copyRecursively(testDir, overwrite = true)

        if (configFileName != "plantuml-context.yml") {
            val configFile = File(testDir, configFileName)
            if (!configFile.exists()) {
                configFile.createNewFile()
            }
        }

        projectDir = testDir
        return testDir
    }

    @Suppress("unused")
    fun cleanup() {
        stopMockLlm()
        scope.cancel()
        projectDir?.deleteRecursively()
        projectDir = null
        buildResult = null
        exception = null
        asyncJobs.clear()
    }
}