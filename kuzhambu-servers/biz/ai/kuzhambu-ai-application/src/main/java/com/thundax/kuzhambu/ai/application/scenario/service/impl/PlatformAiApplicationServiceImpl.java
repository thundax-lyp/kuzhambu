package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand;
import com.thundax.kuzhambu.ai.application.scenario.service.PlatformAiApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.support.PlatformAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.scenario.support.PlatformAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
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
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias(spec.capability()));
        command.setWorkerCapability(spec.workerCapability());
        command.setOperation(spec.operation());
        command.setWorkerPath(spec.workerPath());
        command.setContentRef(com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.ofNullable(
                source.getContentType(), source.getContentId()));
        command.setTargetObjectId(
                com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec.toDomain(source.getObjectId()));
        command.setServiceId(source.getServiceId());
        command.setServiceRole(source.getServiceRole());
        command.setModelId(AiModelIdCodec.toDomain(source.getModelId()));
        command.setModelName(AiModelNameCodec.toDomain(source.getModelName()));
        command.setPromptVersionId(PromptVersionIdCodec.toDomain(source.getPromptVersionId()));
        command.setRequestId(RequestIdCodec.toDomain(source.getRequestId()));
        command.setTraceId(TraceIdCodec.toDomain(source.getTraceId()));
        command.setPromptMessagesJson(source.getPromptMessagesJson());
        command.setPromptVariablesJson(source.getPromptVariablesJson());
        command.setPromptHash(source.getPromptHash());
        command.setInputPayloadJson(source.getInputPayloadJson());
        command.setOutputSchemaJson(source.getOutputSchemaJson());
        command.setStream(false);
        command.setForceJson(source.isForceJson());
        command.setLocale(source.getLocale());
        command.setAllowFallback(source.isAllowFallback());
        command.setCreateCandidate(
                source.getCreateCandidate() == null ? spec.defaultCreateCandidate() : source.getCreateCandidate());
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
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("Platform AI invoke command is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
