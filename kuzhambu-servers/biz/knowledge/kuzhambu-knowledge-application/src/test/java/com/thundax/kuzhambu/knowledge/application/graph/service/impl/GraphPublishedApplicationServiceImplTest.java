package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphPublishedApplicationServiceImplTest {

    @Test
    void mergeNodesShouldRemoveMappingsFromMergedNodesAfterTransfer() {
        GraphPublishedNodeId retainedNodeId = new GraphPublishedNodeId(1L);
        GraphPublishedNodeId mergedNodeId = new GraphPublishedNodeId(2L);
        GraphPublishedNode retainedNode = node(retainedNodeId, "保留节点");
        GraphPublishedNode mergedNode = node(mergedNodeId, "合并节点");
        GraphPublishedNodeRepository nodeRepository = mock(GraphPublishedNodeRepository.class);
        GraphPublishedEdgeRepository edgeRepository = mock(GraphPublishedEdgeRepository.class);
        GraphPublishedNodePropertyRepository nodePropertyRepository = mock(GraphPublishedNodePropertyRepository.class);
        GraphPublishedEdgePropertyRepository edgePropertyRepository = mock(GraphPublishedEdgePropertyRepository.class);
        GraphPublishedNodeMaterialRepository nodeMaterialRepository = mock(GraphPublishedNodeMaterialRepository.class);
        GraphPublishedEdgeMaterialRepository edgeMaterialRepository = mock(GraphPublishedEdgeMaterialRepository.class);
        when(nodeRepository.getById(retainedNodeId)).thenReturn(retainedNode);
        when(nodeRepository.listByIds(List.of(retainedNodeId, mergedNodeId)))
                .thenReturn(List.of(retainedNode, mergedNode));
        when(nodeRepository.updateIfLockVersion(any(), eq(1L))).thenReturn(1);
        when(edgeRepository.listByNodeIds(any())).thenReturn(List.of());
        when(nodeMaterialRepository.listByPublishedNodeId(retainedNodeId)).thenReturn(List.of());
        when(nodeMaterialRepository.listByPublishedNodeId(mergedNodeId))
                .thenReturn(List.of(new GraphPublishedNodeMaterial(
                        mergedNodeId, new ContentRef("SANCAI_ENTRY", 1001L), "{\"title\":\"原文\"}")));
        GraphPublishedApplicationServiceImpl service = new GraphPublishedApplicationServiceImpl(
                nodeRepository,
                edgeRepository,
                nodePropertyRepository,
                edgePropertyRepository,
                nodeMaterialRepository,
                edgeMaterialRepository,
                mock(GraphSchemaResolver.class),
                Clock.fixed(Instant.parse("2026-08-14T00:00:00Z"), ZoneOffset.UTC));

        service.mergeNodes(new GraphPublishedNodeMergeCommand(retainedNodeId, List.of(mergedNodeId), 1L));

        verify(nodeMaterialRepository)
                .insert(argThat(mapping -> retainedNodeId.equals(mapping.getPublishedNodeId())
                        && new ContentRef("SANCAI_ENTRY", 1001L).equals(mapping.getMaterialRef())
                        && "{\"title\":\"原文\"}".equals(mapping.getSourceSnapshotJson())));
        verify(nodeMaterialRepository).deleteByPublishedNodeIds(List.of(mergedNodeId));
    }

    private GraphPublishedNode node(GraphPublishedNodeId id, String name) {
        return new GraphPublishedNode(
                id,
                null,
                GraphNodeType.PERSON,
                name,
                GraphSourceType.MANUAL,
                GraphPublishedStatus.ACTIVE,
                Instant.parse("2026-08-14T00:00:00Z"),
                1L);
    }
}
