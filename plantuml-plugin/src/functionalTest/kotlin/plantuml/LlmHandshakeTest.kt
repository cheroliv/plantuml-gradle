package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class LlmHandshakeTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `should perform handshake with Ollama without full authentication`() {
        // Setup
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"plantuml-handshake-test\"")
        File(testProjectDir, "build.gradle.kts").writeText("plugins { id(\"com.cheroliv.plantuml\") }")

        // Config Ollama avec maxIterations=1 pour accélérer
        File(testProjectDir, "ollama-local-smollm-135.yml").writeText(
            """
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              validation: false
              maxIterations: 1
            input:
              prompts: "prompts"
            output:
              diagrams: "generated/diagrams"
              images: "generated/images"
              validations: "generated/validations"
              rag: "generated/rag"
            """.trimIndent()
        )

        // Prompt file
        File(testProjectDir, "prompts").apply { mkdirs() }
        File(testProjectDir, "prompts/test.prompt").writeText("Create a simple class diagram with one class")

        // Execute
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts", "-Pplantuml.langchain4j.model=ollama")
            .withPluginClasspath()
            .build()

        // Verify - handshake réussi sans erreur fatale
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}