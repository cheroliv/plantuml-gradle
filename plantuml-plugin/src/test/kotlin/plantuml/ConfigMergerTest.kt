package plantuml

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals

class ConfigMergerTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `should read gradle properties file directly`() {
        val gradleProperties = File(testProjectDir, "gradle.properties")
        gradleProperties.writeText("""
            plantuml.input.prompts=custom-prompts
        """.trimIndent())

        val config = ConfigMerger.loadFromGradleProperties(testProjectDir)

        assertEquals("custom-prompts", config.input.prompts)
    }

    @Test
    fun `should use gradle properties as base configuration`() {
        val gradleProperties = File(testProjectDir, "gradle.properties")
        gradleProperties.writeText("""
            plantuml.input.prompts=custom-prompts
            plantuml.output.images=custom-images
            plantuml.langchain.maxIterations=3
        """.trimIndent())

        val yamlConfig = PlantumlConfig()
        val cliParams = emptyMap<String, Any?>()

        val result = ConfigMerger.merge(testProjectDir, yamlConfig, cliParams)

        assertEquals("custom-prompts", result.input.prompts)
        assertEquals("custom-images", result.output.images)
        assertEquals(3, result.langchain.maxIterations)
    }

    @Test
    fun `should override gradle properties with YAML config`() {
        val gradleProperties = File(testProjectDir, "gradle.properties")
        gradleProperties.writeText("""
            plantuml.input.prompts=properties-prompts
            plantuml.output.images=properties-images
        """.trimIndent())

        val yamlConfig = PlantumlConfig(
            input = InputConfig(prompts = "yaml-prompts"),
            output = OutputConfig(images = "yaml-images")
        )
        val cliParams = emptyMap<String, Any?>()

        val result = ConfigMerger.merge(testProjectDir, yamlConfig, cliParams)

        assertEquals("yaml-prompts", result.input.prompts)
        assertEquals("yaml-images", result.output.images)
    }

    @Test
    fun `should override YAML with CLI parameters`() {
        val gradleProperties = File(testProjectDir, "gradle.properties")
        gradleProperties.writeText("""
            plantuml.input.prompts=properties-prompts
        """.trimIndent())

        val yamlConfig = PlantumlConfig(
            input = InputConfig(prompts = "yaml-prompts")
        )
        val cliParams = mapOf("input.prompts" to "cli-prompts")

        val result = ConfigMerger.merge(testProjectDir, yamlConfig, cliParams)

        assertEquals("cli-prompts", result.input.prompts)
    }

    @Test
    fun `should use full priority chain properties less than yaml less than cli`() {
        val gradleProperties = File(testProjectDir, "gradle.properties")
        gradleProperties.writeText("""
            plantuml.input.prompts=properties-prompts
            plantuml.input.defaultLang=properties-lang
            plantuml.output.images=properties-images
            plantuml.output.format=properties-format
        """.trimIndent())

        val yamlConfig = PlantumlConfig(
            input = InputConfig(prompts = "yaml-prompts", defaultLang = "yaml-lang"),
            output = OutputConfig(images = "yaml-images")
        )
        val cliParams = mapOf("input.prompts" to "cli-prompts")

        val result = ConfigMerger.merge(testProjectDir, yamlConfig, cliParams)

        assertEquals("cli-prompts", result.input.prompts)
        assertEquals("yaml-lang", result.input.defaultLang)
        assertEquals("yaml-images", result.output.images)
        assertEquals("properties-format", result.output.format)
    }

    @Test
    fun `should use defaults when no configuration sources provided`() {
        val yamlConfig = PlantumlConfig()
        val cliParams = emptyMap<String, Any?>()

        val result = ConfigMerger.merge(testProjectDir, yamlConfig, cliParams)

        assertEquals("prompts", result.input.prompts)
        assertEquals("generated/images", result.output.images)
        assertEquals(5, result.langchain.maxIterations)
        assertEquals("ollama", result.langchain.model)
    }

    @Test
    fun `should handle missing gradle properties file gracefully`() {
        val yamlConfig = PlantumlConfig(
            input = InputConfig(prompts = "yaml-prompts")
        )
        val cliParams = emptyMap<String, Any?>()

        val result = ConfigMerger.merge(testProjectDir, yamlConfig, cliParams)

        assertEquals("yaml-prompts", result.input.prompts)
    }

    @Test
    fun `should load all configuration categories from gradle properties`() {
        val gradleProperties = File(testProjectDir, "gradle.properties")
        gradleProperties.writeText("""
            plantuml.input.prompts=my-prompts
            plantuml.output.diagrams=my-diagrams
            plantuml.output.images=my-images
            plantuml.output.rag=my-rag
            plantuml.langchain.model=gemini
            plantuml.langchain.maxIterations=10
            plantuml.git.userName=custom-user
            plantuml.rag.tableName=my_embeddings
        """.trimIndent())

        val yamlConfig = PlantumlConfig()
        val cliParams = emptyMap<String, Any?>()

        val result = ConfigMerger.merge(testProjectDir, yamlConfig, cliParams)

        assertEquals("my-prompts", result.input.prompts)
        assertEquals("my-diagrams", result.output.diagrams)
        assertEquals("my-images", result.output.images)
        assertEquals("my-rag", result.output.rag)
        assertEquals("gemini", result.langchain.model)
        assertEquals(10, result.langchain.maxIterations)
        assertEquals("custom-user", result.git.userName)
        assertEquals("my_embeddings", result.rag.tableName)
    }
}
