package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialChangeSet;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GraphMaterialGraphSaver {

    private final GraphMaterialRepository materialRepository;
    private final GraphMaterialNodeRepository nodeRepository;
    private final GraphMaterialEdgeRepository edgeRepository;

    public GraphMaterialGraphSaver(
            GraphMaterialRepository materialRepository,
            GraphMaterialNodeRepository nodeRepository,
            GraphMaterialEdgeRepository edgeRepository) {
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
    public GraphMaterialGraph applyDocument(
            GraphMaterialGraph current, GraphDocumentPlan plan, long expectedLockVersion) {
        if (current == null || plan == null) {
            throw new BizException("Graph document apply arguments are required");
        }
        GraphMaterial material = current.material();
        material.requireEditable();
        material.requireLockVersion(expectedLockVersion);
        ContentRef materialRef = material.getContentRef();

        deleteEdges(plan.deletedEdges());
        deleteNodes(plan.deletedNodes());

        Map<String, GraphMaterialNodeId> nodeIdsByDocumentId = new LinkedHashMap<>();
        plan.matchedNodesByDocumentId()
                .forEach((documentId, node) -> nodeIdsByDocumentId.put(documentId, node.getId()));
        for (Map.Entry<String, GraphMaterialNode> entry :
                plan.createdNodesByDocumentId().entrySet()) {
            GraphMaterialNode node = entry.getValue();
            node.setId(nodeRepository.insert(node));
            nodeIdsByDocumentId.put(entry.getKey(), node.getId());
        }
        nodeRepository.batchUpdate(plan.updatedNodes());
        plan.updatedNodesByDocumentId()
                .forEach((documentId, node) -> nodeIdsByDocumentId.put(documentId, node.getId()));

        insertEdges(toEdges(materialRef, plan.createdEdges(), nodeIdsByDocumentId));
        edgeRepository.batchUpdate(toEdges(materialRef, plan.updatedEdges(), nodeIdsByDocumentId));

        material.refreshStatus(documentGraphEmpty(current, plan));
        updateMaterial(material, expectedLockVersion);
        return GraphMaterialGraph.of(
                material, nodeRepository.listByMaterial(materialRef), edgeRepository.listByMaterial(materialRef));
    }

    private List<GraphMaterialEdge> toEdges(
            ContentRef materialRef,
            List<GraphDocumentPlan.EdgeSpec> edgeSpecs,
            Map<String, GraphMaterialNodeId> nodeIdsByDocumentId) {
        return edgeSpecs.stream()
                .map(edgeSpec -> toEdge(materialRef, edgeSpec, nodeIdsByDocumentId))
                .toList();
    }

    private GraphMaterialEdge toEdge(
            ContentRef materialRef,
            GraphDocumentPlan.EdgeSpec edgeSpec,
            Map<String, GraphMaterialNodeId> nodeIdsByDocumentId) {
        GraphMaterialEdge edge = new GraphMaterialEdge();
        edge.setId(
                edgeSpec.existingEdge() == null ? null : edgeSpec.existingEdge().getId());
        edge.setMaterialRef(materialRef);
        edge.setSourceNodeId(requireNodeId(nodeIdsByDocumentId, edgeSpec.sourceDocumentNodeId()));
        edge.setTargetNodeId(requireNodeId(nodeIdsByDocumentId, edgeSpec.targetDocumentNodeId()));
        edge.setEdgeKey(edgeSpec.edgeKey());
        edge.setRelationType(edgeSpec.relationType());
        edge.setSource(edgeSpec.source());
        edge.setQualifiersJson(edgeSpec.qualifiersJson());
        return edge;
    }

    private GraphMaterialNodeId requireNodeId(Map<String, GraphMaterialNodeId> nodeIdsByDocumentId, String documentId) {
        GraphMaterialNodeId nodeId = nodeIdsByDocumentId.get(documentId);
        if (nodeId == null) {
            throw new BizException("Graph document node has not been persisted");
        }
        return nodeId;
    }

    private void insertNodes(List<GraphMaterialNode> nodes) {
        for (GraphMaterialNode node : nodes) {
            node.setId(nodeRepository.insert(node));
        }
    }

    private void insertEdges(List<GraphMaterialEdge> edges) {
        for (GraphMaterialEdge edge : edges) {
            edge.setId(edgeRepository.insert(edge));
        }
    }

    private void deleteNodes(List<GraphMaterialNode> nodes) {
        nodeRepository.deleteByIds(nodes.stream()
                .map(GraphMaterialNode::getId)
                .filter(id -> id != null)
                .toList());
    }

    private void deleteEdges(List<GraphMaterialEdge> edges) {
        edgeRepository.deleteByIds(edges.stream()
                .map(GraphMaterialEdge::getId)
                .filter(id -> id != null)
                .toList());
    }

    private boolean documentGraphEmpty(GraphMaterialGraph current, GraphDocumentPlan plan) {
        int nodeCount = current.nodes().size()
                - plan.deletedNodes().size()
                + plan.createdNodes().size();
        int edgeCount = current.edges().size()
                - plan.deletedEdges().size()
                + plan.createdEdges().size();
        return nodeCount <= 0 && edgeCount <= 0;
    }

    private void updateMaterial(GraphMaterial material, long expectedLockVersion) {
        int updated = materialRepository.updateIfLockVersion(material, expectedLockVersion);
        if (updated != 1) {
            throw new BizException("Graph material lock version mismatch");
        }
    }
}
