package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedNodeDetailResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceOperation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphManualSource;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphGovernanceOperationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphManualSourceRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import com.thundax.kuzhambu.system.facade.SystemAuditFacade;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphPublishedApplicationServiceImplTest {

    @Test
    void createNodeShouldRecordGovernanceAuditAndManualSource() {
        Fixture fixture = new Fixture();
        GraphPublishedNodeId createdNodeId = new GraphPublishedNodeId(11L);
        GraphPublishedNode node = node(null, "新节点");
        when(fixture.nodeRepository.insert(node)).thenReturn(createdNodeId);

        fixture.service.createNode(
                new GraphPublishedNodeCommand(node, List.of(nodeProperty(null, null, "name", "新节点", true)), "补录节点"));

        verify(fixture.governanceOperationRepository)
                .insert(argThat(operation ->
                        operationMatches(operation, "CREATE", "NODE", createdNodeId.value(), "补录节点", null, "\"新节点\"")));
        verify(fixture.manualSourceRepository)
                .insert(argThat(source -> sourceMatches(source, "NODE", createdNodeId.value(), "补录节点", 9001L)));
    }

    @Test
    void updateEdgeShouldRecordGovernanceAuditWithBeforeAndAfterSnapshot() {
        Fixture fixture = new Fixture();
        GraphPublishedNode sourceNode = node(new GraphPublishedNodeId(1L), "源节点");
        GraphPublishedNode targetNode = node(new GraphPublishedNodeId(2L), "目标节点");
        GraphPublishedEdgeId edgeId = new GraphPublishedEdgeId(21L);
        GraphPublishedEdge existing = edge(edgeId, sourceNode.getId(), targetNode.getId(), "RELATED_TO", 3L);
        GraphPublishedEdge changed = edge(edgeId, sourceNode.getId(), targetNode.getId(), "MENTIONS", 3L);
        when(fixture.edgeRepository.getById(edgeId)).thenReturn(existing);
        when(fixture.nodeRepository.getById(sourceNode.getId())).thenReturn(sourceNode);
        when(fixture.nodeRepository.getById(targetNode.getId())).thenReturn(targetNode);
        when(fixture.edgeRepository.updateIfLockVersion(existing, 3L)).thenReturn(1);

        fixture.service.updateEdge(new GraphPublishedEdgeCommand(changed, List.of(), "修正关系类型"));

        verify(fixture.governanceOperationRepository)
                .insert(argThat(operation -> operationMatches(
                        operation, "UPDATE", "EDGE", edgeId.value(), "修正关系类型", "RELATED_TO", "MENTIONS")));
    }

    @Test
    void deleteNodeShouldRecordGovernanceAuditWithDeletedSnapshot() {
        Fixture fixture = new Fixture();
        GraphPublishedNodeId nodeId = new GraphPublishedNodeId(31L);
        GraphPublishedNode node = node(nodeId, "待删节点");
        when(fixture.nodeRepository.getById(nodeId)).thenReturn(node);
        when(fixture.nodeRepository.updateIfLockVersion(node, 1L)).thenReturn(1);

        fixture.service.deleteNode(new GraphPublishedNodeDeleteCommand(nodeId, true, 1L, "删除重复节点"));

        verify(fixture.governanceOperationRepository)
                .insert(argThat(operation ->
                        operationMatches(operation, "DELETE", "NODE", nodeId.value(), "删除重复节点", "ACTIVE", "DELETED")));
    }

    @Test
    void mergeNodesShouldRecordAuditAndRemoveMappingsFromMergedNodesAfterTransfer() {
        Fixture fixture = new Fixture();
        GraphPublishedNodeId retainedNodeId = new GraphPublishedNodeId(1L);
        GraphPublishedNodeId mergedNodeId = new GraphPublishedNodeId(2L);
        GraphPublishedNode retainedNode = node(retainedNodeId, "保留节点");
        GraphPublishedNode mergedNode = node(mergedNodeId, "合并节点");
        when(fixture.nodeRepository.getById(retainedNodeId)).thenReturn(retainedNode);
        when(fixture.nodeRepository.listByIds(List.of(retainedNodeId, mergedNodeId)))
                .thenReturn(List.of(retainedNode, mergedNode));
        when(fixture.nodeRepository.updateIfLockVersion(any(), eq(1L))).thenReturn(1);
        when(fixture.nodeMaterialRepository.listByPublishedNodeId(mergedNodeId))
                .thenReturn(List.of(new GraphPublishedNodeMaterial(
                        mergedNodeId, new ContentRef("SANCAI_ENTRY", 1001L), "{\"title\":\"原文\"}")));

        fixture.service.mergeNodes(
                new GraphPublishedNodeMergeCommand(retainedNodeId, List.of(mergedNodeId), 1L, "合并同名节点"));

        verify(fixture.nodeMaterialRepository)
                .insert(argThat(mapping -> retainedNodeId.equals(mapping.getPublishedNodeId())
                        && new ContentRef("SANCAI_ENTRY", 1001L).equals(mapping.getMaterialRef())
                        && "{\"title\":\"原文\"}".equals(mapping.getSourceSnapshotJson())));
        verify(fixture.nodeMaterialRepository).deleteByPublishedNodeIds(List.of(mergedNodeId));
        verify(fixture.governanceOperationRepository)
                .insert(argThat(operation -> operationMatches(
                        operation, "MERGE", "NODE", retainedNodeId.value(), "合并同名节点", "保留节点", "保留节点")));
    }

    @Test
    void splitNodeShouldRecordGovernanceAuditForSplitNodeDetail() {
        Fixture fixture = new Fixture();
        GraphPublishedNodeId sourceNodeId = new GraphPublishedNodeId(41L);
        GraphPublishedNodeId splitNodeId = new GraphPublishedNodeId(42L);
        GraphPublishedNode sourceNode = node(sourceNodeId, "原节点");
        GraphPublishedNode splitNode = node(null, "拆分节点");
        GraphPublishedNodeProperty sourceProperty =
                nodeProperty(new GraphPublishedNodePropertyId(401L), sourceNodeId, "name", "原节点", true);
        when(fixture.nodeRepository.getById(sourceNodeId)).thenReturn(sourceNode);
        when(fixture.nodeRepository.getById(splitNodeId)).thenReturn(splitNode);
        when(fixture.nodeRepository.insert(splitNode)).thenReturn(splitNodeId);
        when(fixture.nodeRepository.updateIfLockVersion(sourceNode, 1L)).thenReturn(1);
        when(fixture.nodePropertyRepository.listByPublishedNodeId(sourceNodeId)).thenReturn(List.of(sourceProperty));
        when(fixture.nodePropertyRepository.getById(sourceProperty.getId())).thenReturn(sourceProperty);
        when(fixture.nodePropertyRepository.listByPublishedNodeId(splitNodeId)).thenReturn(List.of(sourceProperty));

        GraphPublishedNodeDetailResult result = fixture.service.splitNode(new GraphPublishedNodeSplitCommand(
                sourceNodeId,
                splitNode,
                List.of(sourceProperty.getId()),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                1L,
                "拆分复合节点"));

        assertThat(result.node()).isSameAs(splitNode);
        verify(fixture.governanceOperationRepository)
                .insert(argThat(operation ->
                        operationMatches(operation, "SPLIT", "NODE", splitNodeId.value(), "拆分复合节点", "原节点", "拆分节点")));
    }

    private static boolean operationMatches(
            GraphGovernanceOperation operation,
            String operationType,
            String targetType,
            Long targetId,
            String reason,
            String beforeContains,
            String afterContains) {
        return operationType.equals(operation.getOperationType())
                && targetType.equals(operation.getTargetType())
                && targetId.equals(operation.getTargetId())
                && reason.equals(operation.getReason())
                && operation.getAuditLogId().equals(9001L)
                && (beforeContains == null || operation.getBeforeSnapshotJson().contains(beforeContains))
                && operation.getAfterSnapshotJson().contains(afterContains);
    }

    private static boolean sourceMatches(
            GraphManualSource source, String targetType, Long targetId, String reason, Long auditLogId) {
        return targetType.equals(source.getTargetType())
                && targetId.equals(source.getTargetId())
                && reason.equals(source.getReason())
                && auditLogId.equals(source.getAuditLogId());
    }

    private static GraphPublishedNode node(GraphPublishedNodeId id, String name) {
        GraphPublishedNode node = new GraphPublishedNode(
                id,
                null,
                GraphNodeType.PERSON,
                name,
                GraphSourceType.MANUAL,
                GraphPublishedStatus.ACTIVE,
                Instant.parse("2026-08-14T00:00:00Z"),
                1L);
        node.refreshNodeKey(null);
        return node;
    }

    private static GraphPublishedEdge edge(
            GraphPublishedEdgeId id,
            GraphPublishedNodeId sourceNodeId,
            GraphPublishedNodeId targetNodeId,
            String relationType,
            long lockVersion) {
        return new GraphPublishedEdge(
                id,
                null,
                sourceNodeId,
                targetNodeId,
                relationType,
                GraphSourceType.MANUAL,
                "{}",
                GraphPublishedStatus.ACTIVE,
                Instant.parse("2026-08-14T00:00:00Z"),
                lockVersion);
    }

    private static GraphPublishedNodeProperty nodeProperty(
            GraphPublishedNodePropertyId id,
            GraphPublishedNodeId publishedNodeId,
            String propertyKey,
            String value,
            boolean preferred) {
        return new GraphPublishedNodeProperty(id, publishedNodeId, propertyKey, value, preferred);
    }

    private static final class Fixture {
        private final GraphPublishedNodeRepository nodeRepository = mock(GraphPublishedNodeRepository.class);
        private final GraphPublishedEdgeRepository edgeRepository = mock(GraphPublishedEdgeRepository.class);
        private final GraphPublishedNodePropertyRepository nodePropertyRepository =
                mock(GraphPublishedNodePropertyRepository.class);
        private final GraphPublishedEdgePropertyRepository edgePropertyRepository =
                mock(GraphPublishedEdgePropertyRepository.class);
        private final GraphPublishedNodeMaterialRepository nodeMaterialRepository =
                mock(GraphPublishedNodeMaterialRepository.class);
        private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository =
                mock(GraphPublishedEdgeMaterialRepository.class);
        private final GraphGovernanceOperationRepository governanceOperationRepository =
                mock(GraphGovernanceOperationRepository.class);
        private final GraphManualSourceRepository manualSourceRepository = mock(GraphManualSourceRepository.class);
        private final GraphSchemaResolver schemaResolver = mock(GraphSchemaResolver.class);
        private final SystemAuditFacade auditFacade = mock(SystemAuditFacade.class);
        private final GraphPublishedApplicationServiceImpl service;

        private Fixture() {
            when(auditFacade.record(any())).thenReturn(9001L);
            when(schemaResolver.directed(any())).thenReturn(true);
            when(schemaResolver.keyQualifiers(any(), any())).thenReturn(java.util.Map.of());
            service = new GraphPublishedApplicationServiceImpl(
                    nodeRepository,
                    edgeRepository,
                    nodePropertyRepository,
                    edgePropertyRepository,
                    nodeMaterialRepository,
                    edgeMaterialRepository,
                    governanceOperationRepository,
                    manualSourceRepository,
                    auditFacade,
                    schemaResolver,
                    Clock.fixed(Instant.parse("2026-08-14T00:00:00Z"), ZoneOffset.UTC));
        }
    }
}
