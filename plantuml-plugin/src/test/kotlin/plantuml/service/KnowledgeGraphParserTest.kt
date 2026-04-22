package plantuml.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class KnowledgeGraphParserTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var graphFile: File
    private lateinit var parser: KnowledgeGraphParser
    private val mapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        graphFile = File(tempDir, "graph.json")
        parser = KnowledgeGraphParser(graphFile)
    }

    @Test
    fun `should parse simple community-based graph`() {
        val json = mapper.writeValueAsString(mapOf(
            "communities" to listOf(
                mapOf(
                    "name" to "service layer",
                    "nodes" to listOf("LlmService", "DiagramProcessor"),
                    "edges" to listOf("LlmService --> DiagramProcessor")
                )
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        assertEquals(2, graph.nodes.size)
        assertEquals(1, graph.edges.size)
        assertEquals(1, graph.communities.size)
        assertEquals("service layer", graph.communities[0].name)
        assertEquals("LlmService", graph.edges[0].source)
        assertEquals("DiagramProcessor", graph.edges[0].target)
    }

    @Test
    fun `should parse rich edge format with type and confidence`() {
        val json = mapper.writeValueAsString(mapOf(
            "communities" to listOf(
                mapOf(
                    "name" to "core",
                    "nodes" to listOf("A", "B", "C"),
                    "edges" to listOf(
                        mapOf(
                            "source" to "A",
                            "target" to "B",
                            "label" to "depends on",
                            "type" to "EXTRACTED",
                            "confidence" to 1.0
                        ),
                        mapOf(
                            "source" to "A",
                            "target" to "C",
                            "label" to "might use",
                            "type" to "INFERRED",
                            "confidence" to 0.75
                        )
                    )
                )
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        assertEquals(2, graph.edges.size)
        val extractedEdge = graph.edges.find { it.source == "A" && it.target == "B" }!!
        assertEquals("depends on", extractedEdge.label)
        assertEquals(1.0, extractedEdge.confidence)

        val inferredEdge = graph.edges.find { it.source == "A" && it.target == "C" }!!
        assertEquals("might use", inferredEdge.label)
        assertEquals(0.75, inferredEdge.confidence)
    }

    @Test
    fun `should parse root-level edges`() {
        val json = mapper.writeValueAsString(mapOf(
            "communities" to listOf(
                mapOf("name" to "alpha", "nodes" to listOf("A"), "edges" to emptyList<String>())
            ),
            "edges" to listOf(
                mapOf(
                    "source" to "A",
                    "target" to "B",
                    "type" to "AMBIGUOUS",
                    "confidence" to 0.5
                )
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        assertEquals(1, graph.edges.size)
        val edge = graph.edges[0]
        assertEquals("A", edge.source)
        assertEquals("B", edge.target)
    }

    @Test
    fun `should parse root-level nodes as objects`() {
        val json = mapper.writeValueAsString(mapOf(
            "nodes" to listOf(
                mapOf(
                    "name" to "LlmService",
                    "type" to "class",
                    "community" to "service layer",
                    "attributes" to listOf("+createModel(): ChatModel", "+getNextKey(): ApiKeyEntry")
                )
            ),
            "communities" to listOf(
                mapOf("name" to "service layer", "nodes" to listOf("LlmService"), "edges" to emptyList<String>())
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        val node = graph.nodes.find { it.name == "LlmService" }!!
        assertEquals("service layer", node.community)
        assertEquals("class", node.type)
        assertEquals(2, node.attributes.size)
        assertTrue(node.attributes[0].contains("createModel"))
    }

    @Test
    fun `should handle graph without communities`() {
        val json = mapper.writeValueAsString(mapOf(
            "nodes" to listOf("A", "B"),
            "edges" to listOf(
                mapOf("source" to "A", "target" to "B", "type" to "EXTRACTED", "confidence" to 1.0)
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        assertEquals(2, graph.nodes.size)
        assertEquals(1, graph.edges.size)
        assertEquals(0, graph.communities.size)
    }

    @Test
    fun `should parse AMBIGUOUS edge type from string`() {
        val json = mapper.writeValueAsString(mapOf(
            "communities" to listOf(
                mapOf(
                    "name" to "test",
                    "nodes" to listOf("X"),
                    "edges" to listOf(
                        mapOf("source" to "X", "target" to "Y", "type" to "AMBIGUOUS", "confidence" to 0.3)
                    )
                )
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        val edge = graph.edges[0]
        assertEquals(plantuml.EdgeType.AMBIGUOUS, edge.type)
        assertEquals(0.3, edge.confidence)
    }

    @Test
    fun `should throw when graph file not found`() {
        val missingFile = File(tempDir, "nonexistent.json")
        val missingParser = KnowledgeGraphParser(missingFile)

        assertFailsWith<IllegalArgumentException> {
            missingParser.parse()
        }
    }

    @Test
    fun `should deduplicate edges`() {
        val json = mapper.writeValueAsString(mapOf(
            "communities" to listOf(
                mapOf(
                    "name" to "core",
                    "nodes" to listOf("A", "B"),
                    "edges" to listOf(
                        mapOf("source" to "A", "target" to "B", "type" to "EXTRACTED", "confidence" to 1.0)
                    )
                )
            ),
            "edges" to listOf(
                mapOf("source" to "A", "target" to "B", "type" to "EXTRACTED", "confidence" to 1.0)
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        assertEquals(1, graph.edges.size)
    }

    @Test
    fun `should deduplicate nodes`() {
        val json = mapper.writeValueAsString(mapOf(
            "communities" to listOf(
                mapOf("name" to "core", "nodes" to listOf("A"), "edges" to emptyList<String>())
            ),
            "nodes" to listOf(
                mapOf("name" to "A", "type" to "class", "community" to "core")
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        assertEquals(1, graph.nodes.size)
    }

    @Test
    fun `should parse multiple communities`() {
        val json = mapper.writeValueAsString(mapOf(
            "communities" to listOf(
                mapOf("name" to "service", "nodes" to listOf("S1", "S2"), "edges" to emptyList<String>()),
                mapOf("name" to "config", "nodes" to listOf("C1", "C2"), "edges" to emptyList<String>()),
                mapOf("name" to "task", "nodes" to listOf("T1"), "edges" to emptyList<String>())
            )
        ))
        graphFile.writeText(json)

        val graph = parser.parse()

        assertEquals(3, graph.communities.size)
        assertEquals(5, graph.nodes.size)
        assertEquals("service", graph.communities[0].name)
        assertEquals("config", graph.communities[1].name)
        assertEquals("task", graph.communities[2].name)
    }
}