package com.thundax.kuzhambu.ai.application.discovery.service.impl;

import com.thundax.kuzhambu.ai.application.discovery.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.discovery.support.DiscoveryAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.discovery.support.DiscoveryAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import com.thundax.kuzhambu.ai.domain.discovery.service.DiscoveryAiDomainService;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class DiscoveryAiApplicationServiceImpl implements DiscoveryAiApplicationService, DiscoveryAiDomainService {

    private static final String SCOPE_DISCOVERY = "discovery";
    private static final String CONTENT_TYPE_DISCOVERY_QUERY = "DISCOVERY_QUERY";

    private final AiWorkerInvocationApplicationService invocationApplicationService;
    private final DiscoveryAiWorkerUsecaseResolver resolver;
    private final AiBusinessInvokeConfigResolver businessInvokeConfigResolver;

    @Autowired
    public DiscoveryAiApplicationServiceImpl(
            AiWorkerInvocationApplicationService invocationApplicationService,
            DiscoveryAiWorkerUsecaseResolver resolver,
            AiBusinessInvokeConfigResolver businessInvokeConfigResolver) {
        this.invocationApplicationService = invocationApplicationService;
        this.resolver = resolver;
        this.businessInvokeConfigResolver = businessInvokeConfigResolver;
    }

    @Override
    public DiscoveryAiResult understandQuery(DiscoveryAiRequest request) {
        return invoke(request, "DISCOVERY_QUERY_UNDERSTANDING", null);
    }

    @Override
    public DiscoveryAiResult rewriteQuery(DiscoveryAiRequest request) {
        return invoke(request, "DISCOVERY_QUERY_REWRITE", null);
    }

    @Override
    public DiscoveryAiResult generateAnswer(DiscoveryAiRequest request) {
        return invoke(request, "DISCOVERY_ANSWER_GENERATION", null);
    }

    @Override
    public DiscoveryAiResult streamAnswer(DiscoveryAiRequest request) {
        return streamAnswer(request, event -> {});
    }

    @Override
    public DiscoveryAiResult streamAnswer(DiscoveryAiRequest request, Consumer<AiStreamEventResult> eventConsumer) {
        return invoke(request, "DISCOVERY_ANSWER_GENERATION_STREAM", eventConsumer);
    }

    private DiscoveryAiResult invoke(
            DiscoveryAiRequest request, String usecase, Consumer<AiStreamEventResult> eventConsumer) {
        validateRequest(request);
        DiscoveryAiWorkerUsecaseSpec spec = resolver.resolve(usecase);
        AiInvokeCommand command = toInvokeCommand(request, spec);
        enrichBusinessInvokeConfig(command);
        AiInvokeResult result = spec.stream()
                ? invocationApplicationService.stream(command, eventConsumer == null ? event -> {} : eventConsumer)
                : invocationApplicationService.invoke(command);
        return toDiscoveryResult(result);
    }

    private AiInvokeCommand toInvokeCommand(DiscoveryAiRequest request, DiscoveryAiWorkerUsecaseSpec spec) {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope(SCOPE_DISCOVERY);
        command.setCapability(spec.capability());
        command.setWorkerCapability(spec.workerCapability());
        command.setOperation(spec.operation());
        command.setWorkerPath(spec.workerPath());
        command.setContentType(CONTENT_TYPE_DISCOVERY_QUERY);
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

    private void enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return;
        }
        businessInvokeConfigResolver.resolve(command);
    }

    private void validateRequest(DiscoveryAiRequest request) {
        if (request == null
                || isBlank(request.getRequestId())
                || isBlank(request.getTraceId())
                || isBlank(request.getInputPayloadJson())) {
            throw new BizException("Discovery AI request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private DiscoveryAiResult toDiscoveryResult(AiInvokeResult result) {
        if (result == null) {
            return new DiscoveryAiResult(
                    null,
                    null,
                    "FAILED",
                    null,
                    null,
                    null,
                    "DISCOVERY_AI_RESULT_MISSING",
                    "Discovery AI result is missing");
        }
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
}
