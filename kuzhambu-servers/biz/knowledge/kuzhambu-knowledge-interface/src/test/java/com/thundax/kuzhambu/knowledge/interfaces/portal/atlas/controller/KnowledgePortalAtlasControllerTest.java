package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalAtlasResult;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request.KnowledgePortalAtlasQuery;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.response.KnowledgePortalAtlasResponse;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class KnowledgePortalAtlasControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepPortalApiPaths() throws Exception {
        assertRequestMapping(KnowledgePortalAtlasController.class, "/api/portal/knowledge/atlas");
        assertGetMapping(KnowledgePortalAtlasController.class, "getAtlas", KnowledgePortalAtlasQuery.class);
    }

    @Test
    void queryAndResponseJsonFieldsShouldRemainStable() throws Exception {
        KnowledgePortalAtlasQuery query = OBJECT_MAPPER.readValue(
                """
                {
                  "focusId": "3001",
                  "focusType": "PERSON",
                  "knowledgeBase": "SANCAI_ENTRY",
                  "keyword": "黄帝",
                  "tag": "上古",
                  "timeRange": "90d"
                }
                """,
                KnowledgePortalAtlasQuery.class);
        assertEquals("3001", query.getFocusId());
        var queryNode = OBJECT_MAPPER.valueToTree(query);
        assertTrue(queryNode.has("focusId"));
        assertTrue(queryNode.has("focusType"));
        assertTrue(queryNode.has("knowledgeBase"));
        assertTrue(queryNode.has("keyword"));
        assertTrue(queryNode.has("tag"));
        assertTrue(queryNode.has("timeRange"));

        var response = new KnowledgePortalAtlasResponse(
                new KnowledgePortalAtlasResponse.FocusNodeResponse(
                        "3001", "黄帝", "PERSON", "上古始祖", "CONFIRMED", 0.95D, null),
                List.of(new KnowledgePortalAtlasResponse.RelationGroupResponse(
                        "ANCESTOR",
                        "ANCESTOR",
                        List.of(new KnowledgePortalAtlasResponse.RelationItemResponse(
                                "person:huangdi", "黄帝", "ANCESTOR", "person:shaodian", "少典", "ANCESTOR", 0.95D)))),
                List.of(new KnowledgePortalAtlasResponse.SourceReferenceResponse(
                        "1001", "三才图会", "SANCAI_ENTRY", "摘要", 1L, "/knowledge/atlas")),
                List.of(new KnowledgePortalAtlasResponse.RelatedTagResponse("11", "上古", "时代", 0.88D)),
                List.of(new KnowledgePortalAtlasResponse.TimelineItemResponse(
                        "首次抽取", "知识首次进入图谱", "说明", "/knowledge/atlas")),
                new KnowledgePortalAtlasResponse.AvailableFiltersResponse(
                        List.of("SANCAI_ENTRY"),
                        List.of("PERSON"),
                        List.of("ANCESTOR"),
                        List.of("上古"),
                        List.of("30d")));
        var node = OBJECT_MAPPER.valueToTree(response);
        assertTrue(node.has("focusNode"));
        assertTrue(node.has("relationGroups"));
        assertTrue(node.has("sourceReferences"));
        assertTrue(node.has("relatedTags"));
        assertTrue(node.has("timelineItems"));
        assertTrue(node.has("availableFilters"));
    }

    @Test
    void getAtlasShouldMapPortalReadModel() {
        KnowledgePortalReadApplicationService service = mock(KnowledgePortalReadApplicationService.class);
        KnowledgePortalAtlasController controller = new KnowledgePortalAtlasController(service);
        when(service.getAtlas())
                .thenReturn(new KnowledgePortalAtlasResult(
                        new KnowledgePortalAtlasResult.FocusNode(
                                "3001", "黄帝", "PERSON", "上古始祖", "CONFIRMED", 0.95D, null),
                        List.of(new KnowledgePortalAtlasResult.RelationGroup(
                                "ANCESTOR",
                                "ANCESTOR",
                                List.of(new KnowledgePortalAtlasResult.RelationItem(
                                        "person:huangdi",
                                        "黄帝",
                                        "ANCESTOR",
                                        "person:shaodian",
                                        "少典",
                                        "ANCESTOR",
                                        0.95D)))),
                        List.of(new KnowledgePortalAtlasResult.SourceReference(
                                "1001", "三才图会", "SANCAI_ENTRY", "摘要", 1L, "/knowledge/atlas")),
                        List.of(),
                        List.of(new KnowledgePortalAtlasResult.TimelineItem(
                                "首次抽取", "知识首次进入图谱", "说明", "/knowledge/atlas")),
                        new KnowledgePortalAtlasResult.AvailableFilters(
                                List.of("SANCAI_ENTRY"),
                                List.of("PERSON"),
                                List.of("ANCESTOR"),
                                List.of(),
                                List.of("30d"))));

        var response = controller.getAtlas(new KnowledgePortalAtlasQuery());

        verify(service).getAtlas();
        assertEquals("3001", response.getFocusNode().getId());
        assertEquals(
                "person:shaodian",
                response.getRelationGroups().get(0).getRelations().get(0).getTargetId());
        assertEquals(
                "SANCAI_ENTRY",
                response.getAvailableFilters().getKnowledgeBases().get(0));
    }

    private void assertRequestMapping(Class<?> type, String expectedPath) {
        RequestMapping mapping = type.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private void assertGetMapping(Class<?> type, String methodName, Class<?>... parameters) throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertEquals(0, mapping.value().length);
    }
}
