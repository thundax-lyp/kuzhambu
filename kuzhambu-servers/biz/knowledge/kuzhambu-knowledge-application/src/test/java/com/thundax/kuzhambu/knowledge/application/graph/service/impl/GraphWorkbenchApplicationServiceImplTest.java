package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
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
}
