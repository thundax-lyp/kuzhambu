package com.thundax.kuzhambu.ai.interfaces.admin.platform.assembler;

import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.request.PlatformAiRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.response.PlatformAiResponses;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class PlatformAiInterfaceAssembler {

    private PlatformAiInterfaceAssembler() {}

    @NonNull
    public static PlatformAiInvokeCommand toCommand(@NonNull PlatformAiRequests.InvokeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new PlatformAiInvokeCommand(
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
                request.getLocale(),
                Boolean.TRUE.equals(request.getAllowFallback()),
                request.getCreateCandidate());
    }

    @NonNull
    public static PlatformAiResponses.InvokeResponse toResponse(@NonNull AiInvokeResult result) {
        Objects.requireNonNull(result, "result must not be null");
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
