package com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalHomeResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.request.KnowledgePortalHomeRequest;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class KnowledgePortalHomeControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepPortalApiPaths() throws Exception {
        assertRequestMapping(KnowledgePortalHomeController.class, "/api/portal/knowledge/home");
        assertPostMapping(KnowledgePortalHomeController.class, "getHome", "get", KnowledgePortalHomeRequest.class);
    }

    @Test
    void responseJsonFieldsShouldRemainStable() throws Exception {
        var response = com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response
                .KnowledgePortalHomeResponse.builder()
                .heroTitle("古籍知识图谱门户")
                .heroSubtitle("说明")
                .searchPlaceholder("搜索")
                .stats(List.of(com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response
                        .KnowledgePortalHomeResponse.PortalStatResponse.builder()
                        .key("tag-count")
                        .label("主题标签")
                        .value("12")
                        .deltaText("治理基线")
                        .trend("steady")
                        .icon("seal")
                        .build()))
                .quickLinks(List.of(com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response
                        .KnowledgePortalHomeResponse.PortalQuickLinkResponse.builder()
                        .key("atlas")
                        .label("图谱浏览")
                        .description("描述")
                        .href("/knowledge/atlas")
                        .type("atlas")
                        .build()))
                .recentUpdates(List.of(com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response
                        .KnowledgePortalHomeResponse.PortalRecentUpdateResponse.builder()
                        .title("版本 2")
                        .subtitle("副标题")
                        .summary("摘要")
                        .updatedAt(1L)
                        .href("/knowledge/atlas")
                        .coverImageUrl(null)
                        .build()))
                .featureCollections(List.of(com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response
                        .KnowledgePortalHomeResponse.PortalFeatureCollectionResponse.builder()
                        .key("quality-brief")
                        .label("质量摘要")
                        .description("描述")
                        .href("/knowledge/quality")
                        .badgeText("质量洞察")
                        .build()))
                .build();
        var node = OBJECT_MAPPER.valueToTree(response);

        assertTrue(node.has("heroTitle"));
        assertTrue(node.has("heroSubtitle"));
        assertTrue(node.has("searchPlaceholder"));
        assertTrue(node.has("stats"));
        assertTrue(node.has("quickLinks"));
        assertTrue(node.has("recentUpdates"));
        assertTrue(node.has("featureCollections"));
    }

    @Test
    void getHomeShouldMapPortalReadModel() {
        KnowledgePortalReadApplicationService service = mock(KnowledgePortalReadApplicationService.class);
        KnowledgePortalHomeController controller = new KnowledgePortalHomeController(service);
        when(service.getHome())
                .thenReturn(new KnowledgePortalHomeResult(
                        "古籍知识图谱门户",
                        "以图谱浏览古籍知识。",
                        "搜索人物、器物、礼制",
                        List.of(new KnowledgePortalHomeResult.PortalStatItem(
                                "tag-count", "主题标签", "12", "治理基线", "steady", "seal")),
                        List.of(
                                new KnowledgePortalHomeResult.PortalQuickLinkItem(
                                        "atlas", "图谱浏览", "进入图谱关系画布", "/knowledge/atlas", "atlas"),
                                new KnowledgePortalHomeResult.PortalQuickLinkItem(
                                        "lineage", "世系图浏览", "进入人物谱系画布", "/knowledge/lineage", "lineage")),
                        List.of(new KnowledgePortalHomeResult.PortalRecentUpdateItem(
                                "SANCAI_ENTRY · 版本 2",
                                "任务 GRAPH / 来源 SANCAI_ENTRY",
                                "摘要",
                                1L,
                                "/knowledge/atlas?focusType=SANCAI_ENTRY&focusId=1001",
                                null)),
                        List.of(new KnowledgePortalHomeResult.PortalFeatureCollectionItem(
                                "quality-brief", "质量摘要", "阅读型摘要", "/knowledge/quality", "质量洞察"))));

        var response = controller.getHome(new KnowledgePortalHomeRequest());

        verify(service).getHome();
        assertEquals("古籍知识图谱门户", response.getHeroTitle());
        assertEquals("12", response.getStats().get(0).getValue());
        assertEquals("/knowledge/atlas", response.getQuickLinks().get(0).getHref());
        assertEquals("/knowledge/lineage", response.getQuickLinks().get(1).getHref());
        assertEquals(
                "/knowledge/atlas?focusType=SANCAI_ENTRY&focusId=1001",
                response.getRecentUpdates().get(0).getHref());
        assertEquals("质量洞察", response.getFeatureCollections().get(0).getBadgeText());
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
