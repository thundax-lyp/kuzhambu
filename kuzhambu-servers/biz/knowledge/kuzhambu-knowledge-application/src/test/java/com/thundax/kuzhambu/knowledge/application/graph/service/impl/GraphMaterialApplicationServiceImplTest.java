package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.KnowledgeGraphMaterialSnapshotFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.KnowledgeGraphMaterialTreeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.KnowledgeGraphMaterialPageFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.KnowledgeGraphMaterialSnapshotFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.KnowledgeGraphMaterialTreeFacadeResponse;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphDocumentMerger;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphSaver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialStatsRefresher;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSnapshotResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialListQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialTreeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialStatsRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphMaterialApplicationServiceImplTest {

    private final GraphMaterialRepository materialRepository = mock(GraphMaterialRepository.class);
    private final ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
    private final GraphMaterialStatsRepository statsRepository = mock(GraphMaterialStatsRepository.class);
    private final GraphExtractionTaskRepository taskRepository = mock(GraphExtractionTaskRepository.class);
    private final GraphExtractionApplicationService extractionApplicationService =
            mock(GraphExtractionApplicationService.class);
    private final GraphMaterialNodeRepository nodeRepository = mock(GraphMaterialNodeRepository.class);
    private final GraphMaterialEdgeRepository edgeRepository = mock(GraphMaterialEdgeRepository.class);

    private final GraphMaterialApplicationServiceImpl service = new GraphMaterialApplicationServiceImpl(
            materialRepository,
            classicsFacade,
            statsRepository,
            taskRepository,
            extractionApplicationService,
            nodeRepository,
            edgeRepository,
            mock(GraphMaterialContentResolver.class),
            mock(GraphMaterialGraphLoader.class),
            mock(GraphMaterialGraphSaver.class),
            mock(GraphMaterialStatsRefresher.class),
            mock(GraphSnapshotResolver.class),
            mock(GraphSchemaResolver.class),
            mock(GraphDocumentMerger.class));

    @Test
    void shouldPageClassicsSourcesAndOverlayKnowledgeStateInSourceOrder() {
        ContentRef firstRef = new ContentRef("SANCAI_ENTRY", 1001L);
        ContentRef secondRef = new ContentRef("SANCAI_ENTRY", 1002L);
        GraphMaterial material =
                new GraphMaterial(11L, firstRef, "素材一", GraphMaterialStatus.DRAFT, null, null, null, null, 3L);
        GraphMaterialStats stats =
                new GraphMaterialStats(11L, 8, 5, 2, 1, 0, 1, 0, 3, Instant.parse("2026-08-17T00:00:00Z"));
        GraphExtractionTask task = latestTask(11L, firstRef);
        when(classicsFacade.pageKnowledgeGraphMaterials(any()))
                .thenReturn(KnowledgeGraphMaterialPageFacadeResponse.builder()
                        .pageNo(1)
                        .pageSize(2)
                        .totalCount(2)
                        .records(List.of(source("1001", "素材一"), source("1002", "素材二")))
                        .build());
        when(materialRepository.listByContentRefs(List.of(firstRef, secondRef))).thenReturn(List.of(material));
        when(statsRepository.listByMaterialIds(List.of(11L))).thenReturn(List.of(stats));
        when(taskRepository.listLatestByMaterialIds(List.of(11L))).thenReturn(List.of(task));

        var result = service.pageMaterials(
                new GraphMaterialListQuery("user-1", "素材", null, "SANCAI_ENTRY", "cat-a", "vol-a", null, null),
                new PageQuery(1, 2));

        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).source().contentRef()).isEqualTo(firstRef);
        assertThat(result.getRecords().get(0).material()).isEqualTo(material);
        assertThat(result.getRecords().get(0).materialStats()).isEqualTo(stats);
        assertThat(result.getRecords().get(0).latestTask().taskId()).isEqualTo(7001L);
        assertThat(result.getRecords().get(1).source().contentRef()).isEqualTo(secondRef);
        assertThat(result.getRecords().get(1).material()).isNull();
        verify(materialRepository, never()).getByContentRef(any());
        verify(extractionApplicationService).syncActiveTasks(List.of(11L));
    }

    @Test
    void shouldFilterMaterialPageByLatestTaskStatusAndDisposition() {
        ContentRef firstRef = new ContentRef("SANCAI_ENTRY", 1001L);
        ContentRef secondRef = new ContentRef("SANCAI_ENTRY", 1002L);
        GraphMaterial firstMaterial =
                new GraphMaterial(11L, firstRef, "素材一", GraphMaterialStatus.DRAFT, null, null, null, null, 3L);
        GraphMaterial secondMaterial =
                new GraphMaterial(12L, secondRef, "素材二", GraphMaterialStatus.DRAFT, null, null, null, null, 3L);
        GraphExtractionTask matchingTask = latestTask(11L, firstRef);
        GraphExtractionTask unmatchedTask = latestTask(12L, secondRef);
        unmatchedTask.setDisposition(null);
        when(taskRepository.listContentRefsByTaskState(
                        GraphExtractionExecutionStatus.SUCCEEDED, GraphExtractionDisposition.PENDING))
                .thenReturn(List.of(firstRef));
        when(classicsFacade.pageKnowledgeGraphMaterials(any()))
                .thenReturn(KnowledgeGraphMaterialPageFacadeResponse.builder()
                        .pageNo(1)
                        .pageSize(2)
                        .totalCount(1)
                        .records(List.of(source("1001", "素材一")))
                        .build());
        when(materialRepository.listByContentRefs(List.of(firstRef))).thenReturn(List.of(firstMaterial));
        when(statsRepository.listByMaterialIds(List.of(11L))).thenReturn(List.of());
        when(taskRepository.listLatestByMaterialIds(List.of(11L))).thenReturn(List.of(matchingTask));

        var result = service.pageMaterials(
                new GraphMaterialListQuery(
                        "user-1", "素材", null, "SANCAI_ENTRY", "cat-a", "vol-a", "SUCCEEDED", "PENDING"),
                new PageQuery(1, 2));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getRecords().get(0).source().contentRef()).isEqualTo(firstRef);
    }

    @Test
    void shouldFilterMaterialPageByStatusBeforeReturningPageTotals() {
        ContentRef firstRef = new ContentRef("SANCAI_ENTRY", 1001L);
        ContentRef secondRef = new ContentRef("SANCAI_ENTRY", 1002L);
        ContentRef thirdRef = new ContentRef("SANCAI_ENTRY", 1003L);
        GraphMaterial publishedMaterial =
                new GraphMaterial(11L, firstRef, "素材一", GraphMaterialStatus.PUBLISHED, null, null, null, null, 3L);
        GraphMaterial draftMaterial =
                new GraphMaterial(12L, secondRef, "素材二", GraphMaterialStatus.DRAFT, null, null, null, null, 3L);
        when(materialRepository.listContentRefsByStatuses(List.of(
                        GraphMaterialStatus.PUBLISHING,
                        GraphMaterialStatus.PUBLISHED,
                        GraphMaterialStatus.WITHDRAWING,
                        GraphMaterialStatus.FAILED)))
                .thenReturn(List.of(firstRef));
        when(classicsFacade.pageKnowledgeGraphMaterials(any()))
                .thenReturn(KnowledgeGraphMaterialPageFacadeResponse.builder()
                        .pageNo(1)
                        .pageSize(1)
                        .totalCount(2)
                        .records(List.of(source("1002", "素材二")))
                        .build());
        when(materialRepository.listByContentRefs(List.of(secondRef))).thenReturn(List.of(draftMaterial));
        when(statsRepository.listByMaterialIds(List.of(12L))).thenReturn(List.of());
        when(taskRepository.listLatestByMaterialIds(List.of(12L))).thenReturn(List.of());

        var result = service.pageMaterials(
                new GraphMaterialListQuery(
                        "user-1", "素材", GraphMaterialStatus.DRAFT, "SANCAI_ENTRY", null, null, null, null),
                new PageQuery(1, 1));

        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).source().contentRef()).isEqualTo(secondRef);
    }

    @Test
    void shouldReturnUninitializedMaterialDetailAfterSourceVisibilityCheck() {
        ContentRef materialRef = new ContentRef("SANCAI_ENTRY", 1001L);
        when(classicsFacade.getKnowledgeGraphMaterialSnapshot(any()))
                .thenReturn(KnowledgeGraphMaterialSnapshotFacadeResponse.builder()
                        .source(source("1001", "素材一"))
                        .contentSnapshot("{\"title\":\"素材一\"}")
                        .build());
        when(materialRepository.getByContentRef(materialRef)).thenReturn(null);

        var result = service.getMaterialGraph(new GraphMaterialQuery("user-1", materialRef));

        assertThat(result.source().title()).isEqualTo("素材一");
        assertThat(result.material()).isNull();
        assertThat(result.nodes()).isEmpty();
        assertThat(result.edges()).isEmpty();
        verify(nodeRepository, never()).listByMaterial(any());
        verify(edgeRepository, never()).listByMaterial(any());
    }

    @Test
    void shouldStopBeforeKnowledgeReadWhenSourceIsInvisible() {
        ContentRef materialRef = new ContentRef("SANCAI_ENTRY", 1001L);
        when(classicsFacade.getKnowledgeGraphMaterialSnapshot(any(KnowledgeGraphMaterialSnapshotFacadeRequest.class)))
                .thenThrow(new BizException("Classics content is invisible"));

        assertThatThrownBy(() -> service.getMaterialGraph(new GraphMaterialQuery("user-1", materialRef)))
                .isInstanceOf(BizException.class);

        verify(materialRepository, never()).getByContentRef(any());
    }

    @Test
    void shouldDelegateMaterialTreeQueryToClassicsFacade() {
        when(classicsFacade.listKnowledgeGraphMaterialTree(any(KnowledgeGraphMaterialTreeFacadeRequest.class)))
                .thenReturn(KnowledgeGraphMaterialTreeFacadeResponse.builder()
                        .nodes(List.of(KnowledgeGraphMaterialTreeFacadeResponse.Node.builder()
                                .id("type:SANCAI_ENTRY:category:TIANWEN")
                                .parentId("type:SANCAI_ENTRY")
                                .title("天文")
                                .nodeType("category")
                                .leaf(false)
                                .build()))
                        .build());

        var nodes = service.listMaterialTree(new GraphMaterialTreeQuery("type:SANCAI_ENTRY"));

        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0).id()).isEqualTo("type:SANCAI_ENTRY:category:TIANWEN");
        assertThat(nodes.get(0).leaf()).isFalse();
        verify(classicsFacade)
                .listKnowledgeGraphMaterialTree(argThat(request -> "type:SANCAI_ENTRY".equals(request.getParentId())));
    }

    private static KnowledgeGraphMaterialPageFacadeResponse.Source source(String contentId, String title) {
        return KnowledgeGraphMaterialPageFacadeResponse.Source.builder()
                .contentType("SANCAI_ENTRY")
                .contentId(contentId)
                .title(title)
                .categoryCode("cat-a")
                .categoryName("分类")
                .volumeCode("vol-a")
                .volumeName("卷一")
                .graphable(true)
                .build();
    }

    private static GraphExtractionTask latestTask(Long materialId, ContentRef contentRef) {
        return new GraphExtractionTask(
                new GraphExtractionTaskId(7001L),
                materialId,
                contentRef,
                "{}",
                "{}",
                "{}",
                "{}",
                GraphExtractionExecutionStatus.SUCCEEDED,
                GraphExtractionDisposition.PENDING,
                1,
                4,
                null,
                null,
                9001L,
                "CANDIDATE_READY",
                100,
                null,
                "idem-1",
                null,
                null,
                null,
                Instant.parse("2026-08-17T00:00:00Z"),
                Instant.parse("2026-08-17T00:01:00Z"),
                null,
                null);
    }
}
