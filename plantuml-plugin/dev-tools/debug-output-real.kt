import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun main() {
    // Create a temporary directory for the test
    val testProjectDir = File("test-project-real")
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
    
    // Create an empty RAG directory
    val ragDir = File(testProjectDir, "generated/rag")
    ragDir.mkdirs()
    
    // Run the task and capture output
    try {
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()
        
        println("=== Output for empty directory ===")
        println(result.output)
        println("==================================")
    } catch (e: Exception) {
        println("Exception for empty directory: ${e.message}")
        e.printStackTrace()
    }
    
    // Create a RAG directory with one file
    val diagramFile = File(ragDir, "test.puml")
    diagramFile.writeText("@startuml\nclass Test\n@enduml")
    
    // Run the task and capture output
    try {
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("reindexPlantumlRag", "--stacktrace")
            .withPluginClasspath()
            .build()
        
        println("=== Output for directory with one file ===")
        println(result.output)
        println("==========================================")
    } catch (e: Exception) {
        println("Exception for directory with one file: ${e.message}")
        e.printStackTrace()
    }
    
    // Clean up
    testProjectDir.deleteRecursively()
}