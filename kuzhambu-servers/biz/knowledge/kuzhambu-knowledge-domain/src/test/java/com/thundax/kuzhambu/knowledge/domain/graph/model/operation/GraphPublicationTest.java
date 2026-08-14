package com.thundax.kuzhambu.knowledge.domain.graph.model.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GraphPublicationTest {

    @Test
    void planShouldCreatePublishedNodeForUnmatchedMaterialNode() {
        GraphPublication publication = GraphPublication.plan(new GraphPublicationContext(
                graph(GraphMaterialStatus.DRAFT), Map.of(), Map.of(), Map.of(), Map.of(), Instant.now()));

        assertEquals(1, publication.createdNodeCount());
        assertEquals(0, publication.reusedNodeCount());
    }

    @Test
    void planShouldProduceBlockingIssueForDeletedPublishedNode() {
        GraphPublishedNode deletedNode = publishedNode(GraphPublishedStatus.DELETED);
        GraphPublication publication = GraphPublication.plan(new GraphPublicationContext(
                graph(GraphMaterialStatus.DRAFT),
                Map.of(new GraphMaterialNodeId(1L), deletedNode),
                Map.of(),
                Map.of(),
                Map.of(),
                Instant.now()));

        assertTrue(publication.changes().hasBlockingIssue());
        assertThrows(DomainException.class, publication::validateForPublication);
    }

    @Test
    void planShouldBlockPublicationWhenMaterialNodeHasNoEffectiveKey() {
        GraphMaterialNode node = materialNode();
        node.setNodeKey(null);
        GraphMaterial material = new GraphMaterial();
        material.setContentRef(new ContentRef("SANCAI_ENTRY", 1L));
        material.setStatus(GraphMaterialStatus.DRAFT);

        GraphPublication publication = GraphPublication.plan(new GraphPublicationContext(
                GraphMaterialGraph.of(material, List.of(node), List.of()),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Instant.now()));

        assertTrue(publication.changes().hasBlockingIssue());
        assertThrows(DomainException.class, publication::validateForPublication);
    }

    private static GraphMaterialGraph graph(GraphMaterialStatus status) {
        GraphMaterial material = new GraphMaterial();
        material.setContentRef(new ContentRef("SANCAI_ENTRY", 1L));
        material.setStatus(status);
        return GraphMaterialGraph.of(material, List.of(materialNode()), List.of());
    }

    private static GraphMaterialNode materialNode() {
        GraphMaterialNode node = new GraphMaterialNode();
        node.setId(new GraphMaterialNodeId(1L));
        node.setMaterialRef(new ContentRef("SANCAI_ENTRY", 1L));
        node.setNodeKey(new GraphNodeKey("node:a"));
        node.setNodeType(GraphNodeType.PERSON);
        node.setName("李白");
        node.setSource(GraphSourceType.MANUAL);
        return node;
    }

    private static GraphPublishedNode publishedNode(GraphPublishedStatus status) {
        GraphPublishedNode node = new GraphPublishedNode();
        node.setNodeKey(new GraphNodeKey("node:a"));
        node.setNodeType(GraphNodeType.PERSON);
        node.setName("李白");
        node.setSource(GraphSourceType.MANUAL);
        node.setStatus(status);
        return node;
    }
}
