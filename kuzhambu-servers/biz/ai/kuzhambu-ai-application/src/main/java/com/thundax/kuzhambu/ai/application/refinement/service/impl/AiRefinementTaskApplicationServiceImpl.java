package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementTaskApplicationService;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.domain.refinement.repository.AiRefinementTaskRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiRefinementTaskApplicationServiceImpl implements AiRefinementTaskApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiRefinementTaskApplicationServiceImpl.class);

    private static final String CONTENT_TYPE_SANCAI_ENTRY = "SANCAI_ENTRY";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "classics_image_describe";
    private static final String CAPABILITY_IMAGE_GEN = "classics_image_generate";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PARTIAL = "PARTIAL";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final int RESULT_PREVIEW_MAX_LENGTH = 500;
    private static final Duration STREAM_SUBSCRIBE_TIMEOUT = Duration.ofMinutes(10L);

    private final AiRefinementTaskRepository taskRepository;
    private final AiRefinementApplicationService refinementApplicationService;
    private final ConcurrentHashMap<Long, TaskStreamHub> streamHubs = new ConcurrentHashMap<>();

    public AiRefinementTaskApplicationServiceImpl(
            AiRefinementTaskRepository taskRepository, AiRefinementApplicationService refinementApplicationService) {
        this.taskRepository = taskRepository;
        this.refinementApplicationService = refinementApplicationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiRefinementTask addTask(AiRefinementRequestCommand command) {
        validateAddCommand(command);
        Instant now = Instant.now();
        AiRefinementTask task = new AiRefinementTask();
        task.setScope(command.getScope());
        task.setCapability(command.getCapability());
        task.setContentType(command.getContentType());
        task.setContentId(command.getContentId());
        task.setObjectId(command.getObjectId());
        task.setRequestedBy(command.getRequestedBy());
        task.setRequestId(command.getRequestId());
        task.setTraceId(command.getTraceId());
        task.setStatus(STATUS_PENDING);
        task.setServiceRole(command.getServiceRole());
        task.setModelId(command.getModelId());
        task.setModelName(command.getModelName());
        task.setPromptVersionId(command.getPromptVersionId());
        task.setStreamEnabled(isStreamEnabledTask(command));
        task.setRequestedAt(now);
        Long taskId = taskRepository.saveTask(task);
        task.setTaskId(taskId);
        scheduleTaskExecution(taskId, command);
        return task;
    }

    @Override
    public AiRefinementTask getTask(Long taskId) {
        return getRequiredTask(taskId);
    }

    @Override
    public PageResult<AiRefinementTask> pageTasks(
            String capability,
            String status,
            String contentType,
            Long contentId,
            Long requestedBy,
            PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        long total = taskRepository.countTasks(capability, status, contentType, contentId, requestedBy);
        return PageResult.of(
                effectivePage.getPageNo(),
                effectivePage.getPageSize(),
                total,
                taskRepository.listTasks(
                        capability,
                        status,
                        contentType,
                        contentId,
                        requestedBy,
                        effectivePage.getPageNo(),
                        effectivePage.getPageSize()));
    }

    @Override
    public void streamTaskEvents(Long taskId, Consumer<AiStreamEventResult> eventConsumer) {
        AiRefinementTask task = getRequiredTask(taskId);
        if (!task.isStreamEnabled()) {
            throw new BizException("AI refinement task stream is not enabled: " + taskId);
        }
        TaskStreamHub hub = streamHubs.computeIfAbsent(taskId, ignored -> new TaskStreamHub());
        publishSnapshotIfTerminal(hub, task);
        hub.subscribe(eventConsumer, STREAM_SUBSCRIBE_TIMEOUT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiRefinementTask cancelTask(Long taskId, Long requestedBy) {
        AiRefinementTask task = getRequiredTask(taskId);
        if (task.getRequestedBy() != null
                && requestedBy != null
                && !task.getRequestedBy().equals(requestedBy)) {
            throw new BizException("AI refinement task cancel requester mismatch: " + taskId);
        }
        if (isTerminal(task.getStatus())) {
            return task;
        }
        task.markCancelled(Instant.now());
        taskRepository.updateTask(task);
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
        AiRefinementTask task = taskRepository.getTask(taskId);
        if (task == null || STATUS_CANCELLED.equals(task.getStatus())) {
            return;
        }
        task.markRunning(Instant.now());
        taskRepository.updateTask(task);

        AiCandidateResult result;
        try {
            result = invoke(taskId, command, task.isStreamEnabled());
        } catch (RuntimeException exception) {
            result = new AiCandidateResult(
                    null,
                    null,
                    STATUS_FAILED,
                    command.getCapability(),
                    "WORKER_REQUEST",
                    null,
                    null,
                    "INTERNAL_FAILURE",
                    exception.getMessage());
            publishFailureEvent(taskId, command, result);
        }

        AiRefinementTask latestTask = taskRepository.getTask(taskId);
        if (latestTask == null || STATUS_CANCELLED.equals(latestTask.getStatus())) {
            return;
        }
        latestTask.setServiceRole(command.getServiceRole());
        latestTask.setModelId(command.getModelId());
        if (!isBlank(command.getModelName())) {
            latestTask.setModelName(command.getModelName());
        }
        applyResult(latestTask, result);
        taskRepository.updateTask(latestTask);
        publishTerminalEvent(taskId, latestTask, result);
    }

    private void scheduleTaskExecution(Long taskId, AiRefinementRequestCommand command) {
        Runnable task = () -> CompletableFuture.runAsync(() -> executeTaskSafely(taskId, command));
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
        AiRefinementTask task = taskRepository.getTask(taskId);
        if (task == null || isTerminal(task.getStatus())) {
            return;
        }
        task.setFailureStage("INTERNAL_EXECUTION");
        task.setErrorType("INTERNAL_FAILURE");
        task.setErrorMessage(exception.getMessage());
        task.setCompletedAt(Instant.now());
        task.setStatus(STATUS_FAILED);
        taskRepository.updateTask(task);
        publishTerminalEvent(
                taskId,
                task,
                new AiCandidateResult(
                        task.getCallId(),
                        task.getCandidateId(),
                        STATUS_FAILED,
                        task.getCapability(),
                        task.getFailureStage(),
                        task.getResultFormat(),
                        task.getResultPreview(),
                        task.getErrorType(),
                        task.getErrorMessage()));
    }

    private AiCandidateResult invoke(Long taskId, AiRefinementRequestCommand command, boolean streamEnabled) {
        String capability = command.getCapability();
        if ("classics_translate".equals(capability)) {
            return refinementApplicationService.translate(command);
        }
        if ("classics_summary".equals(capability)) {
            return refinementApplicationService.summarize(command);
        }
        if ("classics_tags".equals(capability)) {
            return refinementApplicationService.generateTags(command);
        }
        if ("classics_qa".equals(capability)) {
            return refinementApplicationService.generateQa(command);
        }
        if ("classics_image_describe".equals(capability)) {
            return streamEnabled
                    ? refinementApplicationService.analyzeImage(command, event -> publishStreamEvent(taskId, event))
                    : refinementApplicationService.analyzeImage(command);
        }
        if ("classics_visual_describe".equals(capability)) {
            return refinementApplicationService.describeVisual(command);
        }
        if ("classics_image_generate".equals(capability)) {
            return streamEnabled
                    ? refinementApplicationService.generateImage(command, event -> publishStreamEvent(taskId, event))
                    : refinementApplicationService.generateImage(command);
        }
        if ("classics_split".equals(capability)) {
            return refinementApplicationService.splitEntry(command);
        }
        throw new BizException("unsupported ai refinement capability: " + capability);
    }

    private void applyResult(AiRefinementTask task, AiCandidateResult result) {
        Instant completedAt = Instant.now();
        String preview = truncate(result == null ? null : result.getResultPayload());
        if (result != null && STATUS_SUCCEEDED.equals(result.getStatus())) {
            task.markSucceeded(
                    result.getCallId(), result.getCandidateId(), result.getResultFormat(), preview, completedAt);
            return;
        }
        task.setCallId(result == null ? null : result.getCallId());
        task.setCandidateId(result == null || task.isStreamEnabled() ? null : result.getCandidateId());
        task.setResultFormat(result == null ? null : result.getResultFormat());
        task.setResultPreview(preview);
        task.setFailureStage(result == null ? "WORKER_RESULT" : result.getFailureStage());
        task.setErrorType(result == null ? "WORKER_PROTOCOL_FAILURE" : result.getErrorType());
        task.setErrorMessage(result == null ? "Worker returned empty result" : result.getErrorMessage());
        task.setCompletedAt(completedAt);
        task.setStatus(resolveFinalStatus(result == null ? null : result.getStatus()));
    }

    private String resolveFinalStatus(String status) {
        if (STATUS_PARTIAL.equals(status)) {
            return STATUS_PARTIAL;
        }
        if (STATUS_CANCELLED.equals(status)) {
            return STATUS_CANCELLED;
        }
        return STATUS_FAILED;
    }

    private AiRefinementTask getRequiredTask(Long taskId) {
        if (taskId == null) {
            throw new BizException("AI refinement taskId is required");
        }
        AiRefinementTask task = taskRepository.getTask(taskId);
        if (task == null) {
            throw new BizException("AI refinement task not found or expired: " + taskId);
        }
        return task;
    }

    private void validateAddCommand(AiRefinementRequestCommand command) {
        if (command == null
                || isBlank(command.getCapability())
                || isBlank(command.getScope())
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || isBlank(command.getContentType())
                || command.getContentId() == null
                || command.getRequestedBy() == null
                || isBlank(command.getPromptMessagesJson())
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("AI refinement task add command is incomplete");
        }
    }

    private boolean isTerminal(String status) {
        return STATUS_SUCCEEDED.equals(status)
                || STATUS_FAILED.equals(status)
                || STATUS_PARTIAL.equals(status)
                || STATUS_CANCELLED.equals(status);
    }

    private boolean isStreamEnabledTask(AiRefinementRequestCommand command) {
        if (command == null || !CONTENT_TYPE_SANCAI_ENTRY.equals(command.getContentType())) {
            return false;
        }
        return CAPABILITY_IMAGE_ANALYSIS.equals(command.getCapability())
                || CAPABILITY_IMAGE_GEN.equals(command.getCapability());
    }

    private void publishStreamEvent(Long taskId, AiStreamEventResult event) {
        if (taskId == null || event == null) {
            return;
        }
        streamHubs.computeIfAbsent(taskId, ignored -> new TaskStreamHub()).publish(event);
    }

    private void publishTerminalEvent(Long taskId, AiRefinementTask task, AiCandidateResult result) {
        if (taskId == null || task == null || !task.isStreamEnabled()) {
            return;
        }
        TaskStreamHub hub = streamHubs.computeIfAbsent(taskId, ignored -> new TaskStreamHub());
        if (hub.hasTerminalEvent()) {
            return;
        }
        if (STATUS_SUCCEEDED.equals(task.getStatus())) {
            hub.publish(toCompletedEvent(task, result));
            return;
        }
        if (isTerminal(task.getStatus())) {
            hub.publish(toErrorEvent(task));
        }
    }

    private void publishFailureEvent(Long taskId, AiRefinementRequestCommand command, AiCandidateResult result) {
        if (taskId == null || command == null || !isStreamEnabledTask(command)) {
            return;
        }
        AiRefinementTask task = new AiRefinementTask();
        task.setRequestId(command.getRequestId());
        task.setTraceId(command.getTraceId());
        task.setStatus(STATUS_FAILED);
        task.setFailureStage(result == null ? "WORKER_REQUEST" : result.getFailureStage());
        task.setErrorType(result == null ? "INTERNAL_FAILURE" : result.getErrorType());
        task.setErrorMessage(result == null ? "Worker request failed" : result.getErrorMessage());
        streamHubs.computeIfAbsent(taskId, ignored -> new TaskStreamHub()).publish(toErrorEvent(task));
    }

    private void publishSnapshotIfTerminal(TaskStreamHub hub, AiRefinementTask task) {
        if (hub.hasTerminalEvent() || task == null || !isTerminal(task.getStatus())) {
            return;
        }
        if (STATUS_SUCCEEDED.equals(task.getStatus())) {
            hub.publish(toCompletedEvent(task, null));
            return;
        }
        hub.publish(toErrorEvent(task));
    }

    private AiStreamEventResult toCompletedEvent(AiRefinementTask task, AiCandidateResult result) {
        AiStreamEventResult event = baseEvent(task);
        event.setEventType("completed");
        event.setStage("completed");
        event.setStatus(STATUS_SUCCEEDED);
        event.setResultFormat(result == null ? task.getResultFormat() : result.getResultFormat());
        event.setResultPayload(result == null ? task.getResultPreview() : result.getResultPayload());
        return event;
    }

    private AiStreamEventResult toErrorEvent(AiRefinementTask task) {
        AiStreamEventResult event = baseEvent(task);
        event.setEventType("error");
        event.setStage(task.getFailureStage());
        event.setStatus(task.getStatus());
        event.setFailureStage(task.getFailureStage());
        event.setErrorType(task.getErrorType());
        event.setErrorMessage(task.getErrorMessage());
        return event;
    }

    private AiStreamEventResult baseEvent(AiRefinementTask task) {
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

    private static final class TaskStreamHub {

        private final List<AiStreamEventResult> events = new ArrayList<>();
        private final List<Consumer<AiStreamEventResult>> consumers = new ArrayList<>();
        private final CountDownLatch terminalLatch = new CountDownLatch(1);
        private boolean terminalEvent;

        void publish(AiStreamEventResult event) {
            List<Consumer<AiStreamEventResult>> currentConsumers;
            synchronized (this) {
                events.add(event);
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
