import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

fun main() {
    // Create a temporary directory for the test
    val testProjectDir = File("test-project")
    testProjectDir.mkdirs()
    
    // Create build.gradle.kts
    val buildFile = File(testProjectDir, "build.gradle.kts")
    buildFile.writeText("""
        plugins {
            id("com.cheroliv.plantuml")
        }
    """.trimIndent())
    
    // Create settings.gradle.kts
    val settingsFile = File(testProjectDir, "settings.gradle.kts")
    settingsFile.writeText("""
        rootProject.name = "plantuml-rag-test"
    """.trimIndent())
    
    // Create RAG directory with empty files
    val ragDir = File(testProjectDir, "generated/rag")
    ragDir.mkdirs()
    
    // Create empty diagram file
    val emptyDiagram = File(ragDir, "empty.puml")
    emptyDiagram.writeText("")

    // Create valid diagram file
    val validDiagram = File(ragDir, "valid.puml")
    validDiagram.writeText("@startuml\nclass Valid\n@enduml")
    
    // Run the task and capture output
    try {
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()
        
        println("Task outcome: ${result.task(":reindexPlantumlRag")?.outcome}")
        println("Output:")
        println(result.output)
    } catch (e: Exception) {
        println("Exception: ${e.message}")
        e.printStackTrace()
    }
    
    // Clean up
    testProjectDir.deleteRecursively()
}