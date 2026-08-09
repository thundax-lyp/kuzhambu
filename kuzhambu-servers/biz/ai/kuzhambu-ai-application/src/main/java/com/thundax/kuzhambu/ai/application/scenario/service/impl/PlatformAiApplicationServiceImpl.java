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
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand;
import com.thundax.kuzhambu.ai.application.scenario.service.PlatformAiApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.support.PlatformAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.scenario.support.PlatformAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
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
                new AiInvokeContext(null, "platform", AiBusinessCapability.from(spec.capability())),
                new AiInvokeWorkerRoute(spec.workerCapability(), spec.operation(), spec.workerPath()),
                new AiInvokeTarget(source.contentRef(), source.targetObjectId()),
                new AiInvokeModelConfig(source.serviceId(), source.serviceRole(), source.modelId(), source.modelName()),
                new AiInvokeTrace(source.requestId(), source.traceId()),
                new AiInvokePrompt(
                        source.promptVersionId(),
                        source.promptMessagesJson(),
                        source.promptVariablesJson(),
                        source.promptHash()),
                new AiInvokePayload(source.inputPayloadJson(), source.outputSchemaJson()),
                new AiInvokeOptions(
                        false,
                        source.forceJson(),
                        source.locale(),
                        source.allowFallback(),
                        source.createCandidate() == null ? spec.defaultCreateCandidate() : source.createCandidate()));
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
