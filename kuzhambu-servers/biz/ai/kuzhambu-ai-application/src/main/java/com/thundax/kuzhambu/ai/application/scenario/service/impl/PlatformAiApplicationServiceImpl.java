package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand;
import com.thundax.kuzhambu.ai.application.scenario.service.PlatformAiApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.support.PlatformAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.scenario.support.PlatformAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class PlatformAiApplicationServiceImpl implements PlatformAiApplicationService {

    private final AiWorkerInvocationApplicationService invocationApplicationService;
    private final PlatformAiWorkerUsecaseResolver resolver;
    private final AiBusinessInvokeConfigResolver businessInvokeConfigResolver;

    public PlatformAiApplicationServiceImpl(
            AiWorkerInvocationApplicationService invocationApplicationService,
            PlatformAiWorkerUsecaseResolver resolver,
            AiBusinessInvokeConfigResolver businessInvokeConfigResolver) {
        this.invocationApplicationService = invocationApplicationService;
        this.resolver = resolver;
        this.businessInvokeConfigResolver = businessInvokeConfigResolver;
    }

    @Override
    public AiInvokeResult buildPromptSuggestion(PlatformAiInvokeCommand command) {
        return invoke(command, "PLATFORM_PROMPT_SUGGESTION");
    }

    @Override
    public AiInvokeResult summarizeVersion(PlatformAiInvokeCommand command) {
        return invoke(command, "PLATFORM_VERSION_SUMMARY");
    }

    private AiInvokeResult invoke(PlatformAiInvokeCommand command, String usecase) {
        validateCommand(command);
        PlatformAiWorkerUsecaseSpec spec = resolver.resolve(usecase);
        AiInvokeCommand invokeCommand = toInvokeCommand(command, spec);
        invokeCommand = enrichBusinessInvokeConfig(invokeCommand);
        return invocationApplicationService.invoke(invokeCommand);
    }

    private AiInvokeCommand toInvokeCommand(PlatformAiInvokeCommand source, PlatformAiWorkerUsecaseSpec spec) {
        return new AiInvokeCommand(
                null,
                "platform",
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.from(spec.capability()),
                spec.workerCapability(),
                spec.operation(),
                spec.workerPath(),
                source.contentRef(),
                source.targetObjectId(),
                source.serviceId(),
                source.serviceRole(),
                source.modelId(),
                source.modelName(),
                source.promptVersionId(),
                source.requestId(),
                source.traceId(),
                source.promptMessagesJson(),
                source.promptVariablesJson(),
                source.promptHash(),
                source.inputPayloadJson(),
                source.outputSchemaJson(),
                false,
                source.forceJson(),
                source.locale(),
                source.allowFallback(),
                source.createCandidate() == null ? spec.defaultCreateCandidate() : source.createCandidate());
    }

    private AiInvokeCommand enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return command;
        }
        var resolved = businessInvokeConfigResolver.resolveConfig(command);
        return new AiInvokeCommand(
                command.batchId(),
                command.scope(),
                command.capability(),
                command.workerCapability(),
                command.operation(),
                command.workerPath(),
                command.contentRef(),
                command.targetObjectId(),
                resolved.serviceId(),
                resolved.serviceRole(),
                resolved.modelId(),
                resolved.modelName(),
                resolved.promptVersionId(),
                command.requestId(),
                command.traceId(),
                resolved.promptMessagesJson(),
                resolved.promptVariablesJson(),
                command.promptHash(),
                command.inputPayloadJson(),
                resolved.outputSchemaJson(),
                command.stream(),
                command.forceJson(),
                command.locale(),
                command.allowFallback(),
                command.createCandidate());
    }

    private void validateCommand(PlatformAiInvokeCommand command) {
        if (command == null
                || command.requestId() == null
                || command.traceId() == null
                || isBlank(command.inputPayloadJson())) {
            throw new BizException("Platform AI invoke command is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
