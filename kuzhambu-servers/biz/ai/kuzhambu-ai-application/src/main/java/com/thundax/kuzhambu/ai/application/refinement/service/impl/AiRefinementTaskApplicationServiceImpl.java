package com.thundax.kuzhambu.ai.application.refinement.service.impl;

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
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiRefinementTaskApplicationServiceImpl implements AiRefinementTaskApplicationService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PARTIAL = "PARTIAL";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final int RESULT_PREVIEW_MAX_LENGTH = 500;

    private final AiRefinementTaskRepository taskRepository;
    private final AiRefinementApplicationService refinementApplicationService;

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
        task.setRequestedAt(now);
        Long taskId = taskRepository.saveTask(task);
        task.setTaskId(taskId);
        CompletableFuture.runAsync(() -> executeTask(taskId, command));
        return task;
    }

    @Override
    public AiRefinementTask getTask(Long taskId) {
        return getRequiredTask(taskId);
    }

    @Override
    public PageResult<AiRefinementTask> pageTasks(
            String capability, String status, String contentType, Long contentId, Long requestedBy, PageQuery pageQuery) {
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
    @Transactional(rollbackFor = Exception.class)
    public AiRefinementTask cancelTask(Long taskId, Long requestedBy) {
        AiRefinementTask task = getRequiredTask(taskId);
        if (task.getRequestedBy() != null && requestedBy != null && !task.getRequestedBy().equals(requestedBy)) {
            throw new BizException("AI refinement task cancel requester mismatch: " + taskId);
        }
        if (isTerminal(task.getStatus())) {
            return task;
        }
        task.markCancelled(Instant.now());
        taskRepository.updateTask(task);
        return task;
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
            result = invoke(command);
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
        }

        AiRefinementTask latestTask = taskRepository.getTask(taskId);
        if (latestTask == null || STATUS_CANCELLED.equals(latestTask.getStatus())) {
            return;
        }
        applyResult(latestTask, result);
        taskRepository.updateTask(latestTask);
    }

    private AiCandidateResult invoke(AiRefinementRequestCommand command) {
        String capability = command.getCapability();
        if ("translate".equals(capability)) {
            return refinementApplicationService.translate(command);
        }
        if ("summary".equals(capability)) {
            return refinementApplicationService.summarize(command);
        }
        if ("tags".equals(capability)) {
            return refinementApplicationService.generateTags(command);
        }
        if ("qa".equals(capability)) {
            return refinementApplicationService.generateQa(command);
        }
        if ("image_analysis".equals(capability)) {
            return refinementApplicationService.analyzeImage(command);
        }
        if ("visual".equals(capability)) {
            return refinementApplicationService.describeVisual(command);
        }
        if ("split".equals(capability)) {
            return refinementApplicationService.splitEntry(command);
        }
        throw new BizException("unsupported ai refinement capability: " + capability);
    }

    private void applyResult(AiRefinementTask task, AiCandidateResult result) {
        Instant completedAt = Instant.now();
        String preview = truncate(result == null ? null : result.getResultPayload());
        if (result != null && STATUS_SUCCEEDED.equals(result.getStatus())) {
            task.markSucceeded(result.getCallId(), result.getCandidateId(), result.getResultFormat(), preview, completedAt);
            return;
        }
        task.setCallId(result == null ? null : result.getCallId());
        task.setCandidateId(result == null ? null : result.getCandidateId());
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
                || command.getModelId() == null
                || isBlank(command.getModelName())
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= RESULT_PREVIEW_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, RESULT_PREVIEW_MAX_LENGTH);
    }
}
