package plantuml.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import plantuml.EdgeType
import plantuml.KnowledgeGraph
import plantuml.KnowledgeGraphCommunity
import plantuml.KnowledgeGraphEdge
import plantuml.KnowledgeGraphNode
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class KnowledgeGraphRendererTest {

    private lateinit var renderer: KnowledgeGraphRenderer

    @BeforeEach
    fun setUp() {
        renderer = KnowledgeGraphRenderer()
    }

    @Test
    fun `should render empty graph`() {
        val graph = KnowledgeGraph()
        val result = renderer.render(graph)
        assertTrue(result.contains("@startuml"))
        assertTrue(result.contains("@enduml"))
        assertTrue(result.contains("Empty Knowledge Graph"))
    }

    @Test
    fun `should render graph with single community`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(name = "LlmService", community = "service layer"),
                KnowledgeGraphNode(name = "DiagramProcessor", community = "service layer")
            ),
            edges = listOf(
                KnowledgeGraphEdge(
                    source = "LlmService",
                    target = "DiagramProcessor",
                    label = "creates",
                    type = EdgeType.EXTRACTED
                )
            ),
            communities = listOf(
                KnowledgeGraphCommunity(
                    name = "service layer",
                    nodes = listOf("LlmService", "DiagramProcessor"),
                    edges = listOf(
                        KnowledgeGraphEdge(
                            source = "LlmService",
                            target = "DiagramProcessor",
                            label = "creates",
                            type = EdgeType.EXTRACTED
                        )
                    )
                )
            )
        )
        val result = renderer.render(graph)
        assertTrue(result.contains("@startuml"))
        assertTrue(result.contains("service layer"))
        assertTrue(result.contains("LlmService"))
        assertTrue(result.contains("DiagramProcessor"))
        assertTrue(result.contains("-->"))
        assertTrue(result.contains("creates"))
        assertTrue(result.contains("@enduml"))
    }

    @Test
    fun `should render EXTRACTED edges with solid arrow`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(name = "A", community = "test"),
                KnowledgeGraphNode(name = "B", community = "test")
            ),
            edges = listOf(
                KnowledgeGraphEdge(source = "A", target = "B", type = EdgeType.EXTRACTED)
            ),
            communities = listOf(
                KnowledgeGraphCommunity(name = "test", nodes = listOf("A", "B"))
            )
        )
        val result = renderer.render(graph)
        assertTrue(result.contains("A --> B"))
    }

    @Test
    fun `should render INFERRED edges with dashed arrow`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(name = "A", community = "test"),
                KnowledgeGraphNode(name = "B", community = "test")
            ),
            edges = listOf(
                KnowledgeGraphEdge(source = "A", target = "B", type = EdgeType.INFERRED, confidence = 0.8)
            ),
            communities = listOf(
                KnowledgeGraphCommunity(name = "test", nodes = listOf("A", "B"))
            )
        )
        val result = renderer.render(graph)
        assertTrue(result.contains("A ..> B"))
        assertTrue(result.contains("80%"))
    }

    @Test
    fun `should render AMBIGUOUS edges with crossed arrow`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(name = "A", community = "test"),
                KnowledgeGraphNode(name = "B", community = "test")
            ),
            edges = listOf(
                KnowledgeGraphEdge(source = "A", target = "B", type = EdgeType.AMBIGUOUS)
            ),
            communities = listOf(
                KnowledgeGraphCommunity(name = "test", nodes = listOf("A", "B"))
            )
        )
        val result = renderer.render(graph)
        assertTrue(result.contains("A --x B"))
    }

    @Test
    fun `should filter by community`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(name = "AlphaNode", community = "service layer"),
                KnowledgeGraphNode(name = "BetaNode", community = "config layer")
            ),
            communities = listOf(
                KnowledgeGraphCommunity(name = "service layer", nodes = listOf("AlphaNode")),
                KnowledgeGraphCommunity(name = "config layer", nodes = listOf("BetaNode"))
            )
        )
        val result = renderer.render(graph, communityFilter = "service")
        assertTrue(result.contains("service layer"))
        assertTrue(result.contains("AlphaNode"))
        assertFalse(result.contains("config layer"))
        assertFalse(result.contains("BetaNode"))
    }

    @Test
    fun `should filter by edge type`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(name = "A", community = "test"),
                KnowledgeGraphNode(name = "B", community = "test"),
                KnowledgeGraphNode(name = "C", community = "test")
            ),
            edges = listOf(
                KnowledgeGraphEdge(source = "A", target = "B", type = EdgeType.EXTRACTED),
                KnowledgeGraphEdge(source = "A", target = "C", type = EdgeType.INFERRED)
            ),
            communities = listOf(
                KnowledgeGraphCommunity(name = "test", nodes = listOf("A", "B", "C"))
            )
        )
        val result = renderer.render(graph, edgeTypes = setOf(EdgeType.EXTRACTED))
        assertTrue(result.contains("A --> B"))
        assertFalse(result.contains("A ..> C"))
    }

    @Test
    fun `should filter by minimum confidence`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(name = "A", community = "test"),
                KnowledgeGraphNode(name = "B", community = "test"),
                KnowledgeGraphNode(name = "C", community = "test")
            ),
            edges = listOf(
                KnowledgeGraphEdge(source = "A", target = "B", type = EdgeType.INFERRED, confidence = 0.9),
                KnowledgeGraphEdge(source = "A", target = "C", type = EdgeType.INFERRED, confidence = 0.3)
            ),
            communities = listOf(
                KnowledgeGraphCommunity(name = "test", nodes = listOf("A", "B", "C"))
            )
        )
        val result = renderer.render(graph, minConfidence = 0.7)
        assertTrue(result.contains("A ..> B"))
        assertFalse(result.contains("0.3"))
    }

    @Test
    fun `should limit number of nodes with maxNodes`() {
        val graph = KnowledgeGraph(
            nodes = (1..10).map { KnowledgeGraphNode(name = "Node$it", community = "test") },
            communities = listOf(
                KnowledgeGraphCommunity(name = "test", nodes = (1..10).map { "Node$it" })
            )
        )
        val result = renderer.render(graph, maxNodes = 3)
        assertTrue(result.contains("Node1"))
        assertTrue(result.contains("Node2"))
        assertTrue(result.contains("Node3"))
        assertFalse(result.contains("Node4"))
    }

    @Test
    fun `should render nodes with attributes`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(
                    name = "ApiKeyPool",
                    community = "config layer",
                    type = "class",
                    attributes = listOf("+getNextKey(): ApiKeyEntry", "+resetCounters(): Unit")
                )
            ),
            communities = listOf(
                KnowledgeGraphCommunity(name = "config layer", nodes = listOf("ApiKeyPool"))
            )
        )
        val result = renderer.render(graph)
        assertTrue(result.contains("ApiKeyPool"))
        assertTrue(result.contains("getNextKey"))
        assertTrue(result.contains("resetCounters"))
    }

    @Test
    fun `should render legend with edge types`() {
        val graph = KnowledgeGraph(
            nodes = listOf(KnowledgeGraphNode(name = "A", community = "test")),
            communities = listOf(KnowledgeGraphCommunity(name = "test", nodes = listOf("A")))
        )
        val result = renderer.render(graph)
        assertTrue(result.contains("legend"))
        assertTrue(result.contains("Extracted"))
        assertTrue(result.contains("Inferred"))
        assertTrue(result.contains("Ambiguous"))
    }

    @Test
    fun `should sanitize node IDs`() {
        val result = renderer.sanitizeId("My Class.Name (v2)")
        assertEquals("My_Class_Name_v2", result)
    }

    @Test
    fun `should sanitize node IDs starting with digit`() {
        val result = renderer.sanitizeId("3DRenderer")
        assertTrue(result.startsWith("_"))
    }

    @Test
    fun `should render cross-community edges separately`() {
        val graph = KnowledgeGraph(
            nodes = listOf(
                KnowledgeGraphNode(name = "A", community = "alpha"),
                KnowledgeGraphNode(name = "B", community = "alpha"),
                KnowledgeGraphNode(name = "C", community = "beta")
            ),
            edges = listOf(
                KnowledgeGraphEdge(source = "A", target = "B", type = EdgeType.EXTRACTED, label = "internal"),
                KnowledgeGraphEdge(source = "A", target = "C", type = EdgeType.INFERRED, label = "cross")
            ),
            communities = listOf(
                KnowledgeGraphCommunity(name = "alpha", nodes = listOf("A", "B")),
                KnowledgeGraphCommunity(name = "beta", nodes = listOf("C"))
            )
        )
        val result = renderer.render(graph)
        assertTrue(result.contains("Cross-community edges"))
        assertTrue(result.contains("Intra-community edges"))
    }

    private fun assertFalse(condition: Boolean) {
        org.junit.jupiter.api.Assertions.assertFalse(condition)
    }
}