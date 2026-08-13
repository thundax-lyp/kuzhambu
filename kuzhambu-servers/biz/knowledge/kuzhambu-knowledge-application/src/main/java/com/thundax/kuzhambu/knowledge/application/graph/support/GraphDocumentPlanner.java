package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GraphDocumentPlanner {

    private static final String STRATEGY_MERGE = "MERGE";
    private static final String STRATEGY_REPLACE = "REPLACE";

    private final ObjectMapper objectMapper;
    private final GraphSchemaSupport schemaSupport;

    public GraphDocumentPlanner(ObjectMapper objectMapper, GraphSchemaSupport schemaSupport) {
        this.objectMapper = objectMapper;
        this.schemaSupport = schemaSupport;
    }

    public GraphDocumentPlan plan(
            GraphMaterialGraph current, GraphDocument document, GraphSourceType source, String strategy) {
        if (current == null || document == null || source == null) {
            throw new BizException("Graph document planning arguments are required");
        }
        String effectiveStrategy = normalizeStrategy(strategy);
        GraphDocumentPlan plan = new GraphDocumentPlan();
        Map<GraphNodeKey, GraphMaterialNode> currentNodesByKey = currentNodesByKey(current);
        Map<GraphEdgeKey, GraphMaterialEdge> currentEdgesByKey = currentEdgesByKey(current);
        Map<String, GraphMaterialNode> plannedNodesByDocumentId =
                planNodes(plan, current.material().getContentRef(), document.getNodes(), source, currentNodesByKey);
        Set<GraphNodeKey> retainedNodeKeys = new HashSet<>();
        plannedNodesByDocumentId.values().forEach(node -> retainedNodeKeys.add(node.getNodeKey()));
        Set<GraphEdgeKey> retainedEdgeKeys =
                planEdges(plan, document.getEdges(), source, plannedNodesByDocumentId, currentEdgesByKey);
        if (STRATEGY_REPLACE.equals(effectiveStrategy)) {
            current.edges().stream()
                    .filter(edge -> !retainedEdgeKeys.contains(edge.getEdgeKey()))
                    .forEach(plan::addDeletedEdge);
            current.nodes().stream()
                    .filter(node -> !retainedNodeKeys.contains(node.getNodeKey()))
                    .forEach(plan::addDeletedNode);
        }
        return plan;
    }

    private Map<String, GraphMaterialNode> planNodes(
            GraphDocumentPlan plan,
            ContentRef materialRef,
            List<GraphDocumentNode> documentNodes,
            GraphSourceType source,
            Map<GraphNodeKey, GraphMaterialNode> currentNodesByKey) {
        Map<String, GraphMaterialNode> result = new LinkedHashMap<>();
        Set<String> documentNodeIds = new HashSet<>();
        for (GraphDocumentNode documentNode : documentNodes == null ? List.<GraphDocumentNode>of() : documentNodes) {
            if (!documentNodeIds.add(documentNode.getId())) {
                throw new BizException("Graph document node id is duplicated");
            }
            GraphMaterialNode plannedNode = toMaterialNode(materialRef, documentNode, source);
            GraphMaterialNode existingNode = currentNodesByKey.get(plannedNode.getNodeKey());
            result.put(documentNode.getId(), plannedNode);
            if (existingNode == null) {
                plan.addCreatedNode(documentNode.getId(), plannedNode);
                continue;
            }
            plannedNode.setId(existingNode.getId());
            if (sameNode(existingNode, plannedNode)) {
                plan.addUnchangedNode(documentNode.getId(), existingNode);
            } else {
                plan.addUpdatedNode(documentNode.getId(), existingNode, plannedNode);
            }
        }
        return result;
    }

    private Set<GraphEdgeKey> planEdges(
            GraphDocumentPlan plan,
            List<GraphDocumentEdge> documentEdges,
            GraphSourceType source,
            Map<String, GraphMaterialNode> plannedNodesByDocumentId,
            Map<GraphEdgeKey, GraphMaterialEdge> currentEdgesByKey) {
        Set<GraphEdgeKey> retainedEdgeKeys = new HashSet<>();
        Set<GraphEdgeKey> documentEdgeKeys = new HashSet<>();
        for (GraphDocumentEdge documentEdge : documentEdges == null ? List.<GraphDocumentEdge>of() : documentEdges) {
            GraphMaterialNode sourceNode = requireDocumentNode(plannedNodesByDocumentId, documentEdge.getSourceId());
            GraphMaterialNode targetNode = requireDocumentNode(plannedNodesByDocumentId, documentEdge.getTargetId());
            String qualifiersJson = writeOptionalJson(documentEdge.getQualifiers());
            GraphEdgeKey edgeKey = GraphKeyHelper.generateEdgeKey(
                    sourceNode.getNodeKey(),
                    targetNode.getNodeKey(),
                    documentEdge.getRelationType(),
                    schemaSupport.directed(documentEdge.getRelationType()),
                    schemaSupport.keyQualifiers(documentEdge.getRelationType(), qualifiersJson));
            if (!documentEdgeKeys.add(edgeKey)) {
                throw new BizException("Graph document edge key is duplicated");
            }
            retainedEdgeKeys.add(edgeKey);
            GraphMaterialEdge existingEdge = currentEdgesByKey.get(edgeKey);
            GraphDocumentPlan.EdgeSpec edgeSpec = new GraphDocumentPlan.EdgeSpec(
                    documentEdge.getId(),
                    existingEdge,
                    documentEdge.getSourceId(),
                    documentEdge.getTargetId(),
                    edgeKey,
                    documentEdge.getRelationType(),
                    source,
                    qualifiersJson);
            if (existingEdge == null) {
                plan.addCreatedEdge(edgeSpec);
            } else if (!sameEdge(existingEdge, edgeSpec)) {
                plan.addUpdatedEdge(edgeSpec);
            }
        }
        return retainedEdgeKeys;
    }

    private GraphMaterialNode toMaterialNode(
            ContentRef materialRef, GraphDocumentNode documentNode, GraphSourceType source) {
        String propertiesJson = nodePropertiesJson(documentNode);
        GraphMaterialNode node = new GraphMaterialNode();
        node.setMaterialRef(materialRef);
        node.setNodeType(GraphNodeType.from(documentNode.getNodeType()));
        node.setName(documentNode.getName());
        node.setSource(source);
        node.setPropertiesJson(propertiesJson);
        node.refreshNodeKey(schemaSupport.identityQualifier(propertiesJson));
        return node;
    }

    private Map<GraphNodeKey, GraphMaterialNode> currentNodesByKey(GraphMaterialGraph current) {
        Map<GraphNodeKey, GraphMaterialNode> result = new LinkedHashMap<>();
        for (GraphMaterialNode node : current.nodes()) {
            if (node.getNodeKey() != null && result.put(node.getNodeKey(), node) != null) {
                throw new BizException("Current graph material node key is duplicated");
            }
        }
        return result;
    }

    private Map<GraphEdgeKey, GraphMaterialEdge> currentEdgesByKey(GraphMaterialGraph current) {
        Map<GraphEdgeKey, GraphMaterialEdge> result = new LinkedHashMap<>();
        for (GraphMaterialEdge edge : current.edges()) {
            if (edge.getEdgeKey() != null && result.put(edge.getEdgeKey(), edge) != null) {
                throw new BizException("Current graph material edge key is duplicated");
            }
        }
        return result;
    }

    private GraphMaterialNode requireDocumentNode(
            Map<String, GraphMaterialNode> nodesByDocumentId, String documentNodeId) {
        GraphMaterialNode node = nodesByDocumentId.get(documentNodeId);
        if (node == null) {
            throw new BizException("Graph document edge endpoint node is missing");
        }
        return node;
    }

    private boolean sameNode(GraphMaterialNode current, GraphMaterialNode planned) {
        return current.getNodeType() == planned.getNodeType()
                && current.getName().equals(planned.getName())
                && current.getSource() == planned.getSource()
                && equals(current.getPropertiesJson(), planned.getPropertiesJson());
    }

    private boolean sameEdge(GraphMaterialEdge current, GraphDocumentPlan.EdgeSpec planned) {
        return current.getSource() == planned.source()
                && current.getRelationType().equals(planned.relationType())
                && equals(current.getQualifiersJson(), planned.qualifiersJson());
    }

    private String nodePropertiesJson(GraphDocumentNode documentNode) {
        ObjectNode properties = objectMapper.createObjectNode();
        put(properties, "aliases", documentNode.getAliases());
        put(properties, "description", documentNode.getDescription());
        put(properties, "identityQualifier", documentNode.getIdentityQualifier());
        put(properties, "period", documentNode.getPeriod());
        put(properties, "categoryCodes", documentNode.getCategoryCodes());
        put(properties, "imageRefs", documentNode.getImageRefs());
        put(properties, "properties", documentNode.getProperties());
        return writeOptionalJson(properties);
    }

    private void put(ObjectNode objectNode, String fieldName, JsonNode value) {
        if (value != null && !value.isNull()) {
            objectNode.set(fieldName, value);
        }
    }

    private void put(ObjectNode objectNode, String fieldName, String value) {
        if (value != null && !value.isBlank()) {
            objectNode.put(fieldName, value);
        }
    }

    private String writeOptionalJson(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BizException("Graph document contains invalid JSON");
        }
    }

    private String normalizeStrategy(String strategy) {
        if (strategy == null || strategy.isBlank()) {
            return STRATEGY_MERGE;
        }
        String normalized = strategy.trim().toUpperCase();
        if (STRATEGY_MERGE.equals(normalized) || STRATEGY_REPLACE.equals(normalized)) {
            return normalized;
        }
        throw new BizException("Unknown graph document strategy: " + strategy);
    }

    private boolean equals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
