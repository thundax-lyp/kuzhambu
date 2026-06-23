package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphVersionDO;

public final class GraphVersionPersistenceAssembler {

    private GraphVersionPersistenceAssembler() {}

    public static GraphVersionDO toObject(GraphVersion entity) {
        if (entity == null) {
            return null;
        }
        GraphVersionDO dataObject = new GraphVersionDO();
        dataObject.setId(entity.getId());
        dataObject.setVersionId(entity.getVersionId());
        dataObject.setTaskId(
                entity.getTaskId() == null ? null : entity.getTaskId().value());
        dataObject.setCandidateId(entity.getCandidateId());
        dataObject.setTaskType(entity.getTaskType());
        dataObject.setScopeType(entity.getScopeType());
        dataObject.setScopeJson(entity.getScopeJson());
        dataObject.setSourceContentType(entity.getSourceContentType());
        dataObject.setSourceContentId(entity.getSourceContentId());
        dataObject.setVersionNo(entity.getVersionNo());
        dataObject.setStatus(entity.getStatus());
        dataObject.setAppliedAt(entity.getAppliedAt());
        return dataObject;
    }

    public static GraphVersion toDomain(GraphVersionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        GraphVersion entity = new GraphVersion();
        entity.setId(dataObject.getId());
        entity.setVersionId(dataObject.getVersionId());
        entity.setTaskId(GraphExtractionTaskId.ofNullable(dataObject.getTaskId()));
        entity.setCandidateId(dataObject.getCandidateId());
        entity.setTaskType(dataObject.getTaskType());
        entity.setScopeType(dataObject.getScopeType());
        entity.setScopeJson(dataObject.getScopeJson());
        entity.setSourceContentType(dataObject.getSourceContentType());
        entity.setSourceContentId(dataObject.getSourceContentId());
        entity.setVersionNo(dataObject.getVersionNo());
        entity.setStatus(dataObject.getStatus());
        entity.setAppliedAt(dataObject.getAppliedAt());
        return entity;
    }
}
