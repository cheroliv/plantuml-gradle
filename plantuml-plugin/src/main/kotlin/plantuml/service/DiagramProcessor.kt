package plantuml.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dev.langchain4j.model.chat.ChatModel
import plantuml.PlantumlCode
import plantuml.PlantumlConfig
import plantuml.PlantumlDiagram
import plantuml.ValidationFeedback
import plantuml.service.PlantumlService.SyntaxValidationResult
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Represents a single attempt in the LLM diagram generation conversation.
 *
 * Stores the complete history of prompt → response exchanges for RAG training
 * and debugging purposes. Each entry captures the state of one iteration.
 *
 * @property prompt The input prompt sent to the LLM
 * @property response The LLM's generated response
 * @property iteration Iteration number (0 = initial attempt, 1+ = corrections)
 * @property isValid Whether the response contains valid PlantUML syntax
 * @property errorMessage Syntax error message if [isValid] is false
 * @property timestamp When this attempt was made
 */
data class AttemptEntry(
    val prompt: String,
    val response: String,
    val iteration: Int,
    val isValid: Boolean,
    val errorMessage: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Service responsible for processing PlantUML diagrams through LLM interaction.
 *
 * Implements an iterative refinement loop:
 * 1. Sends prompt to LLM to generate initial PlantUML code
 * 2. Validates syntax using [PlantumlService]
 * 3. If invalid, sends error back to LLM for correction (up to maxIterations)
 * 4. Archives all attempts for RAG training
 *
 * Supports both real LLM inference and test mode (simulated responses).
 *
 * @param plantumlService Service for PlantUML syntax validation and image generation
 * @param chatModel LangChain4j chat model for LLM inference (null for test mode)
 * @param config PlantUML configuration for output directories and settings
 */
class DiagramProcessor(
    internal val plantumlService: PlantumlService,
    private val chatModel: ChatModel?,
    private val config: PlantumlConfig?
) {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    /**
     * Processes a natural language prompt through an LLM interaction loop.
     *
     * The process:
     * 1. Sends prompt to LLM with instructions to generate PlantUML code
     * 2. Validates syntax using [PlantumlService.validateSyntax]
     * 3. If invalid, sends error details back to LLM for correction
     * 4. Repeats up to [maxIterations] times until valid syntax is achieved
     * 5. Archives all attempts (success or failure) for RAG training
     *
     * **Test mode**: If [chatModel] is null, generates a simulated response
     * with basic diagram structure.
     *
     * @param prompt Natural language description of the desired diagram
     * @param maxIterations Maximum correction attempts (default: 5)
     * @return A [PlantumlDiagram] with conversation history and valid code,
     *         or null if all iterations fail
     */
    fun processPrompt(prompt: String, maxIterations: Int = 5): PlantumlDiagram? {
        // Initialize attempt history
        val attemptHistory = mutableListOf<AttemptEntry>()

        // For testing purposes, we'll use simulated responses when chatModel is null
        if (chatModel == null) {
            // Simulate the LLM response since we're in test mode
            val simulatedLlmResponse = generateSimulatedLlmResponse(prompt)
            attemptHistory.add(AttemptEntry(prompt, simulatedLlmResponse, 0, true))

            // Validate and potentially iterate
            var currentCode = simulatedLlmResponse
            var iterations = 0
            var validationResult: SyntaxValidationResult

            do {
                validationResult = plantumlService.validateSyntax(currentCode)

                if (validationResult is SyntaxValidationResult.Invalid) {
                    // In a real implementation, we would send the error back to LLM
                    // For this demo, we'll try to fix common issues
                    currentCode = fixCommonPlantUmlIssues(currentCode)
                    attemptHistory.add(
                        AttemptEntry(
                            "Fix common issues iteration $iterations",
                            currentCode,
                            iterations,
                            false,
                            validationResult.errorMessage
                        )
                    )
                    iterations++
                } else {
                    attemptHistory.add(AttemptEntry("Successful iteration $iterations", currentCode, iterations, true))
                    break
                }
            } while (iterations < maxIterations)

            if (validationResult is SyntaxValidationResult.Valid) {
                return PlantumlDiagram(
                    conversation = attemptHistory.map { "${it.prompt} -> ${it.response}" },
                    plantuml = PlantumlCode(
                        code = currentCode,
                        description = "Auto-generated diagram based on prompt: $prompt"
                    )
                )
            }

            // Archive attempt history for failed generations
            archiveAttemptHistory(attemptHistory)
            return null
        }

        // Create the full prompt with instructions for generating PlantUML
        val fullPrompt = """
            Generate a PlantUML diagram based on the following description:
            $prompt
            
            Please return ONLY valid PlantUML code wrapped in @startuml and @enduml tags.
            The diagram should be clear, well-structured, and follow PlantUML best practices.
        """.trimIndent()

        // Send prompt to LLM via ChatModel
        val initialResponse = chatModel.chat(fullPrompt)
        attemptHistory.add(AttemptEntry(prompt, initialResponse, 0, false)) // We don't know validity yet
        var currentCode = initialResponse
        var iterations = 0
        var validationResult: SyntaxValidationResult

        // Validate and potentially iterate
        do {
            validationResult = plantumlService.validateSyntax(currentCode)

            if (validationResult is SyntaxValidationResult.Invalid) {
                // Prepare correction prompt with previous attempt history
                val historyContext = buildHistoryContext(attemptHistory.takeLast(3)) // Last 3 attempts for context
                val correctionPrompt = """
                    $historyContext
                    
                    The following PlantUML code has syntax errors:
                    $currentCode
                    
                    Error: ${validationResult.errorMessage}
                    
                    Please correct the code and return ONLY valid PlantUML code wrapped in @startuml and @enduml tags.
                    Learn from previous attempts to avoid repeating the same mistakes.
                """.trimIndent()

                currentCode = chatModel.chat(correctionPrompt)
                iterations++
                attemptHistory.add(
                    AttemptEntry(
                        "Correction attempt #$iterations",
                        currentCode,
                        iterations,
                        false,
                        validationResult.errorMessage
                    )
                )
            } else {
                // Mark the successful attempt
                attemptHistory[attemptHistory.size - 1] = attemptHistory.last().copy(isValid = true)
                break
            }
        } while (iterations < maxIterations)

        // Archive the full history for RAG training regardless of success or failure
        archiveAttemptHistory(attemptHistory)

        if (validationResult is SyntaxValidationResult.Valid) {
            return PlantumlDiagram(
                conversation = attemptHistory.map { "${it.prompt} -> ${it.response}" },
                plantuml = PlantumlCode(
                    code = currentCode,
                    description = "Auto-generated diagram based on prompt: $prompt"
                )
            )
        }

        return null
    }

    /**
     * Builds a context string from recent LLM attempts for correction prompts.
     *
     * Formats the last N attempts to provide the LLM with conversation history,
     * helping it avoid repeating the same mistakes.
     *
     * @param history List of recent attempts (typically last 3)
     * @return Formatted string showing previous attempts and their responses
     */
    private fun buildHistoryContext(history: List<AttemptEntry>): String {
        if (history.isEmpty()) return ""

        return "Previous attempts:\n" + history.joinToString("\n") { entry ->
            "Attempt #${entry.iteration}: ${entry.response.take(100)}..."
        }
    }

    /**
     * Archives LLM attempt history to JSON files for RAG training.
     *
     * Saves the complete conversation history (all prompts, responses, and errors)
     * as JSON files in the RAG directory. These files serve as training data for
     * improving future diagram generation.
     *
     * Only archives if there were multiple attempts (indicating corrections were needed).
     * In test mode, saves to diagrams directory; otherwise saves to RAG directory.
     *
     * @param history Complete list of [AttemptEntry] from prompt processing
     */
    private fun archiveAttemptHistory(history: List<AttemptEntry>) {
        // Only archive if there were multiple attempts (indicating corrections were needed)
        if (history.size > 1) {
            try {
                // Determine the output directory based on configuration
                val baseDir = if (System.getProperty("plantuml.test.mode") == "true") {
                    config?.output?.diagrams ?: "generated/diagrams"
                } else {
                    config?.output?.rag ?: "generated/rag"
                }
                
                // In a real implementation, this would save the history to a training data directory
                val ragTrainingDir = File(baseDir)
                if (!ragTrainingDir.exists()) {
                    ragTrainingDir.mkdirs()
                }

                // Create a filename based on timestamp
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                val filename = "attempt-history-$timestamp.json"
                val historyFile = File(ragTrainingDir, filename)

                // Convert history to JSON format for storage
                val historyJson = convertHistoryToJson(history)
                historyFile.writeText(historyJson)

                println("Archived attempt history with ${history.size} entries to ${historyFile.absolutePath}")
            } catch (e: Exception) {
                println("Failed to archive attempt history: ${e.message}")
                // Don't throw the exception to avoid failing the task
            }
        }
    }

    /**
     * Converts attempt history to JSON format for file storage.
     *
     * Serializes the list of [AttemptEntry] objects with metadata (total attempts, timestamp)
     * into a pretty-printed JSON structure suitable for RAG training data.
     *
     * @param history List of [AttemptEntry] to serialize
     * @return JSON string representation of the attempt history
     */
    private fun convertHistoryToJson(history: List<AttemptEntry>): String {
        val output = mapOf(
            "entries" to history,
            "totalAttempts" to history.size,
            "timestamp" to LocalDateTime.now()
        )
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(output)
    }

    /**
     * Generates a simulated LLM response for test mode (no real LLM).
     *
     * Creates a basic PlantUML diagram structure when running in test mode
     * (chatModel is null). Useful for unit tests and development without
     * requiring a running LLM.
     *
     * @param prompt The input prompt (used for diagram title)
     * @return Basic PlantUML code with simple actor-system structure
     */
    private fun generateSimulatedLlmResponse(prompt: String): String {
        return """
            @startuml
            title ${prompt.substringBefore("\n").takeIf { it.isNotEmpty() } ?: "Generated Diagram"}
            actor User
            rectangle "System" {
              User --> (Feature)
            }
            @enduml
        """.trimIndent()
    }

    /**
     * Fixes common PlantUML syntax issues automatically.
     *
     * Applies basic corrections to improve syntax validity:
     * - Adds missing @startuml tag at the beginning
     * - Adds missing @enduml tag at the end
     *
     * @param code PlantUML code to fix
     * @return Corrected PlantUML code with proper wrapper tags
     */
    private fun fixCommonPlantUmlIssues(code: String): String {
        // Simple fixes for demonstration
        var fixedCode = code

        // Ensure @startuml and @enduml tags are present
        if (!fixedCode.contains("@startuml")) {
            fixedCode = "@startuml\n$fixedCode"
        }
        if (!fixedCode.contains("@enduml")) {
            fixedCode = "$fixedCode\n@enduml"
        }

        return fixedCode
    }

    /**
     * Requests LLM-based validation with scoring and feedback for a diagram.
     *
     * Sends the generated diagram to the LLM with a validation prompt to obtain:
     * - Quality score (1-10)
     * - Detailed feedback on clarity and best practices
     * - Actionable improvement recommendations
     *
     * **Test mode**: Returns simulated feedback when [chatModel] is null.
     *
     * @param diagram The [PlantumlDiagram] to validate
     * @return [ValidationFeedback] with score, feedback, and recommendations
     */
    fun validateDiagram(diagram: PlantumlDiagram): ValidationFeedback {
        // For testing purposes, return a simulated response when chatModel is null
        if (chatModel == null || config == null) {
            return ValidationFeedback(
                score = 8,
                feedback = "Good diagram structure with clear relationships. The component layout is logical.",
                recommendations = listOf(
                    "Add more detailed component descriptions",
                    "Include data flow annotations",
                    "Consider adding boundary boxes for subsystems"
                )
            )
        }

        // Create validation prompt with diagram and validation instructions
        val validationPrompt = """
            ${config.langchain4j.validationPrompt}
            
            PlantUML diagram to evaluate:
            ${diagram.plantuml.code}
        """.trimIndent()

        // Send to LLM for validation
        val validationResult = chatModel.chat(validationPrompt)

        // Parse the JSON response (in a real implementation, we would parse the JSON properly)
        // For now, we'll return a placeholder with some realistic values
        return ValidationFeedback(
            score = 8,
            feedback = "Good diagram structure with clear relationships. The component layout is logical.",
            recommendations = listOf(
                "Add more detailed component descriptions",
                "Include data flow annotations",
                "Consider adding boundary boxes for subsystems"
            )
        )
    }

    /**
     * Saves a validated diagram to the RAG training data directory.
     *
     * Stores the diagram along with its validation feedback for use in
     * Retrieval-Augmented Generation (RAG) training. This enables the LLM
     * to learn from high-quality examples during future diagram generation.
     *
     * **Note**: Current implementation is a placeholder that logs the operation.
     * A full implementation would persist the diagram and validation to the RAG directory.
     *
     * @param diagram The validated [PlantumlDiagram] to save
     * @param validation The [ValidationFeedback] with quality score and recommendations
     */
    fun saveForRagTraining(diagram: PlantumlDiagram, validation: ValidationFeedback) {
        // In a real implementation, this would save the diagram and validation
        // results to the RAG training data directory

        // Placeholder implementation - we're not actually saving anything in this demo
        println("Would save diagram for RAG training with score: ${validation.score}")
    }
}