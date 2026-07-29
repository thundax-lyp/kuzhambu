package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.configuration.AiRefinementExecutorConfiguration;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.scenario.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementTaskApplicationService;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
public class AiRefinementTaskApplicationServiceImpl implements AiRefinementTaskApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiRefinementTaskApplicationServiceImpl.class);

    private static final String REFINEMENT_SCOPE = "classics";
    private static final String CONTENT_TYPE_SANCAI_ENTRY = "SANCAI_ENTRY";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "classics_image_describe";
    private static final String CAPABILITY_IMAGE_GEN = "classics_image_generate";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final int RESULT_PREVIEW_MAX_LENGTH = 500;
    private static final int STREAM_EVENT_HISTORY_LIMIT = 100;
    private static final Duration STREAM_SUBSCRIBE_TIMEOUT = Duration.ofMinutes(10L);
    private static final Duration ORPHANED_TASK_TIMEOUT = Duration.ofHours(1L);
    private static final int ORPHANED_TASK_EXPIRE_LIMIT = 100;
    private static final List<AiBusinessCapability> REFINEMENT_CAPABILITIES = List.of(
            AiBusinessCapability.CLASSICS_TRANSLATE,
            AiBusinessCapability.CLASSICS_SUMMARY,
            AiBusinessCapability.CLASSICS_TAG_EXTRACT,
            AiBusinessCapability.CLASSICS_QA,
            AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE,
            AiBusinessCapability.CLASSICS_IMAGE_PROMPT_FUSION,
            AiBusinessCapability.CLASSICS_VISUAL_DESCRIBE,
            AiBusinessCapability.CLASSICS_IMAGE_GENERATE,
            AiBusinessCapability.CLASSICS_SPLIT);

    private final AiBatchJobApplicationService batchJobApplicationService;
    private final AiRefinementApplicationService refinementApplicationService;
    private final AiInvocationRepository aiInvocationRepository;
    private final Executor taskExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<Long, TaskStreamHub> streamHubs = new ConcurrentHashMap<>();

    public AiRefinementTaskApplicationServiceImpl(
            AiBatchJobApplicationService batchJobApplicationService,
            AiRefinementApplicationService refinementApplicationService,
            AiInvocationRepository aiInvocationRepository,
            @Qualifier(AiRefinementExecutorConfiguration.TASK_EXECUTOR) Executor taskExecutor) {
        this.batchJobApplicationService = batchJobApplicationService;
        this.refinementApplicationService = refinementApplicationService;
        this.aiInvocationRepository = aiInvocationRepository;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiRefinementTaskResult addTask(AiRefinementRequestCommand command) {
        validateAddCommand(command);
        validateRefinementBatchOwnership(command.getScope(), capabilityValue(command.getCapability()));
        refinementApplicationService.snapshotInvokeConfig(command);
        Long taskId = AiBatchJobIdCodec.toValue(batchJobApplicationService.create(new AiBatchJobCreateCommand(
                command.getScope(), command.getCapability(), command.getContentRef(), 1, null)));
        command.setBatchId(AiBatchJobIdCodec.toDomain(taskId));
        scheduleTaskExecution(taskId, command);
        return toTaskResult(taskId, batchJobApplicationService.get(AiBatchJobIdCodec.toDomain(taskId)), command, null);
    }

    @Override
    public AiRefinementTaskResult getTask(Long taskId) {
        AiBatchJobResult job = batchJobApplicationService.get(AiBatchJobIdCodec.toDomain(taskId));
        validateRefinementBatchOwnership(job);
        return toTaskResult(job);
    }

    @Override
    public PageResult<AiRefinementTaskResult> pageTasks(
            String capability, String status, String contentType, Long contentId, PageQuery pageQuery) {
        String normalizedCapability = normalizeCapability(capability);
        AiContentRef contentRef = AiContentRef.ofNullable(contentType, contentId);
        PageResult<AiBatchJobResult> page = isBlank(normalizedCapability)
                ? batchJobApplicationService.pageByCapabilities(
                        REFINEMENT_SCOPE,
                        REFINEMENT_CAPABILITIES,
                        isBlank(status) ? null : AiBatchJobStatus.from(status),
                        contentRef,
                        pageQuery)
                : batchJobApplicationService.page(
                        REFINEMENT_SCOPE,
                        validateRefinementCapability(normalizedCapability),
                        isBlank(status) ? null : AiBatchJobStatus.from(status),
                        contentRef,
                        pageQuery);
        Map<Long, AiInvocationLog> invocationLogsByBatch = latestInvocationLogsByBatch(page.getRecords(), contentRef);
        Map<Long, AiCandidate> candidatesByBatch = latestCandidatesByBatch(page.getRecords(), contentRef);
        List<AiRefinementTaskResult> records = new ArrayList<>();
        for (AiBatchJobResult record : page.getRecords()) {
            records.add(toTaskResult(
                    record,
                    invocationLogsByBatch.get(AiBatchJobIdCodec.toValue(record.getBatchId())),
                    candidatesByBatch.get(AiBatchJobIdCodec.toValue(record.getBatchId()))));
        }
        return PageResult.of(page.getPageNo(), page.getPageSize(), page.getTotalCount(), records);
    }

    @Override
    public void streamTaskEvents(Long taskId, Consumer<AiStreamEventResult> eventConsumer) {
        AiBatchJobResult job = batchJobApplicationService.get(AiBatchJobIdCodec.toDomain(taskId));
        validateRefinementBatchOwnership(job);
        if (!isStreamEnabledTask(job)) {
            throw new BizException("AI refinement task stream is not enabled: " + taskId);
        }
        TaskStreamHub hub = streamHubs.computeIfAbsent(taskId, ignored -> new TaskStreamHub());
        publishSnapshotIfTerminal(hub, toTaskResult(job));
        try {
            hub.subscribe(eventConsumer, STREAM_SUBSCRIBE_TIMEOUT);
        } finally {
            if (hub.hasTerminalEvent()) {
                streamHubs.remove(taskId, hub);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiRefinementTaskResult cancelTask(Long taskId) {
        AiBatchJobId batchId = AiBatchJobIdCodec.toDomain(taskId);
        AiBatchJobResult job = batchJobApplicationService.get(batchId);
        validateRefinementBatchOwnership(job);
        AiBatchJobResult cancelled = batchJobApplicationService.cancel(batchId);
        AiRefinementTaskResult task = toTaskResult(cancelled);
        publishTerminalEvent(taskId, task, null);
        return task;
    }

    private void executeTaskSafely(Long taskId, AiRefinementRequestCommand command) {
        try {
            executeTask(taskId, command);
        } catch (RuntimeException exception) {
            LOGGER.warn("AI refinement task execution failed unexpectedly, taskId={}", taskId, exception);
            markFailedAfterUnexpectedException(taskId, exception);
        }
    }

    private void executeTask(Long taskId, AiRefinementRequestCommand command) {
        AiBatchJobId batchId = AiBatchJobIdCodec.toDomain(taskId);
        AiBatchJobResult job = batchJobApplicationService.get(batchId);
        if (AiBatchJobStatus.CANCELLED == job.getStatus()) {
            return;
        }

        AiCandidateResult result;
        try {
            refinementApplicationService.validateSnapshotInvokeConfig(command);
            result = invoke(taskId, command, isStreamEnabledTask(command));
        } catch (RuntimeException exception) {
            result = new AiCandidateResult(
                    null,
                    null,
                    AiInvocationStatus.FAILED,
                    command.getCapability(),
                    "WORKER_REQUEST",
                    null,
                    null,
                    "INTERNAL_FAILURE",
                    exception.getMessage());
            publishFailureEvent(taskId, command, result);
        }
        AiBatchJobResult latestJob = batchJobApplicationService.get(batchId);
        if (AiBatchJobStatus.CANCELLED == latestJob.getStatus()) {
            return;
        }
        AiBatchJobResult finalJob;
        if (result != null && AiInvocationStatus.SUCCEEDED == result.getStatus()) {
            finalJob = batchJobApplicationService.recordSuccessIfRunning(batchId);
        } else if (result != null && AiInvocationStatus.PARTIAL == result.getStatus()) {
            finalJob = batchJobApplicationService.recordPartialIfRunning(batchId, failureSummaryJson(result));
        } else {
            finalJob = batchJobApplicationService.recordFailureIfRunning(batchId, failureSummaryJson(result));
        }
        publishTerminalEvent(taskId, toTaskResult(taskId, finalJob, command, result), result);
    }

    private void scheduleTaskExecution(Long taskId, AiRefinementRequestCommand command) {
        Runnable task = () -> CompletableFuture.runAsync(() -> executeTaskSafely(taskId, command), taskExecutor);
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

    private void markFailedAfterUnexpectedException(Long taskId, RuntimeException exception) {
        AiBatchJobId batchId = AiBatchJobIdCodec.toDomain(taskId);
        AiBatchJobResult job = batchJobApplicationService.get(batchId);
        if (job == null
                || isTerminal(job.getStatus() == null ? null : job.getStatus().name())) {
            return;
        }
        AiCandidateResult result = new AiCandidateResult(
                null,
                null,
                AiInvocationStatus.FAILED,
                job.getCapability(),
                "INTERNAL_EXECUTION",
                null,
                null,
                "INTERNAL_FAILURE",
                exception.getMessage());
        AiBatchJobResult failed =
                batchJobApplicationService.recordFailureIfRunning(batchId, failureSummaryJson(result));
        publishTerminalEvent(taskId, toTaskResult(taskId, failed, null, result), result);
    }

    @Scheduled(
            initialDelayString = "${kuzhambu.ai.refinement.orphaned-task-expiry.initial-delay-ms:60000}",
            fixedDelayString = "${kuzhambu.ai.refinement.orphaned-task-expiry.fixed-delay-ms:3600000}")
    @Transactional(rollbackFor = Exception.class)
    public void expireOrphanedRunningTasks() {
        Instant requestedBefore = Instant.now().minus(ORPHANED_TASK_TIMEOUT);
        AiCandidateResult result = new AiCandidateResult(
                null,
                null,
                AiInvocationStatus.FAILED,
                null,
                "INTERNAL_EXECUTION",
                null,
                null,
                "TASK_ORPHANED",
                "AI refinement task execution context was lost before completion");
        int expired = batchJobApplicationService.expireRunning(
                "classics",
                REFINEMENT_CAPABILITIES,
                requestedBefore,
                failureSummaryJson(result),
                ORPHANED_TASK_EXPIRE_LIMIT);
        if (expired > 0) {
            LOGGER.warn("Expired orphaned AI refinement tasks, count={}", expired);
        }
    }

    private AiCandidateResult invoke(Long taskId, AiRefinementRequestCommand command, boolean streamEnabled) {
        AiBusinessCapability capability = command.getCapability();
        if (AiBusinessCapability.CLASSICS_TRANSLATE == capability) {
            return refinementApplicationService.translate(command);
        }
        if (AiBusinessCapability.CLASSICS_SUMMARY == capability) {
            return refinementApplicationService.summarize(command);
        }
        if (AiBusinessCapability.CLASSICS_TAG_EXTRACT == capability) {
            return refinementApplicationService.generateTags(command);
        }
        if (AiBusinessCapability.CLASSICS_QA == capability) {
            return refinementApplicationService.generateQa(command);
        }
        if (AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE == capability) {
            return streamEnabled
                    ? refinementApplicationService.analyzeImage(command, event -> publishStreamEvent(taskId, event))
                    : refinementApplicationService.analyzeImage(command);
        }
        if (AiBusinessCapability.CLASSICS_VISUAL_DESCRIBE == capability) {
            return refinementApplicationService.describeVisual(command);
        }
        if (AiBusinessCapability.CLASSICS_IMAGE_PROMPT_FUSION == capability) {
            return refinementApplicationService.fuseVisualContext(command);
        }
        if (AiBusinessCapability.CLASSICS_IMAGE_GENERATE == capability) {
            return streamEnabled
                    ? refinementApplicationService.generateImage(command, event -> publishStreamEvent(taskId, event))
                    : refinementApplicationService.generateImage(command);
        }
        if (AiBusinessCapability.CLASSICS_SPLIT == capability) {
            return refinementApplicationService.splitEntry(command);
        }
        throw new BizException("unsupported ai refinement capability: " + capabilityValue(capability));
    }

    private String normalizeCapability(String capability) {
        if (capability == null) {
            return null;
        }
        return switch (capability) {
            case "translate" -> "classics_translate";
            case "summary" -> "classics_summary";
            case "tags" -> "classics_tags";
            case "qa" -> "classics_qa";
            case "image_analysis" -> "classics_image_describe";
            case "fusion" -> "classics_image_prompt_fusion";
            case "visual" -> "classics_visual_describe";
            case "image_gen" -> "classics_image_generate";
            case "split" -> "classics_split";
            default -> capability;
        };
    }

    private void validateAddCommand(AiRefinementRequestCommand command) {
        if (command == null
                || command.getCapability() == null
                || isBlank(command.getScope())
                || command.getRequestId() == null
                || command.getTraceId() == null
                || command.getContentRef() == null
                || isBlank(command.getContentRef().contentType())
                || command.getContentRef().contentId() == null
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("AI refinement task add command is incomplete");
        }
    }

    private boolean isTerminal(String status) {
        return AiInvocationStatus.SUCCEEDED.name().equals(status)
                || AiInvocationStatus.FAILED.name().equals(status)
                || AiInvocationStatus.PARTIAL.name().equals(status)
                || STATUS_CANCELLED.equals(status);
    }

    private void validateRefinementBatchOwnership(AiBatchJobResult job) {
        if (job == null) {
            throw new BizException("AI refinement task not found");
        }
        validateRefinementBatchOwnership(
                job.getScope(),
                job.getCapability() == null ? null : job.getCapability().value());
    }

    private void validateRefinementBatchOwnership(String scope, String capability) {
        if (!REFINEMENT_SCOPE.equals(scope)
                || isBlank(capability)
                || !REFINEMENT_CAPABILITIES.contains(AiBusinessCapability.from(capability))) {
            throw new BizException("AI refinement task does not belong to refinement workflow");
        }
    }

    private AiBusinessCapability validateRefinementCapability(String capability) {
        AiBusinessCapability parsedCapability = AiBusinessCapability.from(capability);
        if (!REFINEMENT_CAPABILITIES.contains(parsedCapability)) {
            throw new BizException("unsupported ai refinement capability: " + capability);
        }
        return parsedCapability;
    }

    private boolean isStreamEnabledTask(AiRefinementRequestCommand command) {
        if (command == null
                || !CONTENT_TYPE_SANCAI_ENTRY.equals(AiContentRefCodec.toContentType(command.getContentRef()))) {
            return false;
        }
        return AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE == command.getCapability()
                || AiBusinessCapability.CLASSICS_IMAGE_GENERATE == command.getCapability();
    }

    private boolean isStreamEnabledTask(AiBatchJobResult job) {
        if (job == null || !CONTENT_TYPE_SANCAI_ENTRY.equals(AiContentRefCodec.toContentType(job.getContentRef()))) {
            return false;
        }
        return CAPABILITY_IMAGE_ANALYSIS.equals(job.getCapability().value())
                || CAPABILITY_IMAGE_GEN.equals(job.getCapability().value());
    }

    private void publishStreamEvent(Long taskId, AiStreamEventResult event) {
        if (taskId == null || event == null) {
            return;
        }
        streamHubs.computeIfAbsent(taskId, ignored -> new TaskStreamHub()).publish(event);
    }

    private void publishTerminalEvent(Long taskId, AiRefinementTaskResult task, AiCandidateResult result) {
        if (taskId == null || task == null || !task.isStreamEnabled()) {
            return;
        }
        TaskStreamHub hub = streamHubs.computeIfAbsent(taskId, ignored -> new TaskStreamHub());
        if (hub.hasTerminalEvent()) {
            streamHubs.remove(taskId, hub);
            return;
        }
        if (AiInvocationStatus.SUCCEEDED.name().equals(task.getStatus())) {
            hub.publish(toCompletedEvent(task, result));
            streamHubs.remove(taskId, hub);
            return;
        }
        if (isTerminal(task.getStatus())) {
            hub.publish(toErrorEvent(task));
            streamHubs.remove(taskId, hub);
        }
    }

    private void publishFailureEvent(Long taskId, AiRefinementRequestCommand command, AiCandidateResult result) {
        if (taskId == null || command == null || !isStreamEnabledTask(command)) {
            return;
        }
        AiRefinementTaskResult task = toTaskResult(taskId, null, command, result);
        streamHubs.computeIfAbsent(taskId, ignored -> new TaskStreamHub()).publish(toErrorEvent(task));
    }

    private void publishSnapshotIfTerminal(TaskStreamHub hub, AiRefinementTaskResult task) {
        if (hub.hasTerminalEvent() || task == null || !isTerminal(task.getStatus())) {
            return;
        }
        if (AiInvocationStatus.SUCCEEDED.name().equals(task.getStatus())) {
            hub.publish(toCompletedEvent(task, null));
            return;
        }
        hub.publish(toErrorEvent(task));
    }

    private AiStreamEventResult toCompletedEvent(AiRefinementTaskResult task, AiCandidateResult result) {
        AiStreamEventResult event = baseEvent(task);
        event.setEventType("completed");
        event.setStage("completed");
        event.setStatus(AiInvocationStatus.SUCCEEDED);
        event.setResultFormat(result == null ? task.getResultFormat() : result.getResultFormat());
        event.setResultPayload(result == null ? task.getResultPreview() : result.getResultPayload());
        return event;
    }

    private AiStreamEventResult toErrorEvent(AiRefinementTaskResult task) {
        AiStreamEventResult event = baseEvent(task);
        event.setEventType("error");
        event.setStage(task.getFailureStage());
        event.setStatus(AiInvocationStatus.from(task.getStatus()));
        if (AiInvocationStatus.PARTIAL.name().equals(task.getStatus())) {
            event.setResultFormat(task.getResultFormat());
            event.setResultPayload(task.getResultPreview());
        }
        event.setFailureStage(task.getFailureStage());
        event.setErrorType(task.getErrorType());
        event.setErrorMessage(task.getErrorMessage());
        return event;
    }

    private AiStreamEventResult baseEvent(AiRefinementTaskResult task) {
        AiStreamEventResult event = new AiStreamEventResult();
        event.setEventId("task-" + task.getTaskId() + "-" + System.currentTimeMillis());
        event.setRequestId(RequestIdCodec.toDomain(task.getRequestId()));
        event.setTraceId(TraceIdCodec.toDomain(task.getTraceId()));
        event.setTimestamp(Instant.now());
        return event;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= RESULT_PREVIEW_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, RESULT_PREVIEW_MAX_LENGTH);
    }

    private AiRefinementTaskResult toTaskResult(
            Long taskId, AiBatchJobResult job, AiRefinementRequestCommand command, AiCandidateResult result) {
        String status =
                job == null || job.getStatus() == null ? null : job.getStatus().name();
        if (status == null && result != null) {
            status = result.getStatus() == null ? null : result.getStatus().name();
        }
        return new AiRefinementTaskResult(
                job == null ? taskId : AiBatchJobIdCodec.toValue(job.getBatchId()),
                command == null ? (job == null ? null : job.getScope()) : command.getScope(),
                command == null
                        ? (job == null || job.getCapability() == null
                                ? null
                                : job.getCapability().value())
                        : capabilityValue(command.getCapability()),
                command == null
                        ? (job == null ? null : AiContentRefCodec.toContentType(job.getContentRef()))
                        : AiContentRefCodec.toContentType(command.getContentRef()),
                command == null
                        ? (job == null ? null : AiContentRefCodec.toContentId(job.getContentRef()))
                        : AiContentRefCodec.toContentId(command.getContentRef()),
                command == null ? null : AiTargetObjectIdCodec.toValue(command.getTargetObjectId()),
                command == null ? null : RequestIdCodec.toValue(command.getRequestId()),
                command == null ? null : TraceIdCodec.toValue(command.getTraceId()),
                status,
                command == null ? null : command.getServiceRole(),
                command == null ? null : AiModelIdCodec.toValue(command.getModelId()),
                command == null ? null : AiModelNameCodec.toValue(command.getModelName()),
                command == null ? null : PromptVersionIdCodec.toValue(command.getPromptVersionId()),
                result == null ? null : AiCallIdCodec.toValue(result.getCallId()),
                result == null || (command != null && isStreamEnabledTask(command))
                        ? null
                        : AiCandidateIdCodec.toValue(result.getCandidateId()),
                result == null ? null : result.getResultFormat(),
                result == null ? null : truncate(result.getResultPayload()),
                result == null ? null : result.getFailureStage(),
                result == null ? null : result.getErrorType(),
                result == null ? (job == null ? null : job.getFailureSummaryJson()) : result.getErrorMessage(),
                command != null ? isStreamEnabledTask(command) : isStreamEnabledTask(job),
                job == null ? null : job.getRequestedAt(),
                null,
                job == null ? null : job.getCompletedAt(),
                job == null ? null : job.getCancelledAt());
    }

    private AiRefinementTaskResult toTaskResult(AiBatchJobResult job) {
        if (job == null) {
            return null;
        }
        AiBatchJobId batchId = job.getBatchId();
        AiInvocationLog invocationLog = latestInvocationLog(aiInvocationRepository.listInvocationLogsByBatch(batchId));
        AiCandidate candidate = latestCandidate(aiInvocationRepository.listCandidatesByBatch(batchId));
        return toTaskResult(job, invocationLog, candidate);
    }

    private AiRefinementTaskResult toTaskResult(
            AiBatchJobResult job, AiInvocationLog invocationLog, AiCandidate candidate) {
        AiRefinementTaskResult task = AiRefinementTaskResult.fromBatchJob(job, invocationLog, candidate);
        return new AiRefinementTaskResult(
                task.getTaskId(),
                task.getScope(),
                task.getCapability(),
                task.getContentType(),
                task.getContentId(),
                task.getObjectId(),
                task.getRequestId(),
                task.getTraceId(),
                task.getStatus(),
                task.getServiceRole(),
                task.getModelId(),
                task.getModelName(),
                task.getPromptVersionId(),
                task.getCallId(),
                task.getCandidateId(),
                task.getResultFormat(),
                truncate(task.getResultPreview()),
                task.getFailureStage(),
                task.getErrorType(),
                task.getErrorMessage(),
                task.isStreamEnabled(),
                task.getRequestedAt(),
                task.getStartedAt(),
                task.getCompletedAt(),
                task.getCancelledAt());
    }

    private Map<Long, AiInvocationLog> latestInvocationLogsByBatch(
            List<AiBatchJobResult> jobs, AiContentRef contentRef) {
        List<AiBatchJobId> batchIds = batchIds(jobs);
        Map<Long, AiInvocationLog> records = new LinkedHashMap<>();
        if (batchIds.isEmpty()) {
            return records;
        }
        List<AiInvocationLog> invocationLogs = contentRef == null
                ? aiInvocationRepository.listInvocationLogsByBatches(batchIds)
                : aiInvocationRepository.listInvocationLogsByBatchesAndContent(batchIds, contentRef);
        for (AiInvocationLog record : invocationLogs) {
            Long batchId = AiBatchJobIdCodec.toValue(record.getBatchId());
            records.putIfAbsent(batchId, record);
        }
        return records;
    }

    private Map<Long, AiCandidate> latestCandidatesByBatch(List<AiBatchJobResult> jobs, AiContentRef contentRef) {
        List<AiBatchJobId> batchIds = batchIds(jobs);
        Map<Long, AiCandidate> records = new LinkedHashMap<>();
        if (batchIds.isEmpty()) {
            return records;
        }
        List<AiCandidate> candidates = contentRef == null
                ? aiInvocationRepository.listCandidatesByBatches(batchIds)
                : aiInvocationRepository.listCandidatesByBatchesAndContent(batchIds, contentRef);
        for (AiCandidate record : candidates) {
            Long batchId = AiBatchJobIdCodec.toValue(record.getBatchId());
            records.putIfAbsent(batchId, record);
        }
        return records;
    }

    private List<AiBatchJobId> batchIds(List<AiBatchJobResult> jobs) {
        List<AiBatchJobId> ids = new ArrayList<>();
        if (jobs == null) {
            return ids;
        }
        for (AiBatchJobResult job : jobs) {
            if (job != null && job.getBatchId() != null) {
                ids.add(job.getBatchId());
            }
        }
        return ids;
    }

    private AiInvocationLog latestInvocationLog(List<AiInvocationLog> records) {
        return records == null || records.isEmpty() ? null : records.get(0);
    }

    private AiCandidate latestCandidate(List<AiCandidate> records) {
        return records == null || records.isEmpty() ? null : records.get(0);
    }

    private String failureSummaryJson(AiCandidateResult result) {
        Map<String, String> failure = Map.of(
                "failureStage", result == null ? "WORKER_RESULT" : nullToEmpty(result.getFailureStage()),
                "errorType", result == null ? "WORKER_PROTOCOL_FAILURE" : nullToEmpty(result.getErrorType()),
                "errorMessage",
                        result == null ? "Worker returned empty result" : nullToEmpty(result.getErrorMessage()));
        try {
            return objectMapper.writeValueAsString(failure);
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "AI-REFINEMENT-500",
                    "ai.refinement.failure-summary-invalid",
                    "AI refinement task failure summary is not valid JSON",
                    exception);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String capabilityValue(AiBusinessCapability capability) {
        return capability == null ? null : capability.value();
    }

    private static final class TaskStreamHub {

        private final List<AiStreamEventResult> events = new ArrayList<>();
        private final List<Consumer<AiStreamEventResult>> consumers = new ArrayList<>();
        private final CountDownLatch terminalLatch = new CountDownLatch(1);
        private boolean terminalEvent;

        void publish(AiStreamEventResult event) {
            List<Consumer<AiStreamEventResult>> currentConsumers;
            synchronized (this) {
                events.add(event);
                if (events.size() > STREAM_EVENT_HISTORY_LIMIT) {
                    events.remove(0);
                }
                if (event.isCompleted() || event.isError()) {
                    terminalEvent = true;
                    terminalLatch.countDown();
                }
                currentConsumers = List.copyOf(consumers);
            }
            currentConsumers.forEach(consumer -> notifyConsumer(consumer, event));
        }

        void subscribe(Consumer<AiStreamEventResult> consumer, Duration timeout) {
            if (consumer == null) {
                return;
            }
            List<AiStreamEventResult> snapshot;
            synchronized (this) {
                consumers.add(consumer);
                snapshot = List.copyOf(events);
            }
            snapshot.forEach(event -> notifyConsumer(consumer, event));
            try {
                terminalLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                synchronized (this) {
                    consumers.remove(consumer);
                }
            }
        }

        synchronized boolean hasTerminalEvent() {
            return terminalEvent;
        }

        private void notifyConsumer(Consumer<AiStreamEventResult> consumer, AiStreamEventResult event) {
            try {
                consumer.accept(event);
            } catch (RuntimeException exception) {
                synchronized (this) {
                    consumers.remove(consumer);
                }
            }
        }
    }
}
