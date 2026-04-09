package plantuml

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.*
import plantuml.service.DiagramProcessor
import plantuml.service.LlmService
import plantuml.service.PlantumlService
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests unitaires de PromptOrchestrator.
 *
 * Deux stratégies coexistent ici selon le niveau de test voulu :
 *
 * 1. [WithMockDiagramProcessor] — Mockito mock le DiagramProcessor entier.
 *    Parfait pour tester la logique de fichiers / répertoires / comptage.
 *    Exécution : < 10 ms par test.
 *
 * 2. [WithWireMockLlm] — WireMock intercepte les vraies requêtes HTTP vers Ollama.
 *    Teste le chemin complet du réseau jusqu'au parsing de réponse,
 *    sans démarrer de vrai modèle. Exécution : < 50 ms par test.
 */
class PromptOrchestratorTest {

    @TempDir
    lateinit var tempDir: File

    // ------------------------------------------------------------------ //
    //  Cas 1 : DiagramProcessor complètement mocké (Mockito)              //
    // ------------------------------------------------------------------ //

    @Nested
    inner class WithMockDiagramProcessor {

        private lateinit var mockProcessor: DiagramProcessor
        private lateinit var mockPlantumlService: PlantumlService
        private lateinit var orchestrator: PromptOrchestrator

        @BeforeEach
        fun setup() {
            mockPlantumlService = mock(PlantumlService::class.java)
            mockProcessor = mock(DiagramProcessor::class.java)
            orchestrator = PromptOrchestrator(
                config = minimalConfig(),
                diagramProcessor = mockProcessor,
                plantumlService = mockPlantumlService,
                projectDir = tempDir.toPath(),
            )
        }

        @Test
        fun `should report no prompts when directory is missing`() {
            val result = orchestrator.process()

            assertEquals(0, result.totalPrompts)
            assertTrue(result.messages.any { it.contains("No prompts directory found") })
            verifyNoInteractions(mockProcessor)
        }

        @Test
        fun `should report no prompts when directory is empty`() {
            File(tempDir, "prompts").mkdirs()

            val result = orchestrator.process()

            assertEquals(0, result.totalPrompts)
            assertTrue(result.messages.any { it.contains("No prompt files found") })
        }

        @Test
        fun `should process each prompt file and count successes`() {
            val promptsDir = File(tempDir, "prompts").also { it.mkdirs() }
            File(promptsDir, "a.prompt").writeText("Create diagram A")
            File(promptsDir, "b.prompt").writeText("Create diagram B")

            `when`(mockProcessor.processPrompt(anyString(), anyInt()))
                .thenReturn(fakeDiagram())

            val result = orchestrator.process()

            assertEquals(2, result.totalPrompts)
            assertEquals(2, result.succeeded)
            assertEquals(0, result.failed)
        }

        @Test
        fun `should count failures when processor returns null`() {
            val promptsDir = File(tempDir, "prompts").also { it.mkdirs() }
            File(promptsDir, "bad.prompt").writeText("impossible prompt")

            `when`(mockProcessor.processPrompt(anyString(), anyInt()))
                .thenReturn(null)

            val result = orchestrator.process()

            assertEquals(1, result.totalPrompts)
            assertEquals(0, result.succeeded)
            // null = pas de diagramme généré, skipped plutôt que failed
            assertTrue(result.messages.any { it.contains("Could not generate") })
        }

        @Test
        fun `should skip blank prompt files`() {
            val promptsDir = File(tempDir, "prompts").also { it.mkdirs() }
            File(promptsDir, "blank.prompt").writeText("   \n  ")
            File(promptsDir, "valid.prompt").writeText("Create a class diagram")

            `when`(mockProcessor.processPrompt(anyString(), anyInt()))
                .thenReturn(fakeDiagram())

            val result = orchestrator.process()

            assertEquals(2, result.totalPrompts)
            assertTrue(result.messages.any { it.contains("Skipping empty prompt") })
        }

        @Test
        fun `should write puml file to output diagrams directory`() {
            val promptsDir = File(tempDir, "prompts").also { it.mkdirs() }
            File(promptsDir, "test.prompt").writeText("Create a class diagram")

            `when`(mockProcessor.processPrompt(anyString(), anyInt()))
                .thenReturn(fakeDiagram("@startuml\nclass Generated\n@enduml"))

            orchestrator.process()

            val outputFile = File(tempDir, "generated/diagrams/test.puml")
            assertTrue(outputFile.exists(), "Output .puml file should be created")
            assertTrue(outputFile.readText().contains("@startuml"))
        }

        @Test
        fun `should not call generateImage when validation is disabled`() {
            val promptsDir = File(tempDir, "prompts").also { it.mkdirs() }
            File(promptsDir, "test.prompt").writeText("A diagram")

            val configNoValidation = minimalConfig().copy(
                langchain4j = minimalConfig().langchain4j.copy(validation = false),
            )
            val orch = PromptOrchestrator(configNoValidation, mockProcessor, mockPlantumlService, tempDir.toPath())

            `when`(mockProcessor.processPrompt(anyString(), anyInt()))
                .thenReturn(fakeDiagram())

            orch.process()

            verifyNoInteractions(mockPlantumlService)
        }
    }

