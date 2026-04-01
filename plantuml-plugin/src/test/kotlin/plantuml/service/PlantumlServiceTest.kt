package plantuml.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class PlantumlServiceTest {

    private lateinit var plantumlService: PlantumlService
    
    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        plantumlService = PlantumlService()
    }

    @Test
    fun `should validate valid plantuml syntax`() {
        // Given
        val validPlantuml = """
            @startuml
            actor User
            rectangle "System" {
              User --> (Feature)
            }
            @enduml
        """.trimIndent()

        // When
        val result = plantumlService.validateSyntax(validPlantuml)

        // Then
        assertTrue(result is PlantumlService.SyntaxValidationResult.Valid)
    }

    @Test
    fun `should reject invalid plantuml syntax`() {
        // Given
        val invalidPlantuml = """
            actor User
            rectangle "System" {
              User --> (Feature)
            }
        """.trimIndent()

        // When
        val result = plantumlService.validateSyntax(invalidPlantuml)

        // Then
        assertTrue(result is PlantumlService.SyntaxValidationResult.Invalid)
    }

    @Test
    fun `should generate png image from valid code`() {
        // Given
        val validPlantuml = """
            @startuml
            actor User
            rectangle "System" {
              User --> (Feature)
            }
            @enduml
        """.trimIndent()
        val outputFile = File(tempDir, "test.png")

        // When
        plantumlService.generateImage(validPlantuml, outputFile)

        // Then
        assertTrue(outputFile.exists())
        // Le fichier devrait contenir des données (pas vide)
        assertTrue(outputFile.length() > 0)
    }

    @Test
    fun `should handle plantuml generation errors`() {
        // Given
        val invalidPlantuml = "invalid plantuml code"
        val outputFile = File(tempDir, "error.txt")

        // When
        plantumlService.generateImage(invalidPlantuml, outputFile)

        // Then
        assertTrue(outputFile.exists())
        // En cas d'erreur, on génère un fichier texte avec l'erreur
        assertTrue(outputFile.readText().contains("Error:"))
    }
}