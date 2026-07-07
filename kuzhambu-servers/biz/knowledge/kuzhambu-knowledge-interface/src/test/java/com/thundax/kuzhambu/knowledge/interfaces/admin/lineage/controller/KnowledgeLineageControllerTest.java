package com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.AvailableFiltersView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.NodeView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.RelationView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.SourceRefView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.SummaryView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.VersionOptionView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.VersionView;
import com.thundax.kuzhambu.knowledge.application.lineage.service.KnowledgeLineageReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller.request.LineageCanvasRequest;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class KnowledgeLineageControllerTest {

    @Test
    void routesAndPermissionsShouldRemainStable() throws Exception {
        RequestMapping root = KnowledgeLineageController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/knowledge/lineage", root.value()[0]);
        Method method = KnowledgeLineageController.class.getDeclaredMethod("getCanvas", LineageCanvasRequest.class);
        assertEquals("canvas", method.getAnnotation(PostMapping.class).value()[0]);
        assertEquals(
                "knowledge:graph:view",
                method.getAnnotation(HasPermission.class).value()[0]);
    }

    @Test
    void getCanvasShouldMapRequestAndResponseFields() {
        KnowledgeLineageReadApplicationService service = mock(KnowledgeLineageReadApplicationService.class);
        KnowledgeLineageController controller = new KnowledgeLineageController(service);
        when(service.getCanvas(any())).thenReturn(result());
        LineageCanvasRequest request = new LineageCanvasRequest();
        request.setVersionId(71L);
        request.setFocusNodeId(301L);
        request.setFocusRelationId(401L);
        request.setKeyword("贾");
        request.setNodeType("PERSON");
        request.setRelationType("PARENT_CHILD");
        request.setConfirmationStatus("CONFIRMED");
        request.setDepth(3);

        var response = controller.getCanvas(request);

        ArgumentCaptor<LineageCanvasQuery> captor = ArgumentCaptor.forClass(LineageCanvasQuery.class);
        verify(service).getCanvas(captor.capture());
        assertEquals(71L, captor.getValue().getVersionId());
        assertEquals(301L, captor.getValue().getFocusNodeId());
        assertEquals(401L, captor.getValue().getFocusRelationId());
        assertEquals("贾", captor.getValue().getKeyword());
        assertEquals("PERSON", captor.getValue().getNodeType());
        assertEquals("PARENT_CHILD", captor.getValue().getRelationType());
        assertEquals("CONFIRMED", captor.getValue().getConfirmationStatus());
        assertEquals(3, captor.getValue().getDepth());
        assertEquals(71L, response.getVersion().getVersionId());
        assertEquals(1L, response.getSummary().getNodeCount());
        assertEquals(301L, response.getNodes().get(0).getNodeId());
        assertEquals(
                "SANCAI_ENTRY",
                response.getNodes().get(0).getSourceRefs().get(0).getSourceContentType());
        assertEquals(401L, response.getRelations().get(0).getRelationId());
        assertEquals("PARENT_CHILD", response.getRelations().get(0).getRelationLabel());
        assertEquals(301L, response.getSelectedNode().getNodeId());
        assertEquals(401L, response.getSelectedRelation().getRelationId());
        assertEquals("PERSON", response.getAvailableFilters().getNodeTypes().get(0));
    }

    private static LineageCanvasResult result() {
        NodeView node = new NodeView(
                "lineage-node:301",
                301L,
                "person:father",
                "贾代善",
                "PERSON",
                1,
                "MALE",
                "CONFIRMED",
                null,
                "[]",
                List.of(new SourceRefView("SANCAI_ENTRY", 1001L, "红楼梦", "来源片段", "/source/1001")),
                1_000L,
                2_000L,
                null,
                null);
        RelationView relation = new RelationView(
                "lineage-relation:401",
                401L,
                301L,
                "贾代善",
                302L,
                "贾政",
                "PARENT_CHILD",
                "PARENT_CHILD",
                "CONFIRMED",
                null,
                "[]",
                List.of(new SourceRefView("SANCAI_ENTRY", 1001L, "红楼梦", "关系来源", null)),
                1_000L,
                2_000L);
        return new LineageCanvasResult(
                new VersionView(71L, 3, "LINEAGE", "APPLIED", "SANCAI_ENTRY", 1001L, "PEOPLE", "人物", 3_000L),
                new SummaryView(1L, 1L, 1L, 1L, 301L, 401L),
                List.of(node),
                List.of(relation),
                node,
                relation,
                new AvailableFiltersView(
                        List.of(new VersionOptionView(
                                71L, 3, "LINEAGE", "APPLIED", "SANCAI_ENTRY", 1001L, "PEOPLE", "人物", 3_000L)),
                        List.of("PERSON"),
                        List.of("PARENT_CHILD"),
                        List.of("CONFIRMED")),
                null);
    }
}
