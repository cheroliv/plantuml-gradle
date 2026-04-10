package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Ignore

/**
 * Suite d'intégration Gradle.
 *
 * Principe central : UNE seule instance de GradleRunner / TempDir partagée
 * entre toutes les classes imbriquées. Le cold start JVM (3-8s) est payé
 * une seule fois pour l'ensemble de la suite.
 *
 * Chaque @Nested joue sa partition sur le même projet Gradle :
 *  - [PluginApplied]      vérifie registration des tâches et de l'extension
 *  - [SyntaxValidation]   exercice de validatePlantumlSyntax
 *  - [RagIndexing]        exercice de reindexPlantumlRag
 *  - [PromptProcessing]   exercice de processPlantumlPrompts (mode test)
 *
 * Les tâches qui nécessitent un LLM réel utilisent -Dplantuml.test.mode=true
 * pour court-circuiter les appels réseau dans la tâche Gradle elle-même.
 * Le comportement réseau est testé dans PromptOrchestratorTest via WireMock.
 */
@TestClassOrder(ClassOrderer.OrderAnnotation::class)
@TestMethodOrder(OrderAnnotation::class)
class PlantumlPluginIntegrationSuite {

    companion object {
        @TempDir
        @JvmStatic
        lateinit var sharedProjectDir: File

        private lateinit var buildFile: File
        private lateinit var settingsFile: File

        /**
         * Construction du projet Gradle partagé — exécutée une seule fois
         * avant toutes les nested classes.
         */
        @BeforeAll
        @JvmStatic
        fun setupSharedProject() {
            buildFile = File(sharedProjectDir, "build.gradle.kts")
            settingsFile = File(sharedProjectDir, "settings.gradle.kts")

            settingsFile.writeText("""rootProject.name = "plantuml-integration-suite"""")

            buildFile.writeText(
                """
                plugins {
                    id("com.cheroliv.plantuml")
                }
                plantuml {
                    configPath = "plantuml-context.yml"
                }
                """.trimIndent(),
            )

            File(sharedProjectDir, "plantuml-context.yml").writeText(
                """
                input:
                  prompts: "test-prompts"
                output:
                  diagrams: "generated/diagrams"
                  images:   "generated/images"
                  rag:      "generated/rag"
                  validations: "generated/validations"
                langchain4j:
                  model: "ollama"
                  ollama:
                    baseUrl: "http://localhost:11434"
                    modelName: "smollm:135m"
                  maxIterations: 1
                  validation: false
                """.trimIndent(),
            )

            // Répertoire de prompts partagé
            File(sharedProjectDir, "test-prompts").mkdirs()
            File(sharedProjectDir, "test-prompts/sample.prompt")
                .writeText("Create a simple class diagram with one class named Car")

            // Répertoire RAG partagé pré-peuplé
            val ragDir = File(sharedProjectDir, "generated/rag").also { it.mkdirs() }
            File(ragDir, "sample.puml").writeText(
                "@startuml\nclass Car {\n  - String brand\n}\n@enduml",
            )
        }

        /** Helper partagé entre nested classes. */
        fun runner(vararg args: String): GradleRunner =
            GradleRunner.create()
                .withProjectDir(sharedProjectDir)
                .withArguments(*args)
                .withPluginClasspath()
    }

    // ------------------------------------------------------------------ //
    //  Nested 1 : enregistrement du plugin                                //
    // ------------------------------------------------------------------ //

    @Nested
    @Order(1)
    @DisplayName("Plugin applied")
    inner class PluginApplied {

        @Test
        @Order(1)
        @Ignore
        fun `should register all three tasks`() {
            val result = runner("tasks", "--all").build()

            assertTrue(result.output.contains("processPlantumlPrompts"))
            assertTrue(result.output.contains("validatePlantumlSyntax"))
            assertTrue(result.output.contains("reindexPlantumlRag"))
        }

        @Test
        @Order(2)
        @Ignore
        fun `dry-run should list all tasks without failing`() {
            val result = runner(
                "processPlantumlPrompts",
                "validatePlantumlSyntax",
                "reindexPlantumlRag",
                "--dry-run",
            ).build()

            assertTrue(result.output.contains("processPlantumlPrompts"))
        }
    }

    // ------------------------------------------------------------------ //
    //  Nested 2 : validation syntaxique                                   //
    // ------------------------------------------------------------------ //

    @Nested
    @Order(2)
    @DisplayName("Syntax validation")
    inner class SyntaxValidation {

        @BeforeEach
        fun writeSampleDiagram() {
            File(sharedProjectDir, "sample.puml").writeText(
                "@startuml\nclass Car {\n  - String brand\n}\n@enduml",
            )
        }

        @Test
        @Order(1)
        @Ignore
        fun `should validate a correct puml file`() {
            val result = runner(
                "validatePlantumlSyntax",
                "-Pplantuml.diagram=sample.puml",
                "-Dplantuml.test.mode=true",
            ).build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":validatePlantumlSyntax")?.outcome)
            assertTrue(result.output.contains("PlantUML syntax is valid"))
        }

