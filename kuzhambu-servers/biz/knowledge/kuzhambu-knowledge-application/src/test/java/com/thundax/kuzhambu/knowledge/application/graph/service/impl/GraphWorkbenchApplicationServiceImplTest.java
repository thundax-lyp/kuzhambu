package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphOneHopEdgesQuery;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchSnapshotStore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GraphWorkbenchApplicationServiceImplTest {

    @Test
    void listRecentEdgesShouldReturnOnlyTheDeduplicatedEdgeEndpoints() {
        GraphPublishedNodeRepository nodeRepository = mock(GraphPublishedNodeRepository.class);
        GraphPublishedEdgeRepository edgeRepository = mock(GraphPublishedEdgeRepository.class);
        GraphPublishedEdge first = edge(101L, 1L, 2L);
        GraphPublishedEdge second = edge(102L, 2L, 3L);
        when(edgeRepository.listRecentlyUpdated(200)).thenReturn(List.of(first, second));
        when(nodeRepository.listByIds(List.of(
                        new GraphPublishedNodeId(1L), new GraphPublishedNodeId(2L), new GraphPublishedNodeId(3L))))
                .thenReturn(List.of(node(1L), node(2L), node(3L)));

        var result = service(
                        mock(GraphWorkbenchRepository.class),
                        nodeRepository,
                        edgeRepository,
                        mock(GraphSchemaResolver.class),
                        mock(GraphWorkbenchSnapshotStore.class))
                .listRecentEdges();

        assertThat(result.edges()).containsExactly(first, second);
        assertThat(result.nodes()).extracting(node -> node.getId().value()).containsExactly(1L, 2L, 3L);
        verify(edgeRepository).listRecentlyUpdated(200);
    }

    @Test
    void listOneHopEdgesShouldReturnAFixedRepositoryBatchWithoutIsolatedNodes() {
        GraphPublishedNodeRepository nodeRepository = mock(GraphPublishedNodeRepository.class);
        GraphPublishedEdgeRepository edgeRepository = mock(GraphPublishedEdgeRepository.class);
        GraphPublishedEdge edge = edge(101L, 1L, 2L);
        List<GraphPublishedNodeId> nodeIds = List.of(new GraphPublishedNodeId(1L));
        when(edgeRepository.listOneHopEdges(nodeIds, null))
                .thenReturn(new GraphPublishedEdgeSlice(List.of(edge), null, false));
        when(nodeRepository.listByIds(List.of(new GraphPublishedNodeId(1L), new GraphPublishedNodeId(2L))))
                .thenReturn(List.of(node(1L), node(2L)));

        var result = service(
                        mock(GraphWorkbenchRepository.class),
                        nodeRepository,
                        edgeRepository,
                        mock(GraphSchemaResolver.class),
                        mock(GraphWorkbenchSnapshotStore.class))
                .listOneHopEdges(new GraphOneHopEdgesQuery(nodeIds, null));

        assertThat(result.edges()).containsExactly(edge);
        assertThat(result.nodes()).extracting(node -> node.getId().value()).containsExactly(1L, 2L);
        assertThat(result.truncated()).isFalse();
        assertThat(result.nextCursor()).isNull();
        verify(edgeRepository).listOneHopEdges(nodeIds, null);
    }

    @Test
    void getOverviewShouldOnlyReadTheRedisSnapshot() {
        GraphWorkbenchRepository workbenchRepository = mock(GraphWorkbenchRepository.class);
        GraphPublishedNodeRepository nodeRepository = mock(GraphPublishedNodeRepository.class);
        GraphPublishedEdgeRepository edgeRepository = mock(GraphPublishedEdgeRepository.class);
        GraphSchemaResolver schemaResolver = mock(GraphSchemaResolver.class);
        GraphWorkbenchSnapshotStore snapshotStore = mock(GraphWorkbenchSnapshotStore.class);
        when(snapshotStore.get()).thenReturn(Optional.of(snapshot()));

        var result = service(workbenchRepository, nodeRepository, edgeRepository, schemaResolver, snapshotStore)
                .getOverview();

        assertThat(result.snapshotAt()).isEqualTo(Instant.parse("2026-08-19T04:00:00Z"));
        assertThat(result.publishedNodeCount()).isEqualTo(12L);
        assertThat(result.pendingConflictCount()).isEqualTo(7L);
        verifyNoInteractions(workbenchRepository, nodeRepository, edgeRepository, schemaResolver);
    }

    @Test
    void getOverviewShouldExposeUnavailableSnapshotCodeWithoutDatabaseFallback() {
        GraphWorkbenchRepository workbenchRepository = mock(GraphWorkbenchRepository.class);
        GraphWorkbenchSnapshotStore snapshotStore = mock(GraphWorkbenchSnapshotStore.class);
        when(snapshotStore.get()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service(
                                workbenchRepository,
                                mock(GraphPublishedNodeRepository.class),
                                mock(GraphPublishedEdgeRepository.class),
                                mock(GraphSchemaResolver.class),
                                snapshotStore)
                        .getOverview())
                .isInstanceOfSatisfying(BizException.class, exception -> assertThat(exception.getCode())
                        .isEqualTo("WORKBENCH_SNAPSHOT_UNAVAILABLE"));
        verifyNoInteractions(workbenchRepository);
    }

    private static GraphWorkbenchApplicationServiceImpl service(
            GraphWorkbenchRepository workbenchRepository,
            GraphPublishedNodeRepository nodeRepository,
            GraphPublishedEdgeRepository edgeRepository,
            GraphSchemaResolver schemaResolver,
            GraphWorkbenchSnapshotStore snapshotStore) {
        return new GraphWorkbenchApplicationServiceImpl(
                workbenchRepository, nodeRepository, edgeRepository, schemaResolver, snapshotStore);
    }

    private static GraphWorkbenchOverviewSnapshot snapshot() {
        return new GraphWorkbenchOverviewSnapshot(
                Instant.parse("2026-08-19T04:00:00Z"), "fingerprint", 12L, 18L, 4L, 1L, 2L, 7L, List.of());
    }

    private static GraphPublishedEdge edge(long id, long sourceNodeId, long targetNodeId) {
        return new GraphPublishedEdge(
                new GraphPublishedEdgeId(id),
                null,
                new GraphPublishedNodeId(sourceNodeId),
                new GraphPublishedNodeId(targetNodeId),
                "RELATED_TO",
                GraphSourceType.MANUAL,
                "{}",
                GraphPublishedStatus.ACTIVE,
                Instant.parse("2026-08-19T04:00:00Z"),
                0L);
    }

    private static GraphPublishedNode node(long id) {
        return new GraphPublishedNode(
                new GraphPublishedNodeId(id),
                null,
                GraphNodeType.PERSON,
                "node-" + id,
                GraphSourceType.MANUAL,
                GraphPublishedStatus.ACTIVE,
                Instant.parse("2026-08-19T04:00:00Z"),
                0L);
    }
}
