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
        enrichBusinessInvokeConfig(invokeCommand);
        return invocationApplicationService.invoke(invokeCommand);
    }

    private AiInvokeCommand toInvokeCommand(PlatformAiInvokeCommand source, PlatformAiWorkerUsecaseSpec spec) {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("platform");
        command.setCapability(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.from(spec.capability()));
        command.setWorkerCapability(spec.workerCapability());
        command.setOperation(spec.operation());
        command.setWorkerPath(spec.workerPath());
        command.setContentRef(source.contentRef());
        command.setTargetObjectId(source.targetObjectId());
        command.setServiceId(source.serviceId());
        command.setServiceRole(source.serviceRole());
        command.setModelId(source.modelId());
        command.setModelName(source.modelName());
        command.setPromptVersionId(source.promptVersionId());
        command.setRequestId(source.requestId());
        command.setTraceId(source.traceId());
        command.setPromptMessagesJson(source.promptMessagesJson());
        command.setPromptVariablesJson(source.promptVariablesJson());
        command.setPromptHash(source.promptHash());
        command.setInputPayloadJson(source.inputPayloadJson());
        command.setOutputSchemaJson(source.outputSchemaJson());
        command.setStream(false);
        command.setForceJson(source.forceJson());
        command.setLocale(source.locale());
        command.setAllowFallback(source.allowFallback());
        command.setCreateCandidate(
                source.createCandidate() == null ? spec.defaultCreateCandidate() : source.createCandidate());
        return command;
    }

    private void enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return;
        }
        businessInvokeConfigResolver.resolve(command);
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
