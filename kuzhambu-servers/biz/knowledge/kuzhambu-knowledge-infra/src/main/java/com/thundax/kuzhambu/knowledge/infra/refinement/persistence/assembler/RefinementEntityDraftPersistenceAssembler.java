package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementEntityDraftDO;
import java.util.List;

public final class RefinementEntityDraftPersistenceAssembler {

    private RefinementEntityDraftPersistenceAssembler() {}

    public static RefinementEntityDraft toDomain(RefinementEntityDraftDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new RefinementEntityDraft(
                dataObject.getId(),
                dataObject.getDraftId(),
                dataObject.getRefinementTaskId(),
                dataObject.getEntityId(),
                dataObject.getEntityKey(),
                dataObject.getOriginType(),
                dataObject.getOperationType(),
                dataObject.getName(),
                dataObject.getEntityType(),
                dataObject.getDescription(),
                dataObject.getConfirmationStatus(),
                dataObject.getSourceRefsJson(),
                dataObject.getSortOrder(),
                dataObject.getCreatedBy(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedBy(),
                dataObject.getUpdatedAt());
    }

    public static RefinementEntityDraftDO toObject(RefinementEntityDraft entity) {
        if (entity == null) {
            return null;
        }
        return new RefinementEntityDraftDO(
                entity.getId(),
                entity.getDraftId(),
                entity.getRefinementTaskId(),
                entity.getEntityId(),
                entity.getEntityKey(),
                entity.getOriginType(),
                entity.getOperationType(),
                entity.getName(),
                entity.getEntityType(),
                entity.getDescription(),
                entity.getConfirmationStatus(),
                entity.getSourceRefsJson(),
                entity.getSortOrder(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt());
    }

    public static List<RefinementEntityDraft> toDomainList(List<RefinementEntityDraftDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(RefinementEntityDraftPersistenceAssembler::toDomain)
                        .toList();
    }
}
