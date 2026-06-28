package com.thundax.kuzhambu.ai.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.discovery.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.facade.assembler.AiFacadeAssembler;
import com.thundax.kuzhambu.ai.application.knowledge.service.impl.KnowledgeAiExtractionApplicationServiceImpl;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult.TopCapabilityResult;
import com.thundax.kuzhambu.ai.application.report.service.AiReportApplicationService;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateApplyCheck;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CreateAiBatchJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiCallRecordFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AiFacadeImplTest {

    @Test
    void summaryShouldDelegateAndMapFacadeResponse() {
        AiReportApplicationService aiReportApplicationService = mock(AiReportApplicationService.class);
        Date periodStart = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
        Date periodEnd = Date.from(Instant.parse("2025-01-31T23:59:59Z"));
        when(aiReportApplicationService.summary(periodStart, periodEnd, "DAY"))
                .thenReturn(new AiReportSummaryResult(
                        periodStart,
                        periodEnd,
                        9L,
                        7L,
                        2L,
                        320L,
                        new BigDecimal("12.34"),
                        List.of(new TopCapabilityResult("DISCOVERY_QA", 5L))));
        AiFacadeImpl facade = newFacade(
                aiReportApplicationService,
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationServiceImpl.class),
                mock(AiInvocationRepository.class),
                mock(AiCandidateDomainService.class));

        var response = facade.summary(AiReportSummaryFacadeRequest.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .bucketType("DAY")
                .build());

        assertEquals(periodStart, response.getPeriodStart());
        assertEquals(periodEnd, response.getPeriodEnd());
        assertEquals(9L, response.getInvocationCount());
        assertEquals(7L, response.getSucceededInvocationCount());
        assertEquals(2L, response.getFailedInvocationCount());
        assertEquals(320L, response.getAvgLatencyMs());
        assertEquals(new BigDecimal("12.34"), response.getTotalCostAmount());
        assertEquals(1, response.getTopCapabilities().size());
        assertEquals("DISCOVERY_QA", response.getTopCapabilities().get(0).getCapability());
        assertEquals(5L, response.getTopCapabilities().get(0).getInvocationCount());
    }

    @Test
    void createBatchJobShouldAssembleCommandAndMapActionResponse() {
        AiBatchJobApplicationService aiBatchJobApplicationService = mock(AiBatchJobApplicationService.class);
        when(aiBatchJobApplicationService.create(any())).thenReturn(88L);
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                aiBatchJobApplicationService,
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationServiceImpl.class),
                mock(AiInvocationRepository.class),
                mock(AiCandidateDomainService.class));
        CreateAiBatchJobFacadeRequest request = CreateAiBatchJobFacadeRequest.builder()
                .scope("knowledge")
                .capability("KNOWLEDGE_GRAPH")
                .contentType("WANGQI_DOCUMENT")
                .totalCount(12)
                .failureSummaryJson("{\"retry\":0}")
                .build();

        var response = facade.createBatchJob(request);
        ArgumentCaptor<AiBatchJobCreateCommand> captor = ArgumentCaptor.forClass(AiBatchJobCreateCommand.class);
        verify(aiBatchJobApplicationService).create(captor.capture());

        AiBatchJobCreateCommand command = captor.getValue();
        assertEquals("knowledge", command.getScope());
        assertEquals("KNOWLEDGE_GRAPH", command.getCapability());
        assertEquals("WANGQI_DOCUMENT", command.getContentType());
        assertEquals(12, command.getTotalCount());
        assertEquals("{\"retry\":0}", command.getFailureSummaryJson());
        assertEquals(88L, response.getBatchId());
    }

    @Test
    void understandDiscoveryQueryShouldMapRequestAndResponse() {
        DiscoveryAiApplicationService discoveryAiApplicationService = mock(DiscoveryAiApplicationService.class);
        when(discoveryAiApplicationService.understandQuery(any())).thenAnswer(invocation -> {
            DiscoveryAiRequest request = invocation.getArgument(0);
            assertEquals(21L, request.getServiceId());
            assertEquals("DISCOVERY", request.getServiceRole());
            assertEquals(31L, request.getModelId());
            assertEquals("gpt-5", request.getModelName());
            assertEquals(41L, request.getPromptVersionId());
            assertEquals("req-1", request.getRequestId());
            assertEquals("trace-1", request.getTraceId());
            assertEquals("[\"prompt\"]", request.getPromptMessagesJson());
            assertEquals("{\"lang\":\"zh\"}", request.getPromptVariablesJson());
            assertEquals("hash-1", request.getPromptHash());
            assertEquals("{\"query\":\"苏东坡\"}", request.getInputPayloadJson());
            assertEquals("{\"type\":\"object\"}", request.getOutputSchemaJson());
            assertTrue(request.isStream());
            assertTrue(request.isForceJson());
            assertEquals("zh-CN", request.getLocale());
            return new DiscoveryAiResult(
                    501L,
                    601L,
                    "SUCCEEDED",
                    "DISCOVERY_UNDERSTAND_QUERY",
                    "JSON",
                    "{\"intent\":\"search\"}",
                    null,
                    null);
        });
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                discoveryAiApplicationService,
                mock(KnowledgeAiExtractionApplicationServiceImpl.class),
                mock(AiInvocationRepository.class),
                mock(AiCandidateDomainService.class));

        var response = facade.understandDiscoveryQuery(DiscoveryAiFacadeRequest.builder()
                .serviceId(21L)
                .serviceRole("DISCOVERY")
                .modelId(31L)
                .modelName("gpt-5")
                .promptVersionId(41L)
                .requestId("req-1")
                .traceId("trace-1")
                .promptMessagesJson("[\"prompt\"]")
                .promptVariablesJson("{\"lang\":\"zh\"}")
                .promptHash("hash-1")
                .inputPayloadJson("{\"query\":\"苏东坡\"}")
                .outputSchemaJson("{\"type\":\"object\"}")
                .stream(true)
                .forceJson(true)
                .locale("zh-CN")
                .build());

        assertEquals(501L, response.getCallId());
        assertEquals(601L, response.getCandidateId());
        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals("DISCOVERY_UNDERSTAND_QUERY", response.getCapability());
        assertEquals("JSON", response.getResultFormat());
        assertEquals("{\"intent\":\"search\"}", response.getResultPayload());
        assertNull(response.getErrorType());
        assertNull(response.getErrorMessage());
    }

    @Test
    void extractKnowledgeGraphShouldMapRequestAndResponse() {
        KnowledgeAiExtractionApplicationServiceImpl knowledgeAiExtractionApplicationService =
                mock(KnowledgeAiExtractionApplicationServiceImpl.class);
        when(knowledgeAiExtractionApplicationService.extractGraph(any())).thenAnswer(invocation -> {
            KnowledgeAiExtractionRequest request = invocation.getArgument(0);
            assertEquals("GRAPH", request.getTaskType());
            assertEquals("ENTRY", request.getScopeType());
            assertEquals("{\"entryId\":9}", request.getScopeJson());
            assertEquals("CLASSICS_CONTENT", request.getSourceContentType());
            assertEquals(101L, request.getSourceContentId());
            assertEquals(201L, request.getRequestedBy());
            assertEquals(301L, request.getServiceId());
            assertEquals("KNOWLEDGE", request.getServiceRole());
            assertEquals(401L, request.getModelId());
            assertEquals("kimi-k2", request.getModelName());
            assertEquals(501L, request.getPromptVersionId());
            assertEquals("req-graph", request.getRequestId());
            assertEquals("trace-graph", request.getTraceId());
            assertEquals("[\"graph-prompt\"]", request.getPromptMessagesJson());
            assertEquals("{\"style\":\"full\"}", request.getPromptVariablesJson());
            assertEquals("graph-hash", request.getPromptHash());
            assertEquals("{\"content\":\"x\"}", request.getInputPayloadJson());
            assertEquals("{\"type\":\"graph\"}", request.getOutputSchemaJson());
            assertFalse(request.isForceJson());
            assertEquals("zh-CN", request.getLocale());
            return new KnowledgeAiExtractionResult(
                    701L, 801L, "SUCCEEDED", "KNOWLEDGE_GRAPH", "JSON", "{\"nodes\":1}", null, null);
        });
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                knowledgeAiExtractionApplicationService,
                mock(AiInvocationRepository.class),
                mock(AiCandidateDomainService.class));

        var response = facade.extractKnowledgeGraph(KnowledgeAiExtractionFacadeRequest.builder()
                .taskType("GRAPH")
                .scopeType("ENTRY")
                .scopeJson("{\"entryId\":9}")
                .sourceContentType("CLASSICS_CONTENT")
                .sourceContentId(101L)
                .requestedBy(201L)
                .serviceId(301L)
                .serviceRole("KNOWLEDGE")
                .modelId(401L)
                .modelName("kimi-k2")
                .promptVersionId(501L)
                .requestId("req-graph")
                .traceId("trace-graph")
                .promptMessagesJson("[\"graph-prompt\"]")
                .promptVariablesJson("{\"style\":\"full\"}")
                .promptHash("graph-hash")
                .inputPayloadJson("{\"content\":\"x\"}")
                .outputSchemaJson("{\"type\":\"graph\"}")
                .forceJson(false)
                .locale("zh-CN")
                .build());

        assertEquals(701L, response.getCallId());
        assertEquals(801L, response.getCandidateId());
        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals("KNOWLEDGE_GRAPH", response.getCapability());
        assertEquals("JSON", response.getResultFormat());
        assertEquals("{\"nodes\":1}", response.getResultPayload());
    }

    @Test
    void getCallRecordShouldMapUsageSnapshot() {
        AiInvocationRepository aiInvocationRepository = mock(AiInvocationRepository.class);
        when(aiInvocationRepository.getCallRecord(301L)).thenReturn(callRecord());
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationServiceImpl.class),
                aiInvocationRepository,
                mock(AiCandidateDomainService.class));

        var response = facade.getCallRecord(
                GetAiCallRecordFacadeRequest.builder().callId(301L).build());

        assertEquals(301L, response.getCallId());
        assertEquals(401L, response.getBatchId());
        assertEquals("DISCOVERY_QA", response.getCapability());
        assertEquals("SUCCEEDED", response.getStatus());
        assertTrue(response.isStreamUsed());
        assertTrue(response.isStreamCompleted());
        assertFalse(response.isFallbackUsed());
        assertEquals(12, response.getUsage().getPromptTokens());
        assertEquals(34, response.getUsage().getCompletionTokens());
        assertEquals(46, response.getUsage().getTotalTokens());
        assertEquals(450, response.getUsage().getLatencyMs());
        assertEquals(new BigDecimal("1.23"), response.getUsage().getCostAmount());
        assertNull(response.getUsage().getCurrency());
    }

    @Test
    void requirePendingCandidateShouldMapApplyCheckAndCandidateDto() {
        AiCandidateDomainService aiCandidateDomainService = mock(AiCandidateDomainService.class);
        when(aiCandidateDomainService.requirePendingForApply(any())).thenAnswer(invocation -> {
            AiCandidateApplyCheck check = invocation.getArgument(0);
            assertEquals(901L, check.getCandidateId());
            assertEquals("CLASSICS_CONTENT", check.getContentType());
            assertEquals(902L, check.getContentId());
            assertEquals("KNOWLEDGE_GRAPH", check.getCapability());
            return candidate();
        });
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationServiceImpl.class),
                mock(AiInvocationRepository.class),
                aiCandidateDomainService);

        var response = facade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                .candidateId(901L)
                .contentType("CLASSICS_CONTENT")
                .contentId(902L)
                .capability("KNOWLEDGE_GRAPH")
                .build());

        assertEquals(901L, response.getCandidateId());
        assertEquals(701L, response.getCallId());
        assertEquals(801L, response.getBatchId());
        assertEquals("KNOWLEDGE_GRAPH", response.getCapability());
        assertEquals("CLASSICS_CONTENT", response.getContentType());
        assertEquals(902L, response.getContentId());
        assertEquals(903L, response.getObjectId());
        assertEquals("JSON", response.getResultFormat());
        assertEquals("{\"graph\":true}", response.getResultPayload());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1001L, response.getPromptVersionId());
        assertEquals("gpt-5", response.getModelName());
        assertEquals(Instant.parse("2025-02-01T10:15:30Z"), response.getRequestedAt());
    }

    private static AiFacadeImpl newFacade(
            AiReportApplicationService aiReportApplicationService,
            AiBatchJobApplicationService aiBatchJobApplicationService,
            DiscoveryAiApplicationService discoveryAiApplicationService,
            KnowledgeAiExtractionApplicationServiceImpl knowledgeAiExtractionApplicationService,
            AiInvocationRepository aiInvocationRepository,
            AiCandidateDomainService aiCandidateDomainService) {
        return new AiFacadeImpl(
                aiReportApplicationService,
                aiBatchJobApplicationService,
                discoveryAiApplicationService,
                knowledgeAiExtractionApplicationService,
                aiInvocationRepository,
                aiCandidateDomainService,
                new AiFacadeAssembler());
    }

    private static AiCallRecord callRecord() {
        AiCallRecord record = new AiCallRecord();
        record.setCallId(301L);
        record.setBatchId(401L);
        record.setScope("discovery");
        record.setCapability("DISCOVERY_QA");
        record.setContentType("QUESTION");
        record.setContentId(501L);
        record.setObjectId(601L);
        record.setServiceId(701L);
        record.setServiceRole("DISCOVERY");
        record.setModelId(801L);
        record.setModelName("gpt-5");
        record.setPromptVersionId(901L);
        record.setRequestId("req-call");
        record.setTraceId("trace-call");
        record.setStatus("SUCCEEDED");
        record.setStreamUsed(true);
        record.setStreamCompleted(true);
        record.setFallbackUsed(false);
        record.setUsage(new AiUsageSnapshot(450, 12, 34, new BigDecimal("1.23")));
        record.setRequestedAt(Instant.parse("2025-01-01T08:00:00Z"));
        record.setCompletedAt(Instant.parse("2025-01-01T08:00:01Z"));
        return record;
    }

    private static AiCandidate candidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setCandidateId(901L);
        candidate.setCallId(701L);
        candidate.setBatchId(801L);
        candidate.setCapability("KNOWLEDGE_GRAPH");
        candidate.setContentType("CLASSICS_CONTENT");
        candidate.setContentId(902L);
        candidate.setObjectId(903L);
        candidate.setResultFormat("JSON");
        candidate.setResultPayload("{\"graph\":true}");
        candidate.setStatus("PENDING");
        candidate.setPromptVersionId(1001L);
        candidate.setModelName("gpt-5");
        candidate.setRequestedAt(Instant.parse("2025-02-01T10:15:30Z"));
        return candidate;
    }
}
