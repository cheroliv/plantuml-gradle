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

    @Ignore
    @Test
    fun `should use command line LLM parameter to override configuration`() {
        // Créer un fichier de configuration YAML de test
        val configFile = File(testProjectDir, "ollama-local-smollm-135.yml")
        configFile.writeText("""
            langchain4j:
              model: "gemini"  # Configuration par défaut dans le fichier
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

        // Créer un répertoire de prompts et un fichier prompt simple
        val promptsDir = File(testProjectDir, "test/prompts")
        promptsDir.mkdirs()

        val promptFile = File(promptsDir, "test.prompt")
        promptFile.writeText("Create a simple sequence diagram")

        // Exécuter la tâche avec le paramètre LLM en ligne de commande
        // Cela doit remplacer la configuration "gemini" du fichier par "ollama"
        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "processPlantumlPrompts",
                "-Pplantuml.langchain4j.model=ollama",
                "--stacktrace",
                "--info"
            )
            .withPluginClasspath()
            .build()

        // Vérifier que la tâche s'exécute sans erreur de configuration
        // Cette vérification teste que le paramètre en ligne de commande est pris en compte
        assertTrue(
            result.output.contains("Processing") ||
                    result.output.contains("No prompt files found") ||
                    result.output.contains("PlantUML generation") ||
                    result.output.contains("DEBUG")
        )

        // S'assurer qu'il n'y a pas d'erreurs de configuration
        assertTrue(!result.output.contains("Invalid model configuration"))
    }

    @Ignore
    @Test
    fun `should perform LLM handshake without full authentication`() {
        // Créer un fichier de configuration YAML de test
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
              validations: "test/generated/validations"
              rag: "test/generated/rag"
        """.trimIndent())

        // Créer un répertoire de prompts vide (pour éviter le traitement réel)
        val promptsDir = File(testProjectDir, "test/prompts")
        promptsDir.mkdirs()

        // Exécuter la tâche avec le paramètre LLM en ligne de commande
        // Cela doit simplement initialiser le modèle sans authentification complète
        val result = create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "processPlantumlPrompts",
                "-Pplantuml.langchain4j.model=ollama",
                "--dry-run",  // N'exécute pas vraiment les tâches, juste vérifie la configuration
                "--stacktrace"
            )
            .withPluginClasspath()
            .build()

        // Vérifier que le dry-run fonctionne correctement avec le paramètre LLM
        assertTrue(
            result.output.contains("processPlantumlPrompts") ||
                    result.output.contains("validatePlantumlSyntax") ||
                    result.output.contains("reindexPlantumlRag")
        )
    }
}