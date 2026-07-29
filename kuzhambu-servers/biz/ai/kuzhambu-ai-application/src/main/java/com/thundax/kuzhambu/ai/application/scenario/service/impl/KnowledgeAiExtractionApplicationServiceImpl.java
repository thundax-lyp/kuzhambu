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
        AiInvokeCommand invokeCommand = new AiInvokeCommand();
        invokeCommand.setScope("knowledge");
        invokeCommand.setCapability(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias(spec.capability()));
        invokeCommand.setWorkerCapability(spec.workerCapability());
        invokeCommand.setOperation(spec.operation());
        invokeCommand.setWorkerPath(spec.workerPath());
        invokeCommand.setContentRef(com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.ofNullable(
                command.getSourceContentType(), command.getSourceContentId()));
        invokeCommand.setServiceId(command.getServiceId());
        invokeCommand.setServiceRole(command.getServiceRole());
        invokeCommand.setModelId(command.getModelId());
        invokeCommand.setModelName(command.getModelName());
        invokeCommand.setPromptVersionId(command.getPromptVersionId());
        invokeCommand.setRequestId(command.getRequestId());
        invokeCommand.setTraceId(command.getTraceId());
        invokeCommand.setPromptMessagesJson(command.getPromptMessagesJson());
        invokeCommand.setPromptVariablesJson(command.getPromptVariablesJson());
        invokeCommand.setPromptHash(command.getPromptHash());
        invokeCommand.setInputPayloadJson(command.getInputPayloadJson());
        invokeCommand.setOutputSchemaJson(command.getOutputSchemaJson());
        invokeCommand.setForceJson(command.isForceJson());
        invokeCommand.setLocale(command.getLocale());
        invokeCommand.setCreateCandidate(true);
        enrichBusinessInvokeConfig(invokeCommand);
        AiInvokeResult result = invocationApplicationService.invoke(invokeCommand);
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
                || isBlank(command.getSourceContentType())
                || command.getSourceContentId() == null
                || command.getRequestId() == null
                || command.getTraceId() == null
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("Knowledge AI extraction request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void enrichBusinessInvokeConfig(AiInvokeCommand command) {
        if (businessInvokeConfigResolver == null || command == null) {
            return;
        }
        businessInvokeConfigResolver.resolve(command);
    }
}
