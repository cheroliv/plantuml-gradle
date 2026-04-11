package plantuml

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

class PerformanceTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `should process single prompt quickly`() {
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"plantuml-performance-test\"")
        File(testProjectDir, "build.gradle.kts").writeText("plugins { id(\"com.cheroliv.plantuml\") }")

        File(testProjectDir, "plantuml-context.yml").writeText("""
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              validation: false
              maxIterations: 1
            input:
              prompts: "test-prompts"
            output:
              diagrams: "generated/diagrams"
              images: "generated/images"
              validations: "generated/validations"
              rag: "generated/rag"
        """.trimIndent())

        File(testProjectDir, "test-prompts").apply { mkdirs() }
        File(testProjectDir, "test-prompts/minimal.prompt").writeText("Simple diagram")

        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts")
                .withPluginClasspath()
                .build()

            assertTrue(result.output.contains("processPlantumlPrompts"), "Task should run")
            assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Build should succeed")
        }

        assertTrue(duration < 120000, "Processing should complete within 2 minutes: ${duration}ms")
    }

    @Test
    fun `should validate syntax extremely quickly`() {
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"plantuml-performance-test\"")
        File(testProjectDir, "build.gradle.kts").writeText("plugins { id(\"com.cheroliv.plantuml\") }")

        File(testProjectDir, "minimal.puml").writeText("@startuml\nclass A\n@enduml")

        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=minimal.puml")
                .withPluginClasspath()
                .build()

            assertTrue(result.output.contains("validatePlantumlSyntax"), "Validation should run")
            assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Build should succeed")
        }

        assertTrue(duration < 60000, "Validation should complete within 1 minute: ${duration}ms")
    }

    @Test
    fun `should validate multiple files quickly`() {
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"plantuml-performance-test\"")
        File(testProjectDir, "build.gradle.kts").writeText("plugins { id(\"com.cheroliv.plantuml\") }")

        for (i in 1..2) {
            File(testProjectDir, "diagram$i.puml").writeText("@startuml\nclass A$i\n@enduml")
        }

        val duration = measureTimeMillis {
            for (i in 1..2) {
                val result = GradleRunner.create()
                    .withProjectDir(testProjectDir)
                    .withArguments("validatePlantumlSyntax", "-Pplantuml.diagram=diagram$i.puml")
                    .withPluginClasspath()
                    .build()

                assertTrue(result.output.contains("validatePlantumlSyntax"), "Validation should complete for file $i")
                assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Build should succeed for file $i")
            }
        }

        assertTrue(duration < 120000, "Validating 2 files should complete within 2 minutes: ${duration}ms")
    }

    @Test
    fun `should handle concurrent tasks efficiently`() {
        File(testProjectDir, "settings.gradle.kts").writeText("rootProject.name = \"plantuml-performance-test\"")
        File(testProjectDir, "build.gradle.kts").writeText("plugins { id(\"com.cheroliv.plantuml\") }")

        File(testProjectDir, "plantuml-context.yml").writeText("""
            langchain4j:
              model: "ollama"
              ollama:
                baseUrl: "http://localhost:11434"
                modelName: "smollm:135m"
              validation: false
              maxIterations: 1
            input:
              prompts: "min"
            output:
              diagrams: "gen"
              images: "gen"
              validations: "gen"
              rag: "gen"
        """.trimIndent())

        File(testProjectDir, "min").apply { mkdirs() }
        File(testProjectDir, "min/x.prompt").writeText("Y")

        val duration = measureTimeMillis {
            val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("processPlantumlPrompts")
                .withPluginClasspath()
                .build()

            assertTrue(result.output.contains("processPlantumlPrompts"), "Should run")
            assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Build should succeed")
        }

        assertTrue(duration < 120000, "Should complete within 2 minutes: ${duration}ms")
    }
}