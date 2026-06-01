package com.thundax.kuzhambu.ai.interfaces.admin.refinement.assembler;

import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request.AiRefinementRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response.AiRefinementResponses;

public final class AiRefinementInterfaceAssembler {

    private AiRefinementInterfaceAssembler() {}

    public static AiRefinementRequestCommand toCommand(AiRefinementRequests.RefinementRequest request) {
        AiRefinementRequestCommand command = new AiRefinementRequestCommand();
        command.setScope(request.getScope());
        command.setOperation(request.getOperation());
        command.setContentType(request.getContentType());
        command.setContentId(request.getContentId());
        command.setObjectId(request.getObjectId());
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
}
