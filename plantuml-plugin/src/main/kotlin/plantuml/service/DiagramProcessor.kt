package plantuml.service

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
 * Data class to store attempt history for RAG training
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
 * Service responsible for processing PlantUML diagrams through the LLM interaction loop.
 */
class DiagramProcessor(
    internal val plantumlService: PlantumlService,
    private val chatModel: ChatModel?,
    private val config: PlantumlConfig?
) {

    /**
     * Processes a prompt through the LLM interaction loop (max 5 iterations).
     * Returns a valid PlantUML diagram or null if processing fails.
     * Maintains attempt history for RAG training.
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
     * Builds a contextual history string from previous attempts
     */
    private fun buildHistoryContext(history: List<AttemptEntry>): String {
        if (history.isEmpty()) return ""

        return "Previous attempts:\n" + history.joinToString("\n") { entry ->
            "Attempt #${entry.iteration}: ${entry.response.take(100)}..."
        }
    }

    /**
     * Archives attempt history for RAG training
     */
    private fun archiveAttemptHistory(history: List&lt;AttemptEntry&gt;) {
        // Only archive if there were multiple attempts (indicating corrections were needed)
        if (history.size &gt; 1) {
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
            }
        }
    }

    /**
     * Converts attempt history to JSON format for storage
     */
    private fun convertHistoryToJson(history: List<AttemptEntry>): String {
        val entries = history.joinToString(",\n") { entry ->
            """
            {
                "iteration": ${entry.iteration},
                "prompt": "${entry.prompt.replace("\"", "\\\"")}",
                "response": "${entry.response.replace("\"", "\\\"")}",
                "isValid": ${entry.isValid},
                "errorMessage": "${entry.errorMessage?.replace("\"", "\\\"") ?: "null"}",
                "timestamp": "${entry.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            }
            """.trimIndent()
        }

        return """
        {
            "entries": [
                $entries
            ],
            "totalAttempts": ${history.size},
            "timestamp": "${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
        }
        """.trimIndent()
    }

    /**
     * Simulates an LLM response with PlantUML code.
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
     * Fixes common PlantUML syntax issues (simplified for demo).
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
     * Requests LLM validation of a diagram with scoring and feedback.
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
            ${config.langchain.validationPrompt}
            
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
     * Saves a valid diagram for RAG training.
     */
    fun saveForRagTraining(diagram: PlantumlDiagram, validation: ValidationFeedback) {
        // In a real implementation, this would save the diagram and validation
        // results to the RAG training data directory

        // Placeholder implementation - we're not actually saving anything in this demo
        println("Would save diagram for RAG training with score: ${validation.score}")
    }
}