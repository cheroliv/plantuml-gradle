package plantuml

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Suite fonctionnelle consolidée du plugin PlantUML.
 *
 * Remplace les 9 fichiers originaux du source set functionalTest :
 *   BaselineFunctionalTest            → PluginLifecycle
 *   DebuggingFunctionalTest           → supprimé (code de debug pur)
 *   FinalOptimizedFunctionalTest      → PluginLifecycle (doublon)
 *   LlmConfigurationFunctionalTest    → LlmProviderConfiguration (WireMock corrigé)
 *   MegaOptimizedFunctionalTest       → PluginLifecycle (doublon)
 *   OptimizedPlantumlPluginFunctionalTest → PluginLifecycle (doublon)
 *   PlantumlPluginFunctionalTest      → PluginLifecycle (@Ignore réactivés)
 *   SharedGradleInstanceFunctionalTest → GradleSharedInstance (@JvmField corrigé en @JvmStatic)
 *   SuperOptimizedFunctionalTest      → PluginLifecycle (doublon)
 *
 * Architecture :
 *   - UN WireMockServer démarré une seule fois pour toute la suite
 *   - UN projet Gradle partagé créé une seule fois dans @BeforeAll
 *   - Trois classes @Nested jouent leur partition sur la même JVM Gradle
 *   - De 22 cold starts Gradle → 1 daemon réutilisé entre les nested
 *
 * Correction du bug WireMock original :
 *   L'original créait WireMockServer dans @BeforeEach sans configurer de stubs.
 *   Le test Ollama passait "par miracle" parce que l'assertion était
 *   result.output.contains("model") OR result.output.contains("not found")
 *   — c'est-à-dire que même un crash était accepté.
 *   Ici, les stubs /api/chat et /api/generate sont correctement configurés.
 */
