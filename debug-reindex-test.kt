import java.io.File

fun main() {
    // Test the actual output messages from the ReindexPlantumlRagTask
    val ragDir = File("test-rag-dir")
    ragDir.mkdirs()
    
    // Create test files
    val diagram1 = File(ragDir, "diagram1.puml")
    diagram1.writeText("@startuml\nclass Car\n@enduml")
    
    val diagram2 = File(ragDir, "diagram2.puml")
    diagram2.writeText("@startuml\nactor User\n@enduml")
    
    val invalidDiagram = File(ragDir, "invalid.puml")
    invalidDiagram.writeText("@startuml\nactor User") // Missing @enduml
    
    val emptyDiagram = File(ragDir, "empty.puml")
    emptyDiagram.writeText("")
    
    // Create subdirectory
    val subdir = File(ragDir, "subdir")
    subdir.mkdirs()
    val subDiagram = File(subdir, "sub.puml")
    subDiagram.writeText("@startuml\nclass Sub\n@enduml")
    
    // Count files that would be processed
    val diagramFiles = ragDir.listFiles { file ->
        file.extension == "puml"
    } ?: emptyArray()
    
    println("Found ${diagramFiles.size} PlantUML diagrams for indexing")
    
    // Clean up
    ragDir.deleteRecursively()
}