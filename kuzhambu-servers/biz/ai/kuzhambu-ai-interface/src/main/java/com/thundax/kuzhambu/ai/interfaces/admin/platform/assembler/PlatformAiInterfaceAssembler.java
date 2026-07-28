package com.thundax.kuzhambu.ai.interfaces.admin.platform.assembler;

import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.request.PlatformAiRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.response.PlatformAiResponses;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;

public final class PlatformAiInterfaceAssembler {

    private PlatformAiInterfaceAssembler() {}

    public static PlatformAiInvokeCommand toCommand(PlatformAiRequests.InvokeRequest request) {
        PlatformAiInvokeCommand command = new PlatformAiInvokeCommand();
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
        command.setAllowFallback(Boolean.TRUE.equals(request.getAllowFallback()));
        command.setCreateCandidate(request.getCreateCandidate());
        return command;
    }

    public static PlatformAiResponses.InvokeResponse toResponse(AiInvokeResult result) {
        if (result == null) {
            return PlatformAiResponses.InvokeResponse.builder().build();
        }
        return PlatformAiResponses.InvokeResponse.builder()
                .callId(AiCallIdCodec.toValue(result.getCallId()))
                .candidateId(AiCandidateIdCodec.toValue(result.getCandidateId()))
                .requestId(RequestIdCodec.toValue(result.getRequestId()))
                .traceId(TraceIdCodec.toValue(result.getTraceId()))
                .status(result.getStatus() == null ? null : result.getStatus().name())
                .capability(
                        result.getCapability() == null
                                ? null
                                : result.getCapability().value())
                .resultFormat(result.getResultFormat())
                .resultPayload(result.getResultPayload())
                .artifactReferenceJson(result.getArtifactReferenceJson())
                .warningsJson(result.getWarningsJson())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .failureStage(result.getFailureStage())
                .build();
    }
}
