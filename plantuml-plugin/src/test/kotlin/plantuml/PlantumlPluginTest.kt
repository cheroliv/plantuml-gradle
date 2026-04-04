package plantuml

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import kotlin.test.Ignore
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Ignore
class PlantumlPluginTest {

    @Test
    fun `should create plantuml extension`() {
        // Given
        val project = ProjectBuilder.builder().build()

        // When
        project.pluginManager.apply("com.cheroliv.plantuml")

        // Then
        val extension = project.extensions.findByName("plantuml")
        assertNotNull(extension)
        assertTrue(extension is PlantumlPlugin.PlantumlExtension)
    }

    @Test
    fun `should register tasks`() {
        // Given
        val project = ProjectBuilder.builder().build()

        // When
        project.pluginManager.apply("com.cheroliv.plantuml")

        // Then
        assertNotNull(project.tasks.findByName("processPlantumlPrompts"))
        assertNotNull(project.tasks.findByName("validatePlantumlSyntax"))
        assertNotNull(project.tasks.findByName("reindexPlantumlRag"))
    }

    @Test
    fun `should apply plugin successfully`() {
        // Given
        val project = ProjectBuilder.builder().build()

        // When
        project.pluginManager.apply("com.cheroliv.plantuml")

        // Then
        // Si on arrive ici sans exception, le plugin s'est appliqué correctement
        assertTrue(project.plugins.hasPlugin("com.cheroliv.plantuml"))
    }
}