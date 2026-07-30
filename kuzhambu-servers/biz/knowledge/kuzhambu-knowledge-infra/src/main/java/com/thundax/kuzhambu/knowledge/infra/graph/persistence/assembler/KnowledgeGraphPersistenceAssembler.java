package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCallIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionBatchJobIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionModelIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionModelNameCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionPromptVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionRequestIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionRequesterIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTraceIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class KnowledgeGraphPersistenceAssembler {

    private KnowledgeGraphPersistenceAssembler() {}

    public static GraphExtractionTaskDO toObject(GraphExtractionTask entity) {
        if (entity == null) {
            return null;
        }
        GraphExtractionTaskDO dataObject = new GraphExtractionTaskDO();
        dataObject.setId(GraphExtractionTaskIdCodec.toValue(entity.getId()));
        dataObject.setBatchJobId(GraphExtractionBatchJobIdCodec.toValue(entity.getBatchJobId()));
        dataObject.setTaskType(
                entity.getTaskType() == null ? null : entity.getTaskType().value());
        dataObject.setScopeType(entity.getScopeType());
        dataObject.setScopeJson(entity.getScopeJson());
        dataObject.setTriggerSource(entity.getTriggerSource());
        dataObject.setSelectionScopeJson(entity.getSelectionScopeJson());
        dataObject.setReplaceUnconfirmedOnly(entity.getReplaceUnconfirmedOnly());
        dataObject.setParentTaskId(GraphExtractionTaskIdCodec.toValue(entity.getParentTaskId()));
        dataObject.setSourceContentType(entity.getSourceContentType());
        dataObject.setSourceContentId(GraphExtractionSourceContentIdCodec.toValue(entity.getSourceContentId()));
        dataObject.setModelId(GraphExtractionModelIdCodec.toValue(entity.getModelId()));
        dataObject.setModelName(GraphExtractionModelNameCodec.toValue(entity.getModelName()));
        dataObject.setPromptVersionId(GraphExtractionPromptVersionIdCodec.toValue(entity.getPromptVersionId()));
        dataObject.setRequestId(GraphExtractionRequestIdCodec.toValue(entity.getRequestId()));
        dataObject.setTraceId(GraphExtractionTraceIdCodec.toValue(entity.getTraceId()));
        dataObject.setPromptMessagesJson(entity.getPromptMessagesJson());
        dataObject.setPromptVariablesJson(entity.getPromptVariablesJson());
        dataObject.setPromptHash(entity.getPromptHash());
        dataObject.setInputPayloadJson(entity.getInputPayloadJson());
        dataObject.setOutputSchemaJson(entity.getOutputSchemaJson());
        dataObject.setForceJson(entity.getForceJson());
        dataObject.setLocale(entity.getLocale());
        dataObject.setAiCallId(GraphExtractionAiCallIdCodec.toValue(entity.getAiCallId()));
        dataObject.setAiCandidateId(GraphExtractionAiCandidateIdCodec.toValue(entity.getAiCandidateId()));
        dataObject.setStatus(
                entity.getStatus() == null ? null : entity.getStatus().value());
        dataObject.setErrorType(entity.getErrorType());
        dataObject.setErrorMessage(entity.getErrorMessage());
        dataObject.setRequestedBy(GraphExtractionRequesterIdCodec.toValue(entity.getRequestedBy()));
        dataObject.setRequestedAt(toDate(entity.getRequestedAt()));
        dataObject.setCompletedAt(toDate(entity.getCompletedAt()));
        dataObject.setAppliedAt(toDate(entity.getAppliedAt()));
        return dataObject;
    }

    public static GraphExtractionTask toDomain(GraphExtractionTaskDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        GraphExtractionTask entity = new GraphExtractionTask();
        entity.setId(GraphExtractionTaskIdCodec.toDomain(dataObject.getId()));
        entity.setBatchJobId(GraphExtractionBatchJobIdCodec.toDomain(dataObject.getBatchJobId()));
        entity.setTaskType(GraphExtractionTaskType.from(dataObject.getTaskType()));
        entity.setScopeType(dataObject.getScopeType());
        entity.setScopeJson(dataObject.getScopeJson());
        entity.setTriggerSource(dataObject.getTriggerSource());
        entity.setSelectionScopeJson(dataObject.getSelectionScopeJson());
        entity.setReplaceUnconfirmedOnly(dataObject.getReplaceUnconfirmedOnly());
        entity.setParentTaskId(GraphExtractionTaskIdCodec.toDomain(dataObject.getParentTaskId()));
        entity.setSourceContentType(dataObject.getSourceContentType());
        entity.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(dataObject.getSourceContentId()));
        entity.setModelId(GraphExtractionModelIdCodec.toDomain(dataObject.getModelId()));
        entity.setModelName(GraphExtractionModelNameCodec.toDomain(dataObject.getModelName()));
        entity.setPromptVersionId(GraphExtractionPromptVersionIdCodec.toDomain(dataObject.getPromptVersionId()));
        entity.setRequestId(GraphExtractionRequestIdCodec.toDomain(dataObject.getRequestId()));
        entity.setTraceId(GraphExtractionTraceIdCodec.toDomain(dataObject.getTraceId()));
        entity.setPromptMessagesJson(dataObject.getPromptMessagesJson());
        entity.setPromptVariablesJson(dataObject.getPromptVariablesJson());
        entity.setPromptHash(dataObject.getPromptHash());
        entity.setInputPayloadJson(dataObject.getInputPayloadJson());
        entity.setOutputSchemaJson(dataObject.getOutputSchemaJson());
        entity.setForceJson(dataObject.getForceJson());
        entity.setLocale(dataObject.getLocale());
        entity.setAiCallId(GraphExtractionAiCallIdCodec.toDomain(dataObject.getAiCallId()));
        entity.setAiCandidateId(GraphExtractionAiCandidateIdCodec.toDomain(dataObject.getAiCandidateId()));
        entity.setStatus(GraphExtractionTaskStatus.from(dataObject.getStatus()));
        entity.setErrorType(dataObject.getErrorType());
        entity.setErrorMessage(dataObject.getErrorMessage());
        entity.setRequestedBy(GraphExtractionRequesterIdCodec.toDomain(dataObject.getRequestedBy()));
        entity.setRequestedAt(toInstant(dataObject.getRequestedAt()));
        entity.setCompletedAt(toInstant(dataObject.getCompletedAt()));
        entity.setAppliedAt(toInstant(dataObject.getAppliedAt()));
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

    private static Date toDate(Instant value) {
        return value == null ? null : Date.from(value);
    }

    private static Instant toInstant(Date value) {
        return value == null ? null : value.toInstant();
    }
}