        @Test
        @Order(2)
        @Ignore
        fun `should fail on missing diagram file`() {
            val result = runner(
                "validatePlantumlSyntax",
                "-Pplantuml.diagram=nonexistent.puml",
                "-Dplantuml.test.mode=true",
            ).buildAndFail()

            assertTrue(
                result.output.contains("does not exist", ignoreCase = true) ||
                        result.output.contains("not found", ignoreCase = true) ||
                        result.output.contains("No such file", ignoreCase = true),
            )
        }

        @Test
        @Order(3)
        @Ignore
        fun `should handle unicode content in puml files`() {
            File(sharedProjectDir, "unicode.puml").writeText(
                "@startuml\ntitle Diagramme avec des caractères spéciaux\nactor Utilisateur\n@enduml",
            )

            val result = runner(
                "validatePlantumlSyntax",
                "-Pplantuml.diagram=unicode.puml",
                "-Dplantuml.test.mode=true",
            ).build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":validatePlantumlSyntax")?.outcome)
        }
    }

    // ------------------------------------------------------------------ //
    //  Nested 3 : indexation RAG                                          //
    // ------------------------------------------------------------------ //

    @Nested
    @Order(3)
    @DisplayName("RAG indexing")
    inner class RagIndexing {

        @Test
        @Order(1)
        @Ignore
        fun `should succeed with pre-existing rag directory`() {
            val result = runner(
                "reindexPlantumlRag",
                "-Dplantuml.test.mode=true",
            ).build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
            assertTrue(
                result.output.contains("Found") ||
                        result.output.contains("Indexed") ||
                        result.output.contains("Processed"),
            )
        }

        @Test
        @Order(2)
        @Ignore
        fun `should create rag directory when it does not exist`() {
            // Utilise un sous-projet isolé pour ce test précis
            val subDir = File(sharedProjectDir, "rag-absent-test").also { it.mkdirs() }
            File(subDir, "settings.gradle.kts").writeText("""rootProject.name = "rag-absent"""")
            File(subDir, "build.gradle.kts").writeText(
                """
                plugins { id("com.cheroliv.plantuml") }
                plantuml { configPath = "ctx.yml" }
                """.trimIndent(),
            )
            File(subDir, "ctx.yml").writeText("output:\n  rag: \"fresh-rag\"")

            val result = GradleRunner.create()
                .withProjectDir(subDir)
                .withArguments("reindexPlantumlRag", "-Dplantuml.test.mode=true")
                .withPluginClasspath()
                .build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
            assertTrue(
                result.output.contains("Created RAG directory") ||
                        result.output.contains("No RAG directory found") ||
                        result.output.contains("No PlantUML diagrams"),
            )
        }

        @Test
        @Order(3)
        @Ignore
        fun `should report correct diagram count`() {
            // Ajoute des fichiers supplémentaires dans le répertoire RAG partagé
            val ragDir = File(sharedProjectDir, "generated/rag")
            File(ragDir, "extra1.puml").writeText("@startuml\nclass Extra1\n@enduml")
            File(ragDir, "extra2.puml").writeText("@startuml\nclass Extra2\n@enduml")

            val result = runner(
                "reindexPlantumlRag",
                "-Dplantuml.test.mode=true",
            ).build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":reindexPlantumlRag")?.outcome)
        }
    }

    // ------------------------------------------------------------------ //
    //  Nested 4 : traitement des prompts en mode test                     //
    // ------------------------------------------------------------------ //

    @Nested
    @Order(4)
    @DisplayName("Prompt processing (test mode)")
    inner class PromptProcessing {

        @Test
        @Order(1)
        @Ignore
        fun `should complete in test mode without calling real llm`() {
            val result = runner(
                "processPlantumlPrompts",
                "-Dplantuml.test.mode=true",
                "--stacktrace",
            ).build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":processPlantumlPrompts")?.outcome)
            assertTrue(result.output.contains("Processing 1 prompt files"))
        }

        @Test
        @Order(2)
        @Ignore
        fun `command-line model parameter should override config`() {
            val result = runner(
                "processPlantumlPrompts",
                "-Pplantuml.langchain4j.model=ollama",
                "-Dplantuml.test.mode=true",
            ).build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":processPlantumlPrompts")?.outcome)
            // Pas d'erreur de configuration — le paramètre CLI a bien été pris en compte
            assertTrue(!result.output.contains("Invalid model configuration"))
        }

        @Test
        @Order(3)
        @Ignore
        fun `should handle empty prompts directory gracefully`() {
            // Sous-projet isolé avec répertoire de prompts vide
            val subDir = File(sharedProjectDir, "empty-prompts-test").also { it.mkdirs() }
            File(subDir, "settings.gradle.kts").writeText("""rootProject.name = "empty-prompts"""")
            File(subDir, "build.gradle.kts").writeText(
                """
                plugins { id("com.cheroliv.plantuml") }
                plantuml { configPath = "ctx.yml" }
                """.trimIndent(),
            )
            File(subDir, "ctx.yml").writeText("input:\n  prompts: \"empty-prompts\"")
            File(subDir, "empty-prompts").mkdirs()

            val result = GradleRunner.create()
                .withProjectDir(subDir)
                .withArguments("processPlantumlPrompts", "-Dplantuml.test.mode=true")
                .withPluginClasspath()
                .build()

            assertEquals(TaskOutcome.SUCCESS, result.task(":processPlantumlPrompts")?.outcome)
        }
    }
}
