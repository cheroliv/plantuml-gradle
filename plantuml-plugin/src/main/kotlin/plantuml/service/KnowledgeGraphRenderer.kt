package plantuml.service

import plantuml.EdgeType
import plantuml.KnowledgeGraph
import plantuml.KnowledgeGraphCommunity
import plantuml.KnowledgeGraphEdge
import plantuml.KnowledgeGraphNode

class KnowledgeGraphRenderer {

    private val communityColors = listOf(
        "#E8F4FD", "#FFF3CD", "#D4EDDA", "#F8D7DA", "#D6D8DB",
        "#CCE5FF", "#FFF0F0", "#E8F5E9", "#F3E5F5", "#FFFACD",
        "#E0F7FA", "#FBE9E7", "#F1F8E9", "#EDE7F6", "#E1F5FE"
    )

    private val communityBorderColors = listOf(
        "#3B82F6", "#F59E0B", "#10B981", "#EF4444", "#6B7280",
        "#2563EB", "#DC2626", "#059669", "#8B5CF6", "#D97706",
        "#0891B2", "#EA580C", "#65A30D", "#7C3AED", "#0284C7"
    )

    fun render(
        graph: KnowledgeGraph,
        communityFilter: String? = null,
        edgeTypes: Set<EdgeType> = EdgeType.entries.toSet(),
        minConfidence: Double = 0.0,
        maxNodes: Int = Int.MAX_VALUE
    ): String {
        val filteredGraph = applyFilters(graph, communityFilter, edgeTypes, minConfidence, maxNodes)

        if (filteredGraph.communities.isEmpty() && filteredGraph.nodes.isEmpty()) {
            return "@startuml\ntitle Empty Knowledge Graph\n@enduml"
        }

        val sb = StringBuilder()
        sb.appendLine("@startuml")
        sb.appendLine()
        sb.appendLine("skinparam backgroundColor #FEFEFE")
        sb.appendLine("skinparam packageBorderColor #333333")
        sb.appendLine("skinparam packageFontSize 14")
        sb.appendLine("skinparam nodeFontSize 12")
        sb.appendLine("skinparam arrowThickness 1.5")
        sb.appendLine()

        addLegend(sb, edgeTypes, communityFilter)

        val nodesByCommunity = filteredGraph.nodes.groupBy { it.community }
        val communityIdx = mutableMapOf<String, Int>()
        var colorIdx = 0

        val communitiesToRender = if (filteredGraph.communities.isNotEmpty()) {
            filteredGraph.communities
        } else {
            nodesByCommunity.keys.map { communityName ->
                val commNodes = nodesByCommunity[communityName] ?: emptyList()
                KnowledgeGraphCommunity(
                    name = communityName,
                    nodes = commNodes.map { it.name },
                    edges = filteredGraph.edges.filter { it.source in commNodes.map { n -> n.name } || it.target in commNodes.map { n -> n.name } }
                )
            }
        }

        for ((idx, community) in communitiesToRender.withIndex()) {
            val colorPair = if (community.color.isNotBlank()) {
                community.color to community.color
            } else {
                communityColors[idx % communityColors.size] to communityBorderColors[idx % communityBorderColors.size]
            }

            communityIdx[community.name] = idx
            val bgColor = colorPair.first
            val borderColor = colorPair.second

            sb.appendLine("package \"${community.name}\" {")
            sb.appendLine("    skinparam packageBackgroundColor $bgColor")
            sb.appendLine("    skinparam packageBorderColor $borderColor")

            val communityNodeNames = community.nodes.ifEmpty {
                nodesByCommunity[community.name]?.map { it.name } ?: emptyList()
            }

            val limitedNodes = communityNodeNames.take(maxNodes)
            for (nodeName in limitedNodes) {
                val nodeInfo = filteredGraph.nodes.find { it.name == nodeName }
                if (nodeInfo != null) {
                    renderNode(sb, nodeInfo)
                } else {
                    sb.appendLine("    node \"$nodeName\" as ${sanitizeId(nodeName)}")
                }
            }

            sb.appendLine("}")
            sb.appendLine()
        }

        val allRenderedNodes = communitiesToRender
            .flatMap { c ->
                c.nodes.ifEmpty {
                    nodesByCommunity[c.name]?.map { it.name } ?: emptyList()
                }
            }
            .take(maxNodes)
            .toSet()

        val renderedEdges = filteredGraph.edges.filter {
            it.source in allRenderedNodes && it.target in allRenderedNodes
        }

        val crossCommunityEdges = renderedEdges.filter { edge ->
            val sourceCommunity = communitiesToRender.find { c ->
                val cnodes = c.nodes.ifEmpty { nodesByCommunity[c.name]?.map { it.name } ?: emptyList() }
                edge.source in cnodes
            }
            val targetCommunity = communitiesToRender.find { c ->
                val cnodes = c.nodes.ifEmpty { nodesByCommunity[c.name]?.map { it.name } ?: emptyList() }
                edge.target in cnodes
            }
            sourceCommunity != null && targetCommunity != null && sourceCommunity != targetCommunity
        }

        if (crossCommunityEdges.isNotEmpty()) {
            sb.appendLine("' Cross-community edges")
            for (edge in crossCommunityEdges) {
                renderEdge(sb, edge)
            }
            sb.appendLine()
        }

        val intraCommunityEdges = renderedEdges.filter { edge ->
            edge !in crossCommunityEdges
        }

        if (intraCommunityEdges.isNotEmpty()) {
            sb.appendLine("' Intra-community edges")
            for (edge in intraCommunityEdges) {
                renderEdge(sb, edge)
            }
            sb.appendLine()
        }

        val unassignedNodes = filteredGraph.nodes.filter { it.name !in allRenderedNodes }
        if (unassignedNodes.isNotEmpty()) {
            sb.appendLine("' Unassigned nodes")
            for (node in unassignedNodes) {
                renderNode(sb, node)
            }
            sb.appendLine()
        }

        sb.appendLine("@enduml")
        return sb.toString()
    }

