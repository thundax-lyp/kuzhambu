package com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.AvailableFiltersView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.EmptyView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.NodeView;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult.SummaryView;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class KnowledgePortalLineageControllerTest {

    @Test
    void routesShouldKeepPortalReadOnlyBoundary() throws Exception {
        RequestMapping root = KnowledgePortalLineageController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/portal/knowledge/lineage", root.value()[0]);
        PublicApi publicApi = KnowledgePortalLineageController.class.getAnnotation(PublicApi.class);
        Method method = KnowledgePortalLineageController.class.getDeclaredMethod(
                "getLineage", KnowledgePortalLineageController.Query.class);
        assertEquals("get", method.getAnnotation(PostMapping.class).value()[0]);
        assertEquals(PublicApi.class, publicApi.annotationType());
        assertNull(method.getAnnotation(HasPermission.class));
    }

    @Test
    void getLineageShouldMapQueryAndResponseFields() {
        KnowledgePortalReadApplicationService service = mock(KnowledgePortalReadApplicationService.class);
        KnowledgePortalLineageController controller = new KnowledgePortalLineageController(service);
        when(service.getLineage(any())).thenReturn(result());
        KnowledgePortalLineageController.Query request = new KnowledgePortalLineageController.Query();
        request.setVersionId(71L);
        request.setFocusNodeId(301L);
        request.setFocusRelationId(401L);
        request.setKeyword("贾");
        request.setNodeType("PERSON");
        request.setRelationType("PARENT_CHILD");
        request.setConfirmationStatus("CONFIRMED");
        request.setDepth(2);

        var response = controller.getLineage(request);

        ArgumentCaptor<LineageCanvasQuery> captor = ArgumentCaptor.forClass(LineageCanvasQuery.class);
        verify(service).getLineage(captor.capture());
        assertEquals(71L, captor.getValue().versionId());
        assertEquals(301L, captor.getValue().focusNodeId());
        assertEquals(401L, captor.getValue().focusRelationId());
        assertEquals("贾", captor.getValue().keyword());
        assertEquals("PERSON", captor.getValue().nodeType());
        assertEquals("PARENT_CHILD", captor.getValue().relationType());
        assertEquals("CONFIRMED", captor.getValue().confirmationStatus());
        assertEquals(2, captor.getValue().depth());
        assertEquals(0L, response.getSummary().getNodeCount());
        assertEquals("NO_VERSION", response.getEmpty().getReason());
        assertEquals("请选择世系版本", response.getEmpty().getTitle());
    }

    private static LineageCanvasResult result() {
        return new LineageCanvasResult(
                null,
                new SummaryView(0L, 0L, 0L, 0L, null, null),
                List.of(new NodeView(
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
                        List.of(),
                        null,
                        null,
                        null,
                        null)),
                List.of(),
                null,
                null,
                new AvailableFiltersView(List.of(), List.of("PERSON"), List.of(), List.of("CONFIRMED")),
                new EmptyView("NO_VERSION", "请选择世系版本", "选择一个已应用版本后浏览正式世系节点和关系。", null, null));
    }
}
