package plantuml

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Main entry point for the PlantUML Gradle plugin.
 *
 * This plugin processes PlantUML diagrams using AI assistance with LangChain4j.
 * It watches prompt files, generates diagrams through LLM interaction, validates
 * syntax, and produces images from valid diagrams.
 */
class PlantumlPlugin : Plugin<Project> {

    /** Applies the plugin by registering tasks and configuring extensions. */
    override fun apply(project: Project) {
        with(project) {
            // Register the DSL extension
            extensions.create("plantuml", PlantumlExtension::class.java)
            
            // Register tasks
            PlantumlManager.Tasks.registerTasks(this)
        }
    }

    /**
     * DSL extension for the plantuml plugin.
     *
     * Usage in build.gradle.kts:
     * ```
     * plantuml {
     *     configPath = file("plantuml-context.yml").absolutePath
     * }
     * ```
     */
    open class PlantumlExtension @Inject constructor(objects: ObjectFactory) {
        @Suppress("unused")
        val configPath: Property<String> = objects.property(String::class.java)
    }
}