    private fun applyFilters(
        graph: KnowledgeGraph,
        communityFilter: String?,
        edgeTypes: Set<EdgeType>,
        minConfidence: Double,
        maxNodes: Int
    ): KnowledgeGraph {
        val filteredCommunities = if (communityFilter != null) {
            graph.communities.filter {
                it.name.contains(communityFilter, ignoreCase = true)
            }
        } else {
            graph.communities
        }

        val communityNames = filteredCommunities.map { it.name }.toSet()

        val filteredNodesByCommunity = if (communityFilter != null) {
            val communityNodes = filteredCommunities.flatMap { it.nodes }.toSet()
            graph.nodes.filter { it.name in communityNodes || it.community in communityNames }
        } else {
            graph.nodes
        }

        val limitedNodes = filteredNodesByCommunity.take(maxNodes)
        val nodeNames = limitedNodes.map { it.name }.toSet()

        val filteredEdges = graph.edges
            .filter { it.type in edgeTypes }
            .filter { it.confidence >= minConfidence }
            .filter { it.source in nodeNames || it.target in nodeNames }

        return KnowledgeGraph(
            nodes = limitedNodes,
            edges = filteredEdges,
            communities = filteredCommunities
        )
    }

    private fun renderNode(sb: StringBuilder, node: KnowledgeGraphNode) {
        val id = sanitizeId(node.name)
        if (node.attributes.isNotEmpty()) {
            sb.appendLine("    node \"${node.name}\" as $id {")
            for (attr in node.attributes.take(5)) {
                sb.appendLine("        $attr")
            }
            sb.appendLine("    }")
        } else {
            sb.appendLine("    node \"${node.name}\" as $id")
        }
    }

    private fun renderEdge(sb: StringBuilder, edge: KnowledgeGraphEdge) {
        val sourceId = sanitizeId(edge.source)
        val targetId = sanitizeId(edge.target)
        val label = if (edge.label.isNotBlank()) " : ${edge.label}" else ""
        val confidenceLabel = if (edge.type == EdgeType.INFERRED && edge.confidence < 1.0) {
            " (${String.format("%.0f%%", edge.confidence * 100)})"
        } else ""

        when (edge.type) {
            EdgeType.EXTRACTED -> sb.appendLine("$sourceId --> $targetId$label$confidenceLabel")
            EdgeType.INFERRED -> sb.appendLine("$sourceId ..> $targetId$label$confidenceLabel")
            EdgeType.AMBIGUOUS -> sb.appendLine("$sourceId --x $targetId$label")
        }
    }

    private fun addLegend(sb: StringBuilder, edgeTypes: Set<EdgeType>, communityFilter: String?) {
        sb.appendLine("legend right")
        sb.appendLine("  | Type | Arrow |")
        sb.appendLine("  |------|-------|")
        if (EdgeType.EXTRACTED in edgeTypes) {
            sb.appendLine("  | Extracted | --> |")
        }
        if (EdgeType.INFERRED in edgeTypes) {
            sb.appendLine("  | Inferred  | ..> |")
        }
        if (EdgeType.AMBIGUOUS in edgeTypes) {
            sb.appendLine("  | Ambiguous | --x |")
        }
        if (communityFilter != null) {
            sb.appendLine("  | Filter | $communityFilter |")
        }
        sb.appendLine("endlegend")
        sb.appendLine()
    }

    fun sanitizeId(name: String): String {
        val sanitized = name.replace(" ", "_")
            .replace(".", "_")
            .replace("(", "")
            .replace(")", "")
            .replace("[", "_")
            .replace("]", "")
            .replace("/", "_")
            .replace("\\", "_")
            .replace("-", "_")
            .replace("\"", "")
            .replace("'", "")
        return if (sanitized.first().isDigit()) "_$sanitized" else sanitized
    }
}