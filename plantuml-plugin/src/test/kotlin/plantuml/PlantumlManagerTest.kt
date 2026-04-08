package plantuml

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import plantuml.PlantumlManager.Configuration
import plantuml.PlantumlManager.Tasks
import plantuml.PlantumlPlugin.PlantumlExtension
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PlantumlManagerTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `should load default config when no config file exists`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()

        val config = Configuration.load(project)

        assertNotNull(config)
        assertNotNull(config.input)
        assertNotNull(config.output)
        assertNotNull(config.langchain)
    }

    @Test
    fun `should load default config when config file is empty`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()

        val emptyConfigFile = File(tempDir, "plantuml-context.yml")
        emptyConfigFile.writeText("")

        val config = Configuration.load(project)

        assertNotNull(config)
        assertNotNull(config.input)
        assertNotNull(config.output)
        assertNotNull(config.langchain)
    }

    @Test
    fun `should load default config when YAML is invalid`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()

        val invalidConfigFile = File(tempDir, "plantuml-context.yml")
        invalidConfigFile.writeText("invalid: yaml: content: [")

        val config = Configuration.load(project)

        assertNotNull(config)
        assertNotNull(config.input)
        assertNotNull(config.output)
        assertNotNull(config.langchain)
    }

    @Test
    fun `should load config from extension configPath`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()

        val customConfigFile = File(tempDir, "custom-config.yml")
        customConfigFile.writeText(
            """
            input:
              prompts: "custom/prompts"
            output:
              diagrams: "custom/diagrams"
        """.trimIndent()
        )

        project.extensions.create(
            "plantuml",
            PlantumlExtension::class.java
        ).apply { configPath.set("custom-config.yml") }

        val config = Configuration.load(project)

        assertNotNull(config)
        assertEquals("custom/prompts", config.input.prompts)
        assertEquals("custom/diagrams", config.output.diagrams)
    }

    @Test
    fun `should use default config file when extension not set`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()

        val defaultConfigFile = File(tempDir, "plantuml-context.yml")
        defaultConfigFile.writeText(
            """
            input:
              prompts: "default/prompts"
        """.trimIndent()
        )

        val config = Configuration.load(project)

        assertNotNull(config)
        assertEquals("default/prompts", config.input.prompts)
    }

    @Test
    fun `should register all three tasks correctly`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()

        Tasks.registerTasks(project)

        assertNotNull(project.tasks.findByName("processPlantumlPrompts"))
        assertNotNull(project.tasks.findByName("validatePlantumlSyntax"))
        assertNotNull(project.tasks.findByName("reindexPlantumlRag"))
    }
}
