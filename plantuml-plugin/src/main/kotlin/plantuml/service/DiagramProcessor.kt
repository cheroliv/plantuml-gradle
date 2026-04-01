package plantuml.service

import plantuml.PlantumlCode
import plantuml.PlantumlDiagram
import plantuml.ValidationFeedback
import plantuml.service.PlantumlService.SyntaxValidationResult

/**
 * Service responsible for processing PlantUML diagrams through the LLM interaction loop.
 */
class DiagramProcessor(private val plantumlService: PlantumlService) {
    
    /**
     * Processes a prompt through the LLM interaction loop (max 5 iterations).
     * Returns a valid PlantUML diagram or null if processing fails.
     */
    fun processPrompt(prompt: String, maxIterations: Int = 5): PlantumlDiagram? {
        // In a real implementation with LangChain4j, this would:
        // 1. Send prompt to LLM via ChatLanguageModel
        // 2. Parse JSON response containing PlantUML code
        // 3. Validate syntax using PlantumlService
        // 4. If invalid, send error back to LLM for correction
        // 5. Repeat up to maxIterations
        
        // For demonstration purposes, we'll simulate the LLM response
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
        // In a real implementation, this would send the diagram to the LLM
        // for quality assessment and receive scoring and feedback
        
        // Placeholder implementation with realistic feedback
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