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
import org.testcontainers.containers.PostgreSQLContainer
import plantuml.PlantumlConfig
import plantuml.PlantumlManager
import java.io.File

enum class RagMode { SIMULATION, DATABASE, TESTCONTAINERS }

/**
 * Gradle task: `reindexPlantumlRag`
 *
 * Rebuilds the RAG index with collected PlantUML diagrams.
 *
 * Usage:
 *   ./gradlew reindexPlantumlRag
 *
 * RAG Mode configuration (priority order):
 *   1. CLI parameter: -Prag.mode=simulation|database|testcontainers
 *   2. Environment variable: RAG_MODE=simulation|database|testcontainers
 *   3. Gradle property: rag.mode=simulation|database|testcontainers
 *   4. Config file: rag.databaseUrl (if set → database, else → simulation)
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

        // Extract CLI parameters from project properties and strip "plantuml." prefix
        val cliParams = project.properties
            .filterKeys { it.startsWith("plantuml.") }
            .mapKeys { it.key.removePrefix("plantuml.") }
            .mapValues { it.value }

        // Load configuration with CLI overrides
        val config = PlantumlManager.Configuration.load(project, cliParams)

        // Determine RAG mode from multiple sources (priority: CLI > env > gradle prop > config)
        val ragMode = determineRagMode(cliParams, config)

        // Load valid PlantUML diagrams from the RAG collection directory
        val ragDir = File(config.output.rag)
        if (!ragDir.exists()) {
            logger.lifecycle("  → No RAG directory found at: ${ragDir.absolutePath}")
            ragDir.mkdirs()
            logger.lifecycle("  → Created RAG directory")
            return
        }

        // Check if the path is actually a directory
        if (!ragDir.isDirectory) {
            val errorMsg = "  ✗ Path exists but is not a directory: ${ragDir.absolutePath}"
            logger.error(errorMsg)
            throw RuntimeException(errorMsg)
        }

        // Check if we can read the directory
        if (!ragDir.canRead()) {
            val errorMsg = "  ✗ Permission denied: Cannot read RAG directory at ${ragDir.absolutePath}"
            logger.error(errorMsg)
            throw RuntimeException(errorMsg)
        }

        val diagramFiles = try {
            ragDir.listFiles { file ->
                file.extension == "puml"
            } ?: emptyArray()
        } catch (e: SecurityException) {
            val errorMsg =
                "  ✗ Permission denied: Cannot access files in RAG directory ${ragDir.absolutePath} - ${e.message}"
            logger.error(errorMsg)
            throw RuntimeException(errorMsg, e)
        } catch (e: Exception) {
            // Handle other potential exceptions like AccessDeniedException
            val errorMsg = "  ✗ Error accessing RAG directory ${ragDir.absolutePath} - ${e.message}"
            logger.error(errorMsg)
            throw RuntimeException(errorMsg, e)
        }

        // Also load attempt history files for training data
        val trainingDirPath = if (System.getProperty("plantuml.test.mode") == "true") {
            config.output.diagrams
        } else {
            config.output.rag
        }
        val trainingDir = File(trainingDirPath)
        val historyFiles = if (trainingDir.exists()) {
            try {
                trainingDir.listFiles { file ->
                    file.extension == "json" && file.name.startsWith("attempt-history")
                } ?: emptyArray()
            } catch (e: SecurityException) {
                val errorMsg =
                    "  ✗ Permission denied: Cannot access training directory ${trainingDir.absolutePath} - ${e.message}"
                logger.error(errorMsg)
                throw RuntimeException(errorMsg, e)
            }
        } else {
            emptyArray()
        }

        if (diagramFiles.isEmpty() && historyFiles.isEmpty()) {
            logger.lifecycle("  → No PlantUML diagrams or training data found in RAG directory")
            return
        }

        logger.lifecycle("  → Found ${diagramFiles.size} PlantUML diagrams and ${historyFiles.size} training histories for indexing")

        // Initialize embedding model
        val embeddingModel: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()

        // Initialize document splitter
        val documentSplitter: DocumentSplitter = DocumentSplitters.recursive(300, 0)

        // Execute based on RAG mode
        when (ragMode) {
            RagMode.DATABASE -> {
                executeDatabaseMode(diagramFiles, historyFiles, config, embeddingModel, documentSplitter)
            }
            RagMode.TESTCONTAINERS -> {
                executeTestcontainersMode(diagramFiles, historyFiles, embeddingModel, documentSplitter)
            }
            RagMode.SIMULATION -> {
                simulateIndexing(diagramFiles, historyFiles, embeddingModel, documentSplitter)
            }
        }
    }

    private fun determineRagMode(cliParams: Map<String, Any?>, config: PlantumlConfig): RagMode {
        // Priority 1: CLI parameter (-Prag.mode)
        val cliMode = cliParams["rag.mode"]?.toString()?.lowercase()
        if (cliMode != null) {
            logger.lifecycle("  → RAG mode from CLI parameter: $cliMode")
            return RagMode.valueOf(cliMode.uppercase())
        }

        // Priority 2: Environment variable (RAG_MODE)
        val envMode = System.getenv("RAG_MODE")?.lowercase()
        if (envMode != null) {
            logger.lifecycle("  → RAG mode from environment variable: $envMode")
            return RagMode.valueOf(envMode.uppercase())
        }

        // Priority 3: Gradle property (rag.mode)
        val gradlePropMode = project.findProperty("rag.mode")?.toString()?.lowercase()
        if (gradlePropMode != null) {
            logger.lifecycle("  → RAG mode from Gradle property: $gradlePropMode")
            return RagMode.valueOf(gradlePropMode.uppercase())
        }

        // Priority 4: Config file (if databaseUrl is set → database, else → simulation)
        val useDatabase = config.rag.databaseUrl.isNotBlank() &&
                config.rag.username.isNotBlank() &&
                config.rag.password.isNotBlank()

        return if (useDatabase) {
            logger.lifecycle("  → RAG mode from config file: database")
            RagMode.DATABASE
        } else {
            logger.lifecycle("  → RAG mode from config file: simulation")
            RagMode.SIMULATION
        }
    }

    private fun executeDatabaseMode(
        diagramFiles: Array<File>,
        historyFiles: Array<File>,
        config: PlantumlConfig,
        embeddingModel: EmbeddingModel,
        documentSplitter: DocumentSplitter
    ) {
        logger.lifecycle("  → Using PostgreSQL database for RAG indexing")
        logger.lifecycle("  → Database URL: ${config.rag.databaseUrl}:${config.rag.port}")

        // Initialize PGVector embedding store
        val embeddingStore: EmbeddingStore<TextSegment> = PgVectorEmbeddingStore.builder()
            .host(config.rag.databaseUrl)
            .port(config.rag.port)
            .database("plantuml_rag")
            .user(config.rag.username)
            .password(config.rag.password)
            .table(config.rag.tableName)
            .dimension(384)
            .build()

        indexDiagrams(diagramFiles, historyFiles, embeddingModel, documentSplitter, embeddingStore)

        logger.lifecycle("  ✓ RAG reindexing complete with ${diagramFiles.size} diagrams and ${historyFiles.size} histories")
        logger.lifecycle("  → Embeddings stored in PostgreSQL database")
    }

    private fun executeTestcontainersMode(
        diagramFiles: Array<File>,
        historyFiles: Array<File>,
        embeddingModel: EmbeddingModel,
        documentSplitter: DocumentSplitter
    ) {
        logger.lifecycle("  → Using testcontainers PostgreSQL for RAG indexing")

        val container = org.testcontainers.containers.PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            start()
        }

        logger.lifecycle("  → PostgreSQL container started: ${container.containerId}")
        logger.lifecycle("  → JDBC URL: ${container.jdbcUrl}")

        val embeddingStore: EmbeddingStore<TextSegment> = PgVectorEmbeddingStore.builder()
            .host(container.host)
            .port(container.firstMappedPort)
            .database(container.databaseName)
            .user(container.username)
            .password(container.password)
            .table("embeddings")
            .dimension(384)
            .build()

        indexDiagrams(diagramFiles, historyFiles, embeddingModel, documentSplitter, embeddingStore)

        container.stop()
        logger.lifecycle("  → PostgreSQL container stopped")
        logger.lifecycle("  ✓ RAG reindexing complete with ${diagramFiles.size} diagrams and ${historyFiles.size} histories")
        logger.lifecycle("  → Embeddings stored in testcontainers PostgreSQL")
    }

    private fun indexDiagrams(
        diagramFiles: Array<File>,
        historyFiles: Array<File>,
        embeddingModel: EmbeddingModel,
        documentSplitter: DocumentSplitter,
        embeddingStore: EmbeddingStore<TextSegment>
    ) {
        diagramFiles.forEach { file ->
            logger.lifecycle("    Indexing diagram: ${file.name}")

            val content = file.readText()

            val doc = document(
                content,
                metadata("source", file.name)
                    .put("type", "plantuml")
                    .put("contentType", "diagram")
            )

            val segments = documentSplitter.split(doc)
            logger.lifecycle("      Split into ${segments.size} segments")

            segments.forEach { segment ->
                val embedding: Embedding = embeddingModel.embed(segment.text()).content()
                embeddingStore.add(embedding, segment)
                logger.lifecycle("        Stored embedding for segment: ${segment.text().take(50)}...")
            }
        }

        historyFiles.forEach { file ->
            logger.lifecycle("    Indexing training history: ${file.name}")

            val content = file.readText()

            val doc = document(
                content,
                metadata("source", file.name)
                    .put("type", "training")
                    .put("contentType", "history")
            )

            val segments = documentSplitter.split(doc)
            logger.lifecycle("      Split into ${segments.size} segments")

            segments.forEach { segment ->
                val embedding: Embedding = embeddingModel.embed(segment.text()).content()
                embeddingStore.add(embedding, segment)
                logger.lifecycle("        Stored embedding for segment: ${segment.text().take(50)}...")
            }
        }
    }

    private fun simulateIndexing(
        diagramFiles: Array<File>,
        historyFiles: Array<File>,
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
                metadata("source", file.name).put("type", "plantuml").put("contentType", "diagram")
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

        // Process each history file
        historyFiles.forEach { file ->
            logger.lifecycle("    Indexing training history: ${file.name}")

            // Read the history file
            val content = file.readText()

            // Create document
            val document = document(
                content,
                metadata("source", file.name).put("type", "training").put("contentType", "history")
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

        logger.lifecycle("  ✓ RAG reindexing complete with ${diagramFiles.size} diagrams and ${historyFiles.size} histories")
        logger.lifecycle("  → Note: In a production implementation, embeddings would be stored in a vector database")
        logger.lifecycle("  → To enable actual vector storage, configure PostgreSQL with pgvector extension")
        logger.lifecycle("  → Set database connection properties in plantuml-context.yml")
    }
}


