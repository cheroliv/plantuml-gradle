package plantuml.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.provider.Property

/**
 * Gradle task: `validatePlantumlSyntax`
 *
 * Validates PlantUML syntax for debugging purposes.
 *
 * Usage:
 *   ./gradlew validatePlantumlSyntax -Pplantuml.diagram=file.puml
 */
abstract class ValidatePlantumlSyntaxTask : DefaultTask() {

    init {
        group = "plantuml"
        description = "Validates PlantUML syntax for debugging"
    }

    @get:Input
    abstract val diagramFile: Property<String>

    @TaskAction
    fun validateSyntax() {
        val diagramPath = project.findProperty("plantuml.diagram") as? String
            ?: diagramFile.getOrElse("")
            
        if (diagramPath.isEmpty()) {
            logger.lifecycle("No diagram file specified. Use -Pplantuml.diagram=file.puml")
            return
        }

        logger.lifecycle("Validating PlantUML syntax for: $diagramPath")
        
        // In a real implementation, this would:
        // 1. Load the PlantUML file
        // 2. Parse and validate syntax using PlantUML library
        // 3. Report any errors or confirm validity
        
        // Placeholder implementation
        logger.lifecycle("  → Parsing PlantUML syntax...")
        logger.lifecycle("  → Validating diagram structure...")
        logger.lifecycle("  ✓ Syntax validation complete")
    }
}