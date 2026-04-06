package plantuml.service

import plantuml.PlantumlConfig
import java.io.File
import java.nio.file.Path

/**
 * Logique métier de traitement des prompts, extraite de ProcessPlantumlPromptsTask.
 *
 * Cette classe ne connaît pas Gradle — elle reçoit ses dépendances par injection,
 * ce qui la rend testable directement en JUnit sans démarrer de processus Gradle.
 *
 * La tâche Gradle devient une fine wrapper qui instancie et délègue.
 */
class PromptOrchestrator(
    private val config: PlantumlConfig,
    private val diagramProcessor: DiagramProcessor,
    private val plantumlService: PlantumlService,
    private val projectDir: Path,
) {
    data class ProcessingResult(
        val totalPrompts: Int,
        val succeeded: Int,
        val failed: Int,
        val skipped: Int,
        val messages: List<String>,
    )

    fun process(): ProcessingResult {
        val promptsDir = projectDir.resolve(config.input.prompts).toFile()

        if (!promptsDir.exists() || !promptsDir.isDirectory) {
            return ProcessingResult(
                totalPrompts = 0,
                succeeded = 0,
                failed = 0,
                skipped = 0,
                messages = listOf("No prompts directory found at: ${promptsDir.absolutePath}"),
            )
        }

        val promptFiles = promptsDir.walkTopDown()
            .filter { it.isFile && it.extension == "prompt" }
            .toList()

        if (promptFiles.isEmpty()) {
            return ProcessingResult(
                totalPrompts = 0,
                succeeded = 0,
                failed = 0,
                skipped = 0,
                messages = listOf("No prompt files found in ${promptsDir.absolutePath}"),
            )
        }

        val messages = mutableListOf("Processing ${promptFiles.size} prompt files")
        var succeeded = 0
        var failed = 0

        promptFiles.forEach { promptFile ->
            runCatching {
                processOnePrompt(promptFile, messages)
                succeeded++
            }.onFailure { e ->
                failed++
                messages += "Failed to process ${promptFile.name}: ${e.message}"
            }
        }

        return ProcessingResult(
            totalPrompts = promptFiles.size,
            succeeded = succeeded,
            failed = failed,
            skipped = 0,
            messages = messages,
        )
    }

    private fun processOnePrompt(promptFile: File, messages: MutableList<String>) {
        val promptText = promptFile.readText().trim()
        if (promptText.isBlank()) {
            messages += "Skipping empty prompt: ${promptFile.name}"
            return
        }

        val diagram = diagramProcessor.processPrompt(
            prompt = promptText,
            maxIterations = config.langchain.maxIterations,
        ) ?: run {
            messages += "Could not generate valid diagram for: ${promptFile.name}"
            return
        }

        val outputDir = projectDir.resolve(config.output.diagrams).toFile()
        outputDir.mkdirs()

        val outputFile = File(outputDir, "${promptFile.nameWithoutExtension}.puml")
        outputFile.writeText(diagram.plantuml.code)
        messages += "Generated: ${outputFile.name}"

        if (config.langchain.validation) {
            val imageDir = projectDir.resolve(config.output.images).toFile()
            imageDir.mkdirs()
            val imageFile = File(imageDir, "${promptFile.nameWithoutExtension}.${config.output.format}")
            plantumlService.generateImage(diagram.plantuml.code, imageFile)
        }
    }
}
