package plantuml

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure

/**
 * Central manager for the PlantUML Gradle plugin.
 *
 * Coordinates all plugin functionality by delegating responsibilities to
 * nested objects organized by concern:
 * - [Tasks] — Task registration and configuration
 * - [Extensions] — Extension point management
 */
object PlantumlManager {

    /**
     * Registers all PlantUML-specific Gradle tasks.
     */
    object Tasks {
        fun registerTasks(project: Project) {
            // Register processing tasks
            project.tasks.register("processPlantumlPrompts", ProcessPlantumlPromptsTask::class.java)
            project.tasks.register("validatePlantumlSyntax", ValidatePlantumlSyntaxTask::class.java)
            project.tasks.register("reindexPlantumlRag", ReindexPlantumlRagTask::class.java)
        }
    }

    /**
     * Manages plugin extension points.
     */
    object Extensions {
        fun configureExtensions(project: Project) {
            // Configure any additional extensions needed
        }
    }
}