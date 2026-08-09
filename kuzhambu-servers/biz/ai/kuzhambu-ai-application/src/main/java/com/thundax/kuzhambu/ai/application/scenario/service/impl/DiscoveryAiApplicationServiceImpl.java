package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeContext;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeModelConfig;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeOptions;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePayload;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePrompt;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTarget;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTrace;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeWorkerRoute;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.DiscoveryAiCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.DiscoveryAiInvokeResult;
import com.thundax.kuzhambu.ai.application.scenario.service.DiscoveryAiApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.support.DiscoveryAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.scenario.support.DiscoveryAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
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
        invokeCommand = enrichBusinessInvokeConfig(invokeCommand);
        AiInvokeResult result = spec.stream()
                ? invocationApplicationService.stream(
                        invokeCommand, eventConsumer == null ? event -> {} : eventConsumer)
                : invocationApplicationService.invoke(invokeCommand);
        return toDiscoveryInvokeResult(result);
    }

    private AiInvokeCommand toInvokeCommand(DiscoveryAiCommand command, DiscoveryAiWorkerUsecaseSpec spec) {
        return new AiInvokeCommand(
                new AiInvokeContext(null, SCOPE_DISCOVERY, AiBusinessCapability.from(spec.capability())),
                new AiInvokeWorkerRoute(spec.workerCapability(), spec.operation(), spec.workerPath()),
                new AiInvokeTarget(AiContentRef.ofNullable(CONTENT_TYPE_DISCOVERY_QUERY, null), null),
                new AiInvokeModelConfig(
                        command.serviceId(), command.serviceRole(), command.modelId(), command.modelName()),
                new AiInvokeTrace(command.requestId(), command.traceId()),
                new AiInvokePrompt(
                        command.promptVersionId(),
                        command.promptMessagesJson(),
                        command.promptVariablesJson(),
                        command.promptHash()),
                new AiInvokePayload(command.inputPayloadJson(), command.outputSchemaJson()),
                new AiInvokeOptions(
                        spec.stream() || command.stream(), command.forceJson(), command.locale(), false, false));
    }

    private AiInvokeCommand enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return command;
        }
        var resolved = businessInvokeConfigResolver.resolveConfig(command);
        return new AiInvokeCommand(
                command.context(),
                command.route(),
                command.target(),
                new AiInvokeModelConfig(
                        resolved.serviceId(), resolved.serviceRole(), resolved.modelId(), resolved.modelName()),
                command.trace(),
                new AiInvokePrompt(
                        resolved.promptVersionId(),
                        resolved.promptMessagesJson(),
                        resolved.promptVariablesJson(),
                        command.prompt().promptHash()),
                new AiInvokePayload(command.payload().inputPayloadJson(), resolved.outputSchemaJson()),
                command.options());
    }

    private void validateCommand(DiscoveryAiCommand command) {
        if (command == null
                || command.requestId() == null
                || command.traceId() == null
                || isBlank(command.inputPayloadJson())) {
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
                    AiInvocationStatus.FAILED,
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
