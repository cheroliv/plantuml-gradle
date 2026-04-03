package plantuml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.Project
import plantuml.tasks.ProcessPlantumlPromptsTask
import plantuml.tasks.ReindexPlantumlRagTask
import plantuml.tasks.ValidatePlantumlSyntaxTask
import java.io.File

/**
 * Central manager for the PlantUML Gradle plugin.
 *
 * Coordinates all plugin functionality by delegating responsibilities to
 * nested objects organized by concern:
 * - [Configuration] — Configuration loading and management
 * - [Tasks] — Task registration and configuration
 * - [Extensions] — Extension point management
 */
object PlantumlManager {

    /**
     * Manages plugin configuration.
     */
    object Configuration {
        private val MAPPER: ObjectMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()

        const val CONFIG_FILE_NAME = "plantuml-context.yml"

        fun load(project: Project): PlantumlConfig {
            val extension = project.extensions.findByType(PlantumlPlugin.PlantumlExtension::class.java)
            val configPath = extension?.configPath?.orNull

            val configFile = if (configPath != null) {
                File(project.projectDir, configPath)
            } else {
                File(project.projectDir, CONFIG_FILE_NAME)
            }

            // File absent or empty — fall back to defaults
            if (!configFile.exists() || configFile.length() == 0L) {
                return PlantumlConfig()
                    .also { println("[plantuml] No $CONFIG_FILE_NAME or empty file — using defaults") }
            }

            // File present but invalid YAML — warn and fall back to defaults
            return try {
                MAPPER.readValue(configFile, PlantumlConfig::class.java)
                    .also { println("[plantuml] Config loaded: ${configFile.absolutePath}") }
            } catch (e: Exception) {
                println(
                    "[plantuml] WARNING: $CONFIG_FILE_NAME contains invalid YAML — " +
                            "using defaults (${e.message})"
                )
                PlantumlConfig()
            }
        }
    }

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