package com.thundax.kuzhambu.ai.interfaces.admin.refinement.assembler;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.CancelAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.command.SubmitAiRefinementTaskCommand;
import com.thundax.kuzhambu.ai.application.scenario.query.GetAiRefinementTaskQuery;
import com.thundax.kuzhambu.ai.application.scenario.query.PageAiRefinementTasksQuery;
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
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import java.util.ArrayList;
import java.util.List;

public final class AiRefinementInterfaceAssembler {

    private AiRefinementInterfaceAssembler() {}

    public static AiRefinementRequestCommand toCommand(AiRefinementRequests.RefinementRequest request) {
        return toCommand(request, request == null ? null : request.getCapability());
    }

    public static SubmitAiRefinementTaskCommand toSubmitTaskCommand(AiRefinementRequests.RefinementRequest request) {
        return new SubmitAiRefinementTaskCommand(
                null,
                toCapability(request == null ? null : request.getCapability()),
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

    public static AiRefinementRequestCommand toCommand(
            AiRefinementRequests.RefinementRequest request, String capability) {
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

    public static GetAiRefinementTaskQuery toGetTaskQuery(Long taskId) {
        return new GetAiRefinementTaskQuery(AiBatchJobIdCodec.toDomain(taskId));
    }

    public static SubscribeAiRefinementTaskEventsQuery toSubscribeTaskEventsQuery(Long taskId) {
        return new SubscribeAiRefinementTaskEventsQuery(AiBatchJobIdCodec.toDomain(taskId));
    }

    public static PageAiRefinementTasksQuery toPageTasksQuery(AiRefinementRequests.TaskPageRequest request) {
        return new PageAiRefinementTasksQuery(
                toCapability(request == null ? null : request.getCapability()),
                toTaskStatus(request == null ? null : request.getStatus()),
                AiContentRefCodec.toDomain(
                        request == null ? null : request.getContentType(),
                        request == null ? null : request.getContentId()),
                PageInterfaceAssembler.toPageQuery(request));
    }

    public static CancelAiRefinementTaskCommand toCancelTaskCommand(Long taskId) {
        return new CancelAiRefinementTaskCommand(AiBatchJobIdCodec.toDomain(taskId));
    }

    public static AiBatchJobCreateCommand toCreateBatchCommand(AiRefinementRequests.BatchCreateRequest request) {
        return new AiBatchJobCreateCommand(
                request.getScope(),
                AiBusinessCapability.from(request.getCapability()),
                AiContentRef.ofNullable(request.getContentType(), null),
                request.getTotalCount(),
                request.getFailureSummaryJson());
    }

    public static GetAiBatchJobQuery toGetBatchQuery(AiRefinementRequests.BatchIdRequest request) {
        return new GetAiBatchJobQuery(AiBatchJobIdCodec.toDomain(request.getBatchId()));
    }

    public static GetAiBatchJobQuery toGetBatchQuery(AiBatchJobId batchId) {
        return new GetAiBatchJobQuery(batchId);
    }

    public static CancelAiBatchJobCommand toCancelBatchCommand(AiRefinementRequests.BatchIdRequest request) {
        return new CancelAiBatchJobCommand(AiBatchJobIdCodec.toDomain(request.getBatchId()));
    }

    public static AiRefinementResponses.CandidateResultResponse toResponse(AiCandidateResult result) {
        if (result == null) {
            return AiRefinementResponses.CandidateResultResponse.builder().build();
        }
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

    public static AiRefinementResponses.TaskDetailResponse toTaskDetailResponse(AiRefinementTaskResult task) {
        if (task == null) {
            return AiRefinementResponses.TaskDetailResponse.builder().build();
        }
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

    public static AiRefinementResponses.TaskAcceptedResponse toTaskAcceptedResponse(AiRefinementTaskResult task) {
        if (task == null) {
            return AiRefinementResponses.TaskAcceptedResponse.builder().build();
        }
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

    public static AiRefinementResponses.TaskCancelResponse toTaskCancelResponse(AiRefinementTaskResult task) {
        if (task == null) {
            return AiRefinementResponses.TaskCancelResponse.builder().build();
        }
        Long taskId = AiBatchJobIdCodec.toValue(task.getTaskId());
        return AiRefinementResponses.TaskCancelResponse.builder()
                .taskId(taskId)
                .taskIdText(longText(taskId))
                .status(task.getStatus() == null ? null : task.getStatus().name())
                .cancelledAt(task.getCancelledAt())
                .build();
    }

    public static AiRefinementResponses.TaskPageResponse toTaskPageResponse(
            int pageNo, int pageSize, long total, List<AiRefinementTaskResult> records) {
        List<AiRefinementResponses.TaskDetailResponse> items = new ArrayList<>();
        if (records != null) {
            for (AiRefinementTaskResult record : records) {
                items.add(toTaskDetailResponse(record));
            }
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
