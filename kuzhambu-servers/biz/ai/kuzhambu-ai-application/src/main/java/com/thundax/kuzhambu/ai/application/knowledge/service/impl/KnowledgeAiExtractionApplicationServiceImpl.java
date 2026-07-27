package com.thundax.kuzhambu.ai.application.knowledge.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.knowledge.service.KnowledgeAiExtractionApplicationService;
import com.thundax.kuzhambu.ai.application.knowledge.support.KnowledgeAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.knowledge.support.KnowledgeAiWorkerUsecaseSpec;
import com.thundax.kuzhambu.ai.domain.knowledge.model.entity.KnowledgeAiExtractionRecord;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionInput;
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
    public KnowledgeAiExtractionRecord extractRelations(KnowledgeAiExtractionInput input) {
        return invoke(input, "RELATION");
    }

    @Override
    public KnowledgeAiExtractionRecord extractGraph(KnowledgeAiExtractionInput input) {
        return invoke(input, "GRAPH");
    }

    @Override
    public KnowledgeAiExtractionRecord extractLineage(KnowledgeAiExtractionInput input) {
        return invoke(input, "LINEAGE");
    }

    @Override
    public KnowledgeAiExtractionRecord extractTags(KnowledgeAiExtractionInput input) {
        return invoke(input, "TAG");
    }

    private KnowledgeAiExtractionRecord invoke(KnowledgeAiExtractionInput input, String taskType) {
        validateRequest(input);
        KnowledgeAiWorkerUsecaseSpec spec = resolver.resolve(taskType);
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("knowledge");
        command.setCapability(spec.capability());
        command.setWorkerCapability(spec.workerCapability());
        command.setOperation(spec.operation());
        command.setWorkerPath(spec.workerPath());
        command.setContentType(input.getSourceContentType());
        command.setContentId(input.getSourceContentId());
        command.setServiceId(input.getServiceId());
        command.setServiceRole(input.getServiceRole());
        command.setModelId(input.getModelId());
        command.setModelName(input.getModelName());
        command.setPromptVersionId(input.getPromptVersionId());
        command.setRequestId(input.getRequestId());
        command.setTraceId(input.getTraceId());
        command.setPromptMessagesJson(input.getPromptMessagesJson());
        command.setPromptVariablesJson(input.getPromptVariablesJson());
        command.setPromptHash(input.getPromptHash());
        command.setInputPayloadJson(input.getInputPayloadJson());
        command.setOutputSchemaJson(input.getOutputSchemaJson());
        command.setForceJson(input.isForceJson());
        command.setLocale(input.getLocale());
        command.setCreateCandidate(true);
        enrichBusinessInvokeConfig(command);
        AiInvokeResult result = invocationApplicationService.invoke(command);
        return new KnowledgeAiExtractionRecord(
                result.getCallId(),
                result.getCandidateId(),
                result.getStatus(),
                result.getCapability(),
                result.getResultFormat(),
                result.getResultPayload(),
                result.getErrorType(),
                result.getErrorMessage());
    }

    private void validateRequest(KnowledgeAiExtractionInput input) {
        if (input == null
                || isBlank(input.getSourceContentType())
                || input.getSourceContentId() == null
                || isBlank(input.getRequestId())
                || isBlank(input.getTraceId())
                || isBlank(input.getInputPayloadJson())) {
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
