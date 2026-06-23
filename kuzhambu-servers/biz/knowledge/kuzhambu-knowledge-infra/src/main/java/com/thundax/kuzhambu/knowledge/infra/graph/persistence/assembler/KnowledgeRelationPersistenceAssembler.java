package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeRelationDO;
import java.util.List;

public final class KnowledgeRelationPersistenceAssembler {

    private KnowledgeRelationPersistenceAssembler() {}

    public static KnowledgeRelationDO toObject(KnowledgeRelation entity) {
        if (entity == null) {
            return null;
        }
        KnowledgeRelationDO dataObject = new KnowledgeRelationDO();
        dataObject.setId(entity.getId());
        dataObject.setRelationId(entity.getRelationId());
        dataObject.setRelationKey(entity.getRelationKey());
        dataObject.setSourceEntityKey(entity.getSourceEntityKey());
        dataObject.setTargetEntityKey(entity.getTargetEntityKey());
        dataObject.setSourceName(entity.getSourceName());
        dataObject.setTargetName(entity.getTargetName());
        dataObject.setRelationType(entity.getRelationType());
        dataObject.setEvidence(entity.getEvidence());
        dataObject.setConfirmationStatus(entity.getConfirmationStatus());
        dataObject.setLatestVersionId(entity.getLatestVersionId());
        dataObject.setSourceRefsJson(entity.getSourceRefsJson());
        dataObject.setFirstExtractedAt(entity.getFirstExtractedAt());
        dataObject.setLastExtractedAt(entity.getLastExtractedAt());
        dataObject.setConfirmedAt(entity.getConfirmedAt());
        return dataObject;
    }

    public static KnowledgeRelation toDomain(KnowledgeRelationDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        KnowledgeRelation entity = new KnowledgeRelation();
        entity.setId(dataObject.getId());
        entity.setRelationId(dataObject.getRelationId());
        entity.setRelationKey(dataObject.getRelationKey());
        entity.setSourceEntityKey(dataObject.getSourceEntityKey());
        entity.setTargetEntityKey(dataObject.getTargetEntityKey());
        entity.setSourceName(dataObject.getSourceName());
        entity.setTargetName(dataObject.getTargetName());
        entity.setRelationType(dataObject.getRelationType());
        entity.setEvidence(dataObject.getEvidence());
        entity.setConfirmationStatus(dataObject.getConfirmationStatus());
        entity.setLatestVersionId(dataObject.getLatestVersionId());
        entity.setSourceRefsJson(dataObject.getSourceRefsJson());
        entity.setFirstExtractedAt(dataObject.getFirstExtractedAt());
        entity.setLastExtractedAt(dataObject.getLastExtractedAt());
        entity.setConfirmedAt(dataObject.getConfirmedAt());
        return entity;
    }

    public static List<KnowledgeRelation> toDomainList(List<KnowledgeRelationDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream().map(KnowledgeRelationPersistenceAssembler::toDomain).toList();
    }
}
