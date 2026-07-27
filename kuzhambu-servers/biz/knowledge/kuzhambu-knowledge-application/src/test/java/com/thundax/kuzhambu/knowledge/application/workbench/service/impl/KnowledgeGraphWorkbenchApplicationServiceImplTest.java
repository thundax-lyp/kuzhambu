package com.thundax.kuzhambu.knowledge.application.workbench.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
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

    @Test
    void listManuscriptTreeShouldExposeAllSupportedSources() {
        Fixtures fixtures = new Fixtures();

        var roots = fixtures.service.listManuscriptTree(null, null, null, null);

        assertEquals(3, roots.size());
        assertEquals("SANCAI_ENTRY", roots.get(0).getSourceContentType());
        assertEquals("WANGQI_DOCUMENT", roots.get(1).getSourceContentType());
        assertEquals("MING_CUSTOMS", roots.get(2).getSourceContentType());
    }

    @Test
    void listManuscriptTreeShouldLoadManuscriptsForEachSourceType() {
        Fixtures fixtures = new Fixtures();

        var sancaiNodes =
                fixtures.service.listManuscriptTree("SANCAI_ENTRY", "CATEGORY:SANCAI_ENTRY:sancai", null, null);
        var wangqiNodes =
                fixtures.service.listManuscriptTree("WANGQI_DOCUMENT", "CATEGORY:WANGQI_DOCUMENT:wangqi", null, null);
        var mingNodes = fixtures.service.listManuscriptTree("MING_CUSTOMS", "CATEGORY:MING_CUSTOMS:ming", null, null);

        assertEquals("三才稿件", sancaiNodes.get(0).getTitle());
        assertEquals("王圻稿件", wangqiNodes.get(0).getTitle());
        assertEquals("明俗稿件", mingNodes.get(0).getTitle());
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
    void applyCandidateShouldReturnAppliedGraphStatus() {
        Fixtures fixtures = new Fixtures();
        when(fixtures.graphExtractionApplicationService.applyTaskCandidate(any()))
                .thenReturn(taskResult("9001", "GRAPH", "APPLIED"));
        when(fixtures.graphExtractionApplicationService.pageVersions(
                        eq("GRAPH"), eq(null), eq("SANCAI_ENTRY"), eq(1001L), any(PageQuery.class)))
                .thenReturn(PageResult.of(
                        1,
                        1,
                        1,
                        java.util.List.of(new GraphVersionResult(
                                8001L, "9001", null, "GRAPH", "SANCAI_ENTRY", 1001L, 1, "APPLIED", 99L))));

        var result = fixtures.service.applyCandidate(9001L);

        assertEquals(9001L, result.getTaskId());
        assertEquals(8001L, result.getGraphVersionId());
        assertEquals("APPLIED", result.getGraphStatus());
    }

    private static GraphExtractionTaskResult taskResult(String taskId, String taskType, String status) {
        return new GraphExtractionTaskResult(
                taskId,
                null,
                taskType,
                "CLASSICS_MANUSCRIPT",
                "{\"sourceContentType\":\"SANCAI_ENTRY\",\"sourceContentId\":1001}",
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
            when(classicsFacade.listPublicContents())
                    .thenReturn(ClassicsPublicContentsFacadeResponse.builder()
                            .contents(java.util.List.of(
                                    content("SANCAI_ENTRY", "1001", "sancai", "三才分类", "三才稿件"),
                                    content("WANGQI_DOCUMENT", "2001", "wangqi", "王圻分类", "王圻稿件"),
                                    content("MING_CUSTOMS", "3001", "ming", "明俗分类", "明俗稿件")))
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
                    .title(title)
                    .summary(title + "摘要")
                    .currentVersionNo(1)
                    .build();
        }
    }
}
