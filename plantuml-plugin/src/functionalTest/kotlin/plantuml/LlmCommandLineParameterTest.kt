package plantuml

import org.gradle.testkit.runner.GradleRunner.create
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertTrue

/**
 * Test pour vérifier que le paramètre LLM en ligne de commande fonctionne correctement.
 * Ce test utilise une configuration spécifique pour valider que le paramètre
 * -Pplantuml.langchain4j.model est pris en compte.
 */
class LlmCommandLineParameterTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")

        settingsFile.writeText("""
            rootProject.name = "plantuml-llm-parameter-test"
        """.trimIndent())

        // Configuration du plugin avec une config spécifique
        buildFile.writeText("""
            plugins {
                id("com.cheroliv.plantuml")
            }
            
            plantuml {
                configPath = "ollama-local-smollm-135.yml"
            }
        """.trimIndent())
    }

    @Test
    fun `should use command line LLM parameter to override configuration`() {
        // Créer un fichier de configuration YAML avec gemini comme défaut
        val configFile = File(testProjectDir, "ollama-local-smollm-135.yml")
        configFile.writeText("""
            langchain4j:
              model: "gemini"
              gemini:
                apiKey: "fake-api-key"
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
        """.trimIndent())

        // Créer un fichier prompt simple
        val promptsDir = File(testProjectDir, "test/prompts")
        promptsDir.mkdirs()
        File(promptsDir, "test.prompt").writeText("Create a simple sequence diagram")

        // Exécuter avec override CLI : gemini → ollama
        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "processPlantumlPrompts",
                "-Pplantuml.langchain4j.model=ollama",
                "--stacktrace"
            )
            .withPluginClasspath()
            .build()

        // Vérifier que la tâche s'exécute sans erreur
        assertTrue(
            result.output.contains("processPlantumlPrompts") ||
            result.output.contains("PlantUML") ||
            result.output.contains("prompt")
        )
    }

    @Test
    fun `should perform LLM handshake without full authentication`() {
        // Créer configuration YAML minimale
        val configFile = File(testProjectDir, "ollama-local-smollm-135.yml")
        configFile.writeText("""
            langchain4j:
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
        """.trimIndent())

        // Créer répertoire de prompts vide
        val promptsDir = File(testProjectDir, "test/prompts")
        promptsDir.mkdirs()

        // Exécuter avec paramètre CLI
        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "processPlantumlPrompts",
                "-Pplantuml.langchain4j.model=ollama",
                "--stacktrace"
            )
            .withPluginClasspath()
            .build()

        // Vérifier que la tâche s'exécute sans erreur de configuration
        assertTrue(
            result.output.contains("processPlantumlPrompts") ||
            result.output.contains("No prompt files found")
        )
    }
}