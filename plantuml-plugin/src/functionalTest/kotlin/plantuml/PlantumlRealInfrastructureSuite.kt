package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.assertTrue

/**
 * Tests d'infrastructure réelle.
 *
 * Ces tests sont exclus du cycle de build normal via le tag "real-llm".
 * Ils s'exécutent uniquement en CI sur un runner équipé d'Ollama, ou
 * en local avec : ./gradlew test -Ptest.tags="real-llm"
 *
 * Dans build.gradle.kts :
 *
 *   tasks.test {
 *       val realLlm = project.findProperty("test.tags")?.toString()?.contains("real-llm") ?: false
 *       if (!realLlm) {
 *           useJUnitPlatform { excludeTags("real-llm") }
 *       } else {
 *           useJUnitPlatform()
 *       }
 *   }
 *
 * Architecture : une classe parente avec @BeforeAll qui vérifie la disponibilité
 * d'Ollama. Si Ollama est absent, tous les tests sont ignorés (assumeTrue).
 * Les nested classes se répartissent les scénarios sans démarrer de nouveau
 * processus pour chaque test.
 */
@Tag("real-llm")
@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class PlantumlRealInfrastructureSuite {

    companion object {
        @TempDir
        @JvmStatic
        lateinit var sharedProjectDir: File

        private const val OLLAMA_URL = "http://localhost:11434"
        private const val OLLAMA_MODEL = "smollm:135m"

        private var ollamaAvailable = false

        @BeforeAll
        @JvmStatic
        fun checkOllamaAvailability() {
            ollamaAvailable = try {
                val conn = URL("$OLLAMA_URL/api/tags").openConnection() as HttpURLConnection
                conn.connectTimeout = 2000
                conn.readTimeout = 2000
                conn.responseCode == 200
            } catch (e: Exception) {
                false
            }

            // Prépare le projet partagé même si Ollama est absent
            // (les tests individuels feront assumeTrue)
            setupSharedProject()
        }

        private fun setupSharedProject() {
            File(sharedProjectDir, "settings.gradle.kts")
                .writeText("""rootProject.name = "plantuml-real-infra"""")

            File(sharedProjectDir, "build.gradle.kts").writeText(
                """
                plugins { id("com.cheroliv.plantuml") }
                plantuml { configPath = "plantuml-context.yml" }
                """.trimIndent(),
            )

            File(sharedProjectDir, "plantuml-context.yml").writeText(
                """
                langchain:
                  model: "ollama"
                  ollama:
                    baseUrl: "$OLLAMA_URL"
                    modelName: "$OLLAMA_MODEL"
                  validation: false
                  maxIterations: 1
                input:
                  prompts: "prompts"
                output:
                  diagrams: "generated/diagrams"
                  images:   "generated/images"
                  rag:      "generated/rag"
                """.trimIndent(),
            )

            File(sharedProjectDir, "prompts").mkdirs()
            File(sharedProjectDir, "prompts/simple.prompt")
                .writeText("Create a minimal class diagram with one class named Foo")
        }

        fun runner(vararg args: String) =
            GradleRunner.create()
                .withProjectDir(sharedProjectDir)
                .withArguments(*args, "--stacktrace")
                .withPluginClasspath()
    }

    // ------------------------------------------------------------------ //
    //  Nested 1 : handshake LLM                                           //
    // ------------------------------------------------------------------ //

    @Nested
    @Order(1)
    @DisplayName("LLM handshake")
    inner class LlmHandshake {

        @BeforeEach
        fun requireOllama() {
            assumeTrue(ollamaAvailable, "Ollama not available at $OLLAMA_URL — skipping real-llm tests")
        }

        @Test
        fun `should reach ollama api tags endpoint`() {
            val conn = URL("$OLLAMA_URL/api/tags").openConnection() as HttpURLConnection
            assertTrue(conn.responseCode == 200)
        }

        @Test
        fun `should create chat model without exception`() {
            val config = PlantumlConfig(
                langchain = LangchainConfig(
                    model = "ollama",
                    ollama = OllamaConfig(OLLAMA_URL, OLLAMA_MODEL),
                ),
            )
            // Doit juste ne pas lever d'exception — pas besoin de Gradle
            LlmService(config).createChatModel()
        }
    }

    // ------------------------------------------------------------------ //
    //  Nested 2 : traitement réel d'un prompt                             //
    // ------------------------------------------------------------------ //

    @Nested
    @Order(2)
    @DisplayName("Real prompt processing")
    inner class RealPromptProcessing {

        @BeforeEach
        fun requireOllama() {
            assumeTrue(ollamaAvailable, "Ollama not available — skipping")
        }

        @Test
        fun `should generate a valid puml file from a real prompt`() {
            val result = runner("processPlantumlPrompts").build()

            val diagramsDir = File(sharedProjectDir, "generated/diagrams")
            val generated = diagramsDir.listFiles { f -> f.extension == "puml" } ?: emptyArray()
            assertTrue(generated.isNotEmpty(), "Expected at least one .puml file to be generated")
            assertTrue(generated.first().readText().contains("@startuml"))
        }
    }

    // ------------------------------------------------------------------ //
    //  Nested 3 : timeout réseau                                           //
    // ------------------------------------------------------------------ //

    @Nested
    @Order(3)
    @DisplayName("Network failure handling")
    inner class NetworkFailure {

        @Test
        fun `should fail gracefully when llm endpoint is unreachable`() {
            // Ce test ne requiert PAS Ollama — il teste l'absence intentionnelle
            val subDir = File(sharedProjectDir, "unreachable-test").also { it.mkdirs() }
            File(subDir, "settings.gradle.kts").writeText("""rootProject.name = "unreachable"""")
            File(subDir, "build.gradle.kts").writeText(
                """
                plugins { id("com.cheroliv.plantuml") }
                plantuml { configPath = "ctx.yml" }
                """.trimIndent(),
            )
            File(subDir, "ctx.yml").writeText(
                """
                langchain:
                  model: "ollama"
                  ollama:
                    baseUrl: "http://localhost:19999"
                    modelName: "ghost-model"
                input:
                  prompts: "prompts"
                output:
                  diagrams: "gen/diagrams"
                  images: "gen/images"
                  rag: "gen/rag"
                """.trimIndent(),
            )
            File(subDir, "prompts").mkdirs()
            File(subDir, "prompts/test.prompt").writeText("A diagram")

            val result = GradleRunner.create()
                .withProjectDir(subDir)
                .withArguments("processPlantumlPrompts", "--stacktrace")
                .withPluginClasspath()
                .buildAndFail()

            assertTrue(
                result.output.contains("Connection refused", ignoreCase = true) ||
                    result.output.contains("connect", ignoreCase = true) ||
                    result.output.contains("refused", ignoreCase = true) ||
                    result.output.contains("timeout", ignoreCase = true),
            )
        }
    }

    // ------------------------------------------------------------------ //
    //  Nested 4 : permissions fichier (Unix uniquement)                   //
    // ------------------------------------------------------------------ //

    @Nested
    @Order(4)
    @DisplayName("File permission handling")
    inner class FilePermissions {

        @BeforeEach
        fun requireUnix() {
            assumeTrue(File.separator == "/", "File permission tests require Unix")
        }

        @Test
        fun `should handle unreadable puml file gracefully`() {
            val file = File(sharedProjectDir, "protected.puml")
            file.writeText("@startuml\nclass Protected\n@enduml")

            try {
                file.setReadable(false)

                val result = runner(
                    "validatePlantumlSyntax",
                    "-Pplantuml.diagram=protected.puml",
                ).buildAndFail()

                assertTrue(
                    result.output.contains("Permission denied", ignoreCase = true) ||
                        result.output.contains("Unable to read", ignoreCase = true) ||
                        result.output.contains("does not exist", ignoreCase = true),
                )
            } finally {
                file.setReadable(true)
                file.delete()
            }
        }
    }
}
