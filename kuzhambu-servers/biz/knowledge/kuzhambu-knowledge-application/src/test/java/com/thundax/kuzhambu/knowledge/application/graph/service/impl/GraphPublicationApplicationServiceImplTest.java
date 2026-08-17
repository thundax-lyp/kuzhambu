package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialStatsRefresher;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphPublicationExecutor;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublicationPreviewToken;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublicationPreviewTokenRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphPublicationApplicationServiceImplTest {

    @Test
    void batchPreviewShouldPreserveInputOrderAndIssueIndependentTokens() {
        Fixture fixture = new Fixture();
        ContentRef firstRef = new ContentRef("SANCAI_ENTRY", 1001L);
        ContentRef secondRef = new ContentRef("SANCAI_ENTRY", 1002L);
        when(fixture.graphLoader.require(firstRef)).thenReturn(graph(firstRef, "卷一", 7L));
        when(fixture.graphLoader.require(secondRef)).thenReturn(graph(secondRef, "卷二", 8L));

        var result = fixture.service.previewBatchPublication(
                new GraphBatchPublicationPreviewQuery(List.of(firstRef, secondRef)));

        assertThat(result.materials()).extracting(item -> item.materialRef()).containsExactly(firstRef, secondRef);
        assertThat(result.materials()).extracting(item -> item.previewToken()).doesNotHaveDuplicates();
        ArgumentCaptor<GraphPublicationPreviewToken> captor =
                ArgumentCaptor.forClass(GraphPublicationPreviewToken.class);
        verify(fixture.previewTokenRepository, org.mockito.Mockito.times(2)).insert(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(GraphPublicationPreviewToken::getMaterialRef)
                .containsExactly(firstRef, secondRef);
    }

    @Test
    void batchPublishShouldReturnFailuresInPlaceWithoutShortCircuitingFollowingMaterials() {
        Fixture fixture = new Fixture();
        ContentRef firstRef = new ContentRef("SANCAI_ENTRY", 1001L);
        ContentRef secondRef = new ContentRef("SANCAI_ENTRY", 1002L);
        ContentRef thirdRef = new ContentRef("SANCAI_ENTRY", 1003L);
        GraphPublicationCommand first = command(firstRef);
        GraphPublicationCommand second = command(secondRef);
        GraphPublicationCommand third = command(thirdRef);
        when(fixture.executor.publishOne(first)).thenReturn(success(firstRef));
        when(fixture.executor.publishOne(second)).thenThrow(new RuntimeException("preview stale"));
        when(fixture.executor.publishOne(third)).thenReturn(success(thirdRef));
        when(fixture.graphLoader.require(secondRef)).thenReturn(graph(secondRef, "卷二", 4L));

        var result = fixture.service.publishBatch(new GraphBatchPublicationCommand(List.of(first, second, third)));

        assertThat(result.materials())
                .extracting(GraphPublicationResult::materialRef)
                .containsExactly(firstRef, secondRef, thirdRef);
        assertThat(result.materials())
                .extracting(GraphPublicationResult::success)
                .containsExactly(true, false, true);
        assertThat(result.materials().get(1).materialStatus()).isEqualTo(GraphMaterialStatus.DRAFT);
        assertThat(result.materials().get(1).failureMessage()).isEqualTo("preview stale");
        verify(fixture.executor).publishOne(third);
        verify(fixture.statsRefresher).refresh(firstRef);
        verify(fixture.statsRefresher).refresh(thirdRef);
    }

    @Test
    void batchWithdrawalPreviewShouldReturnFailuresInPlace() {
        Fixture fixture = new Fixture();
        ContentRef firstRef = new ContentRef("SANCAI_ENTRY", 1001L);
        ContentRef secondRef = new ContentRef("SANCAI_ENTRY", 1002L);
        when(fixture.nodeMaterialRepository.listByMaterial(firstRef)).thenReturn(List.of());
        when(fixture.edgeMaterialRepository.listByMaterial(firstRef)).thenReturn(List.of());
        when(fixture.nodeMaterialRepository.listByMaterial(secondRef))
                .thenThrow(new BizException("GRAPH_LOCK_CONFLICT", "graph.lock.conflict", "lock conflict"));

        var result = fixture.service.previewBatchWithdrawal(
                new GraphBatchWithdrawalPreviewQuery(List.of(firstRef, secondRef)));

        assertThat(result.materials()).extracting(item -> item.contentRef()).containsExactly(firstRef, secondRef);
        assertThat(result.materials().get(0).preview()).isNotNull();
        assertThat(result.materials().get(1).preview()).isNull();
        assertThat(result.materials().get(1).failureCode()).isEqualTo("GRAPH_LOCK_CONFLICT");
    }

    @Test
    void batchWithdrawShouldPreserveOrderAndContinueAfterFailure() {
        Fixture fixture = new Fixture();
        ContentRef firstRef = new ContentRef("SANCAI_ENTRY", 1001L);
        ContentRef secondRef = new ContentRef("SANCAI_ENTRY", 1002L);
        ContentRef thirdRef = new ContentRef("SANCAI_ENTRY", 1003L);
        GraphWithdrawalCommand first = new GraphWithdrawalCommand(firstRef, 7L);
        GraphWithdrawalCommand second = new GraphWithdrawalCommand(secondRef, 8L);
        GraphWithdrawalCommand third = new GraphWithdrawalCommand(thirdRef, 9L);
        when(fixture.executor.withdrawOne(first)).thenReturn(material(firstRef, GraphMaterialStatus.DRAFT, 9L));
        when(fixture.executor.withdrawOne(second))
                .thenThrow(new BizException("GRAPH_LOCK_CONFLICT", "graph.lock.conflict", "lock conflict"));
        when(fixture.executor.withdrawOne(third)).thenReturn(material(thirdRef, GraphMaterialStatus.DRAFT, 10L));

        var result = fixture.service.withdrawBatch(
                new GraphBatchWithdrawalCommand(List.of(first, second, third), "batch-001"));

        assertThat(result.batchId()).isEqualTo("batch-001");
        assertThat(result.materials())
                .extracting(GraphWithdrawalResult::contentRef)
                .containsExactly(firstRef, secondRef, thirdRef);
        assertThat(result.materials())
                .extracting(GraphWithdrawalResult::success)
                .containsExactly(true, false, true);
        assertThat(result.materials().get(1).failureCode()).isEqualTo("GRAPH_LOCK_CONFLICT");
        verify(fixture.executor).withdrawOne(third);
        verify(fixture.statsRefresher, org.mockito.Mockito.times(2))
                .refresh(org.mockito.ArgumentMatchers.any(GraphMaterial.class));
    }

    private static GraphPublicationCommand command(ContentRef ref) {
        return new GraphPublicationCommand(ref, 4L, 9001L, "token-" + ref.getContentId(), List.of());
    }

    private static GraphPublicationResult success(ContentRef ref) {
        return new GraphPublicationResult(ref, GraphMaterialStatus.PUBLISHED, true, null, 1, 0, 0, 0, List.of());
    }

    private static GraphMaterialGraph graph(ContentRef ref, String title, long lockVersion) {
        return GraphMaterialGraph.of(
                new GraphMaterial(ref, title, GraphMaterialStatus.DRAFT, null, lockVersion), List.of(), List.of());
    }

    private static GraphMaterial material(ContentRef ref, GraphMaterialStatus status, long lockVersion) {
        return new GraphMaterial(ref, "素材-" + ref.getContentId(), status, null, lockVersion);
    }

    private static final class Fixture {
        private final GraphMaterialGraphLoader graphLoader = mock(GraphMaterialGraphLoader.class);
        private final GraphMaterialStatsRefresher statsRefresher = mock(GraphMaterialStatsRefresher.class);
        private final GraphPublicationExecutor executor = mock(GraphPublicationExecutor.class);
        private final GraphPublishedNodeRepository publishedNodeRepository = mock(GraphPublishedNodeRepository.class);
        private final GraphPublishedEdgeRepository publishedEdgeRepository = mock(GraphPublishedEdgeRepository.class);
        private final GraphPublishedNodeMaterialRepository nodeMaterialRepository =
                mock(GraphPublishedNodeMaterialRepository.class);
        private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository =
                mock(GraphPublishedEdgeMaterialRepository.class);
        private final GraphPublicationPreviewTokenRepository previewTokenRepository =
                mock(GraphPublicationPreviewTokenRepository.class);
        private final GraphPublicationApplicationServiceImpl service = new GraphPublicationApplicationServiceImpl(
                new ObjectMapper(),
                graphLoader,
                statsRefresher,
                executor,
                publishedNodeRepository,
                publishedEdgeRepository,
                nodeMaterialRepository,
                edgeMaterialRepository,
                previewTokenRepository,
                Clock.fixed(Instant.parse("2026-08-14T00:00:00Z"), ZoneOffset.UTC));
    }
}
