package plantuml

enum class EdgeType {
    EXTRACTED,
    INFERRED,
    AMBIGUOUS
}

data class KnowledgeGraphEdge(
    val source: String,
    val target: String,
    val label: String = "",
    val type: EdgeType = EdgeType.EXTRACTED,
    val confidence: Double = 1.0
)

data class KnowledgeGraphNode(
    val name: String,
    val type: String = "class",
    val community: String = "",
    val attributes: List<String> = emptyList()
)

data class KnowledgeGraphCommunity(
    val name: String,
    val color: String = "",
    val nodes: List<String> = emptyList(),
    val edges: List<KnowledgeGraphEdge> = emptyList()
)

data class KnowledgeGraph(
    val nodes: List<KnowledgeGraphNode> = emptyList(),
    val edges: List<KnowledgeGraphEdge> = emptyList(),
    val communities: List<KnowledgeGraphCommunity> = emptyList()
)