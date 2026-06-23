package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeEntityDO;
import java.util.List;

public final class KnowledgeEntityPersistenceAssembler {

    private KnowledgeEntityPersistenceAssembler() {}

    public static KnowledgeEntityDO toObject(KnowledgeEntity entity) {
        if (entity == null) {
            return null;
        }
        KnowledgeEntityDO dataObject = new KnowledgeEntityDO();
        dataObject.setId(entity.getId());
        dataObject.setEntityId(entity.getEntityId());
        dataObject.setEntityKey(entity.getEntityKey());
        dataObject.setName(entity.getName());
        dataObject.setEntityType(entity.getEntityType());
        dataObject.setDescription(entity.getDescription());
        dataObject.setConfirmationStatus(entity.getConfirmationStatus());
        dataObject.setLatestVersionId(entity.getLatestVersionId());
        dataObject.setSourceRefsJson(entity.getSourceRefsJson());
        dataObject.setFirstExtractedAt(entity.getFirstExtractedAt());
        dataObject.setLastExtractedAt(entity.getLastExtractedAt());
        dataObject.setConfirmedAt(entity.getConfirmedAt());
        return dataObject;
    }

    public static KnowledgeEntity toDomain(KnowledgeEntityDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        KnowledgeEntity entity = new KnowledgeEntity();
        entity.setId(dataObject.getId());
        entity.setEntityId(dataObject.getEntityId());
        entity.setEntityKey(dataObject.getEntityKey());
        entity.setName(dataObject.getName());
        entity.setEntityType(dataObject.getEntityType());
        entity.setDescription(dataObject.getDescription());
        entity.setConfirmationStatus(dataObject.getConfirmationStatus());
        entity.setLatestVersionId(dataObject.getLatestVersionId());
        entity.setSourceRefsJson(dataObject.getSourceRefsJson());
        entity.setFirstExtractedAt(dataObject.getFirstExtractedAt());
        entity.setLastExtractedAt(dataObject.getLastExtractedAt());
        entity.setConfirmedAt(dataObject.getConfirmedAt());
        return entity;
    }

    public static List<KnowledgeEntity> toDomainList(List<KnowledgeEntityDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(KnowledgeEntityPersistenceAssembler::toDomain)
                        .toList();
    }
}
