package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.ExpireRunningAiBatchJobsCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobFailureCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsByCapabilitiesQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsQuery;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.CancelAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.SubmitAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.configuration.AiRefinementExecutorConfiguration;
import com.thundax.kuzhambu.ai.application.scenario.query.GetAiRefinementTaskQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.PageAiRefinementTasksQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.SubscribeAiRefinementTaskEventsQuery;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.scenario.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementTaskApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
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
    public AiRefinementTaskResult submit(SubmitAiRefinementTaskCommand command) {
        AiRefinementRequestCommand refinementCommand = toRefinementRequestCommand(command);
        validateAddCommand(refinementCommand);
        validateRefinementBatchOwnership(
                refinementCommand.getScope(), capabilityValue(refinementCommand.getCapability()));
        refinementApplicationService.snapshotInvokeConfig(refinementCommand);
        Long taskId = AiBatchJobIdCodec.toValue(batchJobApplicationService.create(new AiBatchJobCreateCommand(
                refinementCommand.getScope(),
                refinementCommand.getCapability(),
                refinementCommand.getContentRef(),
                1,
                null)));
        refinementCommand.setBatchId(AiBatchJobIdCodec.toDomain(taskId));
        command.setBatchId(AiBatchJobIdCodec.toDomain(taskId));
        scheduleTaskExecution(taskId, refinementCommand);
        return toTaskResult(
                taskId,
                batchJobApplicationService.get(new GetAiBatchJobQuery(AiBatchJobIdCodec.toDomain(taskId))),
                refinementCommand,
                null);
    }

    @Override
    public AiRefinementTaskResult get(GetAiRefinementTaskQuery query) {
        AiBatchJobResult job =
                batchJobApplicationService.get(new GetAiBatchJobQuery(query == null ? null : query.taskId()));
        validateRefinementBatchOwnership(job);
        return toTaskResult(job);
    }

    @Override
    public PageResult<AiRefinementTaskResult> page(PageAiRefinementTasksQuery query) {
        AiBusinessCapability capability = query == null ? null : query.capability();
        AiBatchJobStatus status = query == null ? null : query.status();
        AiContentRef contentRef = query == null ? null : query.contentRef();
        PageQuery pageQuery = query == null ? null : query.pageQuery();
        PageResult<AiBatchJobResult> page = capability == null
                ? batchJobApplicationService.pageByCapabilities(new PageAiBatchJobsByCapabilitiesQuery(
                        REFINEMENT_SCOPE, REFINEMENT_CAPABILITIES, status, contentRef, pageQuery))
                : batchJobApplicationService.page(new PageAiBatchJobsQuery(
                        REFINEMENT_SCOPE, validateRefinementCapability(capability), status, contentRef, pageQuery));
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
    public void subscribeEvents(
            SubscribeAiRefinementTaskEventsQuery query, Consumer<AiStreamEventResult> eventConsumer) {
        AiBatchJobId taskId = query == null ? null : query.taskId();
        AiBatchJobResult job = batchJobApplicationService.get(new GetAiBatchJobQuery(taskId));
        validateRefinementBatchOwnership(job);
        if (!isStreamEnabledTask(job)) {
            throw new BizException("AI refinement task stream is not enabled: " + taskId);
        }
        Long taskIdValue = AiBatchJobIdCodec.toValue(taskId);
        TaskStreamHub hub = streamHubs.computeIfAbsent(taskIdValue, ignored -> new TaskStreamHub());
        publishSnapshotIfTerminal(hub, toTaskResult(job));
        try {
            hub.subscribe(eventConsumer, STREAM_SUBSCRIBE_TIMEOUT);
        } finally {
            if (hub.hasTerminalEvent()) {
                streamHubs.remove(taskIdValue, hub);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiRefinementTaskResult cancel(CancelAiRefinementTaskCommand command) {
        AiBatchJobId taskId = command == null ? null : command.getTaskId();
        AiBatchJobResult job = batchJobApplicationService.get(new GetAiBatchJobQuery(taskId));
        validateRefinementBatchOwnership(job);
        AiBatchJobResult cancelled = batchJobApplicationService.cancel(new CancelAiBatchJobCommand(taskId));
        AiRefinementTaskResult task = toTaskResult(cancelled);
        publishTerminalEvent(AiBatchJobIdCodec.toValue(taskId), task, null);
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
        AiBatchJobResult job = batchJobApplicationService.get(new GetAiBatchJobQuery(batchId));
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
        AiBatchJobResult latestJob = batchJobApplicationService.get(new GetAiBatchJobQuery(batchId));
        if (AiBatchJobStatus.CANCELLED == latestJob.getStatus()) {
            return;
        }
        AiBatchJobResult finalJob;
        if (result != null && AiInvocationStatus.SUCCEEDED == result.getStatus()) {
            finalJob = batchJobApplicationService.recordSuccessIfRunning(new RecordAiBatchJobCommand(batchId));
        } else if (result != null && AiInvocationStatus.PARTIAL == result.getStatus()) {
            finalJob = batchJobApplicationService.recordPartialIfRunning(
                    new RecordAiBatchJobFailureCommand(batchId, failureSummaryJson(result)));
        } else {
            finalJob = batchJobApplicationService.recordFailureIfRunning(
                    new RecordAiBatchJobFailureCommand(batchId, failureSummaryJson(result)));
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
        AiBatchJobResult job = batchJobApplicationService.get(new GetAiBatchJobQuery(batchId));
        if (job == null || isTerminal(job.getStatus())) {
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
        AiBatchJobResult failed = batchJobApplicationService.recordFailureIfRunning(
                new RecordAiBatchJobFailureCommand(batchId, failureSummaryJson(result)));
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
        int expired = batchJobApplicationService.expireRunning(new ExpireRunningAiBatchJobsCommand(
                "classics",
                REFINEMENT_CAPABILITIES,
                requestedBefore,
                failureSummaryJson(result),
                ORPHANED_TASK_EXPIRE_LIMIT));
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

    private AiRefinementRequestCommand toRefinementRequestCommand(SubmitAiRefinementTaskCommand source) {
        if (source == null) {
            return null;
        }
        AiRefinementRequestCommand command = new AiRefinementRequestCommand();
        command.setBatchId(source.getBatchId());
        command.setCapability(source.getCapability());
        command.setScope(source.getScope());
        command.setOperation(source.getOperation());
        command.setContentRef(source.getContentRef());
        command.setTargetObjectId(source.getTargetObjectId());
        command.setServiceId(source.getServiceId());
        command.setServiceRole(source.getServiceRole());
        command.setModelId(source.getModelId());
        command.setModelName(source.getModelName());
        command.setPromptVersionId(source.getPromptVersionId());
        command.setRequestId(source.getRequestId());
        command.setTraceId(source.getTraceId());
        command.setPromptMessagesJson(source.getPromptMessagesJson());
        command.setPromptVariablesJson(source.getPromptVariablesJson());
        command.setPromptHash(source.getPromptHash());
        command.setInputPayloadJson(source.getInputPayloadJson());
        command.setOutputSchemaJson(source.getOutputSchemaJson());
        command.setForceJson(source.isForceJson());
        command.setLocale(source.getLocale());
        return command;
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

    private boolean isTerminal(AiBatchJobStatus status) {
        return AiBatchJobStatus.SUCCEEDED == status
                || AiBatchJobStatus.FAILED == status
                || AiBatchJobStatus.PARTIAL == status
                || AiBatchJobStatus.CANCELLED == status;
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

    private AiBusinessCapability validateRefinementCapability(AiBusinessCapability capability) {
        if (!REFINEMENT_CAPABILITIES.contains(capability)) {
            throw new BizException("unsupported ai refinement capability: " + capability);
        }
        return capability;
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
        return AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE == job.getCapability()
                || AiBusinessCapability.CLASSICS_IMAGE_GENERATE == job.getCapability();
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
        if (AiBatchJobStatus.SUCCEEDED == task.getStatus()) {
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
        if (AiBatchJobStatus.SUCCEEDED == task.getStatus()) {
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
        event.setStatus(
                task.getStatus() == null
                        ? null
                        : AiInvocationStatus.from(task.getStatus().name()));
        if (AiBatchJobStatus.PARTIAL == task.getStatus()) {
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
        event.setRequestId(task.getRequestId());
        event.setTraceId(task.getTraceId());
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
        AiBatchJobStatus status = job == null ? null : job.getStatus();
        if (status == null && result != null) {
            status = result.getStatus() == null
                    ? null
                    : AiBatchJobStatus.valueOf(result.getStatus().name());
        }
        return new AiRefinementTaskResult(
                job == null ? AiBatchJobIdCodec.toDomain(taskId) : job.getBatchId(),
                command == null ? (job == null ? null : job.getScope()) : command.getScope(),
                command == null ? (job == null ? null : job.getCapability()) : command.getCapability(),
                command == null ? (job == null ? null : job.getContentRef()) : command.getContentRef(),
                command == null ? null : command.getTargetObjectId(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                status,
                command == null ? null : command.getServiceRole(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getPromptVersionId(),
                result == null ? null : result.getCallId(),
                result == null || (command != null && isStreamEnabledTask(command)) ? null : result.getCandidateId(),
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
                task.getContentRef(),
                task.getTargetObjectId(),
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
