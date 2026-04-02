package plantuml.service

import java.io.File

fun main() {
    val plantumlService = PlantumlService()
    val tempDir = File("/tmp")
    val invalidPlantuml = "invalid plantuml code"
    val outputFile = File(tempDir, "error.txt")
    
    plantumlService.generateImage(invalidPlantuml, outputFile)
    
    println("File content:")
    println(outputFile.readText())
    println("File exists: ${outputFile.exists()}")
    println("File length: ${outputFile.length()}")
}