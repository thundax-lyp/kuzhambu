package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.application.refinement.support.ClassicsAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class AiRefinementApplicationServiceImpl implements AiRefinementApplicationService {

    private static final String CAPABILITY_TRANSLATE = "classics_translate";
    private static final String CAPABILITY_SUMMARY = "classics_summary";
    private static final String CAPABILITY_TAGS = "classics_tags";
    private static final String CAPABILITY_QA = "classics_qa";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "classics_image_describe";
    private static final String CAPABILITY_FUSION = "classics_image_prompt_fusion";
    private static final String CAPABILITY_IMAGE_GEN = "classics_image_generate";
    private static final String CAPABILITY_VISUAL = "classics_visual_describe";
    private static final String CAPABILITY_SPLIT = "classics_split";

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
        return invokeCandidate(command, capability, event -> {});
    }

    private AiCandidateResult invokeCandidate(
            AiRefinementRequestCommand command, String capability, Consumer<AiStreamEventResult> eventConsumer) {
        validateCommand(command);
        var spec = classicsAiWorkerUsecaseResolver.resolve(command.getContentType(), capability);
        AiInvokeCommand invokeCommand = command.toInvokeCommand(capability);
        invokeCommand.setOperation(spec.operation());
        invokeCommand.setWorkerPath(spec.workerPath());
        enrichBusinessInvokeConfig(invokeCommand);
        copyResolvedInvokeConfig(command, invokeCommand);
        if (CAPABILITY_IMAGE_ANALYSIS.equals(capability) || CAPABILITY_IMAGE_GEN.equals(capability)) {
            invokeCommand.setStream(true);
        }
        AiInvokeResult result = CAPABILITY_IMAGE_ANALYSIS.equals(capability) || CAPABILITY_IMAGE_GEN.equals(capability)
                ? invocationApplicationService.stream(
                        invokeCommand, eventConsumer == null ? event -> {} : eventConsumer)
                : invocationApplicationService.invoke(invokeCommand);
        return AiCandidateResult.from(result);
    }

    private void enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return;
        }
        businessInvokeConfigResolver.resolve(command);
    }

    private void copyResolvedInvokeConfig(AiRefinementRequestCommand command, AiInvokeCommand invokeCommand) {
        command.setServiceId(invokeCommand.getServiceId());
        command.setServiceRole(invokeCommand.getServiceRole());
        command.setModelId(invokeCommand.getModelId());
        command.setModelName(invokeCommand.getModelName());
        command.setPromptVersionId(invokeCommand.getPromptVersionId());
        command.setPromptMessagesJson(invokeCommand.getPromptMessagesJson());
        command.setPromptVariablesJson(invokeCommand.getPromptVariablesJson());
        command.setOutputSchemaJson(invokeCommand.getOutputSchemaJson());
    }

    private void validateCommand(AiRefinementRequestCommand command) {
        if (command == null
                || isBlank(command.getScope())
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || isBlank(command.getContentType())
                || command.getContentId() == null
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("AI refinement request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
