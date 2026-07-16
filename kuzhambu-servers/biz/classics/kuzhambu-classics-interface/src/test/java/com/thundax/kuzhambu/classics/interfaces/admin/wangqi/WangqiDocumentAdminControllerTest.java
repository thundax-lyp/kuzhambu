package com.thundax.kuzhambu.classics.interfaces.admin.wangqi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

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
                WangqiDocumentAdminController.class, "get", "get", "classics:wangqi:view", WangqiDocumentRequest.class);
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
        assertMultipartPostMapping(
                WangqiDocumentAdminController.class,
                "uploadSourceFile",
                "{id}/source-file/upload",
                "classics:wangqi:edit",
                Long.class,
                MultipartFile.class);
        assertPostMapping(
                WangqiDocumentAdminController.class,
                "getSourceFile",
                "source-file/get",
                "classics:wangqi:view",
                WangqiDocumentRequest.class);
        assertGetMapping(
                WangqiDocumentAdminController.class,
                "downloadSourceFile",
                "{id}/source-file/content",
                "classics:wangqi:view",
                Long.class,
                Boolean.class,
                jakarta.servlet.http.HttpServletResponse.class);
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

        JsonNode sourceFileResponse = OBJECT_MAPPER.valueToTree(controller().getSourceFile(request()));
        assertEquals(400000000001L, sourceFileResponse.get("documentId").asLong());
        assertEquals(7001L, sourceFileResponse.get("storageObjectId").asLong());
        assertEquals("wangqi.pdf", sourceFileResponse.get("originalFilename").asText());
        assertEquals("application/pdf", sourceFileResponse.get("contentType").asText());
        assertEquals(10L, sourceFileResponse.get("size").asLong());
        assertEquals(
                "/api/classics/wangqi/documents/400000000001/source-file/content",
                sourceFileResponse.get("contentUrl").asText());
    }

    @Test
    void controllerShouldProxyWangqiDocumentService() throws Exception {
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
        assertEquals(
                7001L,
                controller
                        .uploadSourceFile(
                                400000000001L,
                                new InMemoryMultipartFile("wangqi.pdf", "application/pdf", "source-bin".getBytes()))
                        .getStorageObjectId());
        assertEquals(7001L, controller.getSourceFile(request).getStorageObjectId());

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadSourceFile(400000000001L, false, response);
        assertEquals("application/pdf", response.getContentType());
        assertEquals(10, response.getContentLength());
        Assertions.assertTrue(response.getHeader("Content-Disposition").startsWith("inline;"));
        Assertions.assertTrue(response.getHeader("Content-Disposition").contains("wangqi.pdf"));
        assertEquals("source-bin", response.getContentAsString());
    }

    @Test
    void downloadSourceFileShouldSupportAttachmentAndRfc5987Filename() throws Exception {
        WangqiDocumentAdminController controller = controller();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadSourceFile(400000000002L, true, response);

        String disposition = response.getHeader("Content-Disposition");
        Assertions.assertTrue(disposition.startsWith("attachment;"));
        Assertions.assertTrue(disposition.contains("filename=\"王圻原始.pdf\""));
        Assertions.assertTrue(disposition.contains("filename*=UTF-8''%E7%8E%8B%E5%9C%BB%E5%8E%9F%E5%A7%8B.pdf"));
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
                    if ("changeSourceFile".equals(method.getName())) {
                        WangqiDocumentSourceFileCommand command = (WangqiDocumentSourceFileCommand) args[0];
                        assertEquals(400000000001L, command.getDocumentId());
                        assertEquals("wangqi.pdf", command.getOriginalFilename());
                        assertEquals("application/pdf", command.getContentType());
                        assertEquals(10L, command.getSize());
                        return sourceFile();
                    }
                    if ("getSourceFile".equals(method.getName())) {
                        assertEquals(WangqiDocumentId.of(400000000001L), args[0]);
                        return sourceFile();
                    }
                    if ("getSourceFileContent".equals(method.getName())) {
                        WangqiDocumentId documentId = (WangqiDocumentId) args[0];
                        return sourceFileContent(documentId.value());
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
        assertEquals(true, query.getOperatorPermissions() != null);
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

    private static WangqiDocumentSourceFile sourceFile() {
        return new WangqiDocumentSourceFile(400000000001L, 7001L, "wangqi.pdf", "application/pdf", 10L);
    }

    private static ClassicsStoredContentResult sourceFileContent(Long documentId) {
        String filename = documentId == 400000000002L ? "王圻原始.pdf" : "wangqi.pdf";
        return new ClassicsStoredContentResult(
                7001L, filename, "application/pdf", 10L, new ByteArrayInputStream("source-bin".getBytes()));
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

    private static void assertMultipartPostMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        assertEquals(MediaType.MULTIPART_FORM_DATA_VALUE, mapping.consumes()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }

    private static void assertGetMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }

    private static final class InMemoryMultipartFile implements MultipartFile {

        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        private InMemoryMultipartFile(String originalFilename, String contentType, byte[] content) {
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) {
            throw new UnsupportedOperationException("transferTo");
        }
    }
}
