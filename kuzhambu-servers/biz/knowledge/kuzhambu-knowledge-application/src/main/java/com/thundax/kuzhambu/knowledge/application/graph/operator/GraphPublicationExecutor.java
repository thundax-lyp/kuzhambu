package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.operation.GraphPublication;
import com.thundax.kuzhambu.knowledge.domain.graph.model.operation.GraphPublicationContext;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GraphPublicationExecutor {

    private final ObjectMapper objectMapper;
    private final GraphMaterialGraphLoader graphLoader;
    private final GraphSnapshotResolver snapshotSupport;
    private final GraphSchemaResolver schemaSupport;
    private final GraphMaterialRepository materialRepository;
    private final GraphMaterialVersionRepository versionRepository;
    private final GraphPublishedNodeRepository publishedNodeRepository;
    private final GraphPublishedEdgeRepository publishedEdgeRepository;
    private final GraphPublishedNodePropertyRepository nodePropertyRepository;
    private final GraphPublishedEdgePropertyRepository edgePropertyRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;

    public GraphPublicationExecutor(
            ObjectMapper objectMapper,
            GraphMaterialGraphLoader graphLoader,
            GraphSnapshotResolver snapshotSupport,
            GraphSchemaResolver schemaSupport,
            GraphMaterialRepository materialRepository,
            GraphMaterialVersionRepository versionRepository,
            GraphPublishedNodeRepository publishedNodeRepository,
            GraphPublishedEdgeRepository publishedEdgeRepository,
            GraphPublishedNodePropertyRepository nodePropertyRepository,
            GraphPublishedEdgePropertyRepository edgePropertyRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository) {
        this.objectMapper = objectMapper;
        this.graphLoader = graphLoader;
        this.snapshotSupport = snapshotSupport;
        this.schemaSupport = schemaSupport;
        this.materialRepository = materialRepository;
        this.versionRepository = versionRepository;
        this.publishedNodeRepository = publishedNodeRepository;
        this.publishedEdgeRepository = publishedEdgeRepository;
        this.nodePropertyRepository = nodePropertyRepository;
        this.edgePropertyRepository = edgePropertyRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public GraphPublicationResult publishOne(GraphPublicationCommand command) {
        if (command == null || command.materialRef() == null) {
            throw new BizException("Graph publication command is required");
        }
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        GraphMaterial material = graph.material();
        material.requireReady();
        material.requireLockVersion(command.materialLockVersion());

        Map<GraphMaterialNodeId, GraphPublishedNode> matchedNodes = matchedNodes(graph.nodes());
        Map<GraphMaterialEdgeId, GraphPublishedEdge> matchedEdges = matchedEdges(graph.edges());
        Instant now = Instant.now();
        GraphPublication publication = GraphPublication.plan(
                new GraphPublicationContext(graph, matchedNodes, matchedEdges, Map.of(), Map.of(), now));
        publication.validateForPublication();
        if (!schemaSupport.validateForPublication(graph).isEmpty()) {
            throw new BizException("Graph material is not publishable");
        }

        Map<GraphMaterialNodeId, GraphPublishedNodeId> publishedNodeIds =
                publishNodes(graph.nodes(), matchedNodes, now);
        Map<GraphMaterialEdgeId, GraphPublishedEdgeId> publishedEdgeIds =
                publishEdges(graph.edges(), matchedEdges, publishedNodeIds, now);
        mergeNodeProperties(graph.nodes(), publishedNodeIds);
        mergeEdgeProperties(graph.edges(), publishedEdgeIds);
        writeNodeMaterials(command.materialRef(), graph.nodes(), publishedNodeIds);
        writeEdgeMaterials(command.materialRef(), graph.edges(), publishedEdgeIds);
        insertVersion(graph, command.publishedBy(), now);

        material.publish(now);
        updateMaterial(material, command.materialLockVersion());
        return GraphApplicationAssembler.toPublicationResult(command.materialRef(), material.getStatus(), publication);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public GraphMaterial withdrawOne(GraphWithdrawalCommand command) {
        if (command == null || command.materialRef() == null) {
            throw new BizException("Graph withdrawal command is required");
        }
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        GraphMaterial material = graph.material();
        material.requirePublished();
        material.requireLockVersion(command.materialLockVersion());
        edgeMaterialRepository.deleteByMaterial(command.materialRef());
        nodeMaterialRepository.deleteByMaterial(command.materialRef());
        material.withdraw(graph.nodes().isEmpty());
        updateMaterial(material, command.materialLockVersion());
        return material;
    }

    private Map<GraphMaterialNodeId, GraphPublishedNode> matchedNodes(List<GraphMaterialNode> materialNodes) {
        Map<GraphMaterialNodeId, GraphPublishedNode> result = new LinkedHashMap<>();
        for (GraphMaterialNode materialNode : materialNodes) {
            if (materialNode.getNodeKey() == null) {
                continue;
            }
            GraphPublishedNode publishedNode = publishedNodeRepository.getByNodeKey(materialNode.getNodeKey());
            if (publishedNode != null) {
                result.put(materialNode.getId(), publishedNode);
            }
        }
        return result;
    }

    private Map<GraphMaterialEdgeId, GraphPublishedEdge> matchedEdges(List<GraphMaterialEdge> materialEdges) {
        Map<GraphMaterialEdgeId, GraphPublishedEdge> result = new LinkedHashMap<>();
        for (GraphMaterialEdge materialEdge : materialEdges) {
            if (materialEdge.getEdgeKey() == null) {
                continue;
            }
            GraphPublishedEdge publishedEdge = publishedEdgeRepository.getByEdgeKey(materialEdge.getEdgeKey());
            if (publishedEdge != null) {
                result.put(materialEdge.getId(), publishedEdge);
            }
        }
        return result;
    }

    private Map<GraphMaterialNodeId, GraphPublishedNodeId> publishNodes(
            List<GraphMaterialNode> materialNodes,
            Map<GraphMaterialNodeId, GraphPublishedNode> matchedNodes,
            Instant modifiedAt) {
        Map<GraphMaterialNodeId, GraphPublishedNodeId> result = new HashMap<>();
        for (GraphMaterialNode materialNode : materialNodes) {
            GraphPublishedNode publishedNode = matchedNodes.get(materialNode.getId());
            if (publishedNode == null) {
                publishedNode = new GraphPublishedNode(
                        null,
                        materialNode.getNodeKey(),
                        materialNode.getNodeType(),
                        materialNode.getName(),
                        materialNode.getSource(),
                        GraphPublishedStatus.ACTIVE,
                        modifiedAt,
                        0L);
                publishedNode.setId(insertNodeOrReuse(publishedNode).getId());
            }
            result.put(materialNode.getId(), publishedNode.getId());
        }
        return result;
    }

    private Map<GraphMaterialEdgeId, GraphPublishedEdgeId> publishEdges(
            List<GraphMaterialEdge> materialEdges,
            Map<GraphMaterialEdgeId, GraphPublishedEdge> matchedEdges,
            Map<GraphMaterialNodeId, GraphPublishedNodeId> publishedNodeIds,
            Instant modifiedAt) {
        Map<GraphMaterialEdgeId, GraphPublishedEdgeId> result = new HashMap<>();
        for (GraphMaterialEdge materialEdge : materialEdges) {
            GraphPublishedEdge publishedEdge = matchedEdges.get(materialEdge.getId());
            if (publishedEdge == null) {
                publishedEdge = new GraphPublishedEdge(
                        null,
                        materialEdge.getEdgeKey(),
                        publishedNodeIds.get(materialEdge.getSourceNodeId()),
                        publishedNodeIds.get(materialEdge.getTargetNodeId()),
                        materialEdge.getRelationType(),
                        materialEdge.getSource(),
                        materialEdge.getQualifiersJson(),
                        GraphPublishedStatus.ACTIVE,
                        modifiedAt,
                        0L);
                publishedEdge.setId(insertEdgeOrReuse(publishedEdge).getId());
            }
            result.put(materialEdge.getId(), publishedEdge.getId());
        }
        return result;
    }

    private GraphPublishedNode insertNodeOrReuse(GraphPublishedNode node) {
        try {
            node.setId(publishedNodeRepository.insert(node));
            return node;
        } catch (RuntimeException ex) {
            GraphPublishedNode existing = publishedNodeRepository.getByNodeKey(node.getNodeKey());
            if (existing == null) {
                throw ex;
            }
            return existing;
        }
    }

    private GraphPublishedEdge insertEdgeOrReuse(GraphPublishedEdge edge) {
        try {
            edge.setId(publishedEdgeRepository.insert(edge));
            return edge;
        } catch (RuntimeException ex) {
            GraphPublishedEdge existing = publishedEdgeRepository.getByEdgeKey(edge.getEdgeKey());
            if (existing == null) {
                throw ex;
            }
            return existing;
        }
    }

    private void mergeNodeProperties(
            List<GraphMaterialNode> materialNodes, Map<GraphMaterialNodeId, GraphPublishedNodeId> publishedNodeIds) {
        for (GraphMaterialNode materialNode : materialNodes) {
            GraphPublishedNodeId publishedNodeId = publishedNodeIds.get(materialNode.getId());
            mergeNodeProperties(publishedNodeId, schemaSupport.nodePropertyValues(materialNode.getPropertiesJson()));
        }
    }

    private void mergeNodeProperties(GraphPublishedNodeId publishedNodeId, Map<String, List<String>> propertyValues) {
        List<GraphPublishedNodeProperty> existing =
                new ArrayList<>(nodePropertyRepository.listByPublishedNodeId(publishedNodeId));
        for (Map.Entry<String, List<String>> entry : propertyValues.entrySet()) {
            boolean hasPreferred = existing.stream()
                    .anyMatch(property -> entry.getKey().equals(property.getPropertyKey()) && property.isPreferred());
            for (String value : entry.getValue()) {
                if (containsNodeProperty(existing, entry.getKey(), value)) {
                    continue;
                }
                boolean preferred = !hasPreferred;
                GraphPublishedNodeProperty property =
                        new GraphPublishedNodeProperty(null, publishedNodeId, entry.getKey(), value, preferred);
                property.setId(nodePropertyRepository.insert(property));
                existing.add(property);
                hasPreferred = true;
            }
        }
    }

    private void mergeEdgeProperties(
            List<GraphMaterialEdge> materialEdges, Map<GraphMaterialEdgeId, GraphPublishedEdgeId> publishedEdgeIds) {
        for (GraphMaterialEdge materialEdge : materialEdges) {
            GraphPublishedEdgeId publishedEdgeId = publishedEdgeIds.get(materialEdge.getId());
            mergeEdgeProperties(publishedEdgeId, schemaSupport.edgePropertyValues(materialEdge.getQualifiersJson()));
        }
    }

    private void mergeEdgeProperties(GraphPublishedEdgeId publishedEdgeId, Map<String, List<String>> propertyValues) {
        List<GraphPublishedEdgeProperty> existing =
                new ArrayList<>(edgePropertyRepository.listByPublishedEdgeId(publishedEdgeId));
        for (Map.Entry<String, List<String>> entry : propertyValues.entrySet()) {
            boolean hasPreferred = existing.stream()
                    .anyMatch(property -> entry.getKey().equals(property.getPropertyKey()) && property.isPreferred());
            for (String value : entry.getValue()) {
                if (containsEdgeProperty(existing, entry.getKey(), value)) {
                    continue;
                }
                boolean preferred = !hasPreferred;
                GraphPublishedEdgeProperty property =
                        new GraphPublishedEdgeProperty(null, publishedEdgeId, entry.getKey(), value, preferred);
                property.setId(edgePropertyRepository.insert(property));
                existing.add(property);
                hasPreferred = true;
            }
        }
    }

    private boolean containsNodeProperty(List<GraphPublishedNodeProperty> properties, String key, String value) {
        return properties.stream()
                .anyMatch(property -> key.equals(property.getPropertyKey()) && value.equals(property.getValue()));
    }

    private boolean containsEdgeProperty(List<GraphPublishedEdgeProperty> properties, String key, String value) {
        return properties.stream()
                .anyMatch(property -> key.equals(property.getPropertyKey()) && value.equals(property.getValue()));
    }

    private void writeNodeMaterials(
            ContentRef materialRef,
            List<GraphMaterialNode> materialNodes,
            Map<GraphMaterialNodeId, GraphPublishedNodeId> publishedNodeIds) {
        nodeMaterialRepository.batchInsert(materialNodes.stream()
                .map(node -> new GraphPublishedNodeMaterial(
                        publishedNodeIds.get(node.getId()), materialRef, snapshotJson(node)))
                .toList());
    }

    private void writeEdgeMaterials(
            ContentRef materialRef,
            List<GraphMaterialEdge> materialEdges,
            Map<GraphMaterialEdgeId, GraphPublishedEdgeId> publishedEdgeIds) {
        edgeMaterialRepository.batchInsert(materialEdges.stream()
                .map(edge -> new GraphPublishedEdgeMaterial(
                        publishedEdgeIds.get(edge.getId()), materialRef, snapshotJson(edge)))
                .toList());
    }

    private void insertVersion(GraphMaterialGraph graph, Long publishedBy, Instant publishedAt) {
        GraphMaterialVersion version = new GraphMaterialVersion(
                null,
                graph.material().getContentRef(),
                versionRepository.maxVersionNo(graph.material().getContentRef()) + 1,
                snapshotSupport.serialize(graph),
                publishedBy,
                publishedAt);
        versionRepository.insert(version);
    }

    private void updateMaterial(GraphMaterial material, long expectedLockVersion) {
        if (materialRepository.updateIfLockVersion(material, expectedLockVersion) != 1) {
            throw new BizException("Graph material lock version mismatch");
        }
    }

    private String snapshotJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BizException("Graph publication source snapshot cannot be serialized");
        }
    }
}
