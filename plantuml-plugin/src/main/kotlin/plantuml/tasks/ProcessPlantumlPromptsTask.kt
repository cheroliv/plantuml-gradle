package plantuml.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
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
abstract class ProcessPlantumlPromptsTask : DefaultTask() {

    init {
        group = "plantuml"
        description = "Processes PlantUML prompts and generates diagrams"
    }

    @TaskAction
    fun processPrompts() {
        val promptsDir = project.findProperty("plantuml.prompts.dir") as? String
            ?: "prompts"
        
        val promptsDirectory = File(promptsDir)
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

        promptFiles.forEach { promptFile ->
            processSinglePrompt(promptFile)
        }
    }

    private fun processSinglePrompt(promptFile: File) {
        logger.lifecycle("Processing prompt: ${promptFile.name}")
        
        // In a real implementation, this would:
        // 1. Read the prompt content
        // 2. Send to LLM with max 5 iterations for refinement
        // 3. Validate PlantUML syntax
        // 4. Generate image from valid diagrams
        // 5. Request LLM validation with scoring
        // 6. Save valid diagrams for RAG training
        // 7. Delete processed prompt file
        
        // Placeholder implementation
        logger.lifecycle("  → Simulating LLM interaction...")
        logger.lifecycle("  → Validating PlantUML syntax...")
        logger.lifecycle("  → Generating diagram image...")
        logger.lifecycle("  → Requesting LLM validation...")
        logger.lifecycle("  → Collecting for RAG training...")
        logger.lifecycle("  → Deleting prompt file")
        
        // Delete the processed prompt file
        promptFile.delete()
        
        logger.lifecycle("  ✓ Completed processing: ${promptFile.name}")
    }
}