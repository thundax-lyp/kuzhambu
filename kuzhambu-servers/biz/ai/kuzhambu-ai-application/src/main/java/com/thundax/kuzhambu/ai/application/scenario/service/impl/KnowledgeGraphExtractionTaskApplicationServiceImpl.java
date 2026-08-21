package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.ExpireRunningAiBatchJobsCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobFailureCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.AiBatchJobsQuery;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.scenario.configure.KnowledgeGraphExtractionExecutorConfiguration;
import com.thundax.kuzhambu.ai.application.scenario.result.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.application.scenario.service.KnowledgeAiExtractionApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.KnowledgeGraphExtractionTaskApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.support.KnowledgeAiExtractionSnapshotResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class KnowledgeGraphExtractionTaskApplicationServiceImpl
        implements KnowledgeGraphExtractionTaskApplicationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(KnowledgeGraphExtractionTaskApplicationServiceImpl.class);

    private static final String TASK_TYPE_GRAPH = "GRAPH";
    private static final Duration ORPHANED_TASK_TIMEOUT = Duration.ofSeconds(600L);
    private static final int ORPHANED_TASK_EXPIRE_LIMIT = 100;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiBatchJobApplicationService aiBatchJobApplicationService;
    private final KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService;
    private final KnowledgeAiExtractionSnapshotResolver snapshotResolver;
    private final Executor taskExecutor;

    public KnowledgeGraphExtractionTaskApplicationServiceImpl(
            AiBatchJobApplicationService aiBatchJobApplicationService,
            KnowledgeAiExtractionApplicationService knowledgeAiExtractionApplicationService,
            KnowledgeAiExtractionSnapshotResolver snapshotResolver,
            @Qualifier(KnowledgeGraphExtractionExecutorConfiguration.TASK_EXECUTOR) Executor taskExecutor) {
        this.aiBatchJobApplicationService = aiBatchJobApplicationService;
        this.knowledgeAiExtractionApplicationService = knowledgeAiExtractionApplicationService;
        this.snapshotResolver = snapshotResolver;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobId submitGraph(KnowledgeAiExtractionCommand command) {
        validate(command);
        rejectRunningDuplicate(command);
        KnowledgeAiExtractionCommand snapshotCommand =
                snapshotResolver.resolve(command).command();
        AiBatchJobId batchId = aiBatchJobApplicationService.create(new AiBatchJobCreateCommand(
                snapshotCommand.scopeType(),
                AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT,
                AiContentRef.ofNullable(snapshotCommand.sourceContentType(), snapshotCommand.sourceContentId()),
                1,
                null));
        scheduleGraphExecution(withBatchId(snapshotCommand, batchId));
        return batchId;
    }

    private void validate(KnowledgeAiExtractionCommand command) {
        if (command == null
                || !TASK_TYPE_GRAPH.equals(command.taskType())
                || isBlank(command.scopeType())
                || isBlank(command.sourceContentType())
                || command.sourceContentId() == null
                || isBlank(command.scopeJson())
                || command.requestedBy() == null
                || isBlank(command.inputPayloadJson())) {
            throw new BizException("Knowledge graph extraction job request is incomplete");
        }
    }

    private void rejectRunningDuplicate(KnowledgeAiExtractionCommand command) {
        var page = aiBatchJobApplicationService.page(
                new AiBatchJobsQuery(
                        command.scopeType(),
                        AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT,
                        AiBatchJobStatus.RUNNING,
                        AiContentRef.ofNullable(command.sourceContentType(), command.sourceContentId())),
                new PageQuery(1, 1));
        if (page.getTotalCount() > 0) {
            throw new BizException("Knowledge graph extraction job is already running");
        }
    }

    private KnowledgeAiExtractionCommand withBatchId(KnowledgeAiExtractionCommand source, AiBatchJobId batchId) {
        return new KnowledgeAiExtractionCommand(
                batchId,
                source.taskType(),
                source.scopeType(),
                source.scopeJson(),
                source.sourceContentType(),
                source.sourceContentId(),
                source.requestedBy(),
                source.serviceId(),
                source.serviceRole(),
                source.modelId(),
                source.modelName(),
                source.promptVersionId(),
                source.requestId(),
                source.traceId(),
                source.promptMessagesJson(),
                source.promptVariablesJson(),
                source.promptHash(),
                source.inputPayloadJson(),
                source.outputSchemaJson(),
                source.forceJson(),
                source.locale());
    }

    private void scheduleGraphExecution(KnowledgeAiExtractionCommand command) {
        Runnable task = () -> submitGraphExecution(command);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

    private void submitGraphExecution(KnowledgeAiExtractionCommand command) {
        try {
            CompletableFuture.runAsync(() -> executeGraphSafely(command), taskExecutor);
        } catch (RejectedExecutionException exception) {
            LOGGER.warn(
                    "Knowledge graph extraction executor rejected job, batchId={}",
                    AiBatchJobIdCodec.toValue(command.batchId()),
                    exception);
            aiBatchJobApplicationService.recordFailureIfRunning(new RecordAiBatchJobFailureCommand(
                    command.batchId(), failureSummaryJson("Knowledge graph extraction executor is saturated")));
        }
    }

    private void executeGraphSafely(KnowledgeAiExtractionCommand command) {
        try {
            KnowledgeAiExtractionResult result = knowledgeAiExtractionApplicationService.extractGraph(command);
            if (result != null && AiInvocationStatus.SUCCEEDED == result.getStatus()) {
                aiBatchJobApplicationService.recordSuccessIfRunning(new RecordAiBatchJobCommand(command.batchId()));
                return;
            }
            aiBatchJobApplicationService.recordFailureIfRunning(new RecordAiBatchJobFailureCommand(
                    command.batchId(), failureSummaryJson(result == null ? null : result.getErrorMessage())));
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Knowledge graph extraction job execution failed, batchId={}",
                    AiBatchJobIdCodec.toValue(command.batchId()),
                    exception);
            aiBatchJobApplicationService.recordFailureIfRunning(
                    new RecordAiBatchJobFailureCommand(command.batchId(), failureSummaryJson(exception.getMessage())));
        }
    }

    @Scheduled(
            initialDelayString = "${kuzhambu.ai.knowledge-graph.orphaned-task-expiry.initial-delay-ms:60000}",
            fixedDelayString = "${kuzhambu.ai.knowledge-graph.orphaned-task-expiry.fixed-delay-ms:300000}")
    @Transactional(rollbackFor = Exception.class)
    public void expireOrphanedRunningGraphJobs() {
        Instant requestedBefore = Instant.now().minus(ORPHANED_TASK_TIMEOUT);
        int expired = aiBatchJobApplicationService.expireRunning(new ExpireRunningAiBatchJobsCommand(
                null,
                List.of(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT),
                requestedBefore,
                failureSummaryJson("Knowledge graph extraction job execution context was lost before completion"),
                ORPHANED_TASK_EXPIRE_LIMIT));
        if (expired > 0) {
            LOGGER.warn("Expired orphaned knowledge graph extraction jobs, count={}", expired);
        }
    }

    private String failureSummaryJson(String errorMessage) {
        String message = errorMessage == null ? "Knowledge graph extraction failed" : errorMessage;
        try {
            return objectMapper.writeValueAsString(Map.of("errorType", "INTERNAL_FAILURE", "errorMessage", message));
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "KNOWLEDGE-GRAPH-EXTRACTION-500",
                    "knowledge.graph.extraction.failure-summary-invalid",
                    "Knowledge graph extraction task failure summary is not valid JSON",
                    exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