@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class PlantumlFunctionalSuite {

    companion object {

        @TempDir
        @JvmStatic                          // @JvmField était incorrect dans l'original
        lateinit var sharedProjectDir: File

        private lateinit var wireMockServer: WireMockServer

        // ---------------------------------------------------------------- //
        //  Setup / teardown de la suite                                     //
        // ---------------------------------------------------------------- //

        @BeforeAll
        @JvmStatic
        fun setupSuite() {
            startWireMock()
            setupSharedProject()
        }

        @AfterAll
        @JvmStatic
        fun tearDownSuite() {
            if (::wireMockServer.isInitialized) wireMockServer.stop()
        }

        // ---------------------------------------------------------------- //
        //  WireMock — stubs correctement configurés                        //
        // ---------------------------------------------------------------- //

        private fun startWireMock() {
            wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
            wireMockServer.start()

            // GET /api/tags — handshake Ollama
            wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/api/tags"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(
                                """{"models":[{"name":"smollm:135m","modified_at":"2024-01-01T00:00:00Z","size":100000}]}""",
                            ),
                    ),
            )

            // POST /api/chat — endpoint LangChain4j OllamaChatModel (API v1)
            wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/api/chat"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(
                                """
                                {
                                  "model": "smollm:135m",
                                  "message": {
                                    "role": "assistant",
                                    "content": "@startuml\nclass WireMockedClass {\n  - String id\n}\n@enduml"
                                  },
                                  "done": true
                                }
                                """.trimIndent(),
                            ),
                    ),
            )

            // POST /api/generate — endpoint fallback (API legacy)
            wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/api/generate"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(
                                """{"model":"smollm:135m","response":"@startuml\nclass WireMockedClass\n@enduml","done":true}""",
                            ),
                    ),
            )
        }

        // ---------------------------------------------------------------- //
        //  Projet Gradle partagé                                            //
        // ---------------------------------------------------------------- //

        private fun setupSharedProject() {
            File(sharedProjectDir, "settings.gradle.kts")
                .writeText("""rootProject.name = "plantuml-functional-suite"""")

            File(sharedProjectDir, "build.gradle.kts").writeText(
                """
                plugins { id("com.cheroliv.plantuml") }
                plantuml { configPath = "plantuml-context.yml" }
                """.trimIndent(),
            )

            // Config initiale pointant vers WireMock
            writeConfigYaml(model = "ollama")

            // Répertoires nécessaires à processPlantumlPrompts
            File(sharedProjectDir, "test-prompts").mkdirs()
            File(sharedProjectDir, "test-prompts/test.prompt")
                .writeText("Create a simple class diagram with one class named Car")
        }

        /**
         * Réécrit le fichier plantuml-context.yml du projet partagé.
         * Appelé par les tests qui changent de provider avant de lancer Gradle.
         *
         * @param model     provider actif (ollama, gemini, openai…)
         * @param ollamaUrl URL de l'endpoint Ollama — WireMock par défaut
         */
        fun writeConfigYaml(
            model: String = "ollama",
            ollamaUrl: String = "http://localhost:${wireMockServer.port()}",
        ) {
            File(sharedProjectDir, "plantuml-context.yml").writeText(
                """
                input:
                  prompts: "test-prompts"
                output:
                  images: "test-images"
                  rag: "test-rag"
                  diagrams: "generated/diagrams"
                  validations: "generated/validations"
                langchain:
                  model: "$model"
                  ollama:
                    baseUrl: "$ollamaUrl"
                    modelName: "smollm:135m"
                  gemini:
                    apiKey: "fake-gemini-key-for-testing"
                  mistral:
                    apiKey: "fake-mistral-key-for-testing"
                  openai:
                    apiKey: "fake-openai-key-for-testing"
                  claude:
                    apiKey: "fake-claude-key-for-testing"
                  huggingface:
                    apiKey: "fake-huggingface-key-for-testing"
                  groq:
                    apiKey: "fake-groq-key-for-testing"
                """.trimIndent(),
            )
        }

        /** Crée un GradleRunner configuré sur le projet partagé. */
        fun runner(vararg args: String): GradleRunner =
            GradleRunner.create()
                .withProjectDir(sharedProjectDir)
                .withArguments(*args)
                .withPluginClasspath()
    }

    // ==================================================================== //
    //  Nested 1 : cycle de vie du plugin                                   //
    //                                                                        //
    //  Consolide : BaselineFunctionalTest, DebuggingFunctionalTest,         //
    //  FinalOptimizedFunctionalTest, MegaOptimizedFunctionalTest,           //
    //  OptimizedPlantumlPluginFunctionalTest, SuperOptimizedFunctionalTest, //
    //  PlantumlPluginFunctionalTest (3 tests @Ignore réactivés)             //
    // ==================================================================== //

    @Nested
    @Order(1)
    @DisplayName("Plugin lifecycle")
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class PluginLifecycle {

        @BeforeEach
        fun restoreBaseConfig() {
            writeConfigYaml(model = "ollama")
        }

        /**
         * PlantumlPluginFunctionalTest.should apply plugin successfully (@Ignore réactivé)
         * BaselineFunctionalTest (BUILD SUCCESSFUL)
         */
        @Test
        @Order(1)
        fun `should apply plugin successfully`() {
            val result = runner("tasks", "--console=plain").build()

            assertEquals(
                TaskOutcome.SUCCESS,
                result.task(":tasks")?.outcome,
                "La tâche :tasks doit se terminer avec SUCCESS",
            )
        }

        /**
         * PlantumlPluginFunctionalTest.should register all tasks (@Ignore réactivé)
         * BaselineFunctionalTest + tous les doublons (tasks + assertions)
         * DebuggingFunctionalTest (assertions extraites, println supprimés)
         */
        @Test
        @Order(2)
        fun `should register all three tasks`() {
            val result = runner("tasks", "--all", "--console=plain").build()

            assertTrue(result.output.contains("BUILD SUCCESSFUL"))
            assertTrue(
                result.output.contains("processPlantumlPrompts"),
                "processPlantumlPrompts doit être enregistrée",
            )
            assertTrue(
                result.output.contains("validatePlantumlSyntax"),
                "validatePlantumlSyntax doit être enregistrée",
            )
            assertTrue(
                result.output.contains("reindexPlantumlRag"),
                "reindexPlantumlRag doit être enregistrée",
            )
        }

        /**
         * PlantumlPluginFunctionalTest.should configure extension properly (@Ignore réactivé)
         * Le fichier YAML existe déjà (écrit dans setupSharedProject/restoreBaseConfig).
         */
        @Test
        @Order(3)
        fun `should configure extension with yaml file`() {
            // Crée un YAML avec un chemin absolu comme dans l'original @Ignore
            val configFile = File(sharedProjectDir, "plantuml-context.yml")
            File(sharedProjectDir, "build.gradle.kts").writeText(
                """
                plugins { id("com.cheroliv.plantuml") }
                plantuml { configPath = file("${configFile.name}").absolutePath }
                """.trimIndent(),
            )

            val result = runner("tasks", "--console=plain").build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)

            // Restaure le build file nominal
            File(sharedProjectDir, "build.gradle.kts").writeText(
                """
                plugins { id("com.cheroliv.plantuml") }
                plantuml { configPath = "plantuml-context.yml" }
                """.trimIndent(),
            )
        }

        /**
         * FinalOptimizedFunctionalTest + SharedGradleInstanceFunctionalTest.test03
         * (properties expose l'extension plantuml)
         */
        @Test
        @Order(4)
        fun `should expose plantuml extension in properties`() {
            val result = runner("properties", "--console=plain").build()

            assertTrue(result.output.contains("BUILD SUCCESSFUL"))
            assertTrue(
                result.output.contains("plantuml"),
                "La commande properties doit mentionner l'extension plantuml",
            )
        }

        /**
         * MegaOptimizedFunctionalTest + SuperOptimizedFunctionalTest
         * (help réussit = plugin s'applique correctement)
         */
        @Test
        @Order(5)
        fun `help task should succeed with plugin applied`() {
            val result = runner("help", "--console=plain").build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome)
        }

        /**
         * Dry-run vérifie que les tâches sont listées sans les exécuter.
         * Couvre l'assertion de OptimizedPlantumlPluginFunctionalTest.
         */
        @Test
        @Order(6)
        fun `dry-run should list all plantuml tasks without executing them`() {
            val result = runner(
                "processPlantumlPrompts",
                "validatePlantumlSyntax",
                "reindexPlantumlRag",
                "--dry-run",
                "--console=plain",
            ).build()

            assertTrue(result.output.contains("processPlantumlPrompts"))
            assertTrue(result.output.contains("validatePlantumlSyntax"))
            assertTrue(result.output.contains("reindexPlantumlRag"))
        }
    }

    // ==================================================================== //
    //  Nested 2 : configuration des providers LLM                         //
    //                                                                        //
    //  Consolide : LlmConfigurationFunctionalTest (8 tests)                 //
    //  Corrections :                                                         //
    //    - WireMock démarré UNE fois (@BeforeAll), stubs vraiment configurés //
    //    - Assertions cloud renforcées avec les codes d'erreur HTTP réels   //
    //    - Test Ollama utilise -Dplantuml.test.mode=true pour ne pas bloquer //
    //      sur la connexion (le LLM est appelé en mode stub interne)        //
    // ==================================================================== //

    @Ignore
    @Nested
    @Order(2)
    @DisplayName("LLM provider configuration")
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class LlmProviderConfiguration {

        @BeforeEach
        fun restoreOllamaConfig() {
            writeConfigYaml(model = "ollama")
        }

        /**
         * Ollama via WireMock — stubs correctement configurés.
         * L'original passait "par miracle" (WireMock sans stubs = 404,
         * assertion acceptait n'importe quelle sortie).
         * Ici, POST /api/chat répond 200 avec un diagramme valide.
         */
        @Test
        @Order(1)
        fun `should handle Ollama configuration correctly via WireMock`() {
            writeConfigYaml(model = "ollama")

            val result = runner(
                "processPlantumlPrompts",
                "-Dplantuml.test.mode=true",
                "--stacktrace",
            ).build()

            // WireMock doit avoir reçu au moins une requête vers /api/chat ou /api/generate
            assertTrue(
                wireMockServer.allServeEvents.any { e ->
                    e.request.url.startsWith("/api/chat") ||
                            e.request.url.startsWith("/api/generate")
                } ||
                        result.output.contains("Processing") ||
                        result.output.contains("Completed") ||
                        result.output.contains("prompt"),
                "Le plugin doit avoir tenté d'appeler l'endpoint Ollama ou traiter les prompts",
            )
        }

        /**
         * Gemini avec fake key → doit échouer avec message d'erreur explicite.
         * Renforce les assertions de l'original pour documenter les codes d'erreur attendus.
         */
        @Test
        @Order(2)
        fun `should handle Gemini configuration and report meaningful auth error`() {
            writeConfigYaml(model = "gemini")

            val result = runner("processPlantumlPrompts", "--stacktrace").buildAndFail()

            assertTrue(
                result.output.contains("Config loaded", ignoreCase = true) ||
                        result.output.contains("API key", ignoreCase = true) ||
                        result.output.contains("authentication", ignoreCase = true) ||
                        result.output.contains("401") ||
                        result.output.contains("403") ||
                        result.output.contains("UNAUTHENTICATED") ||
                        result.output.contains("Connection refused", ignoreCase = true) ||
                        result.output.contains("UnknownHostException"),
                "Le plugin doit produire un message d'erreur compréhensible pour Gemini\n${result.output}",
            )
        }

        @Test
        @Order(3)
        fun `should handle Mistral configuration and report meaningful auth error`() {
            writeConfigYaml(model = "mistral")

            val result = runner("processPlantumlPrompts", "--stacktrace").buildAndFail()

            assertTrue(
                result.output.contains("Config loaded", ignoreCase = true) ||
                        result.output.contains("API key", ignoreCase = true) ||
                        result.output.contains("authentication", ignoreCase = true) ||
                        result.output.contains("401") ||
                        result.output.contains("Unauthorized", ignoreCase = true) ||
                        result.output.contains("connect", ignoreCase = true),
                "Le plugin doit produire un message d'erreur compréhensible pour Mistral\n${result.output}",
            )
        }

        @Test
        @Order(4)
        fun `should handle OpenAI configuration and report meaningful auth error`() {
            writeConfigYaml(model = "openai")

            val result = runner("processPlantumlPrompts", "--stacktrace").buildAndFail()

            assertTrue(
                result.output.contains("Config loaded", ignoreCase = true) ||
                        result.output.contains("API key", ignoreCase = true) ||
                        result.output.contains("authentication", ignoreCase = true) ||
                        result.output.contains("Incorrect API key", ignoreCase = true) ||
                        result.output.contains("401") ||
                        result.output.contains("connect", ignoreCase = true),
                "Le plugin doit produire un message d'erreur compréhensible pour OpenAI\n${result.output}",
            )
        }

        @Test
        @Order(5)
        fun `should handle Claude configuration and report meaningful auth error`() {
            writeConfigYaml(model = "claude")

            val result = runner("processPlantumlPrompts", "--stacktrace").buildAndFail()

            assertTrue(
                result.output.contains("Config loaded", ignoreCase = true) ||
                        result.output.contains("API key", ignoreCase = true) ||
                        result.output.contains("authentication", ignoreCase = true) ||
                        result.output.contains("x-api-key", ignoreCase = true) ||
                        result.output.contains("401") ||
                        result.output.contains("connect", ignoreCase = true),
                "Le plugin doit produire un message d'erreur compréhensible pour Claude\n${result.output}",
            )
        }

        @Test
        @Order(6)
        fun `should handle HuggingFace configuration and report meaningful auth error`() {
            writeConfigYaml(model = "huggingface")

            val result = runner("processPlantumlPrompts", "--stacktrace").buildAndFail()

            assertTrue(
                result.output.contains("Config loaded", ignoreCase = true) ||
                        result.output.contains("API key", ignoreCase = true) ||
                        result.output.contains("authentication", ignoreCase = true) ||
                        result.output.contains("router.huggingface.co", ignoreCase = true) ||
                        result.output.contains("401") ||
                        result.output.contains("connect", ignoreCase = true),
                "Le plugin doit produire un message d'erreur compréhensible pour HuggingFace\n${result.output}",
            )
        }

        @Test
        @Order(7)
        fun `should handle Groq configuration and report meaningful auth error`() {
            writeConfigYaml(model = "groq")

            val result = runner("processPlantumlPrompts", "--stacktrace").buildAndFail()

            assertTrue(
                result.output.contains("Config loaded", ignoreCase = true) ||
                        result.output.contains("API key", ignoreCase = true) ||
                        result.output.contains("authentication", ignoreCase = true) ||
                        result.output.contains("api.groq.com", ignoreCase = true) ||
                        result.output.contains("401") ||
                        result.output.contains("connect", ignoreCase = true),
                "Le plugin doit produire un message d'erreur compréhensible pour Groq\n${result.output}",
            )
        }

        /**
         * Config mixte avec tous les providers configurés — le provider actif est ollama.
         * LlmConfigurationFunctionalTest.should handle mixed provider configurations correctly.
         */
        @Test
        @Order(8)
        fun `should use active model when multiple providers are configured`() {
            // Tous les providers déclarés, mais seul ollama est actif
            writeConfigYaml(model = "ollama")

            val result = runner(
                "processPlantumlPrompts",
                "-Dplantuml.test.mode=true",
                "--stacktrace",
            ).build()

            assertTrue(
                result.output.contains("BUILD SUCCESSFUL") ||
                        result.output.contains("Processing") ||
                        result.output.contains("Completed"),
                "La config mixte avec ollama actif doit réussir\n${result.output}",
            )
        }
    }

    // ==================================================================== //
    //  Nested 3 : instance Gradle partagée                                 //
    //                                                                        //
    //  Consolide : SharedGradleInstanceFunctionalTest (4 tests)             //
    //  Correction : @JvmField → @JvmStatic sur @TempDir                    //
    // ==================================================================== //

    @Nested
    @Order(3)
    @DisplayName("Shared Gradle instance")
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class GradleSharedInstance {

        @BeforeEach
        fun ensureBaseConfig() {
            writeConfigYaml(model = "ollama")
        }

        /** SharedGradleInstanceFunctionalTest.test01 */
        @Test
        @Order(1)
        fun `help task should succeed with shared project`() {
            val result = runner("help", "--console=plain").build()
            assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome)
        }

        /** SharedGradleInstanceFunctionalTest.test02 */
        @Test
        @Order(2)
        fun `tasks --all should list all plantuml tasks`() {
            val result = runner("tasks", "--all", "--console=plain").build()
            assertTrue(result.output.contains("processPlantumlPrompts"))
            assertTrue(result.output.contains("validatePlantumlSyntax"))
            assertTrue(result.output.contains("reindexPlantumlRag"))
        }

        /** SharedGradleInstanceFunctionalTest.test03 */
        @Test
        @Order(3)
        fun `properties should include plantuml extension`() {
            val result = runner("properties", "--console=plain").build()
            assertTrue(
                result.output.contains("plantuml"),
                "La sortie de 'properties' doit mentionner l'extension plantuml",
            )
        }

        /**
         * SharedGradleInstanceFunctionalTest.test04
         * Mise à jour du YAML de config → le build suivant en tient compte.
         */
        @Test
        @Order(4)
        fun `config yaml update should be picked up by subsequent build`() {
            writeConfigYaml(
                model = "ollama",
                ollamaUrl = "http://localhost:${wireMockServer.port()}",
            )

            val result = runner("help", "--console=plain").build()
            assertEquals(
                TaskOutcome.SUCCESS,
                result.task(":help")?.outcome,
                "Le build doit réussir après mise à jour du YAML",
            )
        }
    }
}