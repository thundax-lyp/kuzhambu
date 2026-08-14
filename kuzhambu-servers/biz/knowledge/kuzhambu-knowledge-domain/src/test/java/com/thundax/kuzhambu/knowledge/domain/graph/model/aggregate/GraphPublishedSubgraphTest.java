package com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphPublishedSubgraphTest {

    @Test
    void deleteNodeShouldRejectActiveIncidentEdgesWithoutCascade() {
        GraphPublishedSubgraph subgraph =
                new GraphPublishedSubgraph(List.of(node(1L), node(2L)), List.of(edge(10L, 1L, 2L)));

        assertThrows(
                DomainException.class, () -> subgraph.deleteNode(new GraphPublishedNodeId(1L), false, Instant.now()));
    }

    @Test
    void deleteNodeShouldCascadeEdges() {
        GraphPublishedEdge edge = edge(10L, 1L, 2L);
        GraphPublishedSubgraph subgraph = new GraphPublishedSubgraph(List.of(node(1L), node(2L)), List.of(edge));

        GraphPublishedMutationSet changes = subgraph.deleteNode(new GraphPublishedNodeId(1L), true, Instant.now());

        assertEquals(1, changes.updatedNodes().size());
        assertEquals(1, changes.updatedEdges().size());
        assertEquals(GraphPublishedStatus.DELETED, edge.getStatus());
    }

    @Test
    void mergeNodesShouldRedirectEdgesAndDeleteMergedNode() {
        GraphPublishedEdge edge = edge(10L, 2L, 3L);
        GraphPublishedSubgraph subgraph =
                new GraphPublishedSubgraph(List.of(node(1L), node(2L), node(3L)), List.of(edge));

        GraphPublishedMutationSet changes =
                subgraph.mergeNodes(new GraphPublishedNodeId(1L), List.of(new GraphPublishedNodeId(2L)), Instant.now());

        assertEquals(new GraphPublishedNodeId(1L), edge.getSourceNodeId());
        assertEquals(2, changes.updatedNodes().size());
    }

    private static GraphPublishedNode node(Long id) {
        GraphPublishedNode node = new GraphPublishedNode();
        node.setId(new GraphPublishedNodeId(id));
        node.setNodeType(GraphNodeType.PERSON);
        node.setName("节点" + id);
        node.setSource(GraphSourceType.MANUAL);
        node.setStatus(GraphPublishedStatus.ACTIVE);
        return node;
    }

    private static GraphPublishedEdge edge(Long id, Long sourceNodeId, Long targetNodeId) {
        GraphPublishedEdge edge = new GraphPublishedEdge();
        edge.setId(new GraphPublishedEdgeId(id));
        edge.setSourceNodeId(new GraphPublishedNodeId(sourceNodeId));
        edge.setTargetNodeId(new GraphPublishedNodeId(targetNodeId));
        edge.setRelationType("ASSOCIATED_WITH");
        edge.setSource(GraphSourceType.MANUAL);
        edge.setStatus(GraphPublishedStatus.ACTIVE);
        return edge;
    }
}
