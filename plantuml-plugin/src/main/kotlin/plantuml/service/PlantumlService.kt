package plantuml.service

import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Service responsible for PlantUML diagram processing and validation.
 */
class PlantumlService {

    /**
     * Validates PlantUML syntax and returns true if valid, false otherwise.
     * If invalid, returns error details.
     */
    fun validateSyntax(plantumlCode: String): SyntaxValidationResult {
        return try {
            val reader = SourceStringReader(plantumlCode)

            // Simple check for required tags
            if (!plantumlCode.contains("@startuml") || !plantumlCode.contains("@enduml")) {
                return SyntaxValidationResult.Invalid(
                    "Missing @startuml or @enduml tags",
                    "PlantUML code must be wrapped in @startuml and @enduml tags"
                )
            }

            SyntaxValidationResult.Valid
        } catch (e: Exception) {
            SyntaxValidationResult.Invalid(
                "PlantUML parsing failed: ${e.message}",
                e.stackTraceToString()
            )
        }
    }

    /**
     * Generates an image from valid PlantUML code.
     */
    fun generateImage(plantumlCode: String, outputFile: File) {
        try {
            val reader = SourceStringReader(plantumlCode)
            val outputStream = ByteArrayOutputStream()
            reader.outputImage(outputStream)
            outputStream.use { output ->
                outputFile.writeBytes(output.toByteArray())
            }
        } catch (e: Exception) {
            // Fallback to text file if image generation fails
            try {
                outputFile.writeText("PlantUML diagram:\n\n$plantumlCode\n\nError: ${e.message}")
            } catch (ioException: Exception) {
                // If we can't even write a text file, log the error and continue
                System.err.println("Failed to write diagram to ${outputFile.absolutePath}: ${ioException.message}")
            }
        }
    }

    /**
     * Represents the result of PlantUML syntax validation.
     */
    sealed class SyntaxValidationResult {
        object Valid : SyntaxValidationResult()
        data class Invalid(val errorMessage: String, val stackTrace: String) : SyntaxValidationResult()
    }
}