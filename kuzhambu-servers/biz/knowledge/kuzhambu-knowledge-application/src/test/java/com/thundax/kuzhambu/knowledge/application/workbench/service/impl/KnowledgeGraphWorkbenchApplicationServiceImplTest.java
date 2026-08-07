package com.thundax.kuzhambu.knowledge.application.workbench.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeQualityReportApplicationService;
import com.thundax.kuzhambu.knowledge.application.workbench.support.KnowledgeGraphManuscriptPayloadBuilder;
import com.thundax.kuzhambu.knowledge.application.workbench.support.KnowledgeGraphManuscriptPayloadBuilder.ManuscriptExtractionPayload;
import com.thundax.kuzhambu.knowledge.application.workbench.support.KnowledgeGraphManuscriptTreeAssembler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeGraphWorkbenchApplicationServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void listManuscriptTreeShouldExposeSancaiSourceRoot() {
        Fixtures fixtures = new Fixtures();

        var roots = fixtures.service.listManuscriptTree(null, null, null, null);

        assertEquals(1, roots.size());
        assertEquals("SANCAI_ENTRY", roots.get(0).getSourceContentType());
        verify(fixtures.classicsFacade, never()).listWorkbenchContents();
    }

    @Test
    void listManuscriptTreeShouldBatchGraphStatusLookups() {
        Fixtures fixtures = new Fixtures();

        var sancaiNodes =
                fixtures.service.listManuscriptTree("SANCAI_ENTRY", "VOLUME:SANCAI_ENTRY:sancai:11", null, null);

        assertEquals(2, sancaiNodes.size());
        assertEquals("三才稿件", sancaiNodes.get(0).getTitle());
        assertEquals("三才稿件二", sancaiNodes.get(1).getTitle());
        verify(fixtures.graphExtractionApplicationService, times(1))
                .pageTasks(
                        eq("GRAPH"), eq(null), eq(null), eq(null), eq("SANCAI_ENTRY"), eq(null), any(PageQuery.class));
        verify(fixtures.graphExtractionApplicationService, times(1))
                .pageVersions(eq("GRAPH"), eq(null), eq("SANCAI_ENTRY"), eq(null), any(PageQuery.class));
    }

    @Test
    void listManuscriptTreeShouldIgnoreUnsupportedGraphSources() {
        Fixtures fixtures = new Fixtures();

        var nodes = fixtures.service.listManuscriptTree(null, null, "王圻", null);

        assertEquals(0, nodes.size());
    }

    @Test
    void extractManuscriptShouldBuildGraphCommandWithAutomaticPayload() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.payloadBuilder.build("SANCAI_ENTRY", 1001L, "GRAPH"))
                .thenReturn(payload("SANCAI_ENTRY", 1001L, "GRAPH"));
        when(fixtures.graphExtractionApplicationService.requestGraphExtraction(any()))
                .thenReturn(taskResult("9001", "GRAPH", "REQUESTED"));

        fixtures.service.extractManuscript("SANCAI_ENTRY", 1001L, "GRAPH", 99L);

        ArgumentCaptor<RequestGraphExtractionCommand> captor =
                ArgumentCaptor.forClass(RequestGraphExtractionCommand.class);
        verify(fixtures.graphExtractionApplicationService).requestGraphExtraction(captor.capture());
        RequestGraphExtractionCommand command = captor.getValue();
        assertEquals("CLASSICS_MANUSCRIPT", command.getScopeType());
        assertEquals("{\"sourceContentType\":\"SANCAI_ENTRY\",\"sourceContentId\":1001}", command.getScopeJson());
        assertEquals("MANUAL", command.getTriggerSource());
        assertNull(command.getSelectionScopeJson());
        assertEquals(Boolean.TRUE, command.getReplaceUnconfirmedOnly());
        assertEquals("SANCAI_ENTRY", command.getSourceContentType());
        assertEquals(1001L, command.getSourceContentId());
        assertEquals(99L, command.getRequestedBy());
        assertEquals("[{\"role\":\"system\",\"content\":\"extract\"}]", command.getPromptMessagesJson());
        assertEquals("{\"content\":\"三才稿件\"}", command.getInputPayloadJson());
    }

    @Test
    void extractManuscriptShouldRejectUnsupportedSourceType() {
        Fixtures fixtures = new Fixtures();

        assertThrows(
                BizException.class, () -> fixtures.service.extractManuscript("WANGQI_DOCUMENT", 2001L, "GRAPH", 99L));

        verify(fixtures.payloadBuilder, never()).build(any(), any(), any());
        verify(fixtures.graphExtractionApplicationService, never()).requestGraphExtraction(any());
    }

    @Test
    void payloadBuilderShouldExposePromptVariablesAndUseConfiguredOutputSchema() throws Exception {
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        when(classicsFacade.getWorkbenchQaKnowledge(any()))
                .thenReturn(ClassicsQaKnowledgeFacadeResponse.builder()
                        .knowledge(ClassicsQaKnowledgeFacadeDto.builder()
                                .sourceId("SANCAI_ENTRY:1001")
                                .contentType("SANCAI_ENTRY")
                                .contentId("1001")
                                .title("三才稿件")
                                .categoryPath("人物 / 三才")
                                .summary("摘要")
                                .body("正文")
                                .originalText("原文")
                                .translationText("译文")
                                .originalExcerpts("摘录")
                                .build())
                        .build());
        KnowledgeGraphManuscriptPayloadBuilder builder =
                new KnowledgeGraphManuscriptPayloadBuilder(classicsFacade, OBJECT_MAPPER);

        ManuscriptExtractionPayload payload = builder.build("SANCAI_ENTRY", 1001L, "GRAPH");

        JsonNode inputPayload = OBJECT_MAPPER.readTree(payload.inputPayloadJson());
        assertEquals("三才稿件", inputPayload.get("title").asText());
        assertEquals("正文\n\n原文\n\n译文\n\n摘录", inputPayload.get("content").asText());
        assertEquals("三才稿件", inputPayload.get("sourceTitle").asText());
        assertEquals("正文\n\n原文\n\n译文\n\n摘录", inputPayload.get("sourceText").asText());
        assertEquals(
                "SANCAI_ENTRY",
                inputPayload.get("entryRefs").get(0).get("contentType").asText());
        assertEquals("人物 / 三才", inputPayload.get("lineageHint").asText());
        assertNull(payload.modelId());
        assertNull(payload.modelName());
        JsonNode outputSchema = OBJECT_MAPPER.readTree(payload.outputSchemaJson());
        assertEquals(
                "其他",
                outputSchema
                        .get("properties")
                        .get("entities")
                        .get("items")
                        .get("properties")
                        .get("entityType")
                        .get("enum")
                        .get(6)
                        .asText());
    }

    @Test
    void extractManuscriptShouldRouteAllTaskTypes() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.payloadBuilder.build(eq("SANCAI_ENTRY"), eq(1001L), any()))
                .thenAnswer(invocation -> payload("SANCAI_ENTRY", 1001L, invocation.getArgument(2)));
        when(fixtures.graphExtractionApplicationService.requestRelationExtraction(any()))
                .thenReturn(taskResult("9001", "RELATION", "REQUESTED"));
        when(fixtures.graphExtractionApplicationService.requestGraphExtraction(any()))
                .thenReturn(taskResult("9002", "GRAPH", "REQUESTED"));
        when(fixtures.graphExtractionApplicationService.requestLineageExtraction(any()))
                .thenReturn(taskResult("9003", "LINEAGE", "REQUESTED"));

        fixtures.service.extractManuscript("SANCAI_ENTRY", 1001L, "RELATION", 99L);
        fixtures.service.extractManuscript("SANCAI_ENTRY", 1001L, "GRAPH", 99L);
        fixtures.service.extractManuscript("SANCAI_ENTRY", 1001L, "LINEAGE", 99L);

        verify(fixtures.graphExtractionApplicationService)
                .requestRelationExtraction(any(RequestRelationExtractionCommand.class));
        verify(fixtures.graphExtractionApplicationService)
                .requestGraphExtraction(any(RequestGraphExtractionCommand.class));
        verify(fixtures.graphExtractionApplicationService)
                .requestLineageExtraction(any(RequestLineageExtractionCommand.class));
    }

    @Test
    void getLatestCandidateShouldDefaultToGraphTaskType() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.graphExtractionApplicationService.pageTasks(
                        eq("GRAPH"), eq(null), eq(null), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 1, 1, java.util.List.of(taskResult("9001", "GRAPH", "SUCCEEDED"))));
        when(fixtures.aiFacade.getCandidate(any(GetAiCandidateFacadeRequest.class)))
                .thenReturn(AiCandidateFacadeDto.builder()
                        .candidateId(302L)
                        .resultPayload("{\"entities\":[{\"name\":\"黄帝\"}]}")
                        .build());

        var result = fixtures.service.getLatestCandidate("SANCAI_ENTRY", 1001L, null);

        assertEquals(9001L, result.getTaskId());
        assertEquals(302L, result.getAiCandidateId());
        assertEquals("GRAPH", result.getTaskType());
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals("{\"entities\":[{\"name\":\"黄帝\"}]}", result.getCandidatePayloadJson());
    }

    @Test
    void getManuscriptShouldPreferNewerExtractionTaskStatusOverExistingVersion() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.classicsFacade.getWorkbenchContent(any()))
                .thenReturn(ClassicsPublicContentFacadeResponse.builder()
                        .content(Fixtures.content("SANCAI_ENTRY", "1001", "sancai", "三才分类", "三才稿件"))
                        .build());
        when(fixtures.graphExtractionApplicationService.pageTasks(
                        eq("GRAPH"), eq(null), eq(null), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(
                        1, 1, 1, java.util.List.of(taskResult("9002", "GRAPH", "REQUESTED", 1_720_000_000_000L))));
        when(fixtures.graphExtractionApplicationService.pageVersions(
                        eq("GRAPH"), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(
                        1,
                        1,
                        1,
                        java.util.List.of(new GraphVersionResult(
                                8001L,
                                "9001",
                                null,
                                "GRAPH",
                                "SANCAI_ENTRY",
                                1001L,
                                1,
                                "APPLIED",
                                1_710_000_000_000L))));

        var result = fixtures.service.getManuscript("SANCAI_ENTRY", 1001L);

        assertEquals("EXTRACTING", result.getGraphStatus());
        assertEquals("9002", result.getLatestExtractionTask().getTaskId());
        assertEquals(8001L, result.getLatestGraphVersion().getVersionId());
    }

    @Test
    void getManuscriptShouldPreferLatestTaskLineageOverAppliedVersionTimestamp() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.classicsFacade.getWorkbenchContent(any()))
                .thenReturn(ClassicsPublicContentFacadeResponse.builder()
                        .content(Fixtures.content("SANCAI_ENTRY", "1001", "sancai", "三才分类", "三才稿件"))
                        .build());
        when(fixtures.graphExtractionApplicationService.pageTasks(
                        eq("GRAPH"), eq(null), eq(null), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(
                        1, 1, 1, java.util.List.of(taskResult("9002", "GRAPH", "REQUESTED", 1_710_000_000_000L))));
        when(fixtures.graphExtractionApplicationService.pageVersions(
                        eq("GRAPH"), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(
                        1,
                        1,
                        1,
                        java.util.List.of(new GraphVersionResult(
                                8001L,
                                "9001",
                                null,
                                "GRAPH",
                                "SANCAI_ENTRY",
                                1001L,
                                1,
                                "APPLIED",
                                1_720_000_000_000L))));

        var result = fixtures.service.getManuscript("SANCAI_ENTRY", 1001L);

        assertEquals("EXTRACTING", result.getGraphStatus());
        assertEquals("9002", result.getLatestExtractionTask().getTaskId());
        assertEquals(8001L, result.getLatestGraphVersion().getVersionId());
    }

    @Test
    void getManuscriptShouldIgnoreNewerNonGraphTaskWhenResolvingGraphStatus() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.classicsFacade.getWorkbenchContent(any()))
                .thenReturn(ClassicsPublicContentFacadeResponse.builder()
                        .content(Fixtures.content("SANCAI_ENTRY", "1001", "sancai", "三才分类", "三才稿件"))
                        .build());
        when(fixtures.graphExtractionApplicationService.pageTasks(
                        eq("GRAPH"), eq(null), eq(null), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(
                        1, 1, 1, java.util.List.of(taskResult("9001", "GRAPH", "SUCCEEDED", 1_710_000_000_000L))));
        when(fixtures.graphExtractionApplicationService.pageVersions(
                        eq("GRAPH"), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(
                        1,
                        1,
                        1,
                        java.util.List.of(new GraphVersionResult(
                                8001L,
                                "9001",
                                null,
                                "GRAPH",
                                "SANCAI_ENTRY",
                                1001L,
                                1,
                                "APPLIED",
                                1_720_000_000_000L))));

        var result = fixtures.service.getManuscript("SANCAI_ENTRY", 1001L);

        assertEquals("APPLIED", result.getGraphStatus());
        assertEquals("9001", result.getLatestExtractionTask().getTaskId());
        assertEquals(8001L, result.getLatestGraphVersion().getVersionId());
    }

    @Test
    void applyCandidateShouldReturnAppliedGraphStatus() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.graphExtractionApplicationService.getTaskDetail(any()))
                .thenReturn(taskResult("9001", "GRAPH", "SUCCEEDED"));
        when(fixtures.graphExtractionApplicationService.applyTaskCandidate(any(), eq("APPEND")))
                .thenReturn(taskResult("9001", "GRAPH", "APPLIED"));
        when(fixtures.graphExtractionApplicationService.pageVersions(
                        eq("GRAPH"), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(
                        1,
                        1,
                        1,
                        java.util.List.of(new GraphVersionResult(
                                8001L, "9001", null, "GRAPH", "SANCAI_ENTRY", 1001L, 1, "APPLIED", 99L))));

        var result = fixtures.service.applyCandidate(9001L, "APPEND");

        assertEquals(9001L, result.getTaskId());
        assertEquals(8001L, result.getGraphVersionId());
        assertEquals("APPLIED", result.getGraphStatus());
        verify(fixtures.graphExtractionApplicationService).applyTaskCandidate(any(), eq("APPEND"));
    }

    @Test
    void applyCandidateShouldRejectUnsupportedSourceTypeBeforeApplying() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.graphExtractionApplicationService.getTaskDetail(any()))
                .thenReturn(taskResult("9004", "GRAPH", "SUCCEEDED", "WANGQI_DOCUMENT", 2001L, 1_710_000_000_000L));

        assertThrows(BizException.class, () -> fixtures.service.applyCandidate(9004L));

        verify(fixtures.graphExtractionApplicationService, never()).applyTaskCandidate(any(), any());
    }

    private static GraphExtractionTaskResult taskResult(String taskId, String taskType, String status) {
        return taskResult(taskId, taskType, status, 1710000000000L);
    }

    private static GraphExtractionTaskResult taskResult(
            String taskId, String taskType, String status, Long requestedAt) {
        return taskResult(taskId, taskType, status, "SANCAI_ENTRY", 1001L, requestedAt);
    }

    private static GraphExtractionTaskResult taskResult(
            String taskId,
            String taskType,
            String status,
            String sourceContentType,
            Long sourceContentId,
            Long requestedAt) {
        return new GraphExtractionTaskResult(
                taskId,
                null,
                taskType,
                "CLASSICS_MANUSCRIPT",
                "{\"sourceContentType\":\"" + sourceContentType + "\",\"sourceContentId\":" + sourceContentId + "}",
                "MANUAL",
                null,
                Boolean.TRUE,
                null,
                sourceContentType,
                sourceContentId,
                301L,
                302L,
                status,
                null,
                null,
                99L,
                requestedAt,
                null,
                null);
    }

    private static ManuscriptExtractionPayload payload(
            String sourceContentType, Long sourceContentId, String taskType) {
        return new ManuscriptExtractionPayload(
                "CLASSICS_MANUSCRIPT",
                "{\"sourceContentType\":\"" + sourceContentType + "\",\"sourceContentId\":" + sourceContentId + "}",
                sourceContentType,
                sourceContentId,
                1L,
                "gpt-5.5",
                "request-1",
                "trace-1",
                "[{\"role\":\"system\",\"content\":\"extract\"}]",
                "{\"content\":\"三才稿件\"}",
                "{\"type\":\"object\"}",
                true,
                "zh-CN");
    }

    private static final class Fixtures {
        private final ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        private final KnowledgeGraphExtractionApplicationService graphExtractionApplicationService =
                mock(KnowledgeGraphExtractionApplicationService.class);
        private final KnowledgeQualityReportApplicationService qualityReportApplicationService =
                mock(KnowledgeQualityReportApplicationService.class);
        private final AiFacade aiFacade = mock(AiFacade.class);
        private final KnowledgeGraphManuscriptPayloadBuilder payloadBuilder =
                mock(KnowledgeGraphManuscriptPayloadBuilder.class);
        private final KnowledgeGraphWorkbenchApplicationServiceImpl service =
                new KnowledgeGraphWorkbenchApplicationServiceImpl(
                        classicsFacade,
                        graphExtractionApplicationService,
                        qualityReportApplicationService,
                        aiFacade,
                        new KnowledgeGraphManuscriptTreeAssembler(),
                        payloadBuilder);

        private Fixtures() {
            when(classicsFacade.listWorkbenchContents())
                    .thenReturn(ClassicsPublicContentsFacadeResponse.builder()
                            .contents(java.util.List.of(
                                    content("SANCAI_ENTRY", "1001", "sancai", "三才分类", "三才稿件"),
                                    content("SANCAI_ENTRY", "1002", "sancai", "三才分类", "三才稿件二"),
                                    content("WANGQI_DOCUMENT", "2001", "wangqi", "王圻分类", "王圻稿件"),
                                    content("MING_CUSTOMS", "3001", "ming", "明俗分类", "明俗稿件")))
                            .build());
            when(classicsFacade.listWorkbenchContents("sancai", "11"))
                    .thenReturn(ClassicsPublicContentsFacadeResponse.builder()
                            .contents(java.util.List.of(
                                    content("SANCAI_ENTRY", "1001", "sancai", "三才分类", "三才稿件"),
                                    content("SANCAI_ENTRY", "1002", "sancai", "三才分类", "三才稿件二")))
                            .build());
            when(classicsFacade.listWorkbenchVolumeContents())
                    .thenReturn(ClassicsPublicContentsFacadeResponse.builder()
                            .contents(java.util.List.of(content("SANCAI_ENTRY", "11", "sancai", "三才分类", "三才卷目")))
                            .build());
            when(graphExtractionApplicationService.pageTasks(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(PageResult.of(1, 1, 0, java.util.List.of()));
            when(graphExtractionApplicationService.pageVersions(any(), any(), any(), any(), any()))
                    .thenReturn(PageResult.of(1, 1, 0, java.util.List.of()));
        }

        private static ClassicsPublicContentFacadeDto content(
                String contentType, String contentId, String categoryCode, String categoryName, String title) {
            return ClassicsPublicContentFacadeDto.builder()
                    .contentType(contentType)
                    .contentId(contentId)
                    .categoryCode(categoryCode)
                    .categoryName(categoryName)
                    .volumeCode("11")
                    .volumeName("三才卷目")
                    .title(title)
                    .summary(title + "摘要")
                    .currentVersionNo(1)
                    .build();
        }
    }
}
