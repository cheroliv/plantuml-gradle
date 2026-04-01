package plantuml.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.work.DisableCachingByDefault
import org.gradle.api.provider.Property
import plantuml.service.PlantumlService
import java.io.File

/**
 * Gradle task: `validatePlantumlSyntax`
 *
 * Validates PlantUML syntax for debugging purposes.
 *
 * Usage:
 *   ./gradlew validatePlantumlSyntax -Pplantuml.diagram=file.puml
 */
@DisableCachingByDefault(because = "Validation results depend on file content which may change")
abstract class ValidatePlantumlSyntaxTask : DefaultTask() {

    init {
        group = "plantuml"
        description = "Validates PlantUML syntax for debugging"
    }

    @get:Input
    @get:Optional
    abstract val diagramFile: Property<String>

    @TaskAction
    fun validateSyntax() {
        val diagramPath = project.findProperty("plantuml.diagram") as? String
            ?: diagramFile.orNull
            
        if (diagramPath.isNullOrEmpty()) {
            logger.lifecycle("No diagram file specified. Use -Pplantuml.diagram=file.puml")
            return
        }

        val diagramFile = File(diagramPath)
        if (!diagramFile.exists()) {
            logger.lifecycle("Diagram file does not exist: $diagramPath")
            return
        }

        logger.lifecycle("Validating PlantUML syntax for: $diagramPath")
        
        // Load the PlantUML file
        val plantumlCode = diagramFile.readText()
        
        // Parse and validate syntax using PlantUML service
        val plantumlService = PlantumlService()
        val validationResult = plantumlService.validateSyntax(plantumlCode)
        
        when (validationResult) {
            is PlantumlService.SyntaxValidationResult.Valid -> {
                logger.lifecycle("  ✓ PlantUML syntax is valid")
            }
            is PlantumlService.SyntaxValidationResult.Invalid -> {
                logger.lifecycle("  ✗ PlantUML syntax is invalid:")
                logger.lifecycle("    Error: ${validationResult.errorMessage}")
                logger.lifecycle("    Stack trace: ${validationResult.stackTrace}")
            }
        }
    }
}