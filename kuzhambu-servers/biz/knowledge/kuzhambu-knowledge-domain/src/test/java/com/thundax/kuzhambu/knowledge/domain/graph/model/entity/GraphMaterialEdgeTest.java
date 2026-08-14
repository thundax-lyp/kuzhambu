package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GraphMaterialEdgeTest {

    @Test
    void refreshEdgeKeyShouldGenerateStableBusinessKey() {
        GraphMaterialNode sourceNode = node(1L, "李白");
        GraphMaterialNode targetNode = node(2L, "杜甫");
        sourceNode.refreshNodeKey(null);
        targetNode.refreshNodeKey(null);
        GraphMaterialEdge first = edge(1L, 2L);
        GraphMaterialEdge second = edge(2L, 1L);

        first.refreshEdgeKey(sourceNode, targetNode, false, Map.of("role", "friend"));
        second.refreshEdgeKey(targetNode, sourceNode, false, Map.of("role", "friend"));

        assertNotNull(first.getEdgeKey());
        assertEquals(first.getEdgeKey(), second.getEdgeKey());
        assertTrue(first.sameBusinessKey(second));
    }

    @Test
    void replaceEndpointShouldRejectNullEndpoint() {
        GraphMaterialEdge edge = edge(1L, 2L);

        assertThrows(DomainException.class, () -> edge.replaceEndpoint(new GraphMaterialNodeId(1L), null));
    }

    @Test
    void refreshEdgeKeyShouldRejectEndpointMismatch() {
        GraphMaterialNode sourceNode = node(3L, "李白");
        GraphMaterialNode targetNode = node(2L, "杜甫");
        sourceNode.refreshNodeKey(null);
        targetNode.refreshNodeKey(null);
        GraphMaterialEdge edge = edge(1L, 2L);

        assertThrows(DomainException.class, () -> edge.refreshEdgeKey(sourceNode, targetNode, true, Map.of()));
    }

    private static GraphMaterialNode node(Long id, String name) {
        GraphMaterialNode node = new GraphMaterialNode();
        node.setId(new GraphMaterialNodeId(id));
        node.setMaterialRef(new ContentRef("SANCAI_ENTRY", 1L));
        node.setNodeType(GraphNodeType.PERSON);
        node.setName(name);
        node.setSource(GraphSourceType.MANUAL);
        return node;
    }

    private static GraphMaterialEdge edge(Long sourceNodeId, Long targetNodeId) {
        GraphMaterialEdge edge = new GraphMaterialEdge();
        edge.setMaterialRef(new ContentRef("SANCAI_ENTRY", 1L));
        edge.setSourceNodeId(new GraphMaterialNodeId(sourceNodeId));
        edge.setTargetNodeId(new GraphMaterialNodeId(targetNodeId));
        edge.setRelationType("ASSOCIATED_WITH");
        edge.setSource(GraphSourceType.MANUAL);
        return edge;
    }
}
