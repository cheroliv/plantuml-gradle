package plantuml.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import plantuml.PlantumlManager
import plantuml.service.DiagramProcessor
import plantuml.service.LlmService
import plantuml.service.PlantumlService
import java.io.File

/**
 * Gradle task: `processPlantumlPrompts`
 *
 * Processes prompt files in the prompts directory to generate PlantUML diagrams.
 * Monitors the prompts directory for new .prompt files, processes each through
 * LLM interaction loop, validates syntax, generates images, collects RAG training data,
 * and deletes processed prompt files.
 *
 * Usage:
 *   ./gradlew processPlantumlPrompts
 *
 * Optional properties:
 *   -Pplantuml.prompts.dir=custom/prompts/path
 *   -Pplantuml.langchain.model=gemini
 *   -Pplantuml.langchain.maxIterations=3
 */
@DisableCachingByDefault(because = "PlantUML generation involves randomness and AI interaction")
abstract class ProcessPlantumlPromptsTask : DefaultTask() {

    init {
        group = "plantuml"
        description = "Processes PlantUML prompts and generates diagrams"
    }

    @TaskAction
    fun processPrompts() {
        // Load configuration
        val config = PlantumlManager.Configuration.load(project)
        val promptsDir = project.findProperty("plantuml.prompts.dir") as? String
            ?: config.input.prompts
        
        logger.lifecycle("DEBUG: promptsDir from config: ${config.input.prompts}")
        logger.lifecycle("DEBUG: promptsDir from property: ${project.findProperty("plantuml.prompts.dir")}")
        logger.lifecycle("DEBUG: final promptsDir: $promptsDir")
        
        // Resolve prompts directory relative to project directory
        val promptsDirectory = project.file(promptsDir)
        logger.lifecycle("DEBUG: promptsDirectory absolute path: ${promptsDirectory.absolutePath}")
        logger.lifecycle("DEBUG: promptsDirectory exists: ${promptsDirectory.exists()}")
        
        if (!promptsDirectory.exists()) {
            logger.lifecycle("Prompts directory does not exist: ${promptsDirectory.absolutePath}")
            return
        }

        val promptFiles = promptsDirectory.listFiles { file ->
            file.extension == "prompt"
        } ?: emptyArray()

        if (promptFiles.isEmpty()) {
            logger.lifecycle("No prompt files found in: ${promptsDirectory.absolutePath}")
            return
        }

        logger.lifecycle("Processing ${promptFiles.size} prompt files...")

        // Initialize services
        val plantumlService = PlantumlService()
        val llmService = LlmService(config)
        val chatModel = llmService.createChatModel()
        val diagramProcessor = DiagramProcessor(plantumlService, chatModel, config)

        promptFiles.forEach { promptFile ->
            processSinglePrompt(promptFile, config, diagramProcessor)
        }
    }

    private fun processSinglePrompt(promptFile: File, config: plantuml.PlantumlConfig, diagramProcessor: DiagramProcessor) {
        logger.lifecycle("Processing prompt: ${promptFile.name}")
        
        // Read the prompt content
        val promptContent = promptFile.readText()
        
        // Process through LLM interaction loop (max 5 iterations)
        val maxIterations = project.findProperty("plantuml.langchain.maxIterations") as? Int
            ?: config.langchain.maxIterations
            
        val diagram = diagramProcessor.processPrompt(promptContent, maxIterations)
        
        if (diagram != null) {
            // Validate PlantUML syntax
            logger.lifecycle("  → Validating PlantUML syntax...")
            val validationResult = diagramProcessor.plantumlService.validateSyntax(diagram.plantuml.code)
            
            if (validationResult is PlantumlService.SyntaxValidationResult.Invalid) {
                logger.lifecycle("    Validation errors found:")
                logger.lifecycle("      ${validationResult.errorMessage}")
                logger.lifecycle("      ${validationResult.stackTrace}")
                // In a real implementation, we would send this back to LLM for correction
                // For now, we'll continue with the processing
            }
            
            // Generate image from valid diagrams
            logger.lifecycle("  → Generating diagram image...")
            val imagesDir = project.file(config.output.images)
            imagesDir.mkdirs()
            val imageFile = File(imagesDir, "${promptFile.nameWithoutExtension}.${config.output.format}")
            
            // Generate actual PlantUML image
            diagramProcessor.plantumlService.generateImage(diagram.plantuml.code, imageFile)
            
            // Request LLM validation with scoring
            if (config.langchain.validation) {
                logger.lifecycle("  → Requesting LLM validation...")
                val validation = diagramProcessor.validateDiagram(diagram)
                
                // Save validation feedback
                val validationsDir = project.file(config.output.validations)
                validationsDir.mkdirs()
                val validationFile = File(validationsDir, "${promptFile.nameWithoutExtension}.json")
                validationFile.writeText("""
                    {
                      "prompt": "${promptFile.name}",
                      "score": ${validation.score},
                      "feedback": "${validation.feedback}",
                      "recommendations": [${validation.recommendations.joinToString(", ") { "\"$it\"" }}]
                    }
                """.trimIndent())
            }
            
            // Save valid diagrams for RAG training
            logger.lifecycle("  → Collecting for RAG training...")
            val ragDir = project.file(config.output.rag)
            ragDir.mkdirs()
            val diagramFile = File(ragDir, "${promptFile.nameWithoutExtension}.puml")
            diagramFile.writeText(diagram.plantuml.code)
            
            // Save validation feedback for RAG if validation is enabled
            if (config.langchain.validation) {
                val validation = diagramProcessor.validateDiagram(diagram)
                diagramProcessor.saveForRagTraining(diagram, validation)
            }
        } else {
            logger.lifecycle("  → Failed to generate valid diagram after $maxIterations iterations")
        }
        
        // Delete the processed prompt file
        promptFile.delete()
        
        logger.lifecycle("  ✓ Completed processing: ${promptFile.name}")
    }
}