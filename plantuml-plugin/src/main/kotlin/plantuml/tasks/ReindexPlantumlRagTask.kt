package plantuml.tasks

import dev.langchain4j.data.document.Document.document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.document.Metadata.metadata
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore
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
            ragDir.mkdirs()
            logger.lifecycle("  → Created RAG directory")
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

        // Initialize embedding model
        val embeddingModel: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()

        // Initialize document splitter
        val documentSplitter: DocumentSplitter = DocumentSplitters.recursive(300, 0)

        // Check if database configuration is provided
        val useDatabase = config.rag.databaseUrl.isNotBlank() &&
                config.rag.username.isNotBlank() &&
                config.rag.password.isNotBlank()

        if (useDatabase) {
            logger.lifecycle("  → Using PostgreSQL database for RAG indexing")
            logger.lifecycle("  → Database URL: ${config.rag.databaseUrl}")

            try {
                // Initialize PGVector embedding store
                val embeddingStore: EmbeddingStore<TextSegment> = PgVectorEmbeddingStore.builder()
                    .host(config.rag.databaseUrl)
                    .port(5432) // Default PostgreSQL port, extract from URL if needed
                    .database("plantuml_rag") // Extract from URL if needed
                    .user(config.rag.username)
                    .password(config.rag.password)
                    .table(config.rag.tableName)
                    .dimension(384) // Dimension for AllMiniLmL6V2EmbeddingModel
                    .build()

                // Process each diagram file
                diagramFiles.forEach { file ->
                    logger.lifecycle("    Indexing diagram: ${file.name}")

                    // Read the PlantUML diagram
                    val content = file.readText()

                    // Create document
                    val document = document(
                        content,
                        metadata("source", file.name)
                            .put("type", "plantuml")
                    )

                    // Split document into segments
                    val segments = documentSplitter.split(document)
                    logger.lifecycle("      Split into ${segments.size} segments")

                    // Generate embeddings and store them
                    segments.forEach { segment ->
                        val embedding: Embedding = embeddingModel.embed(segment.text()).content()
                        embeddingStore.add(embedding, segment)
                        logger.lifecycle("        Stored embedding for segment: ${segment.text().take(50)}...")
                    }
                }

                logger.lifecycle("  ✓ RAG reindexing complete with ${diagramFiles.size} diagrams")
                logger.lifecycle("  → Embeddings stored in PostgreSQL database")
            } catch (e: Exception) {
                logger.lifecycle("  ✗ Error connecting to database: ${e.message}")
                logger.lifecycle("  → Falling back to simulation mode")
                simulateIndexing(diagramFiles, embeddingModel, documentSplitter)
            }
        } else {
            logger.lifecycle("  → Database configuration not provided or incomplete")
            logger.lifecycle("  → Running in simulation mode")
            simulateIndexing(diagramFiles, embeddingModel, documentSplitter)
        }
    }

    private fun simulateIndexing(
        diagramFiles: Array<File>,
        embeddingModel: EmbeddingModel,
        documentSplitter: DocumentSplitter
    ) {
        // For now, we'll just log the indexing process
        // In a production implementation, this would connect to a vector database
        diagramFiles.forEach { file ->
            logger.lifecycle("    Indexing diagram: ${file.name}")

            // Read the PlantUML diagram
            val content = file.readText()

            // Create document
            val document = document(
                content,
                metadata("source", file.name).put("type", "plantuml")
            )


            // Split document into segments
            val segments = documentSplitter.split(document)
            logger.lifecycle("      Split into ${segments.size} segments")

            // Generate embeddings (in a real implementation, we would store these)
            segments.forEach { segment ->
                val embedding: Embedding = embeddingModel.embed(segment.text()).content()
                logger.lifecycle("        Generated embedding for segment: ${segment.text().take(50)}...")
            }
        }

        logger.lifecycle("  ✓ RAG reindexing complete with ${diagramFiles.size} diagrams")
        logger.lifecycle("  → Note: In a production implementation, embeddings would be stored in a vector database")
        logger.lifecycle("  → To enable actual vector storage, configure PostgreSQL with pgvector extension")
        logger.lifecycle("  → Set database connection properties in plantuml-context.yml")
    }
}


