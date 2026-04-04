package plantuml

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskContainer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import kotlin.test.Ignore
import kotlin.test.assertNotNull

class PlantumlPluginUnitTest {

    private lateinit var project: Project
    private lateinit var plugin: PlantumlPlugin
    private lateinit var extensions: ExtensionContainer
    private lateinit var plugins: PluginContainer
    private lateinit var tasks: TaskContainer

    @BeforeEach
    fun setUp() {
        // Create mocks for the Project dependencies
        project = mock(Project::class.java)
        extensions = mock(ExtensionContainer::class.java)
        plugins = mock(PluginContainer::class.java)
        tasks = mock(TaskContainer::class.java)

        // Configure mock behaviors
        `when`(project.extensions).thenReturn(extensions)
        `when`(project.plugins).thenReturn(plugins)
        `when`(project.tasks).thenReturn(tasks)
    }

    @Ignore
    @Test
    fun `should register plantuml extension when plugin is applied`() {
        // Given
        plugin = PlantumlPlugin()

        // When
        plugin.apply(project)

        // Then
        verify(extensions).create("plantuml", PlantumlPlugin.PlantumlExtension::class.java)
    }

    @Ignore
    @Test
    fun `should register all required tasks when plugin is applied`() {
        // Given
        plugin = PlantumlPlugin()

        // When
        plugin.apply(project)

        // Then
        verify(tasks).register("processPlantumlPrompts", plantuml.tasks.ProcessPlantumlPromptsTask::class.java)
        verify(tasks).register("validatePlantumlSyntax", plantuml.tasks.ValidatePlantumlSyntaxTask::class.java)
        verify(tasks).register("reindexPlantumlRag", plantuml.tasks.ReindexPlantumlRagTask::class.java)
    }

    @Ignore
    @Test
    fun `should create plantuml extension with configurable properties`() {
        // Given
        val objects = mock(org.gradle.api.model.ObjectFactory::class.java)
        val property = mock(org.gradle.api.provider.Property::class.java)

        // This test is more of a compile-time check since we can't easily mock the internal Gradle structures
        // But it ensures the extension class can be instantiated
        val extension = PlantumlPlugin.PlantumlExtension(objects)
        assertNotNull(extension)
        assertNotNull(extension.configPath)
    }
}