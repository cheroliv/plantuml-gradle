package plantuml

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

/**
 * Objet compagnon pour initialiser le template de projet une seule fois.
 */
object TestProjectTemplate {
    private const val TEMPLATE_DIR = "src/functionalTest/resources/test-template"
    private lateinit var templateProjectDir: File
    private var initialized = false

    /**
     * Initialise le template de projet une seule fois pour tous les tests.
     */
    fun init(): File {
        if (!initialized) {
            val templateDir = File(TEMPLATE_DIR)
            if (!templateDir.exists()) {
                templateDir.mkdirs()
                
                // settings.gradle.kts
                File(templateDir, "settings.gradle.kts").writeText("""
                    rootProject.name = "plantuml-functional-test"
                """.trimIndent())
                
                // build.gradle.kts - avec le plugin appliqué
                File(templateDir, "build.gradle.kts").writeText("""
                    plugins {
                        id("com.cheroliv.plantuml")
                    }
                    
                    repositories {
                        mavenCentral()
                    }
                """.trimIndent())
                
                // plantuml-context.yml par défaut
                File(templateDir, "plantuml-context.yml").writeText("""
                    input:
                      prompts: "prompts"
                    output:
                      images: "generated/diagrams"
                      rag: "generated/rag"
                """.trimIndent())
                
                // Prompts directory
                File(templateDir, "prompts").mkdirs()
                
                // Generated directories
                File(templateDir, "generated/diagrams").mkdirs()
                File(templateDir, "generated/images").mkdirs()
                File(templateDir, "generated/rag").mkdirs()
            }
            templateProjectDir = templateDir
            initialized = true
        }
        return templateProjectDir
    }

    /**
     * Copie le template vers un nouveau répertoire de test.
     */
    fun copyTo(testDir: File) {
        if (!initialized) {
            throw IllegalStateException("Template not initialized. Call init() first.")
        }
        templateProjectDir.copyRecursively(testDir, overwrite = true)
    }
}

/**
 * Crée un GradleRunner avec configuration optimisée.
 */
fun createOptimizedGradleRunner(
    projectDir: File,
    vararg arguments: String
): GradleRunner {
    return GradleRunner.create()
        .withProjectDir(projectDir)
        .withArguments(arguments.toList() + "--stacktrace")
        .withPluginClasspath()
        .withGradleVersion("9.4.0") // Spécifier explicitement la version pour éviter les problèmes de compatibilité
        .withDebug(true)
}

/**
 * Exécute une tâche Gradle et retourne le résultat.
 */
fun runGradleTask(
    projectDir: File,
    task: String,
    vararg additionalArgs: String
): BuildResult {
    return createOptimizedGradleRunner(projectDir, task, *additionalArgs)
        .build()
}

/**
 * Exécute une tâche Gradle et s'attend à un échec.
 */
fun runGradleTaskAndFail(
    projectDir: File,
    task: String,
    vararg additionalArgs: String
): BuildResult {
    return createOptimizedGradleRunner(projectDir, task, *additionalArgs)
        .buildAndFail()
}

/**
 * Configure WireMock pour mocker les appels LLM avec des réponses rapides.
 * Utilise Ollama comme endpoint par défaut.
 */
fun configureWireMockForTests(wireMockServer: WireMockServer) {
    // Mock pour Ollama
    wireMockServer.stubFor(
        post(urlPathEqualTo("/api/generate"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "model": "smollm:135m",
                            "created_at": "2024-01-01T00:00:00.000Z",
                            "response": "@startuml\nclass Car {\n  - String brand\n}\n@enduml",
                            "done": true
                        }
                    """.trimIndent())
            )
    )
    
    // Mock pour les autres endpoints LLM potentiels
    wireMockServer.stubFor(
        post(urlPathMatching("/v1/.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "choices": [{
                                "message": {
                                    "content": "@startuml\nclass Car {\n  - String brand\n}\n@enduml"
                                }
                            }]
                        }
                    """.trimIndent())
            )
    )
}
