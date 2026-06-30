package com.thundax.kuzhambu.ai.interfaces.admin.refinement.assembler;

import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;
import java.util.ArrayList;
import java.util.List;

public final class AiRefinementInterfaceAssembler {

    private AiRefinementInterfaceAssembler() {}

    public static AiRefinementRequestCommand toCommand(AiRefinementRequests.RefinementRequest request) {
        AiRefinementRequestCommand command = new AiRefinementRequestCommand();
        command.setCapability(request.getCapability());
        command.setScope(request.getScope());
        command.setOperation(request.getOperation());
        command.setContentType(request.getContentType());
        command.setContentId(request.getContentId());
        command.setObjectId(request.getObjectId());
        command.setRequestedBy(request.getRequestedBy());
        command.setServiceId(request.getServiceId());
        command.setServiceRole(request.getServiceRole());
        command.setModelId(request.getModelId());
        command.setModelName(request.getModelName());
        command.setPromptVersionId(request.getPromptVersionId());
        command.setRequestId(request.getRequestId());
        command.setTraceId(request.getTraceId());
        command.setPromptMessagesJson(request.getPromptMessagesJson());
        command.setPromptVariablesJson(request.getPromptVariablesJson());
        command.setPromptHash(request.getPromptHash());
        command.setInputPayloadJson(request.getInputPayloadJson());
        command.setOutputSchemaJson(request.getOutputSchemaJson());
        command.setForceJson(Boolean.TRUE.equals(request.getForceJson()));
        command.setLocale(request.getLocale());
        return command;
    }

    public static AiRefinementResponses.CandidateResultResponse toResponse(AiCandidateResult result) {
        if (result == null) {
            return AiRefinementResponses.CandidateResultResponse.builder().build();
        }
        return AiRefinementResponses.CandidateResultResponse.builder()
                .callId(result.getCallId())
                .candidateId(result.getCandidateId())
                .status(result.getStatus())
                .capability(result.getCapability())
                .resultFormat(result.getResultFormat())
                .resultPayload(result.getResultPayload())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .build();
    }

    public static AiRefinementResponses.TaskDetailResponse toTaskDetailResponse(AiRefinementTask task) {
        if (task == null) {
            return AiRefinementResponses.TaskDetailResponse.builder().build();
        }
        return AiRefinementResponses.TaskDetailResponse.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .scope(task.getScope())
                .capability(task.getCapability())
                .contentType(task.getContentType())
                .contentId(task.getContentId())
                .objectId(task.getObjectId())
                .requestedBy(task.getRequestedBy())
                .serviceRole(task.getServiceRole())
                .modelId(task.getModelId())
                .modelName(task.getModelName())
                .promptVersionId(task.getPromptVersionId())
                .requestId(task.getRequestId())
                .traceId(task.getTraceId())
                .callId(task.getCallId())
                .candidateId(task.getCandidateId())
                .failureStage(task.getFailureStage())
                .errorType(task.getErrorType())
                .errorMessage(task.getErrorMessage())
                .resultFormat(task.getResultFormat())
                .resultPreview(task.getResultPreview())
                .requestedAt(task.getRequestedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .cancelledAt(task.getCancelledAt())
                .build();
    }

    public static AiRefinementResponses.TaskAcceptedResponse toTaskAcceptedResponse(AiRefinementTask task) {
        if (task == null) {
            return AiRefinementResponses.TaskAcceptedResponse.builder().build();
        }
        return AiRefinementResponses.TaskAcceptedResponse.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .capability(task.getCapability())
                .contentType(task.getContentType())
                .contentId(task.getContentId())
                .requestedAt(task.getRequestedAt())
                .build();
    }

    public static AiRefinementResponses.TaskCancelResponse toTaskCancelResponse(AiRefinementTask task) {
        if (task == null) {
            return AiRefinementResponses.TaskCancelResponse.builder().build();
        }
        return AiRefinementResponses.TaskCancelResponse.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .cancelledAt(task.getCancelledAt())
                .build();
    }

    public static AiRefinementResponses.TaskPageResponse toTaskPageResponse(
            int pageNo, int pageSize, long total, List<AiRefinementTask> records) {
        List<AiRefinementResponses.TaskDetailResponse> items = new ArrayList<>();
        if (records != null) {
            for (AiRefinementTask record : records) {
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
}
