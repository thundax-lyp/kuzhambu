package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeEntityDO;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public final class KnowledgeEntityPersistenceAssembler {

    private KnowledgeEntityPersistenceAssembler() {}

    public static KnowledgeEntityDO toObject(KnowledgeEntity entity) {
        if (entity == null) {
            return null;
        }
        KnowledgeEntityDO dataObject = new KnowledgeEntityDO();
        dataObject.setId(KnowledgeEntityIdCodec.toValue(entity.getId()));
        dataObject.setEntityKey(entity.getEntityKey());
        dataObject.setName(entity.getName());
        dataObject.setEntityType(entity.getEntityType());
        dataObject.setDescription(entity.getDescription());
        dataObject.setConfirmationStatus(
                entity.getConfirmationStatus() == null
                        ? null
                        : entity.getConfirmationStatus().value());
        dataObject.setLatestVersionId(GraphVersionIdCodec.toValue(entity.getLatestVersionId()));
        dataObject.setSourceRefsJson(entity.getSourceRefsJson());
        dataObject.setFirstExtractedAt(toDate(entity.getFirstExtractedAt()));
        dataObject.setLastExtractedAt(toDate(entity.getLastExtractedAt()));
        dataObject.setConfirmedAt(toDate(entity.getConfirmedAt()));
        return dataObject;
    }

    public static KnowledgeEntity toDomain(KnowledgeEntityDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        KnowledgeEntity entity = new KnowledgeEntity();
        entity.setId(KnowledgeEntityIdCodec.toDomain(dataObject.getId()));
        entity.setEntityKey(dataObject.getEntityKey());
        entity.setName(dataObject.getName());
        entity.setEntityType(dataObject.getEntityType());
        entity.setDescription(dataObject.getDescription());
        entity.setConfirmationStatus(KnowledgeConfirmationStatus.from(dataObject.getConfirmationStatus()));
        entity.setLatestVersionId(GraphVersionIdCodec.toDomain(dataObject.getLatestVersionId()));
        entity.setSourceRefsJson(dataObject.getSourceRefsJson());
        entity.setFirstExtractedAt(toInstant(dataObject.getFirstExtractedAt()));
        entity.setLastExtractedAt(toInstant(dataObject.getLastExtractedAt()));
        entity.setConfirmedAt(toInstant(dataObject.getConfirmedAt()));
        return entity;
    }

    public static List<KnowledgeEntity> toDomainList(List<KnowledgeEntityDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(KnowledgeEntityPersistenceAssembler::toDomain)
                        .toList();
    }

    private static Date toDate(Instant value) {
        return value == null ? null : Date.from(value);
    }

    private static Instant toInstant(Date value) {
        return value == null ? null : value.toInstant();
    }
}
