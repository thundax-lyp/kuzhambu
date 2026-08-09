package com.thundax.kuzhambu.ai.interfaces.admin.refinement.assembler;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.CancelAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.SubmitAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.query.AiRefinementTasksQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.GetAiRefinementTaskQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.SubscribeAiRefinementTaskEventsQuery;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.scenario.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class AiRefinementInterfaceAssembler {

    private AiRefinementInterfaceAssembler() {}

    @NonNull
    public static AiRefinementRequestCommand toCommand(@NonNull AiRefinementRequests.RefinementRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return toCommand(request, request.getCapability());
    }

    @NonNull
    public static SubmitAiRefinementTaskCommand toSubmitTaskCommand(
            @NonNull AiRefinementRequests.RefinementRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new SubmitAiRefinementTaskCommand(
                null,
                toCapability(request.getCapability()),
                request.getScope(),
                request.getOperation(),
                AiContentRefCodec.toDomain(request.getContentType(), request.getContentId()),
                AiTargetObjectIdCodec.toDomain(request.getObjectId()),
                request.getServiceId(),
                request.getServiceRole(),
                AiModelIdCodec.toDomain(request.getModelId()),
                AiModelNameCodec.toDomain(request.getModelName()),
                PromptVersionIdCodec.toDomain(request.getPromptVersionId()),
                RequestIdCodec.toDomain(request.getRequestId()),
                TraceIdCodec.toDomain(request.getTraceId()),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                Boolean.TRUE.equals(request.getForceJson()),
                request.getLocale());
    }

    @NonNull
    public static AiRefinementRequestCommand toCommand(
            @NonNull AiRefinementRequests.RefinementRequest request, @NonNull String capability) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(capability, "capability must not be null");
        return new AiRefinementRequestCommand(
                null,
                toCapability(capability),
                request.getScope(),
                request.getOperation(),
                AiContentRefCodec.toDomain(request.getContentType(), request.getContentId()),
                AiTargetObjectIdCodec.toDomain(request.getObjectId()),
                request.getServiceId(),
                request.getServiceRole(),
                AiModelIdCodec.toDomain(request.getModelId()),
                AiModelNameCodec.toDomain(request.getModelName()),
                PromptVersionIdCodec.toDomain(request.getPromptVersionId()),
                RequestIdCodec.toDomain(request.getRequestId()),
                TraceIdCodec.toDomain(request.getTraceId()),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                Boolean.TRUE.equals(request.getForceJson()),
                request.getLocale());
    }

    @NonNull
    public static GetAiRefinementTaskQuery toGetTaskQuery(@NonNull Long taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        return new GetAiRefinementTaskQuery(AiBatchJobIdCodec.toDomain(taskId));
    }

    @NonNull
    public static SubscribeAiRefinementTaskEventsQuery toSubscribeTaskEventsQuery(@NonNull Long taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        return new SubscribeAiRefinementTaskEventsQuery(AiBatchJobIdCodec.toDomain(taskId));
    }

    @NonNull
    public static AiRefinementTasksQuery toPageTasksQuery(@NonNull AiRefinementRequests.TaskPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AiRefinementTasksQuery(
                toCapability(request.getCapability()),
                toTaskStatus(request.getStatus()),
                AiContentRefCodec.toDomain(request.getContentType(), request.getContentId()));
    }

    @NonNull
    public static CancelAiRefinementTaskCommand toCancelTaskCommand(@NonNull Long taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        return new CancelAiRefinementTaskCommand(AiBatchJobIdCodec.toDomain(taskId));
    }

    @NonNull
    public static AiBatchJobCreateCommand toCreateBatchCommand(
            @NonNull AiRefinementRequests.BatchCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AiBatchJobCreateCommand(
                request.getScope(),
                AiBusinessCapability.from(request.getCapability()),
                AiContentRef.ofNullable(request.getContentType(), null),
                request.getTotalCount(),
                request.getFailureSummaryJson());
    }

    @NonNull
    public static GetAiBatchJobQuery toGetBatchQuery(@NonNull AiRefinementRequests.BatchIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GetAiBatchJobQuery(AiBatchJobIdCodec.toDomain(request.getBatchId()));
    }

    @NonNull
    public static GetAiBatchJobQuery toGetBatchQuery(@NonNull AiBatchJobId batchId) {
        Objects.requireNonNull(batchId, "batchId must not be null");
        return new GetAiBatchJobQuery(batchId);
    }

    @NonNull
    public static CancelAiBatchJobCommand toCancelBatchCommand(@NonNull AiRefinementRequests.BatchIdRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CancelAiBatchJobCommand(AiBatchJobIdCodec.toDomain(request.getBatchId()));
    }

    @NonNull
    public static AiRefinementResponses.CandidateResultResponse toResponse(@NonNull AiCandidateResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return AiRefinementResponses.CandidateResultResponse.builder()
                .callId(AiCallIdCodec.toValue(result.getCallId()))
                .callIdText(longText(AiCallIdCodec.toValue(result.getCallId())))
                .candidateId(AiCandidateIdCodec.toValue(result.getCandidateId()))
                .candidateIdText(longText(AiCandidateIdCodec.toValue(result.getCandidateId())))
                .status(result.getStatus() == null ? null : result.getStatus().name())
                .capability(
                        result.getCapability() == null
                                ? null
                                : result.getCapability().value())
                .failureStage(result.getFailureStage())
                .resultFormat(result.getResultFormat())
                .resultPayload(result.getResultPayload())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .build();
    }

    @NonNull
    public static AiRefinementResponses.TaskDetailResponse toTaskDetailResponse(@NonNull AiRefinementTaskResult task) {
        Objects.requireNonNull(task, "task must not be null");
        Long taskId = AiBatchJobIdCodec.toValue(task.getTaskId());
        Long callId = AiCallIdCodec.toValue(task.getCallId());
        Long candidateId = AiCandidateIdCodec.toValue(task.getCandidateId());
        return AiRefinementResponses.TaskDetailResponse.builder()
                .taskId(taskId)
                .taskIdText(longText(taskId))
                .status(task.getStatus() == null ? null : task.getStatus().name())
                .scope(task.getScope())
                .capability(
                        task.getCapability() == null
                                ? null
                                : task.getCapability().value())
                .contentType(AiContentRefCodec.toContentType(task.getContentRef()))
                .contentId(AiContentRefCodec.toContentId(task.getContentRef()))
                .objectId(AiTargetObjectIdCodec.toValue(task.getTargetObjectId()))
                .serviceRole(task.getServiceRole())
                .modelId(AiModelIdCodec.toValue(task.getModelId()))
                .modelName(AiModelNameCodec.toValue(task.getModelName()))
                .promptVersionId(PromptVersionIdCodec.toValue(task.getPromptVersionId()))
                .requestId(RequestIdCodec.toValue(task.getRequestId()))
                .traceId(TraceIdCodec.toValue(task.getTraceId()))
                .callId(callId)
                .callIdText(longText(callId))
                .candidateId(candidateId)
                .candidateIdText(longText(candidateId))
                .failureStage(task.getFailureStage())
                .errorType(task.getErrorType())
                .errorMessage(task.getErrorMessage())
                .streamEnabled(task.isStreamEnabled())
                .resultFormat(task.getResultFormat())
                .resultPreview(task.getResultPreview())
                .requestedAt(task.getRequestedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .cancelledAt(task.getCancelledAt())
                .build();
    }

    @NonNull
    public static AiRefinementResponses.TaskAcceptedResponse toTaskAcceptedResponse(
            @NonNull AiRefinementTaskResult task) {
        Objects.requireNonNull(task, "task must not be null");
        Long taskId = AiBatchJobIdCodec.toValue(task.getTaskId());
        return AiRefinementResponses.TaskAcceptedResponse.builder()
                .taskId(taskId)
                .taskIdText(longText(taskId))
                .status(task.getStatus() == null ? null : task.getStatus().name())
                .capability(
                        task.getCapability() == null
                                ? null
                                : task.getCapability().value())
                .contentType(AiContentRefCodec.toContentType(task.getContentRef()))
                .contentId(AiContentRefCodec.toContentId(task.getContentRef()))
                .requestedAt(task.getRequestedAt())
                .build();
    }

    @NonNull
    public static AiRefinementResponses.TaskCancelResponse toTaskCancelResponse(@NonNull AiRefinementTaskResult task) {
        Objects.requireNonNull(task, "task must not be null");
        Long taskId = AiBatchJobIdCodec.toValue(task.getTaskId());
        return AiRefinementResponses.TaskCancelResponse.builder()
                .taskId(taskId)
                .taskIdText(longText(taskId))
                .status(task.getStatus() == null ? null : task.getStatus().name())
                .cancelledAt(task.getCancelledAt())
                .build();
    }

    @NonNull
    public static AiRefinementResponses.TaskPageResponse toTaskPageResponse(
            int pageNo, int pageSize, long total, @NonNull List<AiRefinementTaskResult> records) {
        Objects.requireNonNull(records, "records must not be null");
        List<AiRefinementResponses.TaskDetailResponse> items = new ArrayList<>();
        for (AiRefinementTaskResult record : records) {
            items.add(toTaskDetailResponse(record));
        }
        return AiRefinementResponses.TaskPageResponse.builder()
                .items(items)
                .total(total)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
    }

    private static String longText(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private static AiBusinessCapability toCapability(String capability) {
        return capability == null || capability.trim().isEmpty() ? null : AiBusinessCapability.from(capability);
    }

    private static AiBatchJobStatus toTaskStatus(String status) {
        return status == null || status.trim().isEmpty() ? null : AiBatchJobStatus.from(status);
    }
}
