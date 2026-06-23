package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeEntityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphExtractionRequests;
import org.junit.jupiter.api.Test;

class KnowledgeGraphExtractionControllerTest {

    @Test
    void pageVersionsShouldMapReadableVersionPage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.VersionPageRequest request = new GraphExtractionRequests.VersionPageRequest();
        request.setPageNo(1);
        request.setPageSize(10);
        request.setTaskType("GRAPH");
        when(service.pageVersions(any(), any(), any(), any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new GraphVersionResult(
                                71L, "31", 901L, "GRAPH", "SANCAI_ENTRY", 1001L, 2, "APPLIED", 1L))));

        var response = controller.pageVersions(request);

        verify(service).pageVersions(any(), any(), any(), any(), any());
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
        when(service.getVersionDetail(71L))
                .thenReturn(new GraphVersionResult(71L, "31", 901L, "GRAPH", "SANCAI_ENTRY", 1001L, 2, "APPLIED", 1L));

        var response = controller.getVersionDetail(request);

        verify(service).getVersionDetail(71L);
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
        when(service.pageEntities(any(), any(), any(), any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new KnowledgeEntityResult(
                                1001L, "person:huangdi", "黄帝", "PERSON", "始祖", "CONFIRMED", 71L, "[]", 1L, 2L, 3L))));

        var response = controller.pageEntities(request);

        verify(service).pageEntities(any(), any(), any(), any(), any());
        assertEquals("person:huangdi", response.getRecords().get(0).getEntityKey());
    }

    @Test
    void getEntityDetailShouldMapSingleEntity() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.EntityIdRequest request = new GraphExtractionRequests.EntityIdRequest();
        request.setEntityId(1001L);
        when(service.getEntityDetail(1001L))
                .thenReturn(new KnowledgeEntityResult(
                        1001L, "person:huangdi", "黄帝", "PERSON", "始祖", "CONFIRMED", 71L, "[]", 1L, 2L, 3L));

        var response = controller.getEntityDetail(request);

        verify(service).getEntityDetail(1001L);
        assertEquals("黄帝", response.getName());
    }

    @Test
    void pageRelationsShouldMapReadableRelationPage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.RelationPageRequest request = new GraphExtractionRequests.RelationPageRequest();
        request.setVersionId(71L);
        when(service.pageRelations(any(), any(), any(), any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        10,
                        1,
                        java.util.List.of(new KnowledgeRelationResult(
                                2001L,
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

        var response = controller.pageRelations(request);

        verify(service).pageRelations(any(), any(), any(), any(), any());
        assertEquals("ANCESTOR", response.getRecords().get(0).getRelationType());
    }

    @Test
    void getRelationDetailShouldMapSingleRelation() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.RelationIdRequest request = new GraphExtractionRequests.RelationIdRequest();
        request.setRelationId(2001L);
        when(service.getRelationDetail(2001L))
                .thenReturn(new KnowledgeRelationResult(
                        2001L,
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

        var response = controller.getRelationDetail(request);

        verify(service).getRelationDetail(2001L);
        assertEquals("黄帝", response.getSourceName());
    }

    @Test
    void pageLineageNodesShouldMapReadableNodePage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.LineageNodePageRequest request = new GraphExtractionRequests.LineageNodePageRequest();
        request.setVersionId(71L);
        when(service.pageLineageNodes(any(), any(), any(), any(), any()))
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

        verify(service).pageLineageNodes(any(), any(), any(), any(), any());
        assertEquals(3001L, response.getRecords().get(0).getNodeId());
    }

    @Test
    void getLineageNodeDetailShouldMapSingleNode() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.LineageNodeIdRequest request = new GraphExtractionRequests.LineageNodeIdRequest();
        request.setNodeId(3001L);
        when(service.getLineageNodeDetail(3001L))
                .thenReturn(new KnowledgeLineageNodeResult(
                        3001L, "person:huangdi", "黄帝", "PERSON", 1, "MALE", "CONFIRMED", 71L, "[]", 1L, 2L, 3L));

        var response = controller.getLineageNodeDetail(request);

        verify(service).getLineageNodeDetail(3001L);
        assertEquals("黄帝", response.getName());
    }

    @Test
    void pageLineageRelationsShouldMapReadableRelationPage() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.LineageRelationPageRequest request =
                new GraphExtractionRequests.LineageRelationPageRequest();
        request.setVersionId(71L);
        when(service.pageLineageRelations(any(), any(), any(), any(), any()))
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

        verify(service).pageLineageRelations(any(), any(), any(), any(), any());
        assertEquals("ANCESTOR", response.getRecords().get(0).getRelationType());
    }

    @Test
    void getLineageRelationDetailShouldMapSingleRelation() {
        KnowledgeGraphExtractionApplicationService service = mock(KnowledgeGraphExtractionApplicationService.class);
        KnowledgeGraphExtractionController controller = new KnowledgeGraphExtractionController(service);
        GraphExtractionRequests.LineageRelationIdRequest request =
                new GraphExtractionRequests.LineageRelationIdRequest();
        request.setRelationId(4001L);
        when(service.getLineageRelationDetail(4001L))
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

        verify(service).getLineageRelationDetail(4001L);
        assertEquals("伏羲", response.getTargetName());
    }
}
