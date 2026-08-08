package com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalQualityResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.request.KnowledgePortalQualityRequest;
import com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.response.KnowledgePortalQualityResponse;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class KnowledgePortalQualityControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepPortalApiPaths() throws Exception {
        assertRequestMapping(KnowledgePortalQualityController.class, "/api/portal/knowledge/quality");
        assertPostMapping(
                KnowledgePortalQualityController.class, "getQuality", "get", KnowledgePortalQualityRequest.class);
    }

    @Test
    void queryAndResponseJsonFieldsShouldRemainStable() throws Exception {
        KnowledgePortalQualityRequest query = OBJECT_MAPPER.readValue(
                """
                {
                  "date": "2026-06-01",
                  "range": "90d",
                  "knowledgeBase": "SANCAI_ENTRY"
                }
                """,
                KnowledgePortalQualityRequest.class);
        assertEquals("2026-06-01", query.getDate());
        var queryNode = OBJECT_MAPPER.valueToTree(query);
        assertTrue(queryNode.has("date"));
        assertTrue(queryNode.has("range"));
        assertTrue(queryNode.has("knowledgeBase"));

        var response = new KnowledgePortalQualityResponse(
                List.of(new KnowledgePortalQualityResponse.QualityStatResponse(
                        "entity-confirmed-rate", "实体确认率", "50%", "ratio", "说明", "watch")),
                List.of(new KnowledgePortalQualityResponse.TrendSeriesResponse(
                        "monthly-new-tags",
                        "月度新增标签",
                        List.of(new KnowledgePortalQualityResponse.TrendPointResponse("2026-05", 3L)))),
                List.of(new KnowledgePortalQualityResponse.SourceBreakdownResponse("MANUAL", "MANUAL", 8L, "描述")),
                List.of(new KnowledgePortalQualityResponse.FocusIssueResponse(
                        "存在待处理治理任务", "说明", "medium", "/knowledge/quality")),
                List.of(new KnowledgePortalQualityResponse.SourceDetailResponse(
                        "SANCAI_ENTRY", "三才图会", 1L, "APPLIED", "/knowledge/atlas")));
        var node = OBJECT_MAPPER.valueToTree(response);
        assertTrue(node.has("qualityStats"));
        assertTrue(node.has("trendSeries"));
        assertTrue(node.has("sourceBreakdowns"));
        assertTrue(node.has("focusIssues"));
        assertTrue(node.has("sourceDetails"));
    }

    @Test
    void getQualityShouldMapPortalReadModel() {
        KnowledgePortalReadApplicationService service = mock(KnowledgePortalReadApplicationService.class);
        KnowledgePortalQualityController controller = new KnowledgePortalQualityController(service);
        when(service.getQuality())
                .thenReturn(new KnowledgePortalQualityResult(
                        List.of(new KnowledgePortalQualityResult.QualityStatItem(
                                "entity-confirmed-rate", "实体确认率", "50%", "ratio", "说明", "watch")),
                        List.of(new KnowledgePortalQualityResult.TrendSeries(
                                "monthly-new-tags",
                                "月度新增标签",
                                List.of(new KnowledgePortalQualityResult.TrendPoint("2026-05", 3L)))),
                        List.of(new KnowledgePortalQualityResult.SourceBreakdownItem("MANUAL", "MANUAL", 8L, "描述")),
                        List.of(new KnowledgePortalQualityResult.FocusIssueItem(
                                "存在待处理治理任务", "说明", "medium", "/knowledge/quality")),
                        List.of(new KnowledgePortalQualityResult.SourceDetailItem(
                                "SANCAI_ENTRY", "三才图会", 1L, "APPLIED", "/knowledge/atlas"))));

        var response = controller.getQuality(new KnowledgePortalQualityRequest());

        verify(service).getQuality();
        assertEquals("50%", response.getQualityStats().get(0).getValue());
        assertEquals(
                "2026-05", response.getTrendSeries().get(0).getPoints().get(0).getLabel());
        assertEquals("MANUAL", response.getSourceBreakdowns().get(0).getSourceKey());
        assertEquals("/knowledge/atlas", response.getSourceDetails().get(0).getHref());
    }

    private void assertRequestMapping(Class<?> type, String expectedPath) {
        RequestMapping mapping = type.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private void assertPostMapping(Class<?> type, String methodName, String expectedPath, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }
}
