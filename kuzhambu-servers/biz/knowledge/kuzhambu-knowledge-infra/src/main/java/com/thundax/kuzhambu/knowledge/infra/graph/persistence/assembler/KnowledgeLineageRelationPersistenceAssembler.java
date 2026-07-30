package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageRelationDO;
import java.util.List;

public final class KnowledgeLineageRelationPersistenceAssembler {

    private KnowledgeLineageRelationPersistenceAssembler() {}

    public static KnowledgeLineageRelationDO toObject(KnowledgeLineageRelation entity) {
        if (entity == null) {
            return null;
        }
        KnowledgeLineageRelationDO dataObject = new KnowledgeLineageRelationDO();
        dataObject.setId(entity.getId());
        dataObject.setRelationKey(entity.getRelationKey());
        dataObject.setSourceNodeKey(entity.getSourceNodeKey());
        dataObject.setTargetNodeKey(entity.getTargetNodeKey());
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

    public static KnowledgeLineageRelation toDomain(KnowledgeLineageRelationDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        KnowledgeLineageRelation entity = new KnowledgeLineageRelation();
        entity.setId(dataObject.getId());
        entity.setRelationKey(dataObject.getRelationKey());
        entity.setSourceNodeKey(dataObject.getSourceNodeKey());
        entity.setTargetNodeKey(dataObject.getTargetNodeKey());
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

    public static List<KnowledgeLineageRelation> toDomainList(List<KnowledgeLineageRelationDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(KnowledgeLineageRelationPersistenceAssembler::toDomain)
                        .toList();
    }
}