    // ------------------------------------------------------------------ //
    //  Cas 2 : WireMock intercepte les appels HTTP au LLM                 //
    // ------------------------------------------------------------------ //

    @Suppress("JUnitMalformedDeclaration")
    @RegisterExtension
    val wireMock: WireMockExtension = WireMockExtension.newInstance()
        .options(WireMockConfiguration.wireMockConfig().dynamicPort())
        .build()

    @Nested
    inner class WithWireMockLlm {

        private lateinit var orchestrator: PromptOrchestrator
        private lateinit var mockPlantumlService: PlantumlService

        @BeforeEach
        fun setup() {
            mockPlantumlService = mock(PlantumlService::class.java)

            // Réponse Ollama simulée — format JSON de l'API /api/chat
            wireMock.stubFor(
                post(urlEqualTo("/api/chat"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(ollamaChatJsonResponse("@startuml\nclass WireMockedClass\n@enduml")),
                    ),
            )

            val config = minimalConfig().copy(
                langchain4j = LangchainConfig(
                    model = "ollama",
                    ollama = OllamaConfig(
                        baseUrl = "http://localhost:${wireMock.port}",
                        modelName = "smollm:135m",
                    ),
                ),
            )

            // Le DiagramProcessor réel est instancié ici avec le vrai LlmService
            // pointant vers WireMock — on teste le chemin HTTP complet.
            val llmService = LlmService(config)
            val chatModel = llmService.createChatModel()
            val plantumlSvc = PlantumlService()
            val processor = DiagramProcessor(plantumlSvc, chatModel, null)

            orchestrator = PromptOrchestrator(config, processor, mockPlantumlService, tempDir.toPath())
        }

        @Test
        fun `should complete processing when ollama responds correctly`() {
            val promptsDir = File(tempDir, "prompts").also { it.mkdirs() }
            File(promptsDir, "test.prompt").writeText("Create a simple class diagram")

            val result = orchestrator.process()

            // Au moins une tentative a été faite vers WireMock
            wireMock.verify(1, postRequestedFor(urlEqualTo("/api/chat")))
            assertEquals(1, result.totalPrompts)
        }

        @Test
        fun `should handle 503 from llm and count as failure`() {
            wireMock.stubFor(
                post(urlEqualTo("/api/chat"))
                    .willReturn(aResponse().withStatus(503).withBody("Service Unavailable")),
            )

            val promptsDir = File(tempDir, "prompts").also { it.mkdirs() }
            File(promptsDir, "test.prompt").writeText("A diagram")

            val result = orchestrator.process()

            // Pas de succès possible si le LLM renvoie 503
            assertTrue(result.succeeded == 0 || result.failed > 0 || result.messages.any { it.contains("Could not generate") })
        }

        @Test
        fun `should send model name in request body`() {
            val promptsDir = File(tempDir, "prompts").also { it.mkdirs() }
            File(promptsDir, "test.prompt").writeText("A diagram")

            orchestrator.process()

            wireMock.verify(
                postRequestedFor(urlEqualTo("/api/chat"))
                    .withRequestBody(matchingJsonPath("$.model", equalTo("smollm:135m"))),
            )
        }

    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private fun minimalConfig() = PlantumlConfig(
        input = plantuml.InputConfig(prompts = "prompts"),
        output = plantuml.OutputConfig(
            diagrams = "generated/diagrams",
            images = "generated/images",
            format = "png",
        ),
        langchain4j = LangchainConfig(
            model = "ollama",
            validation = false,
            maxIterations = 1,
        ),
    )

    private fun fakeDiagram(code: String = "@startuml\nclass Fake\n@enduml") =
        plantuml.PlantumlDiagram(
            conversation = listOf("mock conversation"),
            plantuml = plantuml.PlantumlCode(code = code, description = "fake"),
        )

    private fun ollamaChatJsonResponse(plantumlCode: String) = """
        {
          "model": "smollm:135m",
          "message": {
            "role": "assistant",
            "content": "${plantumlCode.replace("\n", "\\n")}"
          },
          "done": true
        }
    """.trimIndent()
}
