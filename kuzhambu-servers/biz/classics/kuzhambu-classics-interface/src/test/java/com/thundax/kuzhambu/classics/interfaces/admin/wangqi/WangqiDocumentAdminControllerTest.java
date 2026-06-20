package com.thundax.kuzhambu.classics.interfaces.admin.wangqi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.WangqiDocumentAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request.WangqiDocumentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request.WangqiDocumentVersionRequest;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class WangqiDocumentAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminApiPathsAndPermissions() throws Exception {
        assertRequestMapping(WangqiDocumentAdminController.class, "/api/classics/wangqi/documents");
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "page",
                "page",
                "classics:wangqi:view",
                WangqiDocumentRequest.class);
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "get",
                "{id}/get",
                "classics:wangqi:view",
                WangqiDocumentRequest.class);
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "listTimeline",
                "timeline/list",
                "classics:wangqi:view",
                WangqiDocumentRequest.class);
        assertPostMapping(
                WangqiDocumentAdminController.class, "add", "add", "classics:wangqi:edit", WangqiDocumentRequest.class);
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "update",
                "update",
                "classics:wangqi:edit",
                WangqiDocumentRequest.class);
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "delete",
                "delete",
                "classics:wangqi:delete",
                WangqiDocumentRequest.class);
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "listVersions",
                "versions/list",
                "classics:wangqi:view",
                WangqiDocumentVersionRequest.class);
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "getVersion",
                "versions/get",
                "classics:wangqi:view",
                WangqiDocumentVersionRequest.class);
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "resetVersion",
                "versions/reset",
                "classics:wangqi:edit",
                WangqiDocumentVersionRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        WangqiDocumentRequest request = OBJECT_MAPPER.readValue(
                """
                {
                  "id": 400000000001,
                  "title": "王圻文档",
                  "summary": "摘要",
                  "contentFormat": "MARKDOWN",
                  "content": "正文",
                  "documentTime": "2026-01-01T00:00:00.000+00:00",
                  "storageObjectId": 7001,
                  "visibility": "PUBLIC",
                  "keyword": "王圻",
                  "sortDirection": "DESC",
                  "pageNo": 1,
                  "pageSize": 20
                }
                """,
                WangqiDocumentRequest.class);

        assertEquals(400000000001L, request.getId());
        assertEquals("王圻文档", request.getTitle());
        assertEquals("摘要", request.getSummary());
        assertEquals("MARKDOWN", request.getContentFormat());
        assertEquals("正文", request.getContent());
        assertEquals(7001L, request.getStorageObjectId());
        assertEquals("PUBLIC", request.getVisibility());
        assertEquals("王圻", request.getKeyword());
        assertEquals("DESC", request.getSortDirection());
        assertEquals(1, request.getPageNo());
        assertEquals(20, request.getPageSize());

        JsonNode response = OBJECT_MAPPER.valueToTree(controller().get(request()));
        assertEquals(400000000001L, response.get("id").asLong());
        assertEquals("王圻文档", response.get("title").asText());
        assertEquals("摘要", response.get("summary").asText());
        assertEquals("MARKDOWN", response.get("contentFormat").asText());
        assertEquals("正文", response.get("content").asText());
        assertEquals(7001L, response.get("storageObjectId").asLong());
        assertEquals("PUBLIC", response.get("visibility").asText());

        JsonNode versionResponse = OBJECT_MAPPER.valueToTree(controller().getVersion(versionRequest()));
        assertEquals(9001L, versionResponse.get("id").asLong());
        assertEquals("WANGQI_DOCUMENT", versionResponse.get("contentType").asText());
        assertEquals(400000000001L, versionResponse.get("contentId").asLong());
        assertEquals(2, versionResponse.get("versionNo").asInt());
        assertEquals("MANUAL_SAVE", versionResponse.get("changeType").asText());
    }

    @Test
    void controllerShouldProxyWangqiDocumentService() {
        WangqiDocumentAdminController controller = controller();
        WangqiDocumentRequest request = request();

        assertEquals(1, controller.page(request).getRecords().size());
        assertEquals(400000000001L, controller.get(request).getId());
        assertEquals(1, controller.listTimeline(request).size());
        assertEquals(400000000001L, controller.add(request).getId());
        assertEquals(400000000001L, controller.update(request).getId());
        controller.delete(request);
        assertEquals(1, controller.listVersions(versionRequest()).size());
        assertEquals(9001L, controller.getVersion(versionRequest()).getId());
        assertEquals(9002L, controller.resetVersion(versionRequest()).getId());
    }

    private static WangqiDocumentAdminController controller() {
        return new WangqiDocumentAdminController(service(), contentService());
    }

    private static WangqiDocumentApplicationService service() {
        return (WangqiDocumentApplicationService) Proxy.newProxyInstance(
                WangqiDocumentApplicationService.class.getClassLoader(),
                new Class<?>[] {WangqiDocumentApplicationService.class},
                (proxy, method, args) -> {
                    if ("page".equals(method.getName())) {
                        assertQuery(args);
                        PageQuery page = (PageQuery) args[1];
                        assertEquals(1, page.getPageNo());
                        assertEquals(20, page.getPageSize());
                        return PageResult.of(1, 20, 1, List.of(document()));
                    }
                    if ("get".equals(method.getName())) {
                        assertEquals(WangqiDocumentId.of(400000000001L), args[0]);
                        return document();
                    }
                    if ("listTimeline".equals(method.getName())) {
                        assertQuery(args);
                        return List.of(document());
                    }
                    if ("add".equals(method.getName()) || "update".equals(method.getName())) {
                        WangqiDocumentCommand command = (WangqiDocumentCommand) args[0];
                        assertEquals(400000000001L, command.getId());
                        assertEquals("王圻文档", command.getTitle());
                        assertEquals("摘要", command.getSummary());
                        assertEquals(WangqiContentFormat.MARKDOWN, command.getContentFormat());
                        assertEquals("正文", command.getContent());
                        assertEquals(7001L, command.getStorageObjectId());
                        assertEquals(WangqiDocumentVisibility.PUBLIC, command.getVisibility());
                        return WangqiDocumentId.of(400000000001L);
                    }
                    if ("delete".equals(method.getName())) {
                        assertEquals(WangqiDocumentId.of(400000000001L), args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsContentApplicationService contentService() {
        return (ClassicsContentApplicationService) Proxy.newProxyInstance(
                ClassicsContentApplicationService.class.getClassLoader(),
                new Class<?>[] {ClassicsContentApplicationService.class},
                (proxy, method, args) -> {
                    if ("listVersions".equals(method.getName())) {
                        assertEquals("WANGQI_DOCUMENT", args[0]);
                        assertEquals(ClassicsContentId.of(400000000001L), args[1]);
                        return List.of(version(9001L, 2, ClassicsContentChangeType.MANUAL_SAVE));
                    }
                    if ("getVersion".equals(method.getName())) {
                        assertEquals(ClassicsContentVersionId.of(9001L), args[0]);
                        return version(9001L, 2, ClassicsContentChangeType.MANUAL_SAVE);
                    }
                    if ("restoreHistoryVersion".equals(method.getName())) {
                        assertEquals(ClassicsContentVersionId.of(9001L), args[0]);
                        return version(9002L, 3, ClassicsContentChangeType.HISTORY_RESTORED);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static void assertQuery(Object[] args) {
        WangqiDocumentPageQuery query = (WangqiDocumentPageQuery) args[0];
        assertEquals("王圻", query.getKeyword());
        assertEquals(WangqiDocumentVisibility.PUBLIC, query.getVisibility());
        assertEquals(SortDirection.DESC, query.getSortDirection());
    }

    private static WangqiDocumentRequest request() {
        WangqiDocumentRequest request = new WangqiDocumentRequest();
        request.setId(400000000001L);
        request.setTitle("王圻文档");
        request.setSummary("摘要");
        request.setContentFormat("MARKDOWN");
        request.setContent("正文");
        request.setDocumentTime(new Date(1767225600000L));
        request.setStorageObjectId(7001L);
        request.setVisibility("PUBLIC");
        request.setKeyword("王圻");
        request.setSortDirection("DESC");
        request.setPageNo(1);
        request.setPageSize(20);
        return request;
    }

    private static WangqiDocumentVersionRequest versionRequest() {
        WangqiDocumentVersionRequest request = new WangqiDocumentVersionRequest();
        request.setId(400000000001L);
        request.setVersionId(9001L);
        return request;
    }

    private static WangqiDocument document() {
        return new WangqiDocument(
                WangqiDocumentId.of(400000000001L),
                "王圻文档",
                "摘要",
                WangqiContentFormat.MARKDOWN,
                "正文",
                new Date(1767225600000L),
                StorageObjectId.of(7001L),
                WangqiDocumentVisibility.PUBLIC);
    }

    private static ClassicsContentVersion version(Long id, int versionNo, ClassicsContentChangeType changeType) {
        return new ClassicsContentVersion(
                ClassicsContentVersionId.of(id),
                ClassicsContentType.WANGQI_DOCUMENT,
                ClassicsContentId.of(400000000001L),
                versionNo,
                new Date(1767225600000L),
                "{\"contentType\":\"WANGQI_DOCUMENT\",\"contentId\":400000000001}",
                changeType,
                "手动保存");
    }

    private static void assertRequestMapping(Class<?> controllerType, String expectedPath) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }
}
