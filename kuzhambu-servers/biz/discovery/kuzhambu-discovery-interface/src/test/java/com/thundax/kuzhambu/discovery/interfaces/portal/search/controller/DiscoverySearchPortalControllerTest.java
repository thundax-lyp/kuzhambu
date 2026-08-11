package com.thundax.kuzhambu.discovery.interfaces.portal.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchEventResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchClickEventRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchPreviewRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoverySearchPortalControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepPortalApiPaths() throws Exception {
        assertRequestMapping(DiscoverySearchPortalController.class, "/api/portal/discovery/search");
        assertPostMapping(DiscoverySearchPortalController.class, "search", "search", DiscoverySearchRequest.class);
        assertPostMapping(
                DiscoverySearchPortalController.class, "preview", "preview", DiscoverySearchPreviewRequest.class);
        assertPostMapping(
                DiscoverySearchPortalController.class, "click", "click", DiscoverySearchClickEventRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        DiscoverySearchRequest request = OBJECT_MAPPER.readValue(
                """
                {
                  "queryText": "黄帝",
                  "knowledgeBases": ["SANCAI_ENTRY"],
                  "categoryCodes": ["PERSON"],
                  "tagNames": ["上古"],
                  "dateFrom": "2026-01-01T00:00:00Z",
                  "dateTo": "2026-01-02T00:00:00Z",
                  "pageNo": 1,
                  "pageSize": 20
                }
                """,
                DiscoverySearchRequest.class);
        assertEquals("黄帝", request.getQueryText());
        assertJsonFields(
                request,
                "queryText",
                "knowledgeBases",
                "categoryCodes",
                "tagNames",
                "dateFrom",
                "dateTo",
                "pageNo",
                "pageSize");

        DiscoverySearchClickEventRequest clickRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "searchEventId": "1",
                  "contentDomain": "CLASSICS",
                  "contentType": "SANCAI_ENTRY",
                  "contentId": "1001",
                  "contentTitle": "黄帝",
                  "resultGroupKey": "SANCAI_ENTRY",
                  "resultRank": 1,
                  "groupRank": 1,
                  "targetPath": "/classics/sancai/1001"
                }
                """,
                DiscoverySearchClickEventRequest.class);
        assertEquals("1", clickRequest.getSearchEventId());
        assertJsonFields(
                clickRequest,
                "searchEventId",
                "contentDomain",
                "contentType",
                "contentId",
                "contentTitle",
                "resultGroupKey",
                "resultRank",
                "groupRank",
                "targetPath");
    }

    @Test
    void searchRequestShouldAllowNullQueryText() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchPortalController controller = new DiscoverySearchPortalController(service);
        DiscoverySearchRequest request = new DiscoverySearchRequest();
        request.setQueryText(null);
        request.setPageNo(1);
        request.setPageSize(20);
        when(service.search(any(), any()))
                .thenReturn(new SearchEventResult(
                        SearchEventIdCodec.toDomain(1L),
                        "",
                        "",
                        "",
                        "KEYWORD_SEARCH",
                        null,
                        0,
                        0,
                        "SUCCEEDED",
                        null,
                        null,
                        null,
                        "req-empty",
                        "trace-empty",
                        1_718_000_000_000L,
                        List.of()));
        ArgumentCaptor<SearchQuery> queryCaptor = ArgumentCaptor.forClass(SearchQuery.class);

        var response = controller.search(request);

        verify(service).search(queryCaptor.capture(), any());
        assertEquals("", queryCaptor.getValue().queryText());
        assertEquals(0, response.getTotalCount());
    }

    @Test
    void searchShouldMapGroupedResults() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchPortalController controller = new DiscoverySearchPortalController(service);
        DiscoverySearchRequest request = new DiscoverySearchRequest();
        request.setQueryText("黄帝");
        request.setPageNo(1);
        request.setPageSize(20);
        when(service.search(any(), any()))
                .thenReturn(new SearchEventResult(
                        SearchEventIdCodec.toDomain(1L),
                        "黄帝",
                        "黄帝",
                        "黄帝",
                        "ENTITY",
                        null,
                        1,
                        1,
                        "SUCCEEDED",
                        null,
                        null,
                        null,
                        "req-1",
                        "trace-1",
                        1_718_000_000_000L,
                        List.of(new SearchGroupResult(
                                "SANCAI_ENTRY",
                                "三才图会",
                                1,
                                List.of(new SearchResult(
                                        "CLASSICS",
                                        "SANCAI_ENTRY",
                                        "1001",
                                        "黄帝",
                                        "上古帝王",
                                        null,
                                        1,
                                        1,
                                        "/classics/sancai/1001"))))));

        var response = controller.search(request);

        verify(service).search(any(), any());
        assertEquals("1", response.getId());
        assertEquals(1, response.getGroups().size());
        assertEquals("1001", response.getGroups().get(0).getItems().get(0).getContentId());
    }

    @Test
    void previewShouldMapSearchIndexDocumentFields() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchPortalController controller = new DiscoverySearchPortalController(service);
        DiscoverySearchPreviewRequest request = new DiscoverySearchPreviewRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setContentId("1001");
        when(service.getPreview(any()))
                .thenReturn(new SearchPreviewResult(
                        "CLASSICS",
                        "SANCAI_ENTRY",
                        "1001",
                        "SANCAI_ENTRY",
                        "11",
                        "天文",
                        "黄帝",
                        "摘要",
                        "正文",
                        List.of("上古"),
                        3,
                        1_767_225_600_000L,
                        1_767_312_000_000L,
                        "/classics/sancai/1001"));

        var response = controller.preview(request);

        verify(service)
                .getPreview(argThat(
                        query -> "SANCAI_ENTRY".equals(query.contentType()) && "1001".equals(query.contentId())));
        assertEquals("黄帝", response.getTitle());
        assertEquals("正文", response.getBodyText());
        assertEquals("/classics/sancai/1001", response.getTargetPath());
    }

    @Test
    void clickShouldDelegateToApplicationService() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchPortalController controller = new DiscoverySearchPortalController(service);
        DiscoverySearchClickEventRequest request = new DiscoverySearchClickEventRequest();
        request.setSearchEventId("1");
        request.setContentDomain("CLASSICS");
        request.setContentType("SANCAI_ENTRY");
        request.setContentId("1001");
        request.setResultGroupKey("SANCAI_ENTRY");
        request.setResultRank(1);
        request.setGroupRank(1);
        request.setTargetPath("/classics/sancai/1001");
        when(service.recordClick(any())).thenReturn(Boolean.TRUE);

        Boolean result = controller.click(request);

        verify(service)
                .recordClick(argThat(command -> "1".equals(SearchEventIdCodec.toStringValue(command.searchEventId()))
                        && "SANCAI_ENTRY".equals(command.resultGroupKey())
                        && "/classics/sancai/1001".equals(command.targetPath())));
        assertTrue(result);
    }

    @Test
    void clickRequestShouldRejectNonnumericSearchEventId() {
        DiscoverySearchClickEventRequest request = new DiscoverySearchClickEventRequest();
        request.setSearchEventId("EVT-1001");
        request.setContentDomain("CLASSICS");
        request.setContentType("SANCAI_ENTRY");
        request.setContentId("1001");
        request.setResultGroupKey("SANCAI_ENTRY");
        request.setResultRank(1);
        request.setGroupRank(1);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertFalse(validator.validate(request).isEmpty());
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

    private void assertJsonFields(Object value, String... fieldNames) throws Exception {
        var node = OBJECT_MAPPER.valueToTree(value);
        for (String fieldName : fieldNames) {
            assertTrue(node.has(fieldName), "missing field " + fieldName);
        }
    }
}
