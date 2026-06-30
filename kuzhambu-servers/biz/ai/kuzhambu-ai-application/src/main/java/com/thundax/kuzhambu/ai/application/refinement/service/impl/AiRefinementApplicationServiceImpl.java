package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.application.refinement.support.ClassicsAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class AiRefinementApplicationServiceImpl implements AiRefinementApplicationService {

    private static final String CAPABILITY_TRANSLATE = "translate";
    private static final String CAPABILITY_SUMMARY = "summary";
    private static final String CAPABILITY_TAGS = "tags";
    private static final String CAPABILITY_QA = "qa";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "image_analysis";
    private static final String CAPABILITY_VISUAL = "visual";
    private static final String CAPABILITY_SPLIT = "split";

    private final AiWorkerInvocationApplicationService invocationApplicationService;
    private final ClassicsAiWorkerUsecaseResolver classicsAiWorkerUsecaseResolver;

    public AiRefinementApplicationServiceImpl(
            AiWorkerInvocationApplicationService invocationApplicationService,
            ClassicsAiWorkerUsecaseResolver classicsAiWorkerUsecaseResolver) {
        this.invocationApplicationService = invocationApplicationService;
        this.classicsAiWorkerUsecaseResolver = classicsAiWorkerUsecaseResolver;
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
        return invokeCandidate(command, CAPABILITY_IMAGE_ANALYSIS);
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
        validateCommand(command);
        var spec = classicsAiWorkerUsecaseResolver.resolve(command.getContentType(), capability);
        AiInvokeCommand invokeCommand = command.toInvokeCommand(capability);
        invokeCommand.setOperation(spec.operation());
        invokeCommand.setWorkerPath(spec.workerPath());
        if (CAPABILITY_IMAGE_ANALYSIS.equals(capability)) {
            invokeCommand.setStream(true);
        }
        AiInvokeResult result = CAPABILITY_IMAGE_ANALYSIS.equals(capability)
                ? invocationApplicationService.stream(invokeCommand, event -> {})
                : invocationApplicationService.invoke(invokeCommand);
        return AiCandidateResult.from(result);
    }

    private void validateCommand(AiRefinementRequestCommand command) {
        if (command == null
                || isBlank(command.getScope())
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || isBlank(command.getContentType())
                || command.getContentId() == null
                || command.getModelId() == null
                || isBlank(command.getModelName())
                || isBlank(command.getPromptMessagesJson())
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("AI refinement request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
