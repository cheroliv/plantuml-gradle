package plantuml

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import plantuml.tasks.ProcessPlantumlPromptsTask
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests unitaires pour ProcessPlantumlPromptsTask
 * 
 * Tests couverts:
 * - Sortie anticipée quand le répertoire prompts n'existe pas
 * - Sortie anticipée quand aucun fichier .prompt n'est trouvé
 * - Traitement des fichiers prompt avec WireMock LLM
 * - Override de plantuml.prompts.dir
 * - Override de plantuml.langchain.model
 */
class ProcessPlantumlPromptsTaskTest {

    @TempDir
    lateinit var tempDir: File

    @Suppress("JUnitMalformedDeclaration")
    @RegisterExtension
    val wireMock: WireMockExtension = WireMockExtension.newInstance()
        .options(WireMockConfiguration.wireMockConfig().dynamicPort())
        .build()

    private lateinit var project: Project
    private lateinit var task: ProcessPlantumlPromptsTask

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        
        project.pluginManager.apply("com.cheroliv.plantuml")
        task = project.tasks.getByName("processPlantumlPrompts") as ProcessPlantumlPromptsTask
        
        // Configurer WireMock pour retourner une réponse valide
        wireMock.stubFor(
            post(urlEqualTo("/api/chat"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ollamaChatJsonResponse("@startuml\nclass WireMockedClass\n@enduml")),
                ),
        )
        
        // Configurer la validation syntaxe pour retourner valide
        wireMock.stubFor(
            post(urlPathMatching("/validate.*"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody("<result><valid>true</valid></result>"),
                ),
        )
    }

    @Test
    fun `should exit early when prompts directory does not exist`() {
        // Given: prompts directory n'existe pas (non créé)
        
        // When: on exécute la tâche
        task.processPrompts()
        
        // Then: pas d'appel à WireMock (sortie anticipée)
        wireMock.verify(0, postRequestedFor(urlEqualTo("/api/chat")))
        assertTrue(true, "Task should complete without throwing exception")
    }

    @Test
    fun `should exit early when no prompt files found`() {
        // Given: répertoire prompts vide
        val promptsDir = File(tempDir, "prompts")
        promptsDir.mkdirs()
        
        // Fichier non-.prompt (doit être ignoré)
        File(promptsDir, "readme.txt").writeText("This is not a prompt file")
        
        // When: on exécute la tâche
        task.processPrompts()
        
        // Then: pas d'appel à WireMock (pas de fichiers .prompt)
        wireMock.verify(0, postRequestedFor(urlEqualTo("/api/chat")))
        assertTrue(true, "Task should complete without processing non-prompt files")
    }

    @Test
    fun `should process all prompt files in directory`() {
        // Given: répertoire prompts avec 2 fichiers .prompt
        val promptsDir = File(tempDir, "prompts")
        promptsDir.mkdirs()
        val promptFile1 = File(promptsDir, "test1.prompt")
        val promptFile2 = File(promptsDir, "test2.prompt")
        promptFile1.writeText("Create a class diagram")
        promptFile2.writeText("Create a sequence diagram")
        
        // Configuration avec WireMock
        setupProjectConfig()
        
        // When: on exécute la tâche
        task.processPrompts()
        
        // Then: les fichiers prompt ont été supprimés
        assertFalse(promptFile1.exists(), "First prompt should be deleted")
        assertFalse(promptFile2.exists(), "Second prompt should be deleted")
        
        // Et: WireMock appelé 2 fois
        wireMock.verify(2, postRequestedFor(urlEqualTo("/api/chat")))
    }

    @Test
    fun `should respect plantuml prompts dir property override`() {
        // Given: répertoire prompts personnalisé
        val customPromptsDir = File(tempDir, "custom-prompts")
        customPromptsDir.mkdirs()
        val promptFile = File(customPromptsDir, "test.prompt")
        promptFile.writeText("Create a class diagram")
        
        // Override du répertoire prompts
        project.extensions.extraProperties.set("plantuml.prompts.dir", "custom-prompts")
        
        // Configuration avec WireMock
        setupProjectConfig()
        
        // When: on exécute la tâche
        task.processPrompts()
        
        // Then: le fichier a été traité (supprimé)
        assertFalse(promptFile.exists(), "Custom prompt should be processed")
        wireMock.verify(1, postRequestedFor(urlEqualTo("/api/chat")))
    }

    @Test
    fun `should override LLM model name from command line property`() {
        // Given: configuration avec ollama comme modèle par défaut
        val configPath = File(tempDir, "plantuml-context.yml")
        configPath.writeText("""
            langchain:
              model: "ollama"
              maxIterations: 1
              validation: false
              ollama:
                baseUrl: "http://localhost:${wireMock.port}"
                modelName: "smollm:135m"
        """.trimIndent())
        
        // Given: un fichier prompt
        val promptsDir = File(tempDir, "prompts")
        promptsDir.mkdirs()
        val promptFile = File(promptsDir, "test.prompt")
        promptFile.writeText("Create a diagram")
        
        // Override du nom du modèle ollama
        // Doit être fait APRÈS l'écriture du fichier YAML
        project.extensions.extraProperties.set("plantuml.langchain.ollama.modelName", "llama2")
        
        // When: on exécute la tâche
        task.processPrompts()
        
        // Then: le fichier a été traité
        assertFalse(promptFile.exists(), "Prompt should be processed with model override")
        wireMock.verify(1, postRequestedFor(urlEqualTo("/api/chat")))
        
        // Et: le modèle envoyé est "llama2" (override)
        wireMock.verify(
            postRequestedFor(urlEqualTo("/api/chat"))
                .withRequestBody(matchingJsonPath("$.model", equalTo("llama2"))),
        )
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private fun setupProjectConfig() {
        val configPath = File(tempDir, "plantuml-context.yml")
        configPath.writeText("""
            langchain:
              model: "ollama"
              maxIterations: 1
              validation: false
              ollama:
                baseUrl: "http://localhost:${wireMock.port}"
                modelName: "smollm:135m"
        """.trimIndent())
    }

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
