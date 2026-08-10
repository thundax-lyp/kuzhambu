package com.thundax.kuzhambu.classics.interfaces.admin.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.application.content.result.ClassicsExportJobResult;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentExportJobIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.interfaces.admin.common.response.ClassicsBatchOperationResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.ClassicsContentAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class ClassicsContentAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

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
        assertPostMapping(
                ClassicsContentAdminController.class,
                "changeAiCandidates",
                "ai-candidates/batch/apply",
                "classics:content:edit",
                ClassicsContentRequest.AiCandidateBatchApplyRequest.class);
        assertPostMapping(
                ClassicsContentAdminController.class,
                "removeAiCandidates",
                "ai-candidates/batch/reject",
                "classics:content:edit",
                ClassicsContentRequest.AiCandidateBatchRejectRequest.class);
        assertPostMapping(
                ClassicsContentAdminController.class,
                "deleteTag",
                "tags/delete",
                "classics:content:edit",
                ClassicsContentRequest.class);
        assertPostMapping(
                ClassicsContentAdminController.class,
                "deleteQaPair",
                "qa-pairs/delete",
                "classics:content:edit",
                ClassicsContentRequest.class);
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
        request.setObjectId(901L);
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
    void aiCandidateApplyTranslateRequestShouldMapToServiceCommandAndReturnResponse() {
        ClassicsContentAdminController controller = controller();
        ClassicsContentRequest.AiCandidateApplyRequest request = new ClassicsContentRequest.AiCandidateApplyRequest();
        request.setCandidateId(124L);
        request.setContentType("SANCAI_ENTRY");
        request.setContentId(456L);
        request.setCapability("translate");
        request.setObjectId(902L);
        request.setResultFormat("TEXT");
        request.setResultPayload("new translation");
        request.setChangeSummary("AI 应用：译文");

        ClassicsContentResponse.AiCandidateApplyResponse response = controller.changeAiCandidate(request);
        assertEquals(456L, response.getContentId());
        assertEquals("SANCAI_ENTRY", response.getContentType());
        assertEquals(790L, response.getVersionId());
        assertEquals(4, response.getVersionNo());
    }

    @Test
    void aiCandidateApplyRequestShouldRejectUnknownTagApplyMode() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ClassicsContentRequest.AiCandidateApplyRequest request = validAiCandidateApplyRequest();

        assertEquals(0, validator.validate(request).size());
        request.setTagApplyMode("REPLACE");
        assertEquals(1, validator.validate(request).size());

        request.setTagApplyMode("APPEND");
        assertEquals(0, validator.validate(request).size());
        request.setTagApplyMode("COVER");
        assertEquals(0, validator.validate(request).size());
        request.setTagApplyMode(null);
        assertEquals(0, validator.validate(request).size());
    }

    @Test
    void aiCandidateBatchApplyRequestShouldValidateNestedTagApplyMode() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ClassicsContentRequest.AiCandidateBatchApplyRequest request =
                new ClassicsContentRequest.AiCandidateBatchApplyRequest();
        ClassicsContentRequest.AiCandidateApplyRequest item = validAiCandidateApplyRequest();
        item.setTagApplyMode("REPLACE");
        request.setItems(List.of(item));

        assertEquals(1, validator.validate(request).size());
    }

    @Test
    void aiCandidateBatchApplyRequestShouldMapToServiceCommandAndReturnResponse() {
        ClassicsContentRequest.AiCandidateBatchApplyRequest request =
                new ClassicsContentRequest.AiCandidateBatchApplyRequest();
        ClassicsContentRequest.AiCandidateApplyRequest first = new ClassicsContentRequest.AiCandidateApplyRequest();
        first.setCandidateId(111L);
        first.setContentType("SANCAI_ENTRY");
        first.setContentId(4001L);
        first.setCapability("summary");
        first.setObjectId(901L);
        first.setResultFormat("TEXT");
        first.setResultPayload("first");
        first.setChangeSummary("first");
        ClassicsContentRequest.AiCandidateApplyRequest second = new ClassicsContentRequest.AiCandidateApplyRequest();
        second.setCandidateId(112L);
        second.setContentType("WANGQI_DOCUMENT");
        second.setContentId(5001L);
        second.setCapability("tags");
        second.setResultFormat("TEXT");
        second.setResultPayload("second");
        second.setChangeSummary("second");
        request.setItems(List.of(first, second));

        ClassicsBatchOperationResponse response = controller().changeAiCandidates(request);

        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        JsonNode firstSuccess =
                OBJECT_MAPPER.valueToTree(response.getSuccesses().get(0));
        assertEquals(111L, firstSuccess.get("candidateId").asLong());
        assertEquals("summary", firstSuccess.get("capability").asText());
    }

    @Test
    void aiCandidateBatchRejectRequestShouldMapToServiceCommandAndReturnResponse() {
        ClassicsContentRequest.AiCandidateBatchRejectRequest request =
                new ClassicsContentRequest.AiCandidateBatchRejectRequest();
        ClassicsContentRequest.AiCandidateRejectItemRequest first =
                new ClassicsContentRequest.AiCandidateRejectItemRequest();
        first.setCandidateId(113L);
        first.setContentType("SANCAI_ENTRY");
        first.setContentId(4002L);
        first.setCapability("summary");
        first.setObjectId(902L);
        ClassicsContentRequest.AiCandidateRejectItemRequest second =
                new ClassicsContentRequest.AiCandidateRejectItemRequest();
        second.setCandidateId(114L);
        second.setContentType("WANGQI_DOCUMENT");
        second.setContentId(5002L);
        second.setCapability("summary");
        request.setItems(List.of(first, second));

        ClassicsBatchOperationResponse response = controller().removeAiCandidates(request);

        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        JsonNode firstFailure =
                OBJECT_MAPPER.valueToTree(response.getSuccesses().get(0));
        assertEquals(113L, firstFailure.get("candidateId").asLong());
    }

    @Test
    void batchAiCandidateRequestShouldRejectDuplicateCandidateIdAndMissingItems() {
        ClassicsContentAdminController controller = controller();
        ClassicsContentRequest.AiCandidateBatchApplyRequest invalidRequest =
                new ClassicsContentRequest.AiCandidateBatchApplyRequest();
        assertThrows(RuntimeException.class, () -> controller.changeAiCandidates(invalidRequest));

        ClassicsContentRequest.AiCandidateApplyRequest item = new ClassicsContentRequest.AiCandidateApplyRequest();
        item.setCandidateId(111L);
        item.setContentType("SANCAI_ENTRY");
        item.setContentId(4001L);
        item.setCapability("summary");
        item.setResultFormat("TEXT");
        item.setResultPayload("payload");
        ClassicsContentRequest.AiCandidateBatchApplyRequest duplicateRequest =
                new ClassicsContentRequest.AiCandidateBatchApplyRequest();
        duplicateRequest.setItems(List.of(item, item));

        assertThrows(RuntimeException.class, () -> controller.changeAiCandidates(duplicateRequest));

        ClassicsContentRequest.AiCandidateBatchRejectRequest rejectRequest =
                new ClassicsContentRequest.AiCandidateBatchRejectRequest();
        ClassicsContentRequest.AiCandidateRejectItemRequest rejectItem =
                new ClassicsContentRequest.AiCandidateRejectItemRequest();
        rejectItem.setCandidateId(113L);
        rejectItem.setContentType("SANCAI_ENTRY");
        rejectItem.setContentId(4002L);
        rejectItem.setCapability("qa");
        rejectRequest.setItems(List.of(rejectItem, rejectItem));

        assertThrows(RuntimeException.class, () -> controller.removeAiCandidates(rejectRequest));
    }

    private static ClassicsContentRequest.AiCandidateApplyRequest validAiCandidateApplyRequest() {
        ClassicsContentRequest.AiCandidateApplyRequest request = new ClassicsContentRequest.AiCandidateApplyRequest();
        request.setCandidateId(123L);
        request.setContentType("SANCAI_ENTRY");
        request.setContentId(456L);
        request.setCapability("tags");
        request.setResultFormat("STRUCTURED");
        request.setResultPayload("{\"tags\":[\"三才\"]}");
        return request;
    }

    @Test
    void classicsBatchOperationResponseShouldIncludeCandidateFieldsWhenPresent() {
        ClassicsBatchOperationResponse response = ClassicsBatchOperationResponse.from(ClassicsBatchOperationResult.of(
                List.of(ClassicsBatchOperationItemResult.successForCandidate(
                        "SANCAI_ENTRY", 1001L, 3001L, "APPLIED", 7001L, 9001L, "summary")),
                List.of(ClassicsBatchOperationItemResult.failureForCandidate(
                        "WANGQI_DOCUMENT", 2001L, "PERMISSION_DENIED", "无权操作", 7002L, null, "qa"))));

        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());

        JsonNode firstSuccess =
                OBJECT_MAPPER.valueToTree(response.getSuccesses().get(0));
        assertEquals(7001L, firstSuccess.get("candidateId").asLong());
        assertEquals("summary", firstSuccess.get("capability").asText());
        assertEquals(9001L, firstSuccess.get("objectId").asLong());
        assertEquals("SANCAI_ENTRY", firstSuccess.get("contentType").asText());
        assertEquals(1001L, firstSuccess.get("contentId").asLong());
        assertEquals("APPLIED", firstSuccess.get("status").asText());

        JsonNode firstFailure = OBJECT_MAPPER.valueToTree(response.getFailures().get(0));
        assertEquals(7002L, firstFailure.get("candidateId").asLong());
        assertEquals("qa", firstFailure.get("capability").asText());
        assertTrue(firstFailure.get("objectId").isNull());
        assertEquals("WANGQI_DOCUMENT", firstFailure.get("contentType").asText());
        assertEquals(2001L, firstFailure.get("contentId").asLong());
        assertEquals("PERMISSION_DENIED", firstFailure.get("failureCode").asText());
        assertEquals("无权操作", firstFailure.get("failureReason").asText());
    }

    @Test
    void tagRequestsShouldMapToServiceCommandsAndResponses() {
        ClassicsContentAdminController controller = controller();
        ClassicsContentRequest addRequest = new ClassicsContentRequest();
        addRequest.setContentType("SANCAI_ENTRY");
        addRequest.setContentId(456L);
        addRequest.setTagId(2001L);
        addRequest.setTagNameSnapshot("礼制");
        addRequest.setSource("MANUAL");
        addRequest.setStatus("ACTIVE");

        ClassicsContentResponse addResponse = controller.addTag(addRequest);
        assertEquals(3001L, addResponse.getId());

        ClassicsContentRequest updateRequest = new ClassicsContentRequest();
        updateRequest.setId(3001L);
        updateRequest.setContentType("SANCAI_ENTRY");
        updateRequest.setContentId(456L);
        updateRequest.setTagId(2002L);
        updateRequest.setTagNameSnapshot("祭祀");
        updateRequest.setSource("MANUAL");
        updateRequest.setStatus("ACTIVE");

        ClassicsContentResponse updateResponse = controller.updateTag(updateRequest);
        assertEquals(3001L, updateResponse.getId());

        ClassicsContentRequest deleteRequest = new ClassicsContentRequest();
        deleteRequest.setId(3001L);
        assertTrue(controller.deleteTag(deleteRequest));

        ClassicsContentRequest addQaRequest = new ClassicsContentRequest();
        addQaRequest.setContentType("SANCAI_ENTRY");
        addQaRequest.setContentId(457L);
        addQaRequest.setQuestion("是什么");
        addQaRequest.setAnswer("这是一个测试");
        addQaRequest.setSource("MANUAL");

        ClassicsContentResponse addQaResponse = controller.addQaPair(addQaRequest);
        assertEquals(4001L, addQaResponse.getId());

        ClassicsContentRequest updateQaRequest = new ClassicsContentRequest();
        updateQaRequest.setId(4001L);
        updateQaRequest.setContentType("SANCAI_ENTRY");
        updateQaRequest.setContentId(457L);
        updateQaRequest.setQuestion("为何");
        updateQaRequest.setAnswer("为了测试");
        updateQaRequest.setSource("MANUAL");

        ClassicsContentResponse updateQaResponse = controller.updateQaPair(updateQaRequest);
        assertEquals(4001L, updateQaResponse.getId());

        ClassicsContentRequest deleteQaRequest = new ClassicsContentRequest();
        deleteQaRequest.setId(4001L);
        assertTrue(controller.deleteQaPair(deleteQaRequest));

        ClassicsContentRequest listTagsRequest = new ClassicsContentRequest();
        listTagsRequest.setContentType("SANCAI_ENTRY");
        listTagsRequest.setContentId(456L);
        JsonNode listed =
                OBJECT_MAPPER.valueToTree(controller.listTags(listTagsRequest).get(0));
        assertEquals(3001L, listed.get("id").asLong());
        assertEquals("SANCAI_ENTRY", listed.get("contentType").asText());
        assertEquals(456L, listed.get("contentId").asLong());
        assertEquals(2001L, listed.get("tagId").asLong());
        assertEquals("礼制", listed.get("tagNameSnapshot").asText());
        assertEquals("MANUAL", listed.get("source").asText());
        assertEquals("ACTIVE", listed.get("status").asText());
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

    @Test
    void sortTagsShouldMapOrderedIdsToCommand() {
        ClassicsContentAdminController controller = controller();
        var request =
                new com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request
                        .ClassicsContentTagSortRequest();
        request.setOrderedIds(List.of(2L, 1L));

        assertTrue(controller.sortTags(request));
    }

    private static ClassicsContentAdminController controller() {
        return new ClassicsContentAdminController(contentService());
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
                        assertTrue(command.getOperatorPermissions() != null);
                        return new ClassicsExportJobResult(
                                ClassicsContentExportJobIdCodec.toDomain(9001L),
                                ClassicsExportStatus.COMPLETED,
                                StorageObjectIdCodec.toDomain(7001L));
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
                                        ClassicsContentExportJobIdCodec.toDomain(9001L),
                                        ClassicsExportStatus.COMPLETED)));
                    }
                    if ("getExportJob".equals(method.getName())) {
                        ClassicsContentExportJobId jobId = (ClassicsContentExportJobId) args[0];
                        if (jobId.value() == 9001L) {
                            return exportJob(
                                    ClassicsContentExportJobIdCodec.toDomain(9001L), ClassicsExportStatus.COMPLETED);
                        }
                        if (jobId.value() == 9002L) {
                            return exportJob(
                                    ClassicsContentExportJobIdCodec.toDomain(9002L), ClassicsExportStatus.REQUESTED);
                        }
                        if (jobId.value() == 9003L) {
                            return exportJob(
                                    ClassicsContentExportJobIdCodec.toDomain(9003L),
                                    ClassicsExportStatus.COMPLETED,
                                    Instant.now().minusSeconds(60));
                        }
                    }
                    if ("getExportJobContent".equals(method.getName())) {
                        ClassicsContentExportJobId jobId = (ClassicsContentExportJobId) args[0];
                        if (jobId != null && jobId.value() == 9001L) {
                            return storedContent(7001L, "export.zip", "application/zip", "zip-data");
                        }
                        return null;
                    }
                    if ("applyAiCandidate".equals(method.getName())) {
                        AiCandidateApplyContentCommand command = (AiCandidateApplyContentCommand) args[0];
                        assertEquals(ClassicsContentType.SANCAI_ENTRY, command.contentType());
                        assertEquals(456L, command.contentId());
                        assertEquals("TEXT", command.resultFormat());
                        if ("summary".equals(command.capability())) {
                            assertEquals(123L, command.candidateId());
                            assertEquals("new summary", command.resultPayload());
                            assertEquals("AI 应用：摘要", command.changeSummary());
                            assertEquals(901L, command.objectId());
                            return new AiCandidateApplyContentResult(ClassicsContentType.SANCAI_ENTRY, 456L, 789L, 3);
                        }
                        if ("translate".equals(command.capability())) {
                            assertEquals(124L, command.candidateId());
                            assertEquals("new translation", command.resultPayload());
                            assertEquals("AI 应用：译文", command.changeSummary());
                            assertEquals(902L, command.objectId());
                            return new AiCandidateApplyContentResult(ClassicsContentType.SANCAI_ENTRY, 456L, 790L, 4);
                        }
                        throw new UnsupportedOperationException("unexpected apply capability: " + command.capability());
                    }
                    if ("applyAiCandidates".equals(method.getName())) {
                        AiCandidateBatchApplyContentCommand command = (AiCandidateBatchApplyContentCommand) args[0];
                        assertEquals(2, command.items().size());
                        assertEquals(111L, command.items().get(0).candidateId());
                        assertEquals(4001L, command.items().get(0).contentId());
                        assertEquals("summary", command.items().get(0).capability());
                        assertEquals(112L, command.items().get(1).candidateId());
                        assertEquals(5001L, command.items().get(1).contentId());
                        assertEquals("tags", command.items().get(1).capability());
                        return ClassicsBatchOperationResult.of(
                                List.of(
                                        ClassicsBatchOperationItemResult.successForCandidate(
                                                "SANCAI_ENTRY", 4001L, 1L, "APPLIED", 111L, 901L, "summary"),
                                        ClassicsBatchOperationItemResult.successForCandidate(
                                                "WANGQI_DOCUMENT", 5001L, 2L, "APPLIED", 112L, null, "tags")),
                                List.of());
                    }
                    if ("rejectAiCandidates".equals(method.getName())) {
                        AiCandidateBatchRejectContentCommand command = (AiCandidateBatchRejectContentCommand) args[0];
                        assertEquals(null, command.errorType());
                        assertEquals(null, command.errorMessage());
                        assertEquals(2, command.items().size());
                        assertEquals(113L, command.items().get(0).candidateId());
                        assertEquals(
                                "SANCAI_ENTRY",
                                command.items().get(0).contentType().value());
                        assertEquals("summary", command.items().get(0).capability());
                        assertEquals(114L, command.items().get(1).candidateId());
                        assertEquals(
                                "WANGQI_DOCUMENT",
                                command.items().get(1).contentType().value());
                        return ClassicsBatchOperationResult.of(
                                List.of(
                                        ClassicsBatchOperationItemResult.successForCandidate(
                                                "SANCAI_ENTRY", 4002L, 113L, "REJECTED", 113L, 902L, "summary"),
                                        ClassicsBatchOperationItemResult.successForCandidate(
                                                "WANGQI_DOCUMENT", 5002L, 114L, "REJECTED", 114L, null, "summary")),
                                List.of());
                    }
                    if ("addTag".equals(method.getName())) {
                        var command =
                                (com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand) args[0];
                        assertEquals(ClassicsContentType.SANCAI_ENTRY, command.getContentType());
                        assertEquals(456L, command.getContentId());
                        assertEquals(2001L, command.getTagId());
                        assertEquals("礼制", command.getTagNameSnapshot());
                        assertEquals("MANUAL", command.getSource().value());
                        assertEquals("ACTIVE", command.getStatus().value());
                        return com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec.toDomain(
                                3001L);
                    }
                    if ("updateTag".equals(method.getName())) {
                        var command =
                                (com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand) args[0];
                        assertEquals(3001L, command.getId());
                        assertEquals(ClassicsContentType.SANCAI_ENTRY, command.getContentType());
                        assertEquals(456L, command.getContentId());
                        assertEquals(2002L, command.getTagId());
                        assertEquals("祭祀", command.getTagNameSnapshot());
                        assertEquals("MANUAL", command.getSource().value());
                        assertEquals("ACTIVE", command.getStatus().value());
                        return com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec.toDomain(
                                3001L);
                    }
                    if ("deleteTag".equals(method.getName())) {
                        assertEquals(
                                3001L,
                                ((com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId)
                                                args[0])
                                        .value());
                        return null;
                    }
                    if ("addQaPair".equals(method.getName())) {
                        var command = (com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand)
                                args[0];
                        assertEquals(ClassicsContentType.SANCAI_ENTRY, command.getContentType());
                        assertEquals(457L, command.getContentId());
                        assertEquals("是什么", command.getQuestion());
                        assertEquals("这是一个测试", command.getAnswer());
                        return com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec.toDomain(
                                4001L);
                    }
                    if ("updateQaPair".equals(method.getName())) {
                        var command = (com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand)
                                args[0];
                        assertEquals(4001L, command.getId());
                        assertEquals(ClassicsContentType.SANCAI_ENTRY, command.getContentType());
                        assertEquals(457L, command.getContentId());
                        assertEquals("为何", command.getQuestion());
                        assertEquals("为了测试", command.getAnswer());
                        assertEquals("MANUAL", command.getSource().value());
                        return com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec.toDomain(
                                4001L);
                    }
                    if ("deleteQaPair".equals(method.getName())) {
                        assertEquals(
                                4001L,
                                ((com.thundax.kuzhambu.classics.domain.content.model.valueobject
                                                        .ClassicsContentQaPairId)
                                                args[0])
                                        .value());
                        return null;
                    }
                    if ("listTags".equals(method.getName())) {
                        assertEquals("SANCAI_ENTRY", args[0]);
                        assertEquals(
                                456L,
                                ((com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId)
                                                args[1])
                                        .value());
                        var tag = new com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag();
                        tag.setId(com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec.toDomain(
                                3001L));
                        tag.setContentType(ClassicsContentType.SANCAI_ENTRY);
                        tag.setContentId(
                                com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec.toDomain(
                                        456L));
                        tag.setTagId(
                                com.thundax.kuzhambu.classics.domain.common.codec.KnowledgeTagIdCodec.toDomain(2001L));
                        tag.setTagNameSnapshot("礼制");
                        tag.setSource(
                                com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource.MANUAL);
                        tag.setStatus(
                                com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus
                                        .ACTIVE);
                        tag.setPriority(1);
                        return List.of(tag);
                    }
                    if ("sortTags".equals(method.getName())) {
                        ContentTagSortCommand command = (ContentTagSortCommand) args[0];
                        assertEquals(
                                List.of(2L, 1L),
                                command.getOrderedIds().stream()
                                        .map(id -> id.value())
                                        .toList());
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsContentExportJob exportJob(ClassicsContentExportJobId id, ClassicsExportStatus status) {
        return exportJob(
                id,
                status,
                status == ClassicsExportStatus.COMPLETED ? Instant.now().plusSeconds(60) : null);
    }

    private static ClassicsContentExportJob exportJob(
            ClassicsContentExportJobId id, ClassicsExportStatus status, Instant expiresAt) {
        return new ClassicsContentExportJob(
                id,
                ClassicsExportKind.CONTENT_DATASET,
                ClassicsContentType.SANCAI_ENTRY,
                ClassicsExportFormat.HTML,
                ClassicsExportScopeType.CATEGORY,
                "all",
                Instant.now(),
                expiresAt,
                status,
                StorageObjectIdCodec.toDomain(7001L),
                1,
                2,
                SancaiVisibilityRiskStatus.PUBLIC_ONLY,
                false);
    }

    private static ClassicsStoredContentResult storedContent(
            Long storageObjectId, String originalFilename, String contentType, String body) {
        byte[] bytes = body == null ? new byte[0] : body.getBytes();
        return new ClassicsStoredContentResult(
                storageObjectId, originalFilename, contentType, (long) bytes.length, new ByteArrayInputStream(bytes));
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
