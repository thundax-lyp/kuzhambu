package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import java.util.ArrayList;
import java.util.List;

public final class KnowledgeGraphPersistenceAssembler {

    private KnowledgeGraphPersistenceAssembler() {}

    public static GraphExtractionTaskDO toObject(GraphExtractionTask entity) {
        if (entity == null) {
            return null;
        }
        GraphExtractionTaskDO dataObject = new GraphExtractionTaskDO();
        dataObject.setId(valueOf(entity.getId()));
        dataObject.setBatchJobId(entity.getBatchJobId());
        dataObject.setTaskType(entity.getTaskType());
        dataObject.setScopeType(entity.getScopeType());
        dataObject.setScopeJson(entity.getScopeJson());
        dataObject.setTriggerSource(entity.getTriggerSource());
        dataObject.setSelectionScopeJson(entity.getSelectionScopeJson());
        dataObject.setReplaceUnconfirmedOnly(entity.getReplaceUnconfirmedOnly());
        dataObject.setParentTaskId(valueOf(entity.getParentTaskId()));
        dataObject.setSourceContentType(entity.getSourceContentType());
        dataObject.setSourceContentId(entity.getSourceContentId());
        dataObject.setModelId(entity.getModelId());
        dataObject.setModelName(entity.getModelName());
        dataObject.setPromptVersionId(entity.getPromptVersionId());
        dataObject.setRequestId(entity.getRequestId());
        dataObject.setTraceId(entity.getTraceId());
        dataObject.setPromptMessagesJson(entity.getPromptMessagesJson());
        dataObject.setPromptVariablesJson(entity.getPromptVariablesJson());
        dataObject.setPromptHash(entity.getPromptHash());
        dataObject.setInputPayloadJson(entity.getInputPayloadJson());
        dataObject.setOutputSchemaJson(entity.getOutputSchemaJson());
        dataObject.setForceJson(entity.getForceJson());
        dataObject.setLocale(entity.getLocale());
        dataObject.setAiCallId(entity.getAiCallId());
        dataObject.setAiCandidateId(entity.getAiCandidateId());
        dataObject.setStatus(entity.getStatus());
        dataObject.setErrorType(entity.getErrorType());
        dataObject.setErrorMessage(entity.getErrorMessage());
        dataObject.setRequestedBy(entity.getRequestedBy());
        dataObject.setRequestedAt(entity.getRequestedAt());
        dataObject.setCompletedAt(entity.getCompletedAt());
        dataObject.setAppliedAt(entity.getAppliedAt());
        return dataObject;
    }

    public static GraphExtractionTask toDomain(GraphExtractionTaskDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        GraphExtractionTask entity = new GraphExtractionTask();
        entity.setId(GraphExtractionTaskIdCodec.toDomain(dataObject.getId()));
        entity.setBatchJobId(dataObject.getBatchJobId());
        entity.setTaskType(dataObject.getTaskType());
        entity.setScopeType(dataObject.getScopeType());
        entity.setScopeJson(dataObject.getScopeJson());
        entity.setTriggerSource(dataObject.getTriggerSource());
        entity.setSelectionScopeJson(dataObject.getSelectionScopeJson());
        entity.setReplaceUnconfirmedOnly(dataObject.getReplaceUnconfirmedOnly());
        entity.setParentTaskId(GraphExtractionTaskIdCodec.toDomain(dataObject.getParentTaskId()));
        entity.setSourceContentType(dataObject.getSourceContentType());
        entity.setSourceContentId(dataObject.getSourceContentId());
        entity.setModelId(dataObject.getModelId());
        entity.setModelName(dataObject.getModelName());
        entity.setPromptVersionId(dataObject.getPromptVersionId());
        entity.setRequestId(dataObject.getRequestId());
        entity.setTraceId(dataObject.getTraceId());
        entity.setPromptMessagesJson(dataObject.getPromptMessagesJson());
        entity.setPromptVariablesJson(dataObject.getPromptVariablesJson());
        entity.setPromptHash(dataObject.getPromptHash());
        entity.setInputPayloadJson(dataObject.getInputPayloadJson());
        entity.setOutputSchemaJson(dataObject.getOutputSchemaJson());
        entity.setForceJson(dataObject.getForceJson());
        entity.setLocale(dataObject.getLocale());
        entity.setAiCallId(dataObject.getAiCallId());
        entity.setAiCandidateId(dataObject.getAiCandidateId());
        entity.setStatus(dataObject.getStatus());
        entity.setErrorType(dataObject.getErrorType());
        entity.setErrorMessage(dataObject.getErrorMessage());
        entity.setRequestedBy(dataObject.getRequestedBy());
        entity.setRequestedAt(dataObject.getRequestedAt());
        entity.setCompletedAt(dataObject.getCompletedAt());
        entity.setAppliedAt(dataObject.getAppliedAt());
        return entity;
    }

    public static List<GraphExtractionTask> toDomainList(List<GraphExtractionTaskDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<GraphExtractionTask> entities = new ArrayList<>();
        for (GraphExtractionTaskDO dataObject : dataObjects) {
            entities.add(toDomain(dataObject));
        }
        return entities;
    }

    private static Long valueOf(GraphExtractionTaskId id) {
        return id == null ? null : id.value();
    }
}
