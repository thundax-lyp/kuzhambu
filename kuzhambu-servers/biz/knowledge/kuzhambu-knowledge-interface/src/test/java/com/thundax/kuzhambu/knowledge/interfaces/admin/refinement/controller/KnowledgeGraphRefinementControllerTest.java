package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementApplyResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementEntityResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementProgressSummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementRelationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementWorkbenchItemResult;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeGraphRefinementApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request.RefinementRequests;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeGraphRefinementControllerTest {

    @Test
    void pageTasksShouldMapWorkbenchPage() {
        KnowledgeGraphRefinementApplicationService service = mock(KnowledgeGraphRefinementApplicationService.class);
        KnowledgeGraphRefinementController controller = new KnowledgeGraphRefinementController(service);
        RefinementRequests.TaskPageRequest request = new RefinementRequests.TaskPageRequest();
        request.setPageNo(1);
        request.setPageSize(10);
        request.setTaskType("GRAPH");
        when(service.pageTasks(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        List.of(new RefinementWorkbenchItemResult(
                                31L,
                                71L,
                                "GRAPH",
                                "SANCAI_ENTRY",
                                1001L,
                                "myth",
                                "神话",
                                "DRAFT",
                                1L,
                                2L,
                                new RefinementProgressSummaryResult(3, 1, 4, 2)))));

        var response = controller.pageTasks(request);

        verify(service).pageTasks(any(), any());
        assertEquals(1, response.getRecords().size());
        assertEquals(31L, response.getRecords().get(0).getRefinementTaskId());
        assertEquals("myth", response.getRecords().get(0).getSourceCategoryCode());
    }

    @Test
    void openTaskShouldMapDetailResponse() {
        KnowledgeGraphRefinementApplicationService service = mock(KnowledgeGraphRefinementApplicationService.class);
        KnowledgeGraphRefinementController controller = new KnowledgeGraphRefinementController(service);
        RefinementRequests.TaskOpenRequest request = new RefinementRequests.TaskOpenRequest();
        request.setGraphVersionId(71L);
        request.setOpenedBy(1L);
        when(service.openTask(71L, 1L))
                .thenReturn(new RefinementDetailResult(
                        31L,
                        71L,
                        "GRAPH",
                        "SANCAI_ENTRY",
                        1001L,
                        "myth",
                        "神话",
                        "DRAFT",
                        new RefinementProgressSummaryResult(1, 2, 3, 4),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of()));

        var response = controller.openTask(request);

        verify(service).openTask(71L, 1L);
        assertEquals(31L, response.getRefinementTaskId());
        assertEquals(1, response.getProgressSummary().getEntityPendingCount());
    }

    @Test
    void addEntityShouldMapEntityWrite() {
        KnowledgeGraphRefinementApplicationService service = mock(KnowledgeGraphRefinementApplicationService.class);
        KnowledgeGraphRefinementController controller = new KnowledgeGraphRefinementController(service);
        RefinementRequests.EntityUpsertRequest request = new RefinementRequests.EntityUpsertRequest();
        request.setRefinementTaskId(31L);
        request.setName("黄帝");
        when(service.upsertEntity(any()))
                .thenReturn(new RefinementEntityResult(
                        11L,
                        1001L,
                        "person:huangdi",
                        "AI_EXTRACTED",
                        "UPDATED",
                        "黄帝",
                        "PERSON",
                        "始祖",
                        "PENDING",
                        "[]",
                        1));

        var response = controller.addEntity(request);

        verify(service).upsertEntity(any());
        assertEquals("person:huangdi", response.getEntityKey());
    }

    @Test
    void updateRelationShouldMapRelationWrite() {
        KnowledgeGraphRefinementApplicationService service = mock(KnowledgeGraphRefinementApplicationService.class);
        KnowledgeGraphRefinementController controller = new KnowledgeGraphRefinementController(service);
        RefinementRequests.RelationUpsertRequest request = new RefinementRequests.RelationUpsertRequest();
        request.setRefinementTaskId(31L);
        when(service.upsertRelation(any()))
                .thenReturn(new RefinementRelationResult(
                        12L,
                        2001L,
                        "person:huangdi->person:fuxi:ancestor",
                        "AI_EXTRACTED",
                        "UPDATED",
                        "person:huangdi",
                        "person:fuxi",
                        "黄帝",
                        "伏羲",
                        "ANCESTOR",
                        "谱系",
                        "PENDING",
                        "[]",
                        1));

        var response = controller.updateRelation(request);

        verify(service).upsertRelation(any());
        assertEquals("ANCESTOR", response.getRelationType());
    }

    @Test
    void qualitySummaryShouldMapRates() {
        KnowledgeGraphRefinementApplicationService service = mock(KnowledgeGraphRefinementApplicationService.class);
        KnowledgeGraphRefinementController controller = new KnowledgeGraphRefinementController(service);
        RefinementRequests.QualitySummaryRequest request = new RefinementRequests.QualitySummaryRequest();
        request.setRefinementTaskId(31L);
        when(service.qualitySummary(31L)).thenReturn(new QualitySummaryResult(0.9D, 0.8D, 0.7D));

        var response = controller.qualitySummary(request);

        verify(service).qualitySummary(31L);
        assertEquals(0.9D, response.getEntityCoverageRate());
    }

    @Test
    void applyTaskShouldMapGraphFollowUpResponse() {
        KnowledgeGraphRefinementApplicationService service = mock(KnowledgeGraphRefinementApplicationService.class);
        KnowledgeGraphRefinementController controller = new KnowledgeGraphRefinementController(service);
        RefinementRequests.TaskApplyRequest request = new RefinementRequests.TaskApplyRequest();
        request.setRefinementTaskId(31L);
        request.setAppliedBy(9L);
        when(service.applyTask(31L, 9L))
                .thenReturn(new RefinementApplyResult(
                        31L,
                        71L,
                        "GRAPH",
                        "SANCAI_ENTRY",
                        1001L,
                        "myth",
                        "神话",
                        "APPLIED",
                        1_719_100_800_000L,
                        true,
                        true,
                        88L,
                        "{\"sourceContentIds\":[1001]}",
                        true,
                        "REFINEMENT_APPLIED",
                        "OPEN_GRAPH_VERSION",
                        true));

        var response = controller.applyTask(request);

        verify(service).applyTask(31L, 9L);
        assertEquals(71L, response.getGraphVersionId());
        assertEquals("REFINEMENT_APPLIED", response.getTriggerSource());
        assertEquals(true, response.getReplaceUnconfirmedOnly());
        assertEquals("OPEN_GRAPH_VERSION", response.getNextAction());
        assertEquals(true, response.getQualityReportRefreshRequired());
    }
}
