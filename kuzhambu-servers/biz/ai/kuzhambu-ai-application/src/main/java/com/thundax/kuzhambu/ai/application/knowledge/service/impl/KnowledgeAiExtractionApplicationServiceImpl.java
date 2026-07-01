package com.thundax.kuzhambu.ai.application.knowledge.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiWorkerModelConfigResolver;
import com.thundax.kuzhambu.ai.application.knowledge.support.KnowledgeAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.knowledge.support.KnowledgeAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.domain.knowledge.service.KnowledgeAiExtractionDomainService;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class KnowledgeAiExtractionApplicationServiceImpl implements KnowledgeAiExtractionDomainService {

    private final AiWorkerInvocationApplicationService invocationApplicationService;
    private final KnowledgeAiWorkerUsecaseResolver resolver;
    private final AiWorkerModelConfigResolver modelConfigResolver;

    @Autowired
    public KnowledgeAiExtractionApplicationServiceImpl(
            AiWorkerInvocationApplicationService invocationApplicationService,
            KnowledgeAiWorkerUsecaseResolver resolver,
            AiWorkerModelConfigResolver modelConfigResolver) {
        this.invocationApplicationService = invocationApplicationService;
        this.resolver = resolver;
        this.modelConfigResolver = modelConfigResolver;
    }

    @Override
    public KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionRequest request) {
        return invoke(request, "RELATION");
    }

    @Override
    public KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionRequest request) {
        return invoke(request, "GRAPH");
    }

    @Override
    public KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionRequest request) {
        return invoke(request, "LINEAGE");
    }

    private KnowledgeAiExtractionResult invoke(KnowledgeAiExtractionRequest request, String taskType) {
        validateRequest(request);
        KnowledgeAiWorkerUsecaseSpec spec = resolver.resolve(taskType);
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("knowledge");
        command.setCapability(spec.capability());
        command.setOperation(spec.operation());
        command.setWorkerPath(spec.workerPath());
        command.setContentType(request.getSourceContentType());
        command.setContentId(request.getSourceContentId());
        command.setServiceId(request.getServiceId());
        command.setServiceRole(request.getServiceRole());
        command.setModelId(request.getModelId());
        command.setModelName(request.getModelName());
        command.setPromptVersionId(request.getPromptVersionId());
        command.setRequestId(request.getRequestId());
        command.setTraceId(request.getTraceId());
        command.setPromptMessagesJson(request.getPromptMessagesJson());
        command.setPromptVariablesJson(request.getPromptVariablesJson());
        command.setPromptHash(request.getPromptHash());
        command.setInputPayloadJson(request.getInputPayloadJson());
        command.setOutputSchemaJson(request.getOutputSchemaJson());
        command.setForceJson(request.isForceJson());
        command.setLocale(request.getLocale());
        command.setCreateCandidate(true);
        enrichModelConfig(command);
        AiInvokeResult result = invocationApplicationService.invoke(command);
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

    private void validateRequest(KnowledgeAiExtractionRequest request) {
        if (request == null
                || isBlank(request.getSourceContentType())
                || request.getSourceContentId() == null
                || request.getModelId() == null
                || isBlank(request.getModelName())
                || isBlank(request.getRequestId())
                || isBlank(request.getTraceId())
                || isBlank(request.getPromptMessagesJson())
                || isBlank(request.getInputPayloadJson())) {
            throw new BizException("Knowledge AI extraction request is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
}
