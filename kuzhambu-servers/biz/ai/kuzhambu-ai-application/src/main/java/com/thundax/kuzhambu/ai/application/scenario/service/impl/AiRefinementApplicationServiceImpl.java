package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.support.ClassicsAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class AiRefinementApplicationServiceImpl implements AiRefinementApplicationService {

    private static final String CAPABILITY_TRANSLATE = "CLASSICS_TRANSLATE";
    private static final String CAPABILITY_SUMMARY = "CLASSICS_SUMMARY";
    private static final String CAPABILITY_TAGS = "CLASSICS_TAG_EXTRACT";
    private static final String CAPABILITY_QA = "CLASSICS_QA";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "CLASSICS_IMAGE_DESCRIBE";
    private static final String CAPABILITY_FUSION = "CLASSICS_IMAGE_PROMPT_FUSION";
    private static final String CAPABILITY_IMAGE_GEN = "CLASSICS_IMAGE_GENERATE";
    private static final String CAPABILITY_VISUAL = "CLASSICS_VISUAL_DESCRIBE";
    private static final String CAPABILITY_SPLIT = "CLASSICS_SPLIT";

    private final AiWorkerInvocationApplicationService invocationApplicationService;
    private final ClassicsAiWorkerUsecaseResolver classicsAiWorkerUsecaseResolver;
    private final AiBusinessInvokeConfigResolver businessInvokeConfigResolver;

    @Autowired
    public AiRefinementApplicationServiceImpl(
            AiWorkerInvocationApplicationService invocationApplicationService,
            ClassicsAiWorkerUsecaseResolver classicsAiWorkerUsecaseResolver,
            AiBusinessInvokeConfigResolver businessInvokeConfigResolver) {
        this.invocationApplicationService = invocationApplicationService;
        this.classicsAiWorkerUsecaseResolver = classicsAiWorkerUsecaseResolver;
        this.businessInvokeConfigResolver = businessInvokeConfigResolver;
    }

    @Override
    public AiRefinementRequestCommand snapshotInvokeConfig(AiRefinementRequestCommand command) {
        return toResolvedCommand(command, prepareInvokeCommand(command, command.capability()));
    }

    @Override
    public void validateSnapshotInvokeConfig(AiRefinementRequestCommand command) {
        validateCommand(command);
        businessInvokeConfigResolver.validatePromptVersionEnabled(toInvokeCommand(command, command.capability()));
    }

    @Override
    public AiCandidateResult translate(AiRefinementRequestCommand command) {
        return invokeCandidate(command, CAPABILITY_TRANSLATE);
    }

    @Override
    public AiCandidateResult summarize(AiRefinementRequestCommand command) {
        return invokeCandidate(command, CAPABILITY_SUMMARY);
    }

    @Override
    public AiCandidateResult generateTags(AiRefinementRequestCommand command) {
        return invokeCandidate(command, CAPABILITY_TAGS);
    }

    @Override
    public AiCandidateResult generateQa(AiRefinementRequestCommand command) {
        return invokeCandidate(command, CAPABILITY_QA);
    }

    @Override
    public AiCandidateResult analyzeImage(AiRefinementRequestCommand command) {
        return analyzeImage(command, event -> {});
    }

    @Override
    public AiCandidateResult analyzeImage(
            AiRefinementRequestCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        return invokeCandidate(command, CAPABILITY_IMAGE_ANALYSIS, eventConsumer);
    }

    @Override
    public AiCandidateResult fuseVisualContext(AiRefinementRequestCommand command) {
        return invokeCandidate(command, CAPABILITY_FUSION);
    }

    @Override
    public AiCandidateResult generateImage(AiRefinementRequestCommand command) {
        return generateImage(command, event -> {});
    }

    @Override
    public AiCandidateResult generateImage(
            AiRefinementRequestCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        return invokeCandidate(command, CAPABILITY_IMAGE_GEN, eventConsumer);
    }

    @Override
    public AiCandidateResult describeVisual(AiRefinementRequestCommand command) {
        return invokeCandidate(command, CAPABILITY_VISUAL);
    }

    @Override
    public AiCandidateResult splitEntry(AiRefinementRequestCommand command) {
        return invokeCandidate(command, CAPABILITY_SPLIT);
    }

    private AiCandidateResult invokeCandidate(AiRefinementRequestCommand command, String capability) {
        return invokeCandidate(command, AiBusinessCapability.from(capability), event -> {});
    }

    private AiCandidateResult invokeCandidate(
            AiRefinementRequestCommand command, String capability, Consumer<AiStreamEventResult> eventConsumer) {
        return invokeCandidate(command, AiBusinessCapability.from(capability), eventConsumer);
    }

    private AiCandidateResult invokeCandidate(
            AiRefinementRequestCommand command,
            AiBusinessCapability capability,
            Consumer<AiStreamEventResult> eventConsumer) {
        AiInvokeCommand invokeCommand = prepareInvokeCommand(command, capability);
        if (isStreamingCapability(capability)) {
            invokeCommand = withStream(invokeCommand, true);
        }
        AiInvokeResult result = isStreamingCapability(capability)
                ? invocationApplicationService.stream(
                        invokeCommand, eventConsumer == null ? event -> {} : eventConsumer)
                : invocationApplicationService.invoke(invokeCommand);
        return AiCandidateResult.from(result);
    }

    private AiInvokeCommand prepareInvokeCommand(AiRefinementRequestCommand command, AiBusinessCapability capability) {
        validateCommand(command);
        var spec = classicsAiWorkerUsecaseResolver.resolve(command.contentRef().contentType(), capability.value());
        AiInvokeCommand invokeCommand = toInvokeCommand(command, capability);
        invokeCommand = withWorkerUsecase(invokeCommand, spec.workerCapability(), spec.operation(), spec.workerPath());
        if (!hasResolvedInvokeConfig(invokeCommand)) {
            invokeCommand = enrichBusinessInvokeConfig(invokeCommand);
        }
        return invokeCommand;
    }

    private AiInvokeCommand toInvokeCommand(AiRefinementRequestCommand source, AiBusinessCapability capability) {
        return new AiInvokeCommand(
                source.batchId(),
                source.scope(),
                capability,
                null,
                source.operation(),
                null,
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
                false,
                true);
    }

    private boolean isStreamingCapability(AiBusinessCapability capability) {
        return AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE == capability
                || AiBusinessCapability.CLASSICS_IMAGE_GENERATE == capability;
    }

    private AiInvokeCommand enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return command;
        }
        return withResolvedConfig(command, businessInvokeConfigResolver.resolveConfig(command));
    }

    private boolean hasResolvedInvokeConfig(AiInvokeCommand command) {
        return command != null
                && command.modelId() != null
                && command.promptVersionId() != null
                && !isBlank(command.promptMessagesJson())
                && !isBlank(command.promptVariablesJson());
    }

    private AiRefinementRequestCommand toResolvedCommand(
            AiRefinementRequestCommand command, AiInvokeCommand invokeCommand) {
        return new AiRefinementRequestCommand(
                command.batchId(),
                command.capability(),
                command.scope(),
                command.operation(),
                command.contentRef(),
                command.targetObjectId(),
                invokeCommand.serviceId(),
                invokeCommand.serviceRole(),
                invokeCommand.modelId(),
                invokeCommand.modelName(),
                invokeCommand.promptVersionId(),
                command.requestId(),
                command.traceId(),
                invokeCommand.promptMessagesJson(),
                invokeCommand.promptVariablesJson(),
                command.promptHash(),
                command.inputPayloadJson(),
                invokeCommand.outputSchemaJson(),
                command.forceJson(),
                command.locale());
    }

    private AiInvokeCommand withWorkerUsecase(
            AiInvokeCommand command, String workerCapability, String operation, String workerPath) {
        return new AiInvokeCommand(
                command.batchId(),
                command.scope(),
                command.capability(),
                workerCapability,
                operation,
                workerPath,
                command.contentRef(),
                command.targetObjectId(),
                command.serviceId(),
                command.serviceRole(),
                command.modelId(),
                command.modelName(),
                command.promptVersionId(),
                command.requestId(),
                command.traceId(),
                command.promptMessagesJson(),
                command.promptVariablesJson(),
                command.promptHash(),
                command.inputPayloadJson(),
                command.outputSchemaJson(),
                command.stream(),
                command.forceJson(),
                command.locale(),
                command.allowFallback(),
                command.createCandidate());
    }

    private AiInvokeCommand withStream(AiInvokeCommand command, boolean stream) {
        return new AiInvokeCommand(
                command.batchId(),
                command.scope(),
                command.capability(),
                command.workerCapability(),
                command.operation(),
                command.workerPath(),
                command.contentRef(),
                command.targetObjectId(),
                command.serviceId(),
                command.serviceRole(),
                command.modelId(),
                command.modelName(),
                command.promptVersionId(),
                command.requestId(),
                command.traceId(),
                command.promptMessagesJson(),
                command.promptVariablesJson(),
                command.promptHash(),
                command.inputPayloadJson(),
                command.outputSchemaJson(),
                stream,
                command.forceJson(),
                command.locale(),
                command.allowFallback(),
                command.createCandidate());
    }

    private AiInvokeCommand withResolvedConfig(
            AiInvokeCommand command, AiBusinessInvokeConfigResolver.ResolvedBusinessInvokeConfig resolved) {
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

    private void validateCommand(AiRefinementRequestCommand command) {
        if (command == null
                || isBlank(command.scope())
                || command.requestId() == null
                || command.traceId() == null
                || command.contentRef() == null
                || isBlank(command.contentRef().contentType())
                || command.contentRef().contentId() == null
                || isBlank(command.inputPayloadJson())) {
            throw new BizException("AI refinement request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
