package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestMethodOrder(OrderAnnotation::class)
class SharedGradleInstanceFunctionalTest {

    companion object {
        @TempDir
        @JvmField
        var sharedTestProjectDir: File? = null

        private var gradleRunner: GradleRunner? = null
        private var testResults: MutableMap<String, String> = mutableMapOf()

        @BeforeAll
        @JvmStatic
        fun setupSharedGradleInstance() {
            if (sharedTestProjectDir != null) {
                // Créer les fichiers de configuration partagés
                val settingsFile = File(sharedTestProjectDir!!, "settings.gradle.kts")
                val buildFile = File(sharedTestProjectDir!!, "build.gradle.kts")

                settingsFile.writeText("""rootProject.name = "shared-test-project"""")

                buildFile.writeText(
                    """
                    plugins {
                        id("com.cheroliv.plantuml")
                    }
                    
                    plantuml {
                        configPath = file("plantuml-context.yml").absolutePath
                    }
                """.trimIndent()
                )

                // Créer un fichier de configuration minimal
                val configFile = File(sharedTestProjectDir!!, "plantuml-context.yml")
                configFile.writeText(
                    """
                    input:
                      prompts: "test-prompts"
                    output:
                      images: "test-images"
                      rag: "test-rag"
                """.trimIndent()
                )

                // Initialiser GradleRunner une seule fois
                gradleRunner = GradleRunner.create()
                    .withProjectDir(sharedTestProjectDir!!)
                    .withPluginClasspath()
                    .withGradleVersion("9.4.0")
            }
        }
    }

    @Ignore
    @Test
    @Order(1)
    fun `test01 plugin applies successfully`() {
        val result = gradleRunner!!
            .withArguments("help", "--console=plain")
            .build()

        testResults["pluginApplies"] = result.output
        assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome)
    }

    @Ignore
    @Test
    @Order(2)
    fun `test02 all tasks are registered`() {
        val result = gradleRunner!!
            .withArguments("tasks", "--all", "--console=plain")
            .build()

        testResults["taskRegistration"] = result.output
        assertTrue(result.output.contains("processPlantumlPrompts"))
        assertTrue(result.output.contains("validatePlantumlSyntax"))
        assertTrue(result.output.contains("reindexPlantumlRag"))
    }

    @Ignore
    @Test
    @Order(3)
    fun `test03 extension configuration loads`() {
        val result = gradleRunner!!
            .withArguments("properties", "--console=plain")
            .build()

        testResults["extensionConfig"] = result.output
        assertTrue(result.output.contains("plantuml"))
    }

    @Ignore
    @Test
    @Order(4)
    fun `test04 llm provider configurations`() {
        // Créer des configurations pour différents providers
        val configFile = File(sharedTestProjectDir!!, "plantuml-context.yml")
        configFile.writeText(
            """
            input:
              prompts: "test-prompts"
            output:
              images: "test-images"
              rag: "test-rag"
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              gemini:
                apiKey: "fake-key"
        """.trimIndent()
        )

        val result = gradleRunner!!
            .withArguments("help", "--console=plain")
            .build()

        testResults["llmConfig"] = result.output
        assertEquals(TaskOutcome.SUCCESS, result.task(":help")?.outcome)
    }
}