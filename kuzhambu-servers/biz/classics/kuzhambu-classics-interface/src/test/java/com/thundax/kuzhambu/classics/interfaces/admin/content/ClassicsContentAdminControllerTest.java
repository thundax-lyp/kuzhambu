package com.thundax.kuzhambu.classics.interfaces.admin.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.application.content.result.ClassicsExportJobResult;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.ClassicsContentAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class ClassicsContentAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepExportAdminApiPathsAndPermissions() throws Exception {
        assertRequestMapping(ClassicsContentAdminController.class, "/api/classics/content");
        assertPostMapping(
                ClassicsContentAdminController.class,
                "createExport",
                "exports/create",
                "classics:content:export",
                ClassicsContentRequest.class);
        assertPostMapping(
                ClassicsContentAdminController.class,
                "pageExports",
                "exports/page",
                "classics:content:view",
                ClassicsContentRequest.class);
        assertPostMapping(
                ClassicsContentAdminController.class,
                "changeAiCandidate",
                "ai-candidates/change",
                "classics:content:edit",
                ClassicsContentRequest.AiCandidateApplyRequest.class);
        assertGetMapping(
                ClassicsContentAdminController.class,
                "downloadExportContent",
                "exports/{jobId}/content",
                "classics:content:view",
                Long.class,
                Boolean.class,
                jakarta.servlet.http.HttpServletResponse.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() {
        ClassicsContentRequest request = new ClassicsContentRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setExportKind("CONTENT_DATASET");
        request.setExportFormat("HTML");
        request.setScopeType("CATEGORY");
        request.setScopeJson("all");
        request.setPageNo(1);
        request.setPageSize(10);

        ClassicsContentResponse created = controller().createExport(request);
        assertEquals(9001L, created.getId());
        assertEquals("COMPLETED", created.getStatus());

        JsonNode page = OBJECT_MAPPER.valueToTree(
                controller().pageExports(request).getRecords().get(0));
        assertEquals(9001L, page.get("id").asLong());
        assertEquals("SANCAI_ENTRY", page.get("contentType").asText());
        assertEquals("CONTENT_DATASET", page.get("exportKind").asText());
        assertEquals("HTML", page.get("exportFormat").asText());
        assertEquals("CATEGORY", page.get("scopeType").asText());
        assertEquals(
                "/api/classics/content/exports/9001/content",
                page.get("contentUrl").asText());
        assertEquals(
                "/api/classics/content/exports/9001/content?download=true",
                page.get("downloadUrl").asText());
    }

    @Test
    void aiCandidateApplyRequestShouldMapToServiceCommandAndReturnResponse() {
        ClassicsContentAdminController controller = controller();
        ClassicsContentRequest.AiCandidateApplyRequest request = new ClassicsContentRequest.AiCandidateApplyRequest();
        request.setCandidateId(123L);
        request.setContentType("SANCAI_ENTRY");
        request.setContentId(456L);
        request.setCapability("summary");
        request.setResultFormat("TEXT");
        request.setResultPayload("new summary");
        request.setChangeSummary("AI 应用：摘要");

        ClassicsContentResponse.AiCandidateApplyResponse response = controller.changeAiCandidate(request);
        assertEquals(456L, response.getContentId());
        assertEquals("SANCAI_ENTRY", response.getContentType());
        assertEquals(789L, response.getVersionId());
        assertEquals(3, response.getVersionNo());
    }

    @Test
    void controllerShouldProxyExportServiceAndSupportDownloadOrNotFound() throws Exception {
        ClassicsContentAdminController controller = controller();
        ClassicsContentRequest request = new ClassicsContentRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setExportKind("CONTENT_DATASET");
        request.setStatus("COMPLETED");
        request.setPageNo(1);
        request.setPageSize(10);

        assertEquals(1, controller.pageExports(request).getRecords().size());
        assertEquals(9001L, controller.pageExports(request).getRecords().get(0).getId());

        MockHttpServletResponse inlineResponse = new MockHttpServletResponse();
        controller.downloadExportContent(9001L, false, inlineResponse);
        assertEquals("application/zip", inlineResponse.getContentType());
        assertEquals(8, inlineResponse.getContentLength());
        assertTrue(inlineResponse.getHeader("Content-Disposition").startsWith("inline;"));
        assertTrue(inlineResponse.getHeader("Content-Disposition").contains("export.zip"));
        assertEquals("zip-data", inlineResponse.getContentAsString());

        MockHttpServletResponse notFoundResponse = new MockHttpServletResponse();
        controller.downloadExportContent(9002L, false, notFoundResponse);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, notFoundResponse.getStatus());
    }

    @Test
    void controllerShouldRejectExpiredExportDownload() throws Exception {
        ClassicsContentAdminController controller = controller();
        MockHttpServletResponse expiredResponse = new MockHttpServletResponse();

        controller.downloadExportContent(9003L, false, expiredResponse);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, expiredResponse.getStatus());
    }

    private static ClassicsContentAdminController controller() {
        return new ClassicsContentAdminController(contentService(), storageService());
    }

    private static ClassicsContentApplicationService contentService() {
        return (ClassicsContentApplicationService) Proxy.newProxyInstance(
                ClassicsContentApplicationService.class.getClassLoader(),
                new Class<?>[] {ClassicsContentApplicationService.class},
                (proxy, method, args) -> {
                    if ("createExportJob".equals(method.getName())) {
                        ContentExportCommand command = (ContentExportCommand) args[0];
                        assertEquals("SANCAI_ENTRY", command.getContentType().value());
                        assertEquals(ClassicsExportKind.CONTENT_DATASET, command.getExportKind());
                        assertEquals(ClassicsExportFormat.HTML, command.getExportFormat());
                        assertEquals(ClassicsExportScopeType.CATEGORY, command.getScopeType());
                        return new ClassicsExportJobResult(
                                ClassicsContentExportJobId.of(9001L),
                                ClassicsExportStatus.COMPLETED,
                                StorageObjectId.of(7001L));
                    }
                    if ("pageExportJobs".equals(method.getName())) {
                        assertEquals("SANCAI_ENTRY", args[0]);
                        assertEquals("CONTENT_DATASET", args[1]);
                        assertTrue(args[2] == null || "COMPLETED".equals(args[2]));
                        PageQuery page = (PageQuery) args[3];
                        assertEquals(1, page.getPageNo());
                        assertEquals(10, page.getPageSize());
                        return PageResult.of(
                                1,
                                10,
                                1,
                                List.of(exportJob(
                                        ClassicsContentExportJobId.of(9001L), ClassicsExportStatus.COMPLETED)));
                    }
                    if ("getExportJob".equals(method.getName())) {
                        ClassicsContentExportJobId jobId = (ClassicsContentExportJobId) args[0];
                        if (jobId.value() == 9001L) {
                            return exportJob(ClassicsContentExportJobId.of(9001L), ClassicsExportStatus.COMPLETED);
                        }
                        if (jobId.value() == 9002L) {
                            return exportJob(ClassicsContentExportJobId.of(9002L), ClassicsExportStatus.REQUESTED);
                        }
                        if (jobId.value() == 9003L) {
                            return exportJob(
                                    ClassicsContentExportJobId.of(9003L),
                                    ClassicsExportStatus.COMPLETED,
                                    new Date(System.currentTimeMillis() - 60_000L));
                        }
                    }
                    if ("applyAiCandidate".equals(method.getName())) {
                        AiCandidateApplyContentCommand command = (AiCandidateApplyContentCommand) args[0];
                        assertEquals(123L, command.getCandidateId());
                        assertEquals(ClassicsContentType.SANCAI_ENTRY, command.getContentType());
                        assertEquals(456L, command.getContentId());
                        assertEquals("summary", command.getCapability());
                        assertEquals("TEXT", command.getResultFormat());
                        assertEquals("new summary", command.getResultPayload());
                        assertEquals("AI 应用：摘要", command.getChangeSummary());
                        return new AiCandidateApplyContentResult(ClassicsContentType.SANCAI_ENTRY, 456L, 789L, 3);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static StorageApplicationService storageService() {
        return (StorageApplicationService) Proxy.newProxyInstance(
                StorageApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageApplicationService.class},
                (proxy, method, args) -> {
                    if ("openReadableContent".equals(method.getName())) {
                        return new StoredObjectContent(
                                exportStorage(), new ByteArrayInputStream("zip-data".getBytes()));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsContentExportJob exportJob(ClassicsContentExportJobId id, ClassicsExportStatus status) {
        return exportJob(
                id,
                status,
                status == ClassicsExportStatus.COMPLETED ? new Date(System.currentTimeMillis() + 60_000L) : null);
    }

    private static ClassicsContentExportJob exportJob(
            ClassicsContentExportJobId id, ClassicsExportStatus status, Date expiresAt) {
        return new ClassicsContentExportJob(
                id,
                ClassicsExportKind.CONTENT_DATASET,
                ClassicsContentType.SANCAI_ENTRY,
                ClassicsExportFormat.HTML,
                ClassicsExportScopeType.CATEGORY,
                "all",
                new Date(),
                expiresAt,
                status,
                StorageObjectId.of(7001L),
                1,
                2,
                SancaiVisibilityRiskStatus.PUBLIC_ONLY,
                false);
    }

    private static StoredObject exportStorage() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(7001L));
        storage.setOriginalFilename("export.zip");
        storage.setContentType("application/zip");
        storage.setSize(8L);
        return storage;
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
}
