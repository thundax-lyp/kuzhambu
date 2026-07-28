package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.DiscoveryAiCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.DiscoveryAiInvokeResult;
import com.thundax.kuzhambu.ai.application.scenario.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.support.DiscoveryAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.scenario.support.DiscoveryAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class DiscoveryAiApplicationServiceImpl implements DiscoveryAiApplicationService {

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
    public DiscoveryAiInvokeResult understandQuery(DiscoveryAiCommand command) {
        return invoke(command, "DISCOVERY_QUERY_UNDERSTANDING", null);
    }

    @Override
    public DiscoveryAiInvokeResult rewriteQuery(DiscoveryAiCommand command) {
        return invoke(command, "DISCOVERY_QUERY_REWRITE", null);
    }

    @Override
    public DiscoveryAiInvokeResult generateAnswer(DiscoveryAiCommand command) {
        return invoke(command, "DISCOVERY_ANSWER_GENERATION", null);
    }

    @Override
    public DiscoveryAiInvokeResult streamAnswer(DiscoveryAiCommand command) {
        return streamAnswer(command, event -> {});
    }

    @Override
    public DiscoveryAiInvokeResult streamAnswer(
            DiscoveryAiCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        return invoke(command, "DISCOVERY_ANSWER_GENERATION_STREAM", eventConsumer);
    }

    private DiscoveryAiInvokeResult invoke(
            DiscoveryAiCommand command, String usecase, Consumer<AiStreamEventResult> eventConsumer) {
        validateCommand(command);
        DiscoveryAiWorkerUsecaseSpec spec = resolver.resolve(usecase);
        AiInvokeCommand invokeCommand = toInvokeCommand(command, spec);
        enrichBusinessInvokeConfig(invokeCommand);
        AiInvokeResult result = spec.stream()
                ? invocationApplicationService.stream(
                        invokeCommand, eventConsumer == null ? event -> {} : eventConsumer)
                : invocationApplicationService.invoke(invokeCommand);
        return toDiscoveryInvokeResult(result);
    }

    private AiInvokeCommand toInvokeCommand(DiscoveryAiCommand command, DiscoveryAiWorkerUsecaseSpec spec) {
        AiInvokeCommand invokeCommand = new AiInvokeCommand();
        invokeCommand.setScope(SCOPE_DISCOVERY);
        invokeCommand.setCapability(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias(spec.capability()));
        invokeCommand.setWorkerCapability(spec.workerCapability());
        invokeCommand.setOperation(spec.operation());
        invokeCommand.setWorkerPath(spec.workerPath());
        invokeCommand.setContentType(CONTENT_TYPE_DISCOVERY_QUERY);
        invokeCommand.setServiceId(command.getServiceId());
        invokeCommand.setServiceRole(command.getServiceRole());
        invokeCommand.setModelId(command.getModelId());
        invokeCommand.setModelName(command.getModelName());
        invokeCommand.setPromptVersionId(command.getPromptVersionId());
        invokeCommand.setRequestId(command.getRequestId());
        invokeCommand.setTraceId(command.getTraceId());
        invokeCommand.setPromptMessagesJson(command.getPromptMessagesJson());
        invokeCommand.setPromptVariablesJson(command.getPromptVariablesJson());
        invokeCommand.setPromptHash(command.getPromptHash());
        invokeCommand.setInputPayloadJson(command.getInputPayloadJson());
        invokeCommand.setOutputSchemaJson(command.getOutputSchemaJson());
        invokeCommand.setStream(spec.stream() || command.isStream());
        invokeCommand.setForceJson(command.isForceJson());
        invokeCommand.setLocale(command.getLocale());
        invokeCommand.setCreateCandidate(false);
        return invokeCommand;
    }

    private void enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return;
        }
        businessInvokeConfigResolver.resolve(command);
    }

    private void validateCommand(DiscoveryAiCommand command) {
        if (command == null
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("Discovery AI command is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private DiscoveryAiInvokeResult toDiscoveryInvokeResult(AiInvokeResult result) {
        if (result == null) {
            return new DiscoveryAiInvokeResult(
                    null,
                    null,
                    "FAILED",
                    null,
                    null,
                    null,
                    "DISCOVERY_AI_RESULT_MISSING",
                    "Discovery AI result is missing");
        }
        return new DiscoveryAiInvokeResult(
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
