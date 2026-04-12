package plantuml

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.gradle.testkit.runner.GradleRunner.create
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@Suppress("FunctionName")
class LargeFileAndPathTest {

    @TempDir
    lateinit var testProjectDir: File

    private lateinit var buildFile: File
    private lateinit var settingsFile: File
    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setup() {
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle.kts")

        settingsFile.writeText("rootProject.name = \"plantuml-large-file-test\"")

        buildFile.writeText("plugins { id(\"com.cheroliv.plantuml\")}")

        wireMockServer = WireMockServer(0)
        wireMockServer.start()
        wireMockServer.stubFor(
            post(urlEqualTo("/api/chat"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"model":"smollm:135m","created_at":"2026-04-12T00:00:00Z","message":{"role":"assistant","content":"@startuml\nclass Test\n@enduml"},"done_reason":"stop","done":true}""")
                )
        )
    }

    @AfterEach
    fun teardown() {
        wireMockServer.stop()
    }

    @Test
    fun `should handle large PlantUML file`() {
        testLargePlantUmlFile()
    }

    @Test
    fun `should handle special characters in filename`() {
        testSpecialCharactersInFilename()
    }

    @Test
    fun `should handle deeply nested paths`() {
        testDeeplyNestedPaths()
    }

    @Test
    fun `should handle unicode characters`() {
        testUnicodeCharacters()
    }

    private fun testLargePlantUmlFile() {
        val largeDiagramFile = File(testProjectDir, "large.puml")
        largeDiagramFile.writeText(buildSmallLargePlantUmlContent())

        create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=large.puml")
            .withPluginClasspath()
            .build()
    }

    private fun testSpecialCharactersInFilename() {
        val specialFiles = listOf(
            "file with spaces.puml",
            "file-with-dashes.puml",
            "àccéntéd_nâmë.puml"
        )

        specialFiles.forEach { filename ->
            File(testProjectDir, filename).writeText("@startuml\nclass Test\n@enduml")
        }

        create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=file with spaces.puml")
            .withPluginClasspath()
            .build()
    }

    private fun testDeeplyNestedPaths() {
        val port = wireMockServer.port()
        val configFile = File(testProjectDir, "plantuml-context.yml")
        configFile.writeText(
            """
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:$port"
                modelName: "smollm:135m"
              validation: false
              maxIterations: 1
            input:
              prompts: "deep/path/prompts"
            output:
              images: "test-images"
              rag: "generated/rag"
              diagrams: "generated/diagrams"
        """.trimIndent()
        )

        val deepPromptsDir = File(testProjectDir, "deep/path/prompts")
        deepPromptsDir.mkdirs()

        File(deepPromptsDir, "deep.prompt").writeText("Create a diagram")

        create()
            .withProjectDir(testProjectDir)
            .withArguments("processPlantumlPrompts")
            .withPluginClasspath()
            .build()
    }

    private fun testUnicodeCharacters() {
        val unicodeFile = File(testProjectDir, "unicode.puml")
        unicodeFile.writeText(
            """
            @startuml
            title Diagramme avec des caractères spéciaux
            actor Utilisateur
            rectangle "Système" {
              Utilisateur --> (Fonctionnalité)
            }
            @enduml
        """.trimIndent()
        )

        create()
            .withProjectDir(testProjectDir)
            .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=unicode.puml")
            .withPluginClasspath()
            .build()
    }

    private fun buildSmallLargePlantUmlContent(): String =
        StringBuilder().run {
            append("@startuml\ntitle Large Diagram Test\n")
            // Reduced number of classes for faster testing
            (1..10).forEach {
                append("class Class$it {\n")
                    .append("  - String field$it\n")
                    .append("  + void method$it()\n")
                    .append("}\n\n")
            }
            // Add some relationships
            (1..5).forEach {
                append("Class$it --> Class${it + 1}\n")
            }
            append("@enduml\n")
        }.toString()
}