package com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphMaterialGraphTest {

    @Test
    void ofShouldRejectOrphanEdge() {
        GraphMaterial material = material();
        GraphMaterialNode node = node(1L, "node:a");
        GraphMaterialEdge edge = edge(10L, 1L, 2L, "edge:a");

        assertThrows(DomainException.class, () -> GraphMaterialGraph.of(material, List.of(node), List.of(edge)));
    }

    @Test
    void addNodeShouldRejectDuplicateKey() {
        GraphMaterialGraph graph = GraphMaterialGraph.of(material(), List.of(node(1L, "node:a")), List.of());

        assertThrows(DomainException.class, () -> graph.addNode(node(2L, "node:a")));
    }

    @Test
    void removeNodeShouldDeleteConnectedEdgesAndRefreshStatus() {
        GraphMaterial material = material();
        GraphMaterialGraph graph = GraphMaterialGraph.of(
                material, List.of(node(1L, "node:a"), node(2L, "node:b")), List.of(edge(10L, 1L, 2L, "edge:a")));

        graph.removeNode(new GraphMaterialNodeId(1L));

        assertEquals(1, graph.nodes().size());
        assertEquals(0, graph.edges().size());
        assertEquals(GraphMaterialStatus.READY, material.getStatus());
    }

    @Test
    void mergeNodesShouldDeleteMergedNode() {
        GraphMaterialGraph graph = GraphMaterialGraph.of(
                material(), List.of(node(1L, "node:a"), node(2L, "node:b")), List.of(edge(10L, 2L, 1L, "edge:a")));

        GraphMaterialChangeSet changes =
                graph.mergeNodes(new GraphMaterialNodeId(1L), List.of(new GraphMaterialNodeId(2L)));

        assertEquals(1, graph.nodes().size());
        assertEquals(1, changes.deletedNodes().size());
    }

    private static GraphMaterial material() {
        GraphMaterial material = new GraphMaterial();
        material.setContentRef(new ContentRef("SANCAI_ENTRY", 1L));
        material.setStatus(GraphMaterialStatus.READY);
        return material;
    }

    private static GraphMaterialNode node(Long id, String nodeKey) {
        GraphMaterialNode node = new GraphMaterialNode();
        node.setId(new GraphMaterialNodeId(id));
        node.setMaterialRef(new ContentRef("SANCAI_ENTRY", 1L));
        node.setNodeKey(new GraphNodeKey(nodeKey));
        node.setNodeType(GraphNodeType.PERSON);
        node.setName("节点" + id);
        node.setSource(GraphSourceType.MANUAL);
        return node;
    }

    private static GraphMaterialEdge edge(Long id, Long sourceNodeId, Long targetNodeId, String edgeKey) {
        GraphMaterialEdge edge = new GraphMaterialEdge();
        edge.setId(new GraphMaterialEdgeId(id));
        edge.setMaterialRef(new ContentRef("SANCAI_ENTRY", 1L));
        edge.setSourceNodeId(new GraphMaterialNodeId(sourceNodeId));
        edge.setTargetNodeId(new GraphMaterialNodeId(targetNodeId));
        edge.setEdgeKey(new GraphEdgeKey(edgeKey));
        edge.setRelationType("ASSOCIATED_WITH");
        edge.setSource(GraphSourceType.MANUAL);
        return edge;
    }
}
