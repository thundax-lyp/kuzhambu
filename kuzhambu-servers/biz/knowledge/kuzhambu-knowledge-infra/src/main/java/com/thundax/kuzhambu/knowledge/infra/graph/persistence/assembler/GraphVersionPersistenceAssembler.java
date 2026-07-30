package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphVersionDO;

public final class GraphVersionPersistenceAssembler {

    private GraphVersionPersistenceAssembler() {}

    public static GraphVersionDO toObject(GraphVersion entity) {
        if (entity == null) {
            return null;
        }
        GraphVersionDO dataObject = new GraphVersionDO();
        dataObject.setId(GraphVersionIdCodec.toValue(entity.getId()));
        dataObject.setTaskId(GraphExtractionTaskIdCodec.toValue(entity.getTaskId()));
        dataObject.setCandidateId(GraphExtractionAiCandidateIdCodec.toValue(entity.getCandidateId()));
        dataObject.setTaskType(
                entity.getTaskType() == null ? null : entity.getTaskType().value());
        dataObject.setScopeType(entity.getScopeType());
        dataObject.setScopeJson(entity.getScopeJson());
        dataObject.setSourceContentType(entity.getSourceContentType());
        dataObject.setSourceContentId(GraphExtractionSourceContentIdCodec.toValue(entity.getSourceContentId()));
        dataObject.setSourceCategoryCode(entity.getSourceCategoryCode());
        dataObject.setSourceCategoryName(entity.getSourceCategoryName());
        dataObject.setVersionNo(entity.getVersionNo());
        dataObject.setStatus(
                entity.getStatus() == null ? null : entity.getStatus().value());
        dataObject.setAppliedAt(entity.getAppliedAt());
        return dataObject;
    }

    public static GraphVersion toDomain(GraphVersionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        GraphVersion entity = new GraphVersion();
        entity.setId(GraphVersionIdCodec.toDomain(dataObject.getId()));
        entity.setTaskId(GraphExtractionTaskIdCodec.toDomain(dataObject.getTaskId()));
        entity.setCandidateId(GraphExtractionAiCandidateIdCodec.toDomain(dataObject.getCandidateId()));
        entity.setTaskType(GraphExtractionTaskType.from(dataObject.getTaskType()));
        entity.setScopeType(dataObject.getScopeType());
        entity.setScopeJson(dataObject.getScopeJson());
        entity.setSourceContentType(dataObject.getSourceContentType());
        entity.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(dataObject.getSourceContentId()));
        entity.setSourceCategoryCode(dataObject.getSourceCategoryCode());
        entity.setSourceCategoryName(dataObject.getSourceCategoryName());
        entity.setVersionNo(dataObject.getVersionNo());
        entity.setStatus(GraphVersionStatus.from(dataObject.getStatus()));
        entity.setAppliedAt(dataObject.getAppliedAt());
        return entity;
    }
}
