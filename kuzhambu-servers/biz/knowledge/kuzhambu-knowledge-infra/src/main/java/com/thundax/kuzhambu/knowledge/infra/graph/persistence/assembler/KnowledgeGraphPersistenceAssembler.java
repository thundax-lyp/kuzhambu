package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

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
        dataObject.setTaskId(valueOf(entity.getTaskId()));
        dataObject.setTaskType(entity.getTaskType());
        dataObject.setScopeType(entity.getScopeType());
        dataObject.setScopeJson(entity.getScopeJson());
        dataObject.setSourceContentType(entity.getSourceContentType());
        dataObject.setSourceContentId(entity.getSourceContentId());
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
        entity.setId(GraphExtractionTaskId.ofNullable(dataObject.getId()));
        entity.setTaskId(GraphExtractionTaskId.ofNullable(dataObject.getTaskId()));
        entity.setTaskType(dataObject.getTaskType());
        entity.setScopeType(dataObject.getScopeType());
        entity.setScopeJson(dataObject.getScopeJson());
        entity.setSourceContentType(dataObject.getSourceContentType());
        entity.setSourceContentId(dataObject.getSourceContentId());
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
