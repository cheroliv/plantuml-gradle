package plantuml.service

import plantuml.PlantumlCode
import plantuml.PlantumlDiagram
import plantuml.ValidationFeedback
import java.io.File

/**
 * Service responsible for processing PlantUML diagrams through the LLM interaction loop.
 */
class DiagramProcessor(private val plantumlService: PlantumlService) {
    
    /**
     * Processes a prompt through the LLM interaction loop (max 5 iterations).
     * Returns a valid PlantUML diagram or null if processing fails.
     */
    fun processPrompt(prompt: String, maxIterations: Int = 5): PlantumlDiagram? {
        // In a real implementation, this would:
        // 1. Send prompt to LLM
        // 2. Get PlantUML code response
        // 3. Validate syntax using PlantumlService
        // 4. If invalid, send error back to LLM for correction
        // 5. Repeat up to maxIterations
        
        // For this placeholder implementation, we'll generate a simple diagram
        val plantumlCode = PlantumlCode(
            code = """
                @startuml
                title ${prompt.substringBefore("\n").takeIf { it.isNotEmpty() } ?: "Generated Diagram"}
                actor User
                rectangle "System" {
                  User --> (Feature)
                }
                @enduml
            """.trimIndent(),
            description = "Auto-generated diagram based on prompt: $prompt"
        )
        
        // Validate the generated code
        val validationResult = plantumlService.validateSyntax(plantumlCode.code)
        if (validationResult is PlantumlService.SyntaxValidationResult.Valid) {
            return PlantumlDiagram(
                conversation = listOf("Processed prompt: $prompt"),
                plantuml = plantumlCode
            )
        }
        
        return null
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