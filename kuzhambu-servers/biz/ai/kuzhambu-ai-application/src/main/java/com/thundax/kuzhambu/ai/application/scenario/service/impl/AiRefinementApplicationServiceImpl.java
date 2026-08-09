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
            invokeCommand.setStream(true);
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
        invokeCommand.setOperation(spec.operation());
        invokeCommand.setWorkerPath(spec.workerPath());
        invokeCommand.setWorkerCapability(spec.workerCapability());
        if (!hasResolvedInvokeConfig(invokeCommand)) {
            enrichBusinessInvokeConfig(invokeCommand);
        }
        return invokeCommand;
    }

    private AiInvokeCommand toInvokeCommand(AiRefinementRequestCommand source, AiBusinessCapability capability) {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setBatchId(source.batchId());
        command.setScope(source.scope());
        command.setCapability(capability);
        command.setOperation(source.operation());
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
        command.setForceJson(source.forceJson());
        command.setLocale(source.locale());
        command.setCreateCandidate(true);
        return command;
    }

    private boolean isStreamingCapability(AiBusinessCapability capability) {
        return AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE == capability
                || AiBusinessCapability.CLASSICS_IMAGE_GENERATE == capability;
    }

    private void enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return;
        }
        businessInvokeConfigResolver.resolve(command);
    }

    private boolean hasResolvedInvokeConfig(AiInvokeCommand command) {
        return command != null
                && command.getModelId() != null
                && command.getPromptVersionId() != null
                && !isBlank(command.getPromptMessagesJson())
                && !isBlank(command.getPromptVariablesJson());
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
                invokeCommand.getServiceId(),
                invokeCommand.getServiceRole(),
                invokeCommand.getModelId(),
                invokeCommand.getModelName(),
                invokeCommand.getPromptVersionId(),
                command.requestId(),
                command.traceId(),
                invokeCommand.getPromptMessagesJson(),
                invokeCommand.getPromptVariablesJson(),
                command.promptHash(),
                command.inputPayloadJson(),
                invokeCommand.getOutputSchemaJson(),
                command.forceJson(),
                command.locale());
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
