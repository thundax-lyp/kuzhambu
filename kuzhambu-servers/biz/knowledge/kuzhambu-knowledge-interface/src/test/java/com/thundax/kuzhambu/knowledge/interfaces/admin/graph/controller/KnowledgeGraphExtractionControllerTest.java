package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.CancelGraphExtractionBatchCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RegenerateGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphExtractionTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphVersionQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeEntityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeLineageNodeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeLineageRelationQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeRelationQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchCancelResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeEntityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphExtractionRequests;
import org.junit.jupiter.api.Test;

class KnowledgeGraphExtractionControllerTest {

    @Test
    void cancelBatchTaskShouldMapBatchCancelResponse() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.BatchCancelRequest request = new GraphExtractionRequests.BatchCancelRequest();
        request.setBatchJobId(1001L);
        request.setRequestedBy(99L);
        when(service.cancelBatch(any(CancelGraphExtractionBatchCommand.class)))
                .thenReturn(new GraphExtractionBatchCancelResult(1001L, "CANCELLED", 1, 1, 0));

        var response = controller.cancelBatchTask(request);

        verify(service).cancelBatch(argThat(command -> command.batchJobId() == 1001L && command.requestedBy() == 99L));
        assertEquals(1001L, response.getBatchJobId());
        assertEquals("CANCELLED", response.getStatus());
        assertEquals(1, response.getCancelledCount());
    }

    @Test
    void regenerateTaskShouldMapTaskResponse() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.RegenerateRequest request = new GraphExtractionRequests.RegenerateRequest();
        request.setTaskType("GRAPH");
        request.setSourceTaskId(88L);
        request.setTriggerSource("REFINEMENT_APPLIED");
        request.setSelectionScopeJson("{\"sourceContentIds\":[88,89]}");
        request.setReplaceUnconfirmedOnly(Boolean.TRUE);
        request.setRequestedBy(99L);
        when(service.regenerateTask(any(RegenerateGraphExtractionCommand.class)))
                .thenReturn(new GraphExtractionTaskResult(
                        "9001",
                        1001L,
                        "GRAPH",
                        "CLASSICS_ENTRY",
                        "{\"entryId\":88}",
                        "REFINEMENT_APPLIED",
                        "{\"sourceContentIds\":[88,89]}",
                        Boolean.TRUE,
                        88L,
                        "SANCAI_ENTRY",
                        88L,
                        null,
                        null,
                        "REQUESTED",
                        null,
                        null,
                        99L,
                        1710000000000L,
                        null,
                        null));

        var response = controller.regenerateTask(request);

        verify(service)
                .regenerateTask(argThat(command -> "GRAPH".equals(command.taskType())
                        && command.sourceTaskId().value() == 88L
                        && "REFINEMENT_APPLIED".equals(command.triggerSource())
                        && "{\"sourceContentIds\":[88,89]}".equals(command.selectionScopeJson())
                        && Boolean.TRUE.equals(command.replaceUnconfirmedOnly())
                        && command.requestedBy() == 99L));
        assertEquals("9001", response.getTaskId());
        assertEquals(1001L, response.getBatchJobId());
        assertEquals("REFINEMENT_APPLIED", response.getTriggerSource());
    }

    @Test
    void pageTasksShouldForwardBatchAndTriggerFilters() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.PageTaskRequest request = new GraphExtractionRequests.PageTaskRequest();
        request.setPageNo(1);
        request.setPageSize(10);
        request.setTaskType("GRAPH");
        request.setBatchJobId(1001L);
        request.setTriggerSource("QUALITY_REPORT");
        when(service.pageTasks(any(GraphExtractionTaskQuery.class), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new GraphExtractionTaskResult(
                                "11",
                                1001L,
                                "GRAPH",
                                null,
                                null,
                                "QUALITY_REPORT",
                                null,
                                Boolean.TRUE,
                                null,
                                "SANCAI_ENTRY",
                                1001L,
                                null,
                                null,
                                "SUCCEEDED",
                                null,
                                null,
                                1L,
                                1710000000000L,
                                1710000001000L,
                                null))));

        var response = controller.pageTasks(request);

        verify(service)
                .pageTasks(
                        argThat((GraphExtractionTaskQuery query) -> "GRAPH".equals(query.taskType())
                                && Long.valueOf(1001L).equals(query.batchJobId())
                                && "QUALITY_REPORT".equals(query.triggerSource())),
                        any());
        assertEquals(1, response.getRecords().size());
        assertEquals(1001L, response.getRecords().get(0).getBatchJobId());
        assertEquals("QUALITY_REPORT", response.getRecords().get(0).getTriggerSource());
    }

    @Test
    void pageVersionsShouldMapReadableVersionPage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.VersionPageRequest request = new GraphExtractionRequests.VersionPageRequest();
        request.setPageNo(1);
        request.setPageSize(10);
        request.setTaskType("GRAPH");
        when(service.pageVersions(any(GraphVersionQuery.class), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new GraphVersionResult(
                                71L, "31", 901L, "GRAPH", "SANCAI_ENTRY", 1001L, 2, "APPLIED", 1L))));

        var response = controller.pageVersions(request);

        verify(service).pageVersions(any(GraphVersionQuery.class), any());
        assertEquals(1, response.getRecords().size());
        assertEquals(71L, response.getRecords().get(0).getVersionId());
        assertEquals("31", response.getRecords().get(0).getTaskId());
    }

    @Test
    void getVersionDetailShouldMapSingleVersion() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.VersionIdRequest request = new GraphExtractionRequests.VersionIdRequest();
        request.setVersionId(71L);
        when(service.getVersionDetail(GraphVersionIdCodec.toDomain(71L)))
                .thenReturn(new GraphVersionResult(71L, "31", 901L, "GRAPH", "SANCAI_ENTRY", 1001L, 2, "APPLIED", 1L));

        var response = controller.getVersionDetail(request);

        verify(service).getVersionDetail(GraphVersionIdCodec.toDomain(71L));
        assertEquals(71L, response.getVersionId());
        assertEquals("GRAPH", response.getTaskType());
    }

    @Test
    void pageEntitiesShouldMapReadableEntityPage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.EntityPageRequest request = new GraphExtractionRequests.EntityPageRequest();
        request.setVersionId(71L);
        request.setKeyword("黄帝");
        when(service.pageEntities(any(KnowledgeEntityQuery.class), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new KnowledgeEntityResult(
                                1001L, "person:huangdi", "黄帝", "PERSON", "始祖", "CONFIRMED", 71L, "[]", 1L, 2L, 3L))));

        var response = controller.pageEntities(request);

        verify(service).pageEntities(any(KnowledgeEntityQuery.class), any());
        assertEquals("person:huangdi", response.getRecords().get(0).getEntityKey());
    }

    @Test
    void getEntityDetailShouldMapSingleEntity() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.EntityIdRequest request = new GraphExtractionRequests.EntityIdRequest();
        request.setEntityId(1001L);
        when(service.getEntityDetail(KnowledgeEntityIdCodec.toDomain(1001L)))
                .thenReturn(new KnowledgeEntityResult(
                        1001L, "person:huangdi", "黄帝", "PERSON", "始祖", "CONFIRMED", 71L, "[]", 1L, 2L, 3L));

        var response = controller.getEntityDetail(request);

        verify(service).getEntityDetail(KnowledgeEntityIdCodec.toDomain(1001L));
        assertEquals("黄帝", response.getName());
    }

    @Test
    void pageRelationsShouldMapReadableRelationPage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.RelationPageRequest request = new GraphExtractionRequests.RelationPageRequest();
        request.setVersionId(71L);
        when(service.pageRelations(any(KnowledgeRelationQuery.class), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new KnowledgeRelationResult(
                                2001L,
                                "person:huangdi->person:fuxi:ancestor",
                                "黄帝",
                                "人物",
                                "伏羲",
                                "人物",
                                "ANCESTOR",
                                "谱系",
                                "CONFIRMED",
                                71L,
                                "[]",
                                1L,
                                2L,
                                3L))));

        var response = controller.pageRelations(request);

        verify(service).pageRelations(any(KnowledgeRelationQuery.class), any());
        assertEquals("ANCESTOR", response.getRecords().get(0).getRelationType());
    }

    @Test
    void getRelationDetailShouldMapSingleRelation() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.RelationIdRequest request = new GraphExtractionRequests.RelationIdRequest();
        request.setRelationId(2001L);
        when(service.getRelationDetail(any(KnowledgeRelationQuery.class)))
                .thenReturn(new KnowledgeRelationResult(
                        2001L,
                        "person:huangdi->person:fuxi:ancestor",
                        "黄帝",
                        "人物",
                        "伏羲",
                        "人物",
                        "ANCESTOR",
                        "谱系",
                        "CONFIRMED",
                        71L,
                        "[]",
                        1L,
                        2L,
                        3L));

        var response = controller.getRelationDetail(request);

        verify(service).getRelationDetail(argThat(query -> Long.valueOf(2001L).equals(query.relationId())));
        assertEquals("黄帝", response.getSourceName());
    }

    @Test
    void pageLineageNodesShouldMapReadableNodePage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.LineageNodePageRequest request = new GraphExtractionRequests.LineageNodePageRequest();
        request.setVersionId(71L);
        when(service.pageLineageNodes(any(KnowledgeLineageNodeQuery.class), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new KnowledgeLineageNodeResult(
                                3001L,
                                "person:huangdi",
                                "黄帝",
                                "PERSON",
                                1,
                                "MALE",
                                "CONFIRMED",
                                71L,
                                "[]",
                                1L,
                                2L,
                                3L))));

        var response = controller.pageLineageNodes(request);

        verify(service).pageLineageNodes(any(KnowledgeLineageNodeQuery.class), any());
        assertEquals(3001L, response.getRecords().get(0).getNodeId());
    }

    @Test
    void getLineageNodeDetailShouldMapSingleNode() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.LineageNodeIdRequest request = new GraphExtractionRequests.LineageNodeIdRequest();
        request.setNodeId(3001L);
        when(service.getLineageNodeDetail(any(KnowledgeLineageNodeQuery.class)))
                .thenReturn(new KnowledgeLineageNodeResult(
                        3001L, "person:huangdi", "黄帝", "PERSON", 1, "MALE", "CONFIRMED", 71L, "[]", 1L, 2L, 3L));

        var response = controller.getLineageNodeDetail(request);

        verify(service)
                .getLineageNodeDetail(argThat(query -> Long.valueOf(3001L).equals(query.nodeId())));
        assertEquals("黄帝", response.getName());
    }

    @Test
    void pageLineageRelationsShouldMapReadableRelationPage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.LineageRelationPageRequest request =
                new GraphExtractionRequests.LineageRelationPageRequest();
        request.setVersionId(71L);
        when(service.pageLineageRelations(any(KnowledgeLineageRelationQuery.class), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new KnowledgeLineageRelationResult(
                                4001L,
                                "person:huangdi->person:fuxi:ancestor",
                                "黄帝",
                                "伏羲",
                                "ANCESTOR",
                                "谱系",
                                "CONFIRMED",
                                71L,
                                "[]",
                                1L,
                                2L,
                                3L))));

        var response = controller.pageLineageRelations(request);

        verify(service).pageLineageRelations(any(KnowledgeLineageRelationQuery.class), any());
        assertEquals("ANCESTOR", response.getRecords().get(0).getRelationType());
    }

    @Test
    void getLineageRelationDetailShouldMapSingleRelation() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.LineageRelationIdRequest request =
                new GraphExtractionRequests.LineageRelationIdRequest();
        request.setRelationId(4001L);
        when(service.getLineageRelationDetail(any(KnowledgeLineageRelationQuery.class)))
                .thenReturn(new KnowledgeLineageRelationResult(
                        4001L,
                        "person:huangdi->person:fuxi:ancestor",
                        "黄帝",
                        "伏羲",
                        "ANCESTOR",
                        "谱系",
                        "CONFIRMED",
                        71L,
                        "[]",
                        1L,
                        2L,
                        3L));

        var response = controller.getLineageRelationDetail(request);

        verify(service)
                .getLineageRelationDetail(argThat(query -> Long.valueOf(4001L).equals(query.relationId())));
        assertEquals("伏羲", response.getTargetName());
    }
}
