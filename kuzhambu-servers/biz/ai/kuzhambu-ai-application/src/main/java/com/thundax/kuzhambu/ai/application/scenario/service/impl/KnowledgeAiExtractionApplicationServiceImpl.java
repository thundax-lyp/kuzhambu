package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.application.scenario.service.KnowledgeAiExtractionApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.support.KnowledgeAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.scenario.support.KnowledgeAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class KnowledgeAiExtractionApplicationServiceImpl implements KnowledgeAiExtractionApplicationService {

    private final AiWorkerInvocationApplicationService invocationApplicationService;
    private final KnowledgeAiWorkerUsecaseResolver resolver;
    private final AiBusinessInvokeConfigResolver businessInvokeConfigResolver;

    @Autowired
    public KnowledgeAiExtractionApplicationServiceImpl(
            AiWorkerInvocationApplicationService invocationApplicationService,
            KnowledgeAiWorkerUsecaseResolver resolver,
            AiBusinessInvokeConfigResolver businessInvokeConfigResolver) {
        this.invocationApplicationService = invocationApplicationService;
        this.resolver = resolver;
        this.businessInvokeConfigResolver = businessInvokeConfigResolver;
    }

    @Override
    public KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionCommand command) {
        return invoke(command, "RELATION");
    }

    @Override
    public KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionCommand command) {
        return invoke(command, "GRAPH");
    }

    @Override
    public KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionCommand command) {
        return invoke(command, "LINEAGE");
    }

    @Override
    public KnowledgeAiExtractionResult extractTags(KnowledgeAiExtractionCommand command) {
        return invoke(command, "TAG");
    }

    private KnowledgeAiExtractionResult invoke(KnowledgeAiExtractionCommand command, String taskType) {
        validateRequest(command);
        KnowledgeAiWorkerUsecaseSpec spec = resolver.resolve(taskType);
        AiInvokeCommand invokeCommand = enrichBusinessInvokeConfig(toInvokeCommand(command, spec));
        AiInvokeResult result = invocationApplicationService.stream(invokeCommand, null);
        return new KnowledgeAiExtractionResult(
                result.getCallId(),
                result.getCandidateId(),
                result.getStatus(),
                result.getCapability(),
                result.getResultFormat(),
                result.getResultPayload(),
                result.getErrorType(),
                result.getErrorMessage());
    }

    private void validateRequest(KnowledgeAiExtractionCommand command) {
        if (command == null
                || isBlank(command.sourceContentType())
                || command.sourceContentId() == null
                || command.requestId() == null
                || command.traceId() == null
                || isBlank(command.inputPayloadJson())) {
            throw new BizException("Knowledge AI extraction request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private AiInvokeCommand toInvokeCommand(KnowledgeAiExtractionCommand command, KnowledgeAiWorkerUsecaseSpec spec) {
        return new AiInvokeCommand(
                null,
                "knowledge",
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.from(spec.capability()),
                spec.workerCapability(),
                spec.operation(),
                spec.workerPath(),
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.ofNullable(
                        command.sourceContentType(), command.sourceContentId()),
                null,
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
                false,
                command.forceJson(),
                command.locale(),
                false,
                true);
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
}
