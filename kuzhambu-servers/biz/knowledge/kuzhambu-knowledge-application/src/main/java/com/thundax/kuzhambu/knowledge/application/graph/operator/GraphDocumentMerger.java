package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentEdgeDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentNodeDto;
import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GraphDocumentMerger {

    private final ObjectMapper objectMapper;
    private final GraphSchemaResolver schemaSupport;

    public GraphDocumentMerger(ObjectMapper objectMapper, GraphSchemaResolver schemaSupport) {
        this.objectMapper = objectMapper;
        this.schemaSupport = schemaSupport;
    }

    public GraphDocumentDto merge(GraphDocumentDto current, GraphDocumentDto incoming) {
        if (current == null || incoming == null) {
            throw new BizException("Graph documents are required for merge");
        }
        GraphDocumentDto merged = new GraphDocumentDto();
        merged.setSchemaVersion(current.getSchemaVersion());
        List<GraphDocumentNodeDto> nodes = new ArrayList<>(safeNodes(current));
        Map<GraphNodeKey, String> nodeIdsByKey = new HashMap<>();
        for (GraphDocumentNodeDto node : nodes) {
            nodeIdsByKey.put(requireNodeKey(node), node.getId());
        }
        Map<String, String> incomingNodeIds = new HashMap<>();
        int nextNodeId = nodes.size() + 1;
        for (GraphDocumentNodeDto node : safeNodes(incoming)) {
            String incomingNodeId = node.getId();
            GraphNodeKey nodeKey = requireNodeKey(node);
            String mergedNodeId = nodeIdsByKey.get(nodeKey);
            if (mergedNodeId == null) {
                mergedNodeId = "merge-node-" + nextNodeId++;
                node.setId(mergedNodeId);
                nodes.add(node);
                nodeIdsByKey.put(nodeKey, mergedNodeId);
            }
            if (incomingNodeIds.put(incomingNodeId, mergedNodeId) != null) {
                throw new BizException("Graph document node id is duplicated");
            }
        }
        merged.setNodes(nodes);

        List<GraphDocumentEdgeDto> edges = new ArrayList<>(safeEdges(current));
        Set<GraphEdgeKey> edgeKeys = new HashSet<>();
        for (GraphDocumentEdgeDto edge : edges) {
            edgeKeys.add(requireEdgeKey(edge, nodeIdsByKey));
        }
        int nextEdgeId = edges.size() + 1;
        for (GraphDocumentEdgeDto edge : safeEdges(incoming)) {
            String sourceId = incomingNodeIds.get(edge.getSourceId());
            String targetId = incomingNodeIds.get(edge.getTargetId());
            if (sourceId == null || targetId == null) {
                throw new BizException("Graph document edge endpoint node is missing");
            }
            edge.setSourceId(sourceId);
            edge.setTargetId(targetId);
            if (!edgeKeys.add(requireEdgeKey(edge, nodeIdsByKey))) {
                continue;
            }
            edge.setId("merge-edge-" + nextEdgeId++);
            edges.add(edge);
        }
        merged.setEdges(edges);
        return merged;
    }

    private GraphNodeKey requireNodeKey(GraphDocumentNodeDto node) {
        GraphNodeKey key = GraphKeyHelper.generateNodeKey(
                GraphNodeType.from(node.getNodeType()), node.getName(), node.getIdentityQualifier());
        if (key == null) {
            throw new BizException("Graph document node key is required for merge");
        }
        return key;
    }

    private GraphEdgeKey requireEdgeKey(GraphDocumentEdgeDto edge, Map<GraphNodeKey, String> nodeIdsByKey) {
        GraphNodeKey source = nodeKeyById(nodeIdsByKey, edge.getSourceId());
        GraphNodeKey target = nodeKeyById(nodeIdsByKey, edge.getTargetId());
        GraphEdgeKey key = GraphKeyHelper.generateEdgeKey(
                source,
                target,
                edge.getRelationType(),
                schemaSupport.directed(edge.getRelationType()),
                schemaSupport.keyQualifiers(edge.getRelationType(), writeJson(edge.getQualifiers())));
        if (key == null) {
            throw new BizException("Graph document edge key is required for merge");
        }
        return key;
    }

    private GraphNodeKey nodeKeyById(Map<GraphNodeKey, String> nodeIdsByKey, String nodeId) {
        return nodeIdsByKey.entrySet().stream()
                .filter(entry -> entry.getValue().equals(nodeId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new BizException("Graph document edge endpoint node is missing"));
    }

    private String writeJson(JsonNode value) {
        try {
            return value == null || value.isNull() ? null : objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BizException("Graph document qualifiers cannot be serialized");
        }
    }

    private List<GraphDocumentNodeDto> safeNodes(GraphDocumentDto document) {
        return document.getNodes() == null ? List.of() : document.getNodes();
    }

    private List<GraphDocumentEdgeDto> safeEdges(GraphDocumentDto document) {
        return document.getEdges() == null ? List.of() : document.getEdges();
    }
}
