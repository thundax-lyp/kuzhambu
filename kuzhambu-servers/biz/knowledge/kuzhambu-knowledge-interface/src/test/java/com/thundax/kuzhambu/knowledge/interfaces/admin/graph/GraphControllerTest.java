package com.thundax.kuzhambu.knowledge.interfaces.admin.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.common.web.exception.ApiException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionDecisionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialListQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialTreeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskDetailQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchWithdrawalResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionCandidatePreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialTreeNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWorkbenchOverviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialDeletionApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublicationApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublishedApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphDeletionRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphMaterialRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublicationRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphWorkbenchRequests;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphControllerTest {

    @BeforeEach
    void setUpSubject() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "900001",
                KuzhambuSubjectType.ADMIN_USER,
                "graph-admin",
                "test-token",
                List.of("knowledge:graph:edit")));
    }

    @AfterEach
    void tearDownSubject() {
        KuzhambuContextHolder.clear();
    }

    @Test
    void shouldKeepReadAndWritePermissionsOnAdminEndpoints() throws Exception {
        assertThat(permission("materialPage")).isEqualTo("knowledge:graph:view");
        assertThat(permission("materialTreeList")).isEqualTo("knowledge:graph:view");
        assertThat(permission("materialGet")).isEqualTo("knowledge:graph:view");
        assertThat(permission("materialNodeCreate")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("taskPage")).isEqualTo("knowledge:graph:view");
        assertThat(permission("taskGet")).isEqualTo("knowledge:graph:view");
        assertThat(permission("extractionCreate")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("candidateApply")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("publicationPublish")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("withdrawalBatchPreview")).isEqualTo("knowledge:graph:view");
        assertThat(permission("withdrawalBatch")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("deletionChangePage")).isEqualTo("knowledge:graph:view");
        assertThat(permission("deletionChangeDecision")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("deletionTaskRetry")).isEqualTo("knowledge:graph:edit");
        assertThat(permission("publishedNodeDelete")).isEqualTo("knowledge:graph:edit");
    }

    @Test
    void shouldMapMaterialNodeCreateThroughAssemblerAndApplicationService() {
        GraphMaterialApplicationService materialService = mock(GraphMaterialApplicationService.class);
        GraphController controller = controller(materialService);
        GraphMaterialRequests.MaterialObjectRequest request = materialNodeRequest();
        when(materialService.getMaterialGraph(any()))
                .thenReturn(new GraphMaterialResult(
                        null,
                        new GraphMaterial(
                                new ContentRef("SANCAI_ENTRY", 1001L), "三才图会", GraphMaterialStatus.DRAFT, null, 7),
                        null,
                        List.of(),
                        List.of(),
                        null));

        var response = controller.materialNodeCreate(request);

        ArgumentCaptor<GraphMaterialNodeCommand> captor = ArgumentCaptor.forClass(GraphMaterialNodeCommand.class);
        verify(materialService).createNode(captor.capture());
        GraphMaterialNodeCommand command = captor.getValue();
        assertThat(command.materialLockVersion()).isEqualTo(7);
        assertThat(command.node().getMaterialRef()).isEqualTo(new ContentRef("SANCAI_ENTRY", 1001L));
        assertThat(command.node().getName()).isEqualTo("张三");
        assertThat(command.node().getPropertiesJson()).contains("identityQualifier");
        assertThat(response.material().contentRef().contentRefId()).isEqualTo("1001");
    }

    @Test
    void shouldMapMaterialPageTaskFiltersThroughAssemblerAndApplicationService() {
        GraphMaterialApplicationService materialService = mock(GraphMaterialApplicationService.class);
        GraphController controller = controller(materialService);
        GraphMaterialRequests.MaterialPageRequest request = new GraphMaterialRequests.MaterialPageRequest();
        request.setTaskExecutionStatus("SUCCEEDED");
        request.setTaskDisposition("PENDING");
        request.setPageNo("1");
        request.setPageSize("10");
        when(materialService.pageMaterials(any(), any())).thenReturn(PageResult.of(1, 10, 0, List.of()));

        controller.materialPage(request);

        ArgumentCaptor<GraphMaterialListQuery> captor = ArgumentCaptor.forClass(GraphMaterialListQuery.class);
        verify(materialService).pageMaterials(captor.capture(), any());
        assertThat(captor.getValue().taskExecutionStatus()).isEqualTo("SUCCEEDED");
        assertThat(captor.getValue().taskDisposition()).isEqualTo("PENDING");
    }

    @Test
    void shouldLoadMaterialTaskSummaryAndTaskRecordsForMaterialDetail() {
        GraphMaterialApplicationService materialService = mock(GraphMaterialApplicationService.class);
        GraphExtractionApplicationService extractionService = mock(GraphExtractionApplicationService.class);
        GraphController controller = new GraphController(
                mock(GraphWorkbenchApplicationService.class),
                materialService,
                extractionService,
                mock(GraphPublicationApplicationService.class),
                mock(GraphPublishedApplicationService.class),
                mock(GraphMaterialDeletionApplicationService.class));
        ContentRef contentRef = new ContentRef("SANCAI_ENTRY", 1001L);
        GraphMaterialRequests.ContentRefRequest request = new GraphMaterialRequests.ContentRefRequest();
        request.setContentType(contentRef.getContentType());
        request.setContentRefId(String.valueOf(contentRef.getContentId()));
        when(materialService.getMaterialGraph(any()))
                .thenReturn(new GraphMaterialResult(
                        null,
                        new GraphMaterial(contentRef, "三才图会", GraphMaterialStatus.DRAFT, null, 7),
                        null,
                        List.of(),
                        List.of(),
                        null));
        when(extractionService.pageTasks(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        50,
                        1,
                        List.of(new GraphExtractionTaskResult(
                                7001L,
                                contentRef,
                                "SUCCEEDED",
                                "PENDING",
                                1,
                                1L,
                                null,
                                8001L,
                                "完成",
                                100,
                                Instant.now(),
                                Instant.now(),
                                null,
                                null))));
        when(extractionService.getTask(any()))
                .thenReturn(new GraphExtractionTaskDetailResult(
                        new GraphExtractionTaskResult(
                                7001L,
                                contentRef,
                                "SUCCEEDED",
                                "PENDING",
                                1,
                                1L,
                                null,
                                8001L,
                                "完成",
                                100,
                                Instant.now(),
                                Instant.now(),
                                null,
                                null),
                        List.of(),
                        List.of(),
                        new GraphExtractionCandidatePreviewResult(
                                8001L, "GRAPH_DOCUMENT_V1", "{\"nodes\":[],\"edges\":[]}")));

        var response = controller.materialGet(request);

        ArgumentCaptor<GraphTaskQuery> captor = ArgumentCaptor.forClass(GraphTaskQuery.class);
        verify(extractionService).pageTasks(captor.capture(), any());
        assertThat(captor.getValue().contentRefs()).containsExactly(contentRef);
        assertThat(response.extractionTasks())
                .singleElement()
                .extracting(task -> task.id())
                .isEqualTo("7001");
        assertThat(response.taskSummary().activeTaskCount()).isEqualTo("0");
        assertThat(response.taskSummary().totalTaskCount()).isEqualTo("1");
        assertThat(response.latestTaskCandidate())
                .extracting(candidate -> candidate.candidateId(), candidate -> candidate.resultFormat())
                .containsExactly("8001", "GRAPH_DOCUMENT_V1");
        ArgumentCaptor<GraphTaskDetailQuery> detailCaptor = ArgumentCaptor.forClass(GraphTaskDetailQuery.class);
        verify(extractionService).getTask(detailCaptor.capture());
        assertThat(detailCaptor.getValue().taskId()).isEqualTo(7001L);
    }

    @Test
    void shouldMapOverviewSnapshotTimestamp() {
        GraphWorkbenchApplicationService workbenchService = mock(GraphWorkbenchApplicationService.class);
        when(workbenchService.getOverview())
                .thenReturn(new GraphWorkbenchOverviewResult(
                        Instant.parse("2026-08-19T04:00:00Z"), 12L, 18L, 4L, 1L, 2L, List.of(), 3L));
        GraphController controller = new GraphController(
                workbenchService,
                mock(GraphMaterialApplicationService.class),
                mock(GraphExtractionApplicationService.class),
                mock(GraphPublicationApplicationService.class),
                mock(GraphPublishedApplicationService.class),
                mock(GraphMaterialDeletionApplicationService.class));

        var response = controller.overview(new GraphWorkbenchRequests.OverviewGetRequest());

        assertThat(response.snapshotAt()).isEqualTo("1787112000000");
        assertThat(response.publishedNodeCount()).isEqualTo("12");
    }

    @Test
    void shouldMapMaterialTreeParentIdThroughAssemblerAndApplicationService() {
        GraphMaterialApplicationService materialService = mock(GraphMaterialApplicationService.class);
        GraphController controller = controller(materialService);
        GraphMaterialRequests.MaterialTreeRequest request = new GraphMaterialRequests.MaterialTreeRequest();
        request.setParentId("type:SANCAI_ENTRY:category:TIANWEN");
        when(materialService.listMaterialTree(any()))
                .thenReturn(List.of(new GraphMaterialTreeNodeResult(
                        "type:SANCAI_ENTRY:category:TIANWEN:volume:V1",
                        "type:SANCAI_ENTRY:category:TIANWEN",
                        "卷一",
                        "volume",
                        true)));

        var response = controller.materialTreeList(request);

        ArgumentCaptor<GraphMaterialTreeQuery> captor = ArgumentCaptor.forClass(GraphMaterialTreeQuery.class);
        verify(materialService).listMaterialTree(captor.capture());
        assertThat(captor.getValue().parentId()).isEqualTo("type:SANCAI_ENTRY:category:TIANWEN");
        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo("type:SANCAI_ENTRY:category:TIANWEN:volume:V1");
        assertThat(response.get(0).leaf()).isTrue();
    }

    @Test
    void shouldPropagateBusinessErrorForUnifiedApiExceptionMapping() {
        GraphMaterialApplicationService materialService = mock(GraphMaterialApplicationService.class);
        GraphController controller = controller(materialService);
        GraphMaterialRequests.MaterialObjectRequest request = materialNodeRequest();
        when(materialService.createNode(any())).thenThrow(new ApiException("GRAPH_LOCK_CONFLICT"));

        assertThrows(ApiException.class, () -> controller.materialNodeCreate(request));
    }

    @Test
    void shouldMapBatchWithdrawalThroughAssemblerAndApplicationService() {
        GraphPublicationApplicationService publicationService = mock(GraphPublicationApplicationService.class);
        GraphController controller = controller(publicationService);
        GraphPublicationRequests.BatchWithdrawalRequest request = batchWithdrawalRequest();
        when(publicationService.withdrawBatch(any()))
                .thenReturn(new GraphBatchWithdrawalResult(
                        "batch-001",
                        List.of(new GraphWithdrawalResult(
                                new ContentRef("SANCAI_ENTRY", 1001L),
                                true,
                                new GraphMaterial(
                                        new ContentRef("SANCAI_ENTRY", 1001L),
                                        "三才图会",
                                        GraphMaterialStatus.DRAFT,
                                        null,
                                        8L),
                                null,
                                null))));

        var response = controller.withdrawalBatch(request);

        ArgumentCaptor<GraphBatchWithdrawalCommand> captor = ArgumentCaptor.forClass(GraphBatchWithdrawalCommand.class);
        verify(publicationService).withdrawBatch(captor.capture());
        assertThat(captor.getValue().idempotencyKey()).isEqualTo("batch-001");
        assertThat(captor.getValue().materials()).hasSize(2);
        assertThat(captor.getValue().materials().get(1).materialRef()).isEqualTo(new ContentRef("SANCAI_ENTRY", 1002L));
        assertThat(captor.getValue().materials().get(1).materialLockVersion()).isEqualTo(9L);
        assertThat(response.materials())
                .extracting(item -> item.contentRef().contentRefId())
                .containsExactly("1001");
    }

    @Test
    void shouldMapDeletionDecisionThroughAssemblerAndApplicationService() {
        GraphMaterialDeletionApplicationService deletionService = mock(GraphMaterialDeletionApplicationService.class);
        GraphController controller = controller(deletionService);
        GraphDeletionRequests.DeletionDecisionRequest request = new GraphDeletionRequests.DeletionDecisionRequest();
        request.setChangeId("9101");
        request.setDecision("WITHDRAW_ASSOCIATIONS");
        request.setLockVersion("4");
        when(deletionService.decide(any()))
                .thenReturn(new GraphMaterialDeletionTask(
                        new GraphMaterialDeletionTaskId(9201L),
                        new GraphMaterialDeletionChangeId(9101L),
                        "graph-material-deletion:9101",
                        GraphMaterialDeletionStatus.PENDING,
                        5L,
                        0,
                        null,
                        null,
                        Instant.parse("2026-08-17T00:00:00Z"),
                        null));

        var response = controller.deletionChangeDecision(request);

        ArgumentCaptor<GraphMaterialDeletionDecisionCommand> captor =
                ArgumentCaptor.forClass(GraphMaterialDeletionDecisionCommand.class);
        verify(deletionService).decide(captor.capture());
        assertThat(captor.getValue().changeId().value()).isEqualTo(9101L);
        assertThat(captor.getValue().decision().name()).isEqualTo("WITHDRAW_ASSOCIATIONS");
        assertThat(captor.getValue().lockVersion()).isEqualTo(4L);
        assertThat(response.id()).isEqualTo("9201");
        assertThat(response.lockVersion()).isEqualTo("5");
    }

    private static String permission(String methodName) {
        for (Method method : GraphController.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method.getAnnotation(HasPermission.class).value()[0];
            }
        }
        throw new AssertionError("missing method " + methodName);
    }

    private static GraphController controller(GraphMaterialApplicationService materialService) {
        return new GraphController(
                mock(GraphWorkbenchApplicationService.class),
                materialService,
                mock(GraphExtractionApplicationService.class),
                mock(GraphPublicationApplicationService.class),
                mock(GraphPublishedApplicationService.class),
                mock(GraphMaterialDeletionApplicationService.class));
    }

    private static GraphController controller(GraphPublicationApplicationService publicationService) {
        return new GraphController(
                mock(GraphWorkbenchApplicationService.class),
                mock(GraphMaterialApplicationService.class),
                mock(GraphExtractionApplicationService.class),
                publicationService,
                mock(GraphPublishedApplicationService.class),
                mock(GraphMaterialDeletionApplicationService.class));
    }

    private static GraphController controller(GraphMaterialDeletionApplicationService deletionService) {
        return new GraphController(
                mock(GraphWorkbenchApplicationService.class),
                mock(GraphMaterialApplicationService.class),
                mock(GraphExtractionApplicationService.class),
                mock(GraphPublicationApplicationService.class),
                mock(GraphPublishedApplicationService.class),
                deletionService);
    }

    private static GraphMaterialRequests.MaterialObjectRequest materialNodeRequest() {
        GraphMaterialRequests.MaterialObjectRequest request = new GraphMaterialRequests.MaterialObjectRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setContentRefId("1001");
        request.setMaterialLockVersion("7");
        GraphMaterialRequests.MaterialObjectRequestData node = new GraphMaterialRequests.MaterialObjectRequestData();
        node.setNodeType("PERSON");
        node.setName("张三");
        node.setSource("MANUAL");
        node.setProperties(Map.of("identityQualifier", "明代"));
        request.setNode(node);
        return request;
    }

    private static GraphPublicationRequests.BatchWithdrawalRequest batchWithdrawalRequest() {
        GraphPublicationRequests.BatchWithdrawalRequest request = new GraphPublicationRequests.BatchWithdrawalRequest();
        request.setIdempotencyKey("batch-001");
        GraphPublicationRequests.WithdrawalRequest first = withdrawalRequest("1001", "8");
        GraphPublicationRequests.WithdrawalRequest second = withdrawalRequest("1002", "9");
        request.setMaterials(List.of(first, second));
        return request;
    }

    private static GraphPublicationRequests.WithdrawalRequest withdrawalRequest(
            String contentRefId, String lockVersion) {
        GraphPublicationRequests.WithdrawalRequest request = new GraphPublicationRequests.WithdrawalRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setContentRefId(contentRefId);
        request.setMaterialLockVersion(lockVersion);
        return request;
    }
}
