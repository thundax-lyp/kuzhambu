package com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateApplyResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateSummaryResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptDetailResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptTreeNodeResult;
import com.thundax.kuzhambu.knowledge.application.workbench.service.KnowledgeGraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.request.KnowledgeGraphWorkbenchRequests;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class KnowledgeGraphWorkbenchControllerTest {

    @AfterEach
    void tearDown() {
        KuzhambuContextHolder.clear();
    }

    @Test
    void listManuscriptTreeShouldMapSupportedSourceRoots() {
        KnowledgeGraphWorkbenchApplicationService service = mock(KnowledgeGraphWorkbenchApplicationService.class);
        KnowledgeGraphWorkbenchController controller = new KnowledgeGraphWorkbenchController(service);
        KnowledgeGraphWorkbenchRequests.ManuscriptTreeRequest request =
                new KnowledgeGraphWorkbenchRequests.ManuscriptTreeRequest();
        when(service.listManuscriptTree(null, null, null, null))
                .thenReturn(List.of(
                        sourceRoot("SOURCE_ROOT:SANCAI_ENTRY", "三才图会", "SANCAI_ENTRY"),
                        sourceRoot("SOURCE_ROOT:WANGQI_DOCUMENT", "王圻文档", "WANGQI_DOCUMENT"),
                        sourceRoot("SOURCE_ROOT:MING_CUSTOMS", "明俗稿件", "MING_CUSTOMS")));

        var response = controller.listManuscriptTree(request);

        verify(service).listManuscriptTree(null, null, null, null);
        assertEquals(3, response.size());
        assertEquals("SANCAI_ENTRY", response.get(0).getSourceContentType());
        assertEquals("WANGQI_DOCUMENT", response.get(1).getSourceContentType());
        assertEquals("MING_CUSTOMS", response.get(2).getSourceContentType());
    }

    @Test
    void getManuscriptShouldMapDetailResponse() {
        KnowledgeGraphWorkbenchApplicationService service = mock(KnowledgeGraphWorkbenchApplicationService.class);
        KnowledgeGraphWorkbenchController controller = new KnowledgeGraphWorkbenchController(service);
        KnowledgeGraphWorkbenchRequests.ManuscriptRequest request =
                new KnowledgeGraphWorkbenchRequests.ManuscriptRequest();
        request.setSourceContentType("SANCAI_ENTRY");
        request.setSourceContentId(1001L);
        when(service.getManuscript("SANCAI_ENTRY", 1001L))
                .thenReturn(ManuscriptDetailResult.builder()
                        .sourceContentType("SANCAI_ENTRY")
                        .sourceContentId(1001L)
                        .title("三才稿件")
                        .graphStatus("CANDIDATE_READY")
                        .latestExtractionTask(taskResult("9001", "GRAPH", "SUCCEEDED"))
                        .build());

        var response = controller.getManuscript(request);

        verify(service).getManuscript(eq("SANCAI_ENTRY"), eq(1001L));
        assertEquals("三才稿件", response.getTitle());
        assertEquals("CANDIDATE_READY", response.getGraphStatus());
        assertEquals("9001", response.getLatestExtractionTask().getTaskId());
    }

    @Test
    void extractManuscriptShouldMapTaskResponse() {
        KnowledgeGraphWorkbenchApplicationService service = mock(KnowledgeGraphWorkbenchApplicationService.class);
        KnowledgeGraphWorkbenchController controller = new KnowledgeGraphWorkbenchController(service);
        KuzhambuContextHolder.setSubject(
                new KuzhambuSubject("99", KuzhambuSubjectType.ADMIN_USER, "Admin", "token-1", List.of()));
        KnowledgeGraphWorkbenchRequests.ManuscriptExtractRequest request =
                new KnowledgeGraphWorkbenchRequests.ManuscriptExtractRequest();
        request.setSourceContentType("SANCAI_ENTRY");
        request.setSourceContentId(1001L);
        request.setTaskType("GRAPH");
        when(service.extractManuscript("SANCAI_ENTRY", 1001L, "GRAPH", 99L))
                .thenReturn(taskResult("9002", "GRAPH", "REQUESTED"));

        var response = controller.extractManuscript(request);

        verify(service).extractManuscript(eq("SANCAI_ENTRY"), eq(1001L), eq("GRAPH"), eq(99L));
        assertEquals("9002", response.getTaskId());
        assertEquals("GRAPH", response.getTaskType());
        assertEquals("REQUESTED", response.getStatus());
    }

    @Test
    void getLatestCandidateShouldMapCandidateSummary() {
        KnowledgeGraphWorkbenchApplicationService service = mock(KnowledgeGraphWorkbenchApplicationService.class);
        KnowledgeGraphWorkbenchController controller = new KnowledgeGraphWorkbenchController(service);
        KnowledgeGraphWorkbenchRequests.CandidateRequest request =
                new KnowledgeGraphWorkbenchRequests.CandidateRequest();
        request.setSourceContentType("MING_CUSTOMS");
        request.setSourceContentId(3001L);
        request.setTaskType("LINEAGE");
        when(service.getLatestCandidate("MING_CUSTOMS", 3001L, "LINEAGE"))
                .thenReturn(CandidateSummaryResult.builder()
                        .taskId(9003L)
                        .aiCandidateId(7003L)
                        .taskType("LINEAGE")
                        .status("SUCCEEDED")
                        .sourceContentType("MING_CUSTOMS")
                        .sourceContentId(3001L)
                        .candidatePayloadJson("{\"lineageNodes\":[{\"name\":\"朱元璋\"}]}")
                        .build());

        var response = controller.getLatestCandidate(request);

        verify(service).getLatestCandidate(eq("MING_CUSTOMS"), eq(3001L), eq("LINEAGE"));
        assertEquals(9003L, response.getTaskId());
        assertEquals(7003L, response.getAiCandidateId());
        assertEquals("LINEAGE", response.getTaskType());
        assertEquals("{\"lineageNodes\":[{\"name\":\"朱元璋\"}]}", response.getCandidatePayloadJson());
    }

    @Test
    void applyCandidateShouldMapApplyResult() {
        KnowledgeGraphWorkbenchApplicationService service = mock(KnowledgeGraphWorkbenchApplicationService.class);
        KnowledgeGraphWorkbenchController controller = new KnowledgeGraphWorkbenchController(service);
        KnowledgeGraphWorkbenchRequests.CandidateApplyRequest request =
                new KnowledgeGraphWorkbenchRequests.CandidateApplyRequest();
        request.setTaskId(9001L);
        when(service.applyCandidate(9001L))
                .thenReturn(CandidateApplyResult.builder()
                        .taskId(9001L)
                        .graphVersionId(8001L)
                        .graphStatus("APPLIED")
                        .build());

        var response = controller.applyCandidate(request);

        verify(service).applyCandidate(eq(9001L));
        assertEquals(9001L, response.getTaskId());
        assertEquals(8001L, response.getGraphVersionId());
        assertEquals("APPLIED", response.getGraphStatus());
    }

    @Test
    void workbenchEndpointsShouldDeclareExpectedPermissions() throws NoSuchMethodException {
        assertPermission(
                "listManuscriptTree",
                "knowledge:graph:view",
                KnowledgeGraphWorkbenchRequests.ManuscriptTreeRequest.class);
        assertPermission(
                "getManuscript", "knowledge:graph:view", KnowledgeGraphWorkbenchRequests.ManuscriptRequest.class);
        assertPermission(
                "extractManuscript",
                "knowledge:graph:edit",
                KnowledgeGraphWorkbenchRequests.ManuscriptExtractRequest.class);
        assertPermission(
                "getLatestCandidate", "knowledge:graph:view", KnowledgeGraphWorkbenchRequests.CandidateRequest.class);
        assertPermission(
                "applyCandidate", "knowledge:graph:apply", KnowledgeGraphWorkbenchRequests.CandidateApplyRequest.class);
    }

    private static ManuscriptTreeNodeResult sourceRoot(String nodeKey, String title, String sourceContentType) {
        return ManuscriptTreeNodeResult.builder()
                .nodeKey(nodeKey)
                .nodeType("SOURCE_ROOT")
                .title(title)
                .sourceContentType(sourceContentType)
                .graphStatus("NOT_EXTRACTED")
                .children(List.of())
                .build();
    }

    private static GraphExtractionTaskResult taskResult(String taskId, String taskType, String status) {
        return new GraphExtractionTaskResult(
                taskId,
                null,
                taskType,
                "CLASSICS_MANUSCRIPT",
                "{\"sourceContentId\":1001}",
                "MANUAL",
                null,
                Boolean.TRUE,
                null,
                "SANCAI_ENTRY",
                1001L,
                301L,
                302L,
                status,
                null,
                null,
                99L,
                1710000000000L,
                null,
                null);
    }

    private static void assertPermission(String methodName, String permission, Class<?> requestType)
            throws NoSuchMethodException {
        Method method = KnowledgeGraphWorkbenchController.class.getMethod(methodName, requestType);
        HasPermission annotation = method.getAnnotation(HasPermission.class);
        assertTrue(annotation != null);
        assertArrayEquals(new String[] {permission}, annotation.value());
    }
}
