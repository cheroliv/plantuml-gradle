package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertTrue

/**
 * Test pour vérifier uniquement le handshake avec les fournisseurs LLM
 * sans aller jusqu'à l'authentification complète.
 */
class LlmHandshakeTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")

        settingsFile.writeText(
            """
            rootProject.name = "plantuml-handshake-test"
        """.trimIndent()
        )

        // Configuration du plugin
        buildFile.writeText(
            """
            plugins {
                id("com.cheroliv.plantuml")
            }
        """.trimIndent()
        )
    }

    @Ignore
    @Test
    fun `should perform handshake with Ollama without full authentication`() {
        // Créer un fichier de configuration pour Ollama
        val configFile = File(testProjectDir, "ollama-local-smollm-135.yml")
        configFile.writeText(
            """
            langchain:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              validation: false

            input:
              prompts: "test/prompts"
              
            output:
              diagrams: "test/generated/diagrams"
              images: "test/generated/images"
              validations: "test/generated/validations"
              rag: "test/generated/rag"
        """.trimIndent()
        )

        // Créer un répertoire de prompts et un fichier prompt simple
        val promptsDir = File(testProjectDir, "test/prompts")
        promptsDir.mkdirs()

        val promptFile = File(promptsDir, "test.prompt")
        promptFile.writeText("Create a simple class diagram with one class")

        // Exécuter la tâche avec le paramètre LLM
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "processPlantumlPrompts",
                "-Pplantuml.langchain.model=ollama",
                "--stacktrace",
                "--info"
            )
            .withPluginClasspath()
            .build()

        // Vérifier que la tâche s'exécute sans erreur de connexion
        // Note: Ce test ne vérifie que le handshake initial, pas l'authentification complète
        assertTrue(
            result.output.contains("Processing") ||
                    result.output.contains("No prompt files found") ||
                    result.output.contains("PlantUML generation involves randomness")
        )
    }
}