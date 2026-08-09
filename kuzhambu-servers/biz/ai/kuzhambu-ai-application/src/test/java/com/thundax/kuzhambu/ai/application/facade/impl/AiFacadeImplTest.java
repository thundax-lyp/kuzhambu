package com.thundax.kuzhambu.ai.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.application.facade.assembler.AiFacadeAssembler;
import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiReportSummaryResult.TopCapabilityResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.service.AiCandidateApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.service.AiReportApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.command.DiscoveryAiCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.DiscoveryAiInvokeResult;
import com.thundax.kuzhambu.ai.application.scenario.result.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.application.scenario.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.KnowledgeAiExtractionApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiPromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiReportBucketType;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CreateAiBatchJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiInvocationLogFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RejectAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AiFacadeImplTest {

    @Test
    void summaryShouldDelegateAndMapFacadeResponse() {
        AiReportApplicationService aiReportApplicationService = mock(AiReportApplicationService.class);
        Instant periodStart = Instant.parse("2025-01-01T00:00:00.123456Z");
        Instant periodEnd = Instant.parse("2025-01-31T23:59:59.987654Z");
        when(aiReportApplicationService.summary(argThat(query -> query != null
                        && periodStart.equals(query.periodStart())
                        && periodEnd.equals(query.periodEnd())
                        && AiReportBucketType.DAY == query.bucketType())))
                .thenReturn(new AiReportSummaryResult(
                        periodStart,
                        periodEnd,
                        9L,
                        7L,
                        2L,
                        320L,
                        new BigDecimal("12.34"),
                        List.of(new TopCapabilityResult(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION, 5L))));
        AiFacadeImpl facade = newFacade(
                aiReportApplicationService,
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationService.class),
                mock(AiInvocationRepository.class),
                mock(AiCandidateApplicationService.class));

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
        assertEquals(
                AiBusinessCapability.DISCOVERY_ANSWER_GENERATION.value(),
                response.getTopCapabilities().get(0).getCapability());
        assertEquals(5L, response.getTopCapabilities().get(0).getInvocationCount());
    }

    @Test
    void createBatchJobShouldAssembleCommandAndMapActionResponse() {
        AiBatchJobApplicationService aiBatchJobApplicationService = mock(AiBatchJobApplicationService.class);
        when(aiBatchJobApplicationService.create(any())).thenReturn(new AiBatchJobId(88L));
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                aiBatchJobApplicationService,
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationService.class),
                mock(AiInvocationRepository.class),
                mock(AiCandidateApplicationService.class));
        CreateAiBatchJobFacadeRequest request = CreateAiBatchJobFacadeRequest.builder()
                .scope("knowledge")
                .capability("KNOWLEDGE_GRAPH_EXTRACT")
                .contentType("WANGQI_DOCUMENT")
                .totalCount(12)
                .failureSummaryJson("{\"retry\":0}")
                .build();

        var response = facade.createBatchJob(request);
        ArgumentCaptor<AiBatchJobCreateCommand> captor = ArgumentCaptor.forClass(AiBatchJobCreateCommand.class);
        verify(aiBatchJobApplicationService).create(captor.capture());

        AiBatchJobCreateCommand command = captor.getValue();
        assertEquals("knowledge", command.scope());
        assertEquals(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT, command.capability());
        assertEquals("WANGQI_DOCUMENT", command.contentRef().contentType());
        assertEquals(12, command.totalCount());
        assertEquals("{\"retry\":0}", command.failureSummaryJson());
        assertEquals(88L, response.getBatchId());
    }

    @Test
    void understandDiscoveryQueryShouldMapRequestAndResponse() {
        DiscoveryAiApplicationService discoveryAiApplicationService = mock(DiscoveryAiApplicationService.class);
        when(discoveryAiApplicationService.understandQuery(any())).thenAnswer(invocation -> {
            DiscoveryAiCommand command = invocation.getArgument(0);
            assertEquals(21L, command.serviceId());
            assertEquals("DISCOVERY", command.serviceRole());
            assertEquals(new AiModelId(31L), command.modelId());
            assertEquals(AiModelName.of("gpt-5"), command.modelName());
            assertEquals(new PromptVersionId(41L), command.promptVersionId());
            assertEquals(RequestIdCodec.toDomain("req-1"), command.requestId());
            assertEquals(TraceIdCodec.toDomain("trace-1"), command.traceId());
            assertEquals("[\"prompt\"]", command.promptMessagesJson());
            assertEquals("{\"lang\":\"zh\"}", command.promptVariablesJson());
            assertEquals("hash-1", command.promptHash());
            assertEquals("{\"query\":\"苏东坡\"}", command.inputPayloadJson());
            assertEquals("{\"type\":\"object\"}", command.outputSchemaJson());
            assertTrue(command.stream());
            assertTrue(command.forceJson());
            assertEquals("zh-CN", command.locale());
            return discoveryResult(
                    501L,
                    601L,
                    AiInvocationStatus.SUCCEEDED,
                    AiBusinessCapability.DISCOVERY_QUERY_UNDERSTANDING,
                    "JSON",
                    "{\"intent\":\"search\"}",
                    null,
                    null);
        });
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                discoveryAiApplicationService,
                mock(KnowledgeAiExtractionApplicationService.class),
                mock(AiInvocationRepository.class),
                mock(AiCandidateApplicationService.class));

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
        assertEquals(AiBusinessCapability.DISCOVERY_QUERY_UNDERSTANDING.value(), response.getCapability());
        assertEquals("JSON", response.getResultFormat());
        assertEquals("{\"intent\":\"search\"}", response.getResultPayload());
        assertNull(response.getErrorType());
        assertNull(response.getErrorMessage());
    }

    @Test
    void generateDiscoveryAnswerShouldMapCallIdAndFailureFields() {
        DiscoveryAiApplicationService discoveryAiApplicationService = mock(DiscoveryAiApplicationService.class);
        when(discoveryAiApplicationService.generateAnswer(any())).thenAnswer(invocation -> {
            DiscoveryAiCommand command = invocation.getArgument(0);
            assertEquals(21L, command.serviceId());
            assertEquals("DISCOVERY", command.serviceRole());
            assertEquals(new AiModelId(31L), command.modelId());
            assertEquals(AiModelName.of("gpt-5"), command.modelName());
            assertEquals(new PromptVersionId(41L), command.promptVersionId());
            assertEquals(RequestIdCodec.toDomain("req-answer"), command.requestId());
            assertEquals(TraceIdCodec.toDomain("trace-answer"), command.traceId());
            assertEquals("[\"answer-prompt\"]", command.promptMessagesJson());
            assertEquals("{\"lang\":\"zh\"}", command.promptVariablesJson());
            assertEquals("answer-hash", command.promptHash());
            assertEquals("{\"question\":\"王圻是谁\"}", command.inputPayloadJson());
            assertEquals("{\"type\":\"object\"}", command.outputSchemaJson());
            assertFalse(command.stream());
            assertTrue(command.forceJson());
            assertEquals("zh-CN", command.locale());
            return discoveryResult(
                    701L,
                    null,
                    AiInvocationStatus.FAILED,
                    AiBusinessCapability.DISCOVERY_ANSWER_GENERATION,
                    "JSON",
                    null,
                    "WORKER_STREAM",
                    "stream interrupted");
        });
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                discoveryAiApplicationService,
                mock(KnowledgeAiExtractionApplicationService.class),
                mock(AiInvocationRepository.class),
                mock(AiCandidateApplicationService.class));

        var response = facade.generateDiscoveryAnswer(DiscoveryAiFacadeRequest.builder()
                .serviceId(21L)
                .serviceRole("DISCOVERY")
                .modelId(31L)
                .modelName("gpt-5")
                .promptVersionId(41L)
                .requestId("req-answer")
                .traceId("trace-answer")
                .promptMessagesJson("[\"answer-prompt\"]")
                .promptVariablesJson("{\"lang\":\"zh\"}")
                .promptHash("answer-hash")
                .inputPayloadJson("{\"question\":\"王圻是谁\"}")
                .outputSchemaJson("{\"type\":\"object\"}")
                .stream(false)
                .forceJson(true)
                .locale("zh-CN")
                .build());

        assertEquals(701L, response.getCallId());
        assertNull(response.getCandidateId());
        assertEquals("FAILED", response.getStatus());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION.value(), response.getCapability());
        assertEquals("JSON", response.getResultFormat());
        assertNull(response.getResultPayload());
        assertEquals("WORKER_STREAM", response.getErrorType());
        assertEquals("stream interrupted", response.getErrorMessage());
    }

    @Test
    void streamDiscoveryAnswerShouldForwardDeltaEventsAndMapFinalResponse() {
        DiscoveryAiApplicationService discoveryAiApplicationService = mock(DiscoveryAiApplicationService.class);
        when(discoveryAiApplicationService.streamAnswer(any(), any())).thenAnswer(invocation -> {
            DiscoveryAiCommand command = invocation.getArgument(0);
            assertEquals(RequestIdCodec.toDomain("req-answer"), command.requestId());
            assertFalse(command.stream());
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<AiStreamEventResult> eventConsumer = invocation.getArgument(1);
            eventConsumer.accept(deltaEvent("王圻"));
            eventConsumer.accept(deltaEvent("文档答案"));
            return discoveryResult(
                    702L,
                    null,
                    AiInvocationStatus.SUCCEEDED,
                    AiBusinessCapability.DISCOVERY_ANSWER_GENERATION,
                    "JSON",
                    "{\"answer\":\"王圻文档答案\"}",
                    null,
                    null);
        });
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                discoveryAiApplicationService,
                mock(KnowledgeAiExtractionApplicationService.class),
                mock(AiInvocationRepository.class),
                mock(AiCandidateApplicationService.class));
        List<String> deltas = new java.util.ArrayList<>();

        var response = facade.streamDiscoveryAnswer(
                DiscoveryAiFacadeRequest.builder()
                        .serviceId(21L)
                        .serviceRole("DISCOVERY")
                        .modelId(31L)
                        .modelName("gpt-5")
                        .promptVersionId(41L)
                        .requestId("req-answer")
                        .traceId("trace-answer")
                        .promptMessagesJson("[\"answer-prompt\"]")
                        .inputPayloadJson("{\"question\":\"王圻是谁\"}")
                        .stream(false)
                        .forceJson(true)
                        .locale("zh-CN")
                        .build(),
                deltas::add);

        assertEquals(List.of("王圻", "文档答案"), deltas);
        assertEquals(702L, response.getCallId());
        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals("{\"answer\":\"王圻文档答案\"}", response.getResultPayload());
    }

    @Test
    void extractKnowledgeGraphShouldMapRequestAndResponse() {
        KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService =
                mock(KnowledgeAiExtractionApplicationService.class);
        when(knowledgeAiExtractionApplicationService.extractGraph(any())).thenAnswer(invocation -> {
            KnowledgeAiExtractionCommand command = invocation.getArgument(0);
            assertEquals("GRAPH", command.taskType());
            assertEquals("ENTRY", command.scopeType());
            assertEquals("{\"entryId\":9}", command.scopeJson());
            assertEquals("CLASSICS_CONTENT", command.sourceContentType());
            assertEquals(101L, command.sourceContentId());
            assertEquals(201L, command.requestedBy());
            assertEquals(301L, command.serviceId());
            assertEquals("KNOWLEDGE", command.serviceRole());
            assertEquals(new AiModelId(401L), command.modelId());
            assertEquals(AiModelName.of("kimi-k2"), command.modelName());
            assertEquals(new PromptVersionId(501L), command.promptVersionId());
            assertEquals(RequestIdCodec.toDomain("req-graph"), command.requestId());
            assertEquals(TraceIdCodec.toDomain("trace-graph"), command.traceId());
            assertEquals("[\"graph-prompt\"]", command.promptMessagesJson());
            assertEquals("{\"style\":\"full\"}", command.promptVariablesJson());
            assertEquals("graph-hash", command.promptHash());
            assertEquals("{\"content\":\"x\"}", command.inputPayloadJson());
            assertEquals("{\"type\":\"graph\"}", command.outputSchemaJson());
            assertFalse(command.forceJson());
            assertEquals("zh-CN", command.locale());
            return new KnowledgeAiExtractionResult(
                    new AiCallId(701L),
                    new AiCandidateId(801L),
                    AiInvocationStatus.SUCCEEDED,
                    AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT,
                    "JSON",
                    "{\"nodes\":1}",
                    null,
                    null);
        });
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                knowledgeAiExtractionApplicationService,
                mock(AiInvocationRepository.class),
                mock(AiCandidateApplicationService.class));

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
        assertEquals(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT.value(), response.getCapability());
        assertEquals("JSON", response.getResultFormat());
        assertEquals("{\"nodes\":1}", response.getResultPayload());
    }

    @Test
    void extractKnowledgeTagsShouldMapRequestAndResponse() {
        KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService =
                mock(KnowledgeAiExtractionApplicationService.class);
        when(knowledgeAiExtractionApplicationService.extractTags(any())).thenAnswer(invocation -> {
            KnowledgeAiExtractionCommand command = invocation.getArgument(0);
            assertEquals("TAG", command.taskType());
            assertEquals("CONTENT", command.scopeType());
            assertEquals("{\"contentType\":\"SANCAI_ENTRY\",\"contentIds\":[1001]}", command.scopeJson());
            assertEquals("SANCAI_ENTRY", command.sourceContentType());
            assertEquals(1001L, command.sourceContentId());
            assertEquals(2001L, command.requestedBy());
            assertEquals(3001L, command.serviceId());
            assertEquals("KNOWLEDGE", command.serviceRole());
            assertEquals(new AiModelId(4001L), command.modelId());
            assertEquals(AiModelName.of("gpt-5"), command.modelName());
            assertEquals(new PromptVersionId(5001L), command.promptVersionId());
            assertEquals(RequestIdCodec.toDomain("req-tag"), command.requestId());
            assertEquals(TraceIdCodec.toDomain("trace-tag"), command.traceId());
            assertEquals("[\"tag-prompt\"]", command.promptMessagesJson());
            assertEquals("{\"maxTags\":10}", command.promptVariablesJson());
            assertEquals("tag-hash", command.promptHash());
            assertEquals("{\"contentText\":\"正文\"}", command.inputPayloadJson());
            assertEquals("{\"type\":\"object\",\"required\":[\"tags\"]}", command.outputSchemaJson());
            assertTrue(command.forceJson());
            assertEquals("zh-CN", command.locale());
            return new KnowledgeAiExtractionResult(
                    new AiCallId(711L),
                    new AiCandidateId(811L),
                    AiInvocationStatus.SUCCEEDED,
                    AiBusinessCapability.KNOWLEDGE_TAG_EXTRACT,
                    "STRUCTURED",
                    "{\"tags\":[]}",
                    null,
                    null);
        });
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                knowledgeAiExtractionApplicationService,
                mock(AiInvocationRepository.class),
                mock(AiCandidateApplicationService.class));

        var response = facade.extractKnowledgeTags(KnowledgeAiExtractionFacadeRequest.builder()
                .taskType("TAG")
                .scopeType("CONTENT")
                .scopeJson("{\"contentType\":\"SANCAI_ENTRY\",\"contentIds\":[1001]}")
                .sourceContentType("SANCAI_ENTRY")
                .sourceContentId(1001L)
                .requestedBy(2001L)
                .serviceId(3001L)
                .serviceRole("KNOWLEDGE")
                .modelId(4001L)
                .modelName("gpt-5")
                .promptVersionId(5001L)
                .requestId("req-tag")
                .traceId("trace-tag")
                .promptMessagesJson("[\"tag-prompt\"]")
                .promptVariablesJson("{\"maxTags\":10}")
                .promptHash("tag-hash")
                .inputPayloadJson("{\"contentText\":\"正文\"}")
                .outputSchemaJson("{\"type\":\"object\",\"required\":[\"tags\"]}")
                .forceJson(true)
                .locale("zh-CN")
                .build());

        assertEquals(711L, response.getCallId());
        assertEquals(811L, response.getCandidateId());
        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals(AiBusinessCapability.KNOWLEDGE_TAG_EXTRACT.value(), response.getCapability());
        assertEquals("STRUCTURED", response.getResultFormat());
        assertEquals("{\"tags\":[]}", response.getResultPayload());
        assertNull(response.getErrorType());
        assertNull(response.getErrorMessage());
    }

    @Test
    void getInvocationLogShouldMapUsageSnapshot() {
        AiInvocationRepository aiInvocationRepository = mock(AiInvocationRepository.class);
        when(aiInvocationRepository.getInvocationLog(AiCallIdCodec.toDomain(301L)))
                .thenReturn(invocationLog());
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationService.class),
                aiInvocationRepository,
                mock(AiCandidateApplicationService.class));

        var response = facade.getInvocationLog(
                GetAiInvocationLogFacadeRequest.builder().callId(301L).build());

        assertEquals(301L, response.getCallId());
        assertEquals(401L, response.getBatchId());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION.value(), response.getCapability());
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
    void getInvocationLogShouldReturnNullWhenLogIsMissing() {
        AiInvocationRepository aiInvocationRepository = mock(AiInvocationRepository.class);
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationService.class),
                aiInvocationRepository,
                mock(AiCandidateApplicationService.class));

        var response = facade.getInvocationLog(
                GetAiInvocationLogFacadeRequest.builder().callId(302L).build());

        assertNull(response);
    }

    @Test
    void getCandidateShouldReturnNullWhenCandidateIsMissing() {
        AiInvocationRepository aiInvocationRepository = mock(AiInvocationRepository.class);
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationService.class),
                aiInvocationRepository,
                mock(AiCandidateApplicationService.class));

        var response = facade.getCandidate(
                GetAiCandidateFacadeRequest.builder().candidateId(402L).build());

        assertNull(response);
    }

    @Test
    void requirePendingCandidateShouldMapApplyCheckAndCandidateDto() {
        AiCandidateApplicationService aiCandidateApplicationService = mock(AiCandidateApplicationService.class);
        when(aiCandidateApplicationService.requirePendingForApply(argThat(query -> query != null
                        && new AiCandidateId(901L).equals(query.candidateId())
                        && AiContentRef.of("CLASSICS_CONTENT", 902L).equals(query.contentRef())
                        && AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT == query.capability()
                        && new AiTargetObjectId(903L).equals(query.targetObjectId()))))
                .thenReturn(candidate());
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationService.class),
                mock(AiInvocationRepository.class),
                aiCandidateApplicationService);

        var response = facade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                .candidateId(901L)
                .contentType("CLASSICS_CONTENT")
                .contentId(902L)
                .objectId(903L)
                .capability("KNOWLEDGE_GRAPH_EXTRACT")
                .build());

        assertEquals(901L, response.getCandidateId());
        assertEquals(701L, response.getCallId());
        assertEquals(801L, response.getBatchId());
        assertEquals(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT.value(), response.getCapability());
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

    @Test
    void rejectCandidateShouldDelegateToDomainServiceAndMapDto() {
        AiCandidateApplicationService aiCandidateApplicationService = mock(AiCandidateApplicationService.class);
        when(aiCandidateApplicationService.reject(argThat(command -> command != null
                        && new AiCandidateId(901L).equals(command.candidateId())
                        && "USER_REJECTED".equals(command.errorType())
                        && "not useful".equals(command.errorMessage()))))
                .thenReturn(candidate());
        AiFacadeImpl facade = newFacade(
                mock(AiReportApplicationService.class),
                mock(AiBatchJobApplicationService.class),
                mock(DiscoveryAiApplicationService.class),
                mock(KnowledgeAiExtractionApplicationService.class),
                mock(AiInvocationRepository.class),
                aiCandidateApplicationService);

        var response = facade.rejectCandidate(RejectAiCandidateFacadeRequest.builder()
                .candidateId(901L)
                .errorType("USER_REJECTED")
                .errorMessage("not useful")
                .build());

        assertEquals(901L, response.getCandidateId());
        verify(aiCandidateApplicationService)
                .reject(argThat(command -> command != null
                        && new AiCandidateId(901L).equals(command.candidateId())
                        && "USER_REJECTED".equals(command.errorType())
                        && "not useful".equals(command.errorMessage())));
    }

    private static AiFacadeImpl newFacade(
            AiReportApplicationService aiReportApplicationService,
            AiBatchJobApplicationService aiBatchJobApplicationService,
            DiscoveryAiApplicationService discoveryAiApplicationService,
            KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService,
            AiInvocationRepository aiInvocationRepository,
            AiCandidateApplicationService aiCandidateApplicationService) {
        return new AiFacadeImpl(
                aiReportApplicationService,
                aiBatchJobApplicationService,
                discoveryAiApplicationService,
                knowledgeAiExtractionApplicationService,
                aiCandidateApplicationService,
                aiInvocationRepository,
                new AiFacadeAssembler());
    }

    private static DiscoveryAiInvokeResult discoveryResult(
            Long callId,
            Long candidateId,
            AiInvocationStatus status,
            AiBusinessCapability capability,
            String resultFormat,
            String resultPayload,
            String errorType,
            String errorMessage) {
        return new DiscoveryAiInvokeResult(
                callId == null ? null : new AiCallId(callId),
                candidateId == null ? null : new AiCandidateId(candidateId),
                status,
                capability,
                resultFormat,
                resultPayload,
                errorType,
                errorMessage);
    }

    private static AiInvocationLog invocationLog() {
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setCallId(AiCallIdCodec.toDomain(301L));
        invocationLog.setBatchId(AiBatchJobIdCodec.toDomain(401L));
        invocationLog.setScope("discovery");
        invocationLog.setCapability(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION);
        invocationLog.setContentRef(AiContentRef.of("QUESTION", 501L));
        invocationLog.setTargetObjectId(AiTargetObjectIdCodec.toDomain(601L));
        invocationLog.setServiceId(701L);
        invocationLog.setServiceRole("DISCOVERY");
        invocationLog.setModelId(new AiModelId(801L));
        invocationLog.setModelName(AiModelName.of("gpt-5"));
        invocationLog.setPromptVersionId(new PromptVersionId(901L));
        invocationLog.setRequestId(RequestIdCodec.toDomain("req-call"));
        invocationLog.setTraceId(TraceIdCodec.toDomain("trace-call"));
        invocationLog.setStatus(AiInvocationStatus.SUCCEEDED);
        invocationLog.setStreamUsed(true);
        invocationLog.setStreamCompleted(true);
        invocationLog.setFallbackUsed(false);
        invocationLog.setUsage(new AiUsageSnapshot(450, 12, 34, new BigDecimal("1.23")));
        invocationLog.setRequestedAt(Instant.parse("2025-01-01T08:00:00Z"));
        invocationLog.setCompletedAt(Instant.parse("2025-01-01T08:00:01Z"));
        return invocationLog;
    }

    private static AiCandidate candidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateIdCodec.toDomain(901L));
        candidate.setCallId(AiCallIdCodec.toDomain(701L));
        candidate.setBatchId(AiBatchJobIdCodec.toDomain(801L));
        candidate.setCapability(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT);
        candidate.setContentRef(AiContentRef.of("CLASSICS_CONTENT", 902L));
        candidate.setTargetObjectId(AiTargetObjectIdCodec.toDomain(903L));
        candidate.setResultFormat("JSON");
        candidate.setResultPayload("{\"graph\":true}");
        candidate.setStatus(AiCandidateStatus.PENDING);
        candidate.setPromptVersionId(AiPromptVersionIdCodec.toDomain(1001L));
        candidate.setModelName(AiModelName.of("gpt-5"));
        candidate.setRequestedAt(Instant.parse("2025-02-01T10:15:30Z"));
        return candidate;
    }

    private static AiStreamEventResult deltaEvent(String content) {
        AiStreamEventResult event = new AiStreamEventResult();
        event.setEventType("delta");
        event.setDeltaText(content);
        return event;
    }
}
