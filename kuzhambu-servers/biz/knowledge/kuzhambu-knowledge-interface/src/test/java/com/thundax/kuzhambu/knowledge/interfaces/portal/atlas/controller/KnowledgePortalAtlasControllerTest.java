package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.knowledge.application.portal.query.KnowledgePortalAtlasQuery;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalAtlasResult;
import com.thundax.kuzhambu.knowledge.application.portal.service.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.response.KnowledgePortalAtlasResponse;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class KnowledgePortalAtlasControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepPortalApiPaths() throws Exception {
        assertRequestMapping(KnowledgePortalAtlasController.class, "/api/portal/knowledge/atlas");
        assertPostMapping(
                KnowledgePortalAtlasController.class,
                "getAtlas",
                "get",
                com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request.KnowledgePortalAtlasRequest
                        .class);
    }

    @Test
    void queryAndResponseJsonFieldsShouldRemainStable() throws Exception {
        com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request.KnowledgePortalAtlasRequest query =
                OBJECT_MAPPER.readValue(
                        """
                        {
                          "level": "detail",
                          "categoryCode": "ANIMALS",
                          "entityId": 3001,
                          "knowledgeBase": "SANCAI_ENTRY",
                          "keyword": "黄帝",
                          "tag": "上古",
                          "timeRange": "90d"
                        }
                        """,
                        com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request
                                .KnowledgePortalAtlasRequest.class);
        assertEquals("detail", query.getLevel());
        var queryNode = OBJECT_MAPPER.valueToTree(query);
        assertTrue(queryNode.has("level"));
        assertTrue(queryNode.has("categoryCode"));
        assertTrue(queryNode.has("entityId"));
        assertTrue(queryNode.has("knowledgeBase"));
        assertTrue(queryNode.has("keyword"));
        assertTrue(queryNode.has("tag"));
        assertTrue(queryNode.has("timeRange"));

        var response = KnowledgePortalAtlasResponse.builder()
                .currentLevel("detail")
                .breadcrumbItems(List.of(KnowledgePortalAtlasResponse.BreadcrumbItemResponse.builder()
                        .level("detail")
                        .label("黄帝")
                        .href("/knowledge/atlas?level=detail&entityId=3001")
                        .build()))
                .detailView(KnowledgePortalAtlasResponse.DetailViewResponse.builder()
                        .focusNode(KnowledgePortalAtlasResponse.FocusNodeResponse.builder()
                                .id("3001")
                                .title("黄帝")
                                .type("PERSON")
                                .summary("上古始祖")
                                .status("CONFIRMED")
                                .confidence(0.95D)
                                .coverImageUrl(null)
                                .build())
                        .relationGroups(List.of(KnowledgePortalAtlasResponse.RelationGroupResponse.builder()
                                .groupKey("ANCESTOR")
                                .groupLabel("ANCESTOR")
                                .relations(List.of(KnowledgePortalAtlasResponse.RelationItemResponse.builder()
                                        .sourceId("person:huangdi")
                                        .sourceLabel("黄帝")
                                        .relationLabel("ANCESTOR")
                                        .targetId("person:shaodian")
                                        .targetLabel("少典")
                                        .relationType("ANCESTOR")
                                        .weight(0.95D)
                                        .build()))
                                .build()))
                        .sourceReferences(List.of(KnowledgePortalAtlasResponse.SourceReferenceResponse.builder()
                                .sourceId("1001")
                                .sourceTitle("三才图会")
                                .sourceType("SANCAI_ENTRY")
                                .snippet("摘要")
                                .updatedAt(1L)
                                .href("/knowledge/atlas")
                                .build()))
                        .timelineItems(List.of(KnowledgePortalAtlasResponse.TimelineItemResponse.builder()
                                .timeLabel("首次抽取")
                                .title("知识首次进入图谱")
                                .description("说明")
                                .href("/knowledge/atlas")
                                .build()))
                        .relatedTags(List.of(KnowledgePortalAtlasResponse.RelatedTagResponse.builder()
                                .tagId("11")
                                .tagName("上古")
                                .tagCategory("时代")
                                .score(0.88D)
                                .build()))
                        .build())
                .availableFilters(KnowledgePortalAtlasResponse.AvailableFiltersResponse.builder()
                        .knowledgeBases(List.of("SANCAI_ENTRY"))
                        .entityTypes(List.of("PERSON"))
                        .relationTypes(List.of("ANCESTOR"))
                        .tagNames(List.of("上古"))
                        .timeRanges(List.of("30d"))
                        .build())
                .canvasView(KnowledgePortalAtlasResponse.CanvasViewResponse.builder()
                        .mode("detail")
                        .title("黄帝关系图谱")
                        .description("展示实体及其一跳关系。")
                        .focusNodeId("entity:3001")
                        .empty(false)
                        .emptyTitle(null)
                        .emptyDescription(null)
                        .nodes(List.of(KnowledgePortalAtlasResponse.CanvasNodeResponse.builder()
                                .id("entity:3001")
                                .kind("entity")
                                .label("黄帝")
                                .subtitle("PERSON")
                                .metricLabel("置信度")
                                .metricValue(95L)
                                .status("CONFIRMED")
                                .categoryCode("PEOPLE")
                                .entityId(3001L)
                                .href("/knowledge/atlas?level=detail&entityId=3001")
                                .weight(0.95D)
                                .x(0D)
                                .y(0D)
                                .build()))
                        .edges(List.of())
                        .build())
                .build();
        var node = OBJECT_MAPPER.valueToTree(response);
        assertTrue(node.has("currentLevel"));
        assertTrue(node.has("breadcrumbItems"));
        assertTrue(node.has("detailView"));
        assertTrue(node.has("availableFilters"));
        assertTrue(node.has("canvasView"));
    }

    @Test
    void getAtlasShouldMapOverviewPortalReadModel() {
        KnowledgePortalReadApplicationService service = mock(KnowledgePortalReadApplicationService.class);
        KnowledgePortalAtlasController controller = new KnowledgePortalAtlasController(service);
        when(service.getAtlas(any(KnowledgePortalAtlasQuery.class)))
                .thenReturn(new KnowledgePortalAtlasResult(
                        "overview",
                        List.of(new KnowledgePortalAtlasResult.BreadcrumbItem(
                                "overview", "图谱总览", "/knowledge/atlas?level=overview")),
                        new KnowledgePortalAtlasResult.OverviewView(
                                "十四门类知识鸟瞰",
                                "先看门类分布，再进入单门类浏览与单实体详情。",
                                List.of(new KnowledgePortalAtlasResult.OverviewCategoryCard(
                                        "ANIMALS",
                                        "鸟兽",
                                        2L,
                                        1L,
                                        2L,
                                        3,
                                        "/knowledge/atlas?level=category&categoryCode=ANIMALS"))),
                        null,
                        null,
                        new KnowledgePortalAtlasResult.AvailableFilters(
                                List.of("SANCAI_ENTRY"), List.of(), List.of(), List.of(), List.of("30d"))));

        var response = controller.getAtlas(
                new com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request
                        .KnowledgePortalAtlasRequest());

        verify(service).getAtlas(any(KnowledgePortalAtlasQuery.class));
        assertEquals("overview", response.getCurrentLevel());
        assertEquals(
                "ANIMALS", response.getOverviewView().getCategoryCards().get(0).getCategoryCode());
        assertEquals(
                "SANCAI_ENTRY",
                response.getAvailableFilters().getKnowledgeBases().get(0));
    }

    @Test
    void getAtlasShouldMapCategoryPortalReadModel() {
        KnowledgePortalReadApplicationService service = mock(KnowledgePortalReadApplicationService.class);
        KnowledgePortalAtlasController controller = new KnowledgePortalAtlasController(service);
        when(service.getAtlas(any(KnowledgePortalAtlasQuery.class)))
                .thenReturn(new KnowledgePortalAtlasResult(
                        "category",
                        List.of(
                                new KnowledgePortalAtlasResult.BreadcrumbItem(
                                        "overview", "图谱总览", "/knowledge/atlas?level=overview"),
                                new KnowledgePortalAtlasResult.BreadcrumbItem(
                                        "category", "鸟兽", "/knowledge/atlas?level=category&categoryCode=ANIMALS")),
                        null,
                        new KnowledgePortalAtlasResult.CategoryView(
                                "ANIMALS",
                                "鸟兽",
                                71L,
                                3,
                                List.of(new KnowledgePortalAtlasResult.CategoryEntityHighlight(
                                        "3001",
                                        "鸾",
                                        "CREATURE",
                                        "CONFIRMED",
                                        "/knowledge/atlas?level=detail&entityId=3001")),
                                List.of(new KnowledgePortalAtlasResult.RelationGroup(
                                        "KIN",
                                        "KIN",
                                        List.of(new KnowledgePortalAtlasResult.RelationItem(
                                                "bird:luan", "鸾", "KIN", "bird:feng", "凤", "KIN", 0.95D)))),
                                List.of(new KnowledgePortalAtlasResult.SourceReference(
                                        "1001", "羽族", "SANCAI_ENTRY", "摘要", 1L, "/knowledge/atlas"))),
                        null,
                        new KnowledgePortalAtlasResult.AvailableFilters(
                                List.of("SANCAI_ENTRY"),
                                List.of("CREATURE"),
                                List.of("KIN"),
                                List.of(),
                                List.of("30d"))));

        var request =
                new com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request
                        .KnowledgePortalAtlasRequest();
        request.setLevel("category");
        request.setCategoryCode("ANIMALS");
        var response = controller.getAtlas(request);

        verify(service).getAtlas(any(KnowledgePortalAtlasQuery.class));
        assertEquals("category", response.getCurrentLevel());
        assertEquals(
                "鸾", response.getCategoryView().getEntityHighlights().get(0).getEntityName());
        assertEquals(
                "KIN", response.getCategoryView().getRelationGroups().get(0).getGroupKey());
    }

    @Test
    void getAtlasShouldMapDetailPortalReadModel() {
        KnowledgePortalReadApplicationService service = mock(KnowledgePortalReadApplicationService.class);
        KnowledgePortalAtlasController controller = new KnowledgePortalAtlasController(service);
        when(service.getAtlas(any(KnowledgePortalAtlasQuery.class)))
                .thenReturn(new KnowledgePortalAtlasResult(
                        "detail",
                        List.of(
                                new KnowledgePortalAtlasResult.BreadcrumbItem(
                                        "overview", "图谱总览", "/knowledge/atlas?level=overview"),
                                new KnowledgePortalAtlasResult.BreadcrumbItem(
                                        "category", "鸟兽", "/knowledge/atlas?level=category&categoryCode=ANIMALS"),
                                new KnowledgePortalAtlasResult.BreadcrumbItem(
                                        "detail", "黄帝", "/knowledge/atlas?level=detail&entityId=3001")),
                        null,
                        null,
                        new KnowledgePortalAtlasResult.DetailView(
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
                                List.of(new KnowledgePortalAtlasResult.TimelineItem(
                                        "首次抽取", "知识首次进入图谱", "说明", "/knowledge/atlas")),
                                List.of()),
                        new KnowledgePortalAtlasResult.AvailableFilters(
                                List.of("SANCAI_ENTRY"),
                                List.of("PERSON"),
                                List.of("ANCESTOR"),
                                List.of(),
                                List.of("30d"))));

        var request =
                new com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request
                        .KnowledgePortalAtlasRequest();
        request.setLevel("detail");
        request.setEntityId(3001L);
        var response = controller.getAtlas(request);

        verify(service).getAtlas(any(KnowledgePortalAtlasQuery.class));
        assertEquals("detail", response.getCurrentLevel());
        assertEquals("3001", response.getDetailView().getFocusNode().getId());
        assertEquals(
                "person:shaodian",
                response.getDetailView()
                        .getRelationGroups()
                        .get(0)
                        .getRelations()
                        .get(0)
                        .getTargetId());
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
