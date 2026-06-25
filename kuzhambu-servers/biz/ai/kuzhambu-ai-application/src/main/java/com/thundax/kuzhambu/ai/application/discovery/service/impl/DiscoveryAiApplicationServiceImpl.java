package com.thundax.kuzhambu.ai.application.discovery.service.impl;

import com.thundax.kuzhambu.ai.application.discovery.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.discovery.support.DiscoveryAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.discovery.support.DiscoveryAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import com.thundax.kuzhambu.ai.domain.discovery.service.DiscoveryAiDomainService;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class DiscoveryAiApplicationServiceImpl implements DiscoveryAiApplicationService, DiscoveryAiDomainService {

    private static final String SCOPE_DISCOVERY = "discovery";

    private final AiWorkerInvocationApplicationService invocationApplicationService;
    private final DiscoveryAiWorkerUsecaseResolver resolver;

    public DiscoveryAiApplicationServiceImpl(
            AiWorkerInvocationApplicationService invocationApplicationService,
            DiscoveryAiWorkerUsecaseResolver resolver) {
        this.invocationApplicationService = invocationApplicationService;
        this.resolver = resolver;
    }

    @Override
    public DiscoveryAiResult understandQuery(DiscoveryAiRequest request) {
        return invoke(request, "DISCOVERY_QUERY_UNDERSTANDING");
    }

    @Override
    public DiscoveryAiResult rewriteQuery(DiscoveryAiRequest request) {
        return invoke(request, "DISCOVERY_QUERY_REWRITE");
    }

    @Override
    public DiscoveryAiResult generateAnswer(DiscoveryAiRequest request) {
        return invoke(request, "DISCOVERY_ANSWER_GENERATION");
    }

    @Override
    public DiscoveryAiResult streamAnswer(DiscoveryAiRequest request) {
        return invoke(request, "DISCOVERY_ANSWER_GENERATION_STREAM");
    }

    private DiscoveryAiResult invoke(DiscoveryAiRequest request, String usecase) {
        validateRequest(request);
        DiscoveryAiWorkerUsecaseSpec spec = resolver.resolve(usecase);
        AiInvokeCommand command = toInvokeCommand(request, spec);
        AiInvokeResult result = spec.stream()
                ? invocationApplicationService.stream(command, event -> {})
                : invocationApplicationService.invoke(command);
        return new DiscoveryAiResult(
                result.getCallId(),
                result.getCandidateId(),
                result.getStatus(),
                result.getCapability(),
                result.getResultFormat(),
                result.getResultPayload(),
                result.getErrorType(),
                result.getErrorMessage());
    }

    private AiInvokeCommand toInvokeCommand(DiscoveryAiRequest request, DiscoveryAiWorkerUsecaseSpec spec) {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope(SCOPE_DISCOVERY);
        command.setCapability(spec.capability());
        command.setOperation(spec.operation());
        command.setWorkerPath(spec.workerPath());
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
        command.setStream(spec.stream() || request.isStream());
        command.setForceJson(request.isForceJson());
        command.setLocale(request.getLocale());
        command.setCreateCandidate(false);
        return command;
    }

    private void validateRequest(DiscoveryAiRequest request) {
        if (request == null
                || request.getServiceId() == null
                || isBlank(request.getServiceRole())
                || request.getModelId() == null
                || isBlank(request.getModelName())
                || request.getPromptVersionId() == null
                || isBlank(request.getRequestId())
                || isBlank(request.getTraceId())
                || isBlank(request.getPromptMessagesJson())
                || isBlank(request.getInputPayloadJson())) {
            throw new BizException("Discovery AI request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
