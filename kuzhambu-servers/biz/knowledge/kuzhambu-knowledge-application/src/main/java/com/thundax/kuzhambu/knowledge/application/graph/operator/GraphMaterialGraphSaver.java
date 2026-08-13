package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentEdgeDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentNodeDto;
import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialChangeSet;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GraphMaterialGraphSaver {

    private final ObjectMapper objectMapper;
    private final GraphSchemaResolver schemaSupport;
    private final GraphMaterialRepository materialRepository;
    private final GraphMaterialNodeRepository nodeRepository;
    private final GraphMaterialEdgeRepository edgeRepository;

    public GraphMaterialGraphSaver(
            ObjectMapper objectMapper,
            GraphSchemaResolver schemaSupport,
            GraphMaterialRepository materialRepository,
            GraphMaterialNodeRepository nodeRepository,
            GraphMaterialEdgeRepository edgeRepository) {
        this.objectMapper = objectMapper;
        this.schemaSupport = schemaSupport;
        this.materialRepository = materialRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
    }

    @Transactional
    public void save(GraphMaterialGraph graph, GraphMaterialChangeSet changes, long expectedLockVersion) {
        if (graph == null || changes == null) {
            throw new BizException("Graph material save arguments are required");
        }
        graph.material().requireLockVersion(expectedLockVersion);
        deleteEdges(changes.deletedEdges());
        deleteNodes(changes.deletedNodes());
        insertNodes(changes.createdNodes());
        nodeRepository.batchUpdate(changes.updatedNodes());
        insertEdges(changes.createdEdges());
        edgeRepository.batchUpdate(changes.updatedEdges());
        updateMaterial(graph.material(), expectedLockVersion);
    }

    @Transactional
    public GraphMaterialGraph replaceDocument(
            GraphMaterialGraph current, GraphDocumentDto document, GraphSourceType source, long expectedLockVersion) {
        if (current == null || document == null || source == null) {
            throw new BizException("Graph document replacement arguments are required");
        }
        GraphMaterial material = current.material();
        material.requireEditable();
        material.requireLockVersion(expectedLockVersion);
        ContentRef materialRef = material.getContentRef();

        deleteEdges(current.edges());
        deleteNodes(current.nodes());

        Map<String, GraphMaterialNode> nodesByDocumentId = new LinkedHashMap<>();
        Set<GraphNodeKey> nodeKeys = new HashSet<>();
        for (GraphDocumentNodeDto documentNode : safeNodes(document)) {
            if (nodesByDocumentId.put(documentNode.getId(), toNode(materialRef, documentNode, source)) != null) {
                throw new BizException("Graph document node id is duplicated");
            }
            GraphMaterialNode node = nodesByDocumentId.get(documentNode.getId());
            if (!nodeKeys.add(node.getNodeKey())) {
                throw new BizException("Graph document node key is duplicated");
            }
            node.setId(nodeRepository.insert(node));
        }

        Set<GraphEdgeKey> edgeKeys = new HashSet<>();
        for (GraphDocumentEdgeDto documentEdge : safeEdges(document)) {
            GraphMaterialNode sourceNode = requireNode(nodesByDocumentId, documentEdge.getSourceId());
            GraphMaterialNode targetNode = requireNode(nodesByDocumentId, documentEdge.getTargetId());
            String qualifiersJson = writeOptionalJson(documentEdge.getQualifiers());
            GraphEdgeKey edgeKey = GraphKeyHelper.generateEdgeKey(
                    sourceNode.getNodeKey(),
                    targetNode.getNodeKey(),
                    documentEdge.getRelationType(),
                    schemaSupport.directed(documentEdge.getRelationType()),
                    schemaSupport.keyQualifiers(documentEdge.getRelationType(), qualifiersJson));
            if (!edgeKeys.add(edgeKey)) {
                throw new BizException("Graph document edge key is duplicated");
            }
            GraphMaterialEdge edge = new GraphMaterialEdge();
            edge.setMaterialRef(materialRef);
            edge.setSourceNodeId(sourceNode.getId());
            edge.setTargetNodeId(targetNode.getId());
            edge.setEdgeKey(edgeKey);
            edge.setRelationType(documentEdge.getRelationType());
            edge.setSource(source);
            edge.setQualifiersJson(qualifiersJson);
            edgeRepository.insert(edge);
        }

        material.refreshStatus(
                nodesByDocumentId.isEmpty() && safeEdges(document).isEmpty());
        updateMaterial(material, expectedLockVersion);
        return GraphMaterialGraph.of(
                material, nodeRepository.listByMaterial(materialRef), edgeRepository.listByMaterial(materialRef));
    }

    private GraphMaterialNode toNode(
            ContentRef materialRef, GraphDocumentNodeDto documentNode, GraphSourceType source) {
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

    private GraphMaterialNode requireNode(Map<String, GraphMaterialNode> nodes, String documentNodeId) {
        GraphMaterialNode node = nodes.get(documentNodeId);
        if (node == null) {
            throw new BizException("Graph document edge endpoint node is missing");
        }
        return node;
    }

    private String nodePropertiesJson(GraphDocumentNodeDto documentNode) {
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
        try {
            return value == null || value.isNull() || value.isEmpty() ? null : objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BizException("Graph document properties cannot be serialized");
        }
    }

    private List<GraphDocumentNodeDto> safeNodes(GraphDocumentDto document) {
        return document.getNodes() == null ? List.of() : document.getNodes();
    }

    private List<GraphDocumentEdgeDto> safeEdges(GraphDocumentDto document) {
        return document.getEdges() == null ? List.of() : document.getEdges();
    }

    private void insertNodes(List<GraphMaterialNode> nodes) {
        for (GraphMaterialNode node : nodes) {
            if (node.getId() == null) {
                node.setId(nodeRepository.insert(node));
            }
        }
    }

    private void insertEdges(List<GraphMaterialEdge> edges) {
        for (GraphMaterialEdge edge : edges) {
            edge.setId(edgeRepository.insert(edge));
        }
    }

    private void deleteNodes(List<GraphMaterialNode> nodes) {
        nodeRepository.batchDeleteById(nodes.stream()
                .map(GraphMaterialNode::getId)
                .filter(id -> id != null)
                .toList());
    }

    private void deleteEdges(List<GraphMaterialEdge> edges) {
        edgeRepository.batchDeleteById(edges.stream()
                .map(GraphMaterialEdge::getId)
                .filter(id -> id != null)
                .toList());
    }

    private void updateMaterial(GraphMaterial material, long expectedLockVersion) {
        if (materialRepository.updateIfLockVersion(material, expectedLockVersion) != 1) {
            throw new BizException("Graph material lock version mismatch");
        }
    }
}
