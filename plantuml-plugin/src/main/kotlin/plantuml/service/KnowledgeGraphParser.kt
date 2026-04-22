package plantuml.service

import com.fasterxml.jackson.databind.ObjectMapper
import plantuml.EdgeType
import plantuml.KnowledgeGraph
import plantuml.KnowledgeGraphCommunity
import plantuml.KnowledgeGraphEdge
import plantuml.KnowledgeGraphNode
import java.io.File

class KnowledgeGraphParser(
    private val graphFile: File
) {
    private val mapper = ObjectMapper()

    fun parse(): KnowledgeGraph {
        if (!graphFile.exists()) {
            throw IllegalArgumentException("Graph file not found: ${graphFile.absolutePath}")
        }

        val root = mapper.readTree(graphFile)

        val nodes = mutableListOf<KnowledgeGraphNode>()
        val edges = mutableListOf<KnowledgeGraphEdge>()
        val communities = mutableListOf<KnowledgeGraphCommunity>()

        val communitiesNode = root.get("communities")
        if (communitiesNode != null && communitiesNode.isArray) {
            for (communityNode in communitiesNode) {
                val communityName = communityNode.get("name")?.asText() ?: "unknown"
                val communityNodeNames = communityNode.get("nodes")?.map { it.asText() } ?: emptyList()

                val communityEdges = mutableListOf<KnowledgeGraphEdge>()

                val edgesNode = communityNode.get("edges")
                if (edgesNode != null && edgesNode.isArray) {
                    for (edgeNode in edgesNode) {
                        if (edgeNode.isObject) {
                            val source = edgeNode.get("source")?.asText() ?: ""
                            val target = edgeNode.get("target")?.asText() ?: ""
                            val label = edgeNode.get("label")?.asText() ?: ""
                            val typeStr = edgeNode.get("type")?.asText() ?: "EXTRACTED"
                            val confidence = edgeNode.get("confidence")?.asDouble() ?: 1.0

                            val edge = KnowledgeGraphEdge(
                                source = source,
                                target = target,
                                label = label,
                                type = parseEdgeType(typeStr),
                                confidence = confidence
                            )
                            communityEdges.add(edge)
                            edges.add(edge)
                        } else if (edgeNode.isTextual) {
                            val edgeStr = edgeNode.asText()
                            val parsedEdge = parseEdgeString(edgeStr)
                            if (parsedEdge != null) {
                                communityEdges.add(parsedEdge)
                                edges.add(parsedEdge)
                            }
                        }
                    }
                }

                for (nodeName in communityNodeNames) {
                    if (nodes.none { it.name == nodeName }) {
                        nodes.add(KnowledgeGraphNode(name = nodeName, community = communityName))
                    }
                }

                communities.add(
                    KnowledgeGraphCommunity(
                        name = communityName,
                        nodes = communityNodeNames,
                        edges = communityEdges
                    )
                )
            }
        }

        val rootEdgesNode = root.get("edges")
        if (rootEdgesNode != null && rootEdgesNode.isArray) {
            for (edgeNode in rootEdgesNode) {
                if (edgeNode.isObject) {
                    val source = edgeNode.get("source")?.asText() ?: continue
                    val target = edgeNode.get("target")?.asText() ?: continue
                    val label = edgeNode.get("label")?.asText() ?: ""
                    val typeStr = edgeNode.get("type")?.asText() ?: "EXTRACTED"
                    val confidence = edgeNode.get("confidence")?.asDouble() ?: 1.0

                    val edge = KnowledgeGraphEdge(
                        source = source,
                        target = target,
                        label = label,
                        type = parseEdgeType(typeStr),
                        confidence = confidence
                    )

                    if (edges.none { it.source == edge.source && it.target == edge.target && it.type == edge.type }) {
                        edges.add(edge)
                    }
                } else if (edgeNode.isTextual) {
                    val edgeStr = edgeNode.asText()
                    val parsedEdge = parseEdgeString(edgeStr)
                    if (parsedEdge != null) {
                        if (edges.none { it.source == parsedEdge.source && it.target == parsedEdge.target && it.type == parsedEdge.type }) {
                            edges.add(parsedEdge)
                        }
                    }
                }
            }
        }

        val rootNodesNode = root.get("nodes")
        if (rootNodesNode != null && rootNodesNode.isArray) {
            for (nodeEntry in rootNodesNode) {
                if (nodeEntry.isObject) {
                    val name = nodeEntry.get("name")?.asText() ?: continue
                    val type = nodeEntry.get("type")?.asText() ?: "class"
                    val community = nodeEntry.get("community")?.asText() ?: ""
                    val attributes = nodeEntry.get("attributes")
                        ?.map { it.asText() }
                        ?.toList() ?: emptyList()

                    val knNode = KnowledgeGraphNode(
                        name = name,
                        type = type,
                        community = community,
                        attributes = attributes
                    )

                    val existingIndex = nodes.indexOfFirst { it.name == knNode.name }
                    if (existingIndex >= 0) {
                        nodes[existingIndex] = knNode
                    } else {
                        nodes.add(knNode)
                    }
                } else if (nodeEntry.isTextual) {
                    val name = nodeEntry.asText()
                    if (nodes.none { it.name == name }) {
                        nodes.add(KnowledgeGraphNode(name = name))
                    }
                }
            }
        }

        return KnowledgeGraph(
            nodes = nodes,
            edges = edges,
            communities = communities
        )
    }

    private fun parseEdgeType(typeStr: String): EdgeType {
        return when (typeStr.uppercase()) {
            "EXTRACTED" -> EdgeType.EXTRACTED
            "INFERRED" -> EdgeType.INFERRED
            "AMBIGUOUS" -> EdgeType.AMBIGUOUS
            else -> EdgeType.EXTRACTED
        }
    }

    private fun parseEdgeString(edgeStr: String): KnowledgeGraphEdge? {
        val arrowPatterns = listOf("--x", "..>", "-->", "->")
        for (arrow in arrowPatterns) {
            if (edgeStr.contains(arrow)) {
                val parts = edgeStr.split(arrow, limit = 2)
                if (parts.size == 2) {
                    val source = parts[0].trim()
                    val targetAndLabel = parts[1].trim()
                    val labelParts = targetAndLabel.split(":", limit = 2)
                    val target = labelParts[0].trim()
                    val label = if (labelParts.size > 1) labelParts[1].trim() else ""
                    val type = when (arrow) {
                        "..>" -> EdgeType.INFERRED
                        "--x" -> EdgeType.AMBIGUOUS
                        else -> EdgeType.EXTRACTED
                    }
                    return KnowledgeGraphEdge(source = source, target = target, label = label, type = type)
                }
            }
        }
        return null
    }
}