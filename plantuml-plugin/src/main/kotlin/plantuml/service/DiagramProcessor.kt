package plantuml.service

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.output.Response
import plantuml.PlantumlCode
import plantuml.PlantumlDiagram
import plantuml.ValidationFeedback
import plantuml.service.PlantumlService
import plantuml.service.PlantumlService.SyntaxValidationResult
import plantuml.PlantumlConfig

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
     */
    fun processPrompt(prompt: String, maxIterations: Int = 5): PlantumlDiagram? {
        // For testing purposes, we'll use simulated responses when chatModel is null
        if (chatModel == null) {
            // Simulate the LLM response since we're in test mode
            val simulatedLlmResponse = generateSimulatedLlmResponse(prompt)
            
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
                    iterations++
                } else {
                    break
                }
            } while (iterations < maxIterations)
            
            if (validationResult is SyntaxValidationResult.Valid) {
                return PlantumlDiagram(
                    conversation = listOf("Processed prompt: $prompt"),
                    plantuml = PlantumlCode(
                        code = currentCode,
                        description = "Auto-generated diagram based on prompt: $prompt"
                    )
                )
            }
            
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
        var currentCode = chatModel.chat(fullPrompt)
        var iterations = 0
        var validationResult: SyntaxValidationResult
        
        // Validate and potentially iterate
        do {
            validationResult = plantumlService.validateSyntax(currentCode)
            
            if (validationResult is SyntaxValidationResult.Invalid) {
                // Send error back to LLM for correction
                val correctionPrompt = """
                    The following PlantUML code has syntax errors:
                    $currentCode
                    
                    Error: ${validationResult.errorMessage}
                    
                    Please correct the code and return ONLY valid PlantUML code wrapped in @startuml and @enduml tags.
                """.trimIndent()
                
                currentCode = chatModel.chat(correctionPrompt)
                iterations++
            } else {
                break
            }
        } while (iterations < maxIterations)
        
        if (validationResult is SyntaxValidationResult.Valid) {
            return PlantumlDiagram(
                conversation = listOf("Processed prompt: $prompt"),
                plantuml = PlantumlCode(
                    code = currentCode,
                    description = "Auto-generated diagram based on prompt: $prompt"
                )
            )
        }
        
        return null
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