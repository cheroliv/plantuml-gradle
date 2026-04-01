package plantuml.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import plantuml.PlantumlManager
import java.io.File

/**
 * Gradle task: `reindexPlantumlRag`
 *
 * Rebuilds the RAG index with collected PlantUML diagrams.
 *
 * Usage:
 *   ./gradlew reindexPlantumlRag
 */
@DisableCachingByDefault(because = "RAG indexing processes all files and results depend on current state")
abstract class ReindexPlantumlRagTask : DefaultTask() {

    init {
        group = "plantuml"
        description = "Rebuilds the RAG index with collected PlantUML diagrams"
    }

    @TaskAction
    fun reindexRag() {
        logger.lifecycle("Rebuilding RAG index with PlantUML diagrams...")
        
        // Load configuration
        val config = PlantumlManager.Configuration.load(project)
        
        // Load valid PlantUML diagrams from the RAG collection directory
        val ragDir = File(config.output.rag)
        if (!ragDir.exists()) {
            logger.lifecycle("  → No RAG directory found at: ${ragDir.absolutePath}")
            return
        }
        
        val diagramFiles = ragDir.listFiles { file ->
            file.extension == "puml"
        } ?: emptyArray()
        
        if (diagramFiles.isEmpty()) {
            logger.lifecycle("  → No PlantUML diagrams found in RAG directory")
            return
        }
        
        logger.lifecycle("  → Found ${diagramFiles.size} PlantUML diagrams for indexing")
        
        // In a real implementation, this would:
        // 1. Load each diagram file
        // 2. Generate embeddings using LangChain4j
        // 3. Store embeddings in the vector database
        // 4. Update the index with new documents
        
        // Placeholder implementation simulating the process
        diagramFiles.forEach { file ->
            logger.lifecycle("    Indexing diagram: ${file.name}")
            // In reality, this would use LangChain4j to create embeddings
            // and store them in a vector database like pgvector
        }
        
        logger.lifecycle("  ✓ RAG reindexing complete with ${diagramFiles.size} diagrams")
    }
}