package com.thundax.kuzhambu.ai.application.platform.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiWorkerModelConfigResolver;
import com.thundax.kuzhambu.ai.application.platform.command.PlatformAiInvokeCommand;
import com.thundax.kuzhambu.ai.application.platform.service.PlatformAiApplicationService;
import com.thundax.kuzhambu.ai.application.platform.support.PlatformAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.platform.support.PlatformAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class PlatformAiApplicationServiceImpl implements PlatformAiApplicationService {

    private final AiWorkerInvocationApplicationService invocationApplicationService;
    private final PlatformAiWorkerUsecaseResolver resolver;
    private final AiWorkerModelConfigResolver modelConfigResolver;

    public PlatformAiApplicationServiceImpl(
            AiWorkerInvocationApplicationService invocationApplicationService,
            PlatformAiWorkerUsecaseResolver resolver,
            AiWorkerModelConfigResolver modelConfigResolver) {
        this.invocationApplicationService = invocationApplicationService;
        this.resolver = resolver;
        this.modelConfigResolver = modelConfigResolver;
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
        AiInvokeCommand invokeCommand = command.toInvokeCommand(spec);
        enrichModelConfig(invokeCommand);
        return invocationApplicationService.invoke(invokeCommand);
    }

    private void enrichModelConfig(AiInvokeCommand command) {
        if (modelConfigResolver == null || command == null) {
            return;
        }
        if (isBlank(command.getServiceRole()) && command.getServiceId() == null) {
            return;
        }
        var resolved = modelConfigResolver.resolve(command);
        command.setServiceRole(resolved.serviceRole());
        command.setModelName(resolved.modelName());
    }

    private void validateCommand(PlatformAiInvokeCommand command) {
        if (command == null
                || command.getModelId() == null
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || isBlank(command.getPromptMessagesJson())
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("Platform AI invoke command is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
