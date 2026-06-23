package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementRelationDraftDO;
import java.util.List;

public final class RefinementRelationDraftPersistenceAssembler {

    private RefinementRelationDraftPersistenceAssembler() {}

    public static RefinementRelationDraft toDomain(RefinementRelationDraftDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new RefinementRelationDraft(
                dataObject.getId(),
                dataObject.getDraftId(),
                dataObject.getRefinementTaskId(),
                dataObject.getRelationId(),
                dataObject.getRelationKey(),
                dataObject.getOriginType(),
                dataObject.getOperationType(),
                dataObject.getSourceEntityKey(),
                dataObject.getTargetEntityKey(),
                dataObject.getSourceName(),
                dataObject.getTargetName(),
                dataObject.getRelationType(),
                dataObject.getEvidence(),
                dataObject.getConfirmationStatus(),
                dataObject.getSourceRefsJson(),
                dataObject.getSortOrder(),
                dataObject.getCreatedBy(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedBy(),
                dataObject.getUpdatedAt());
    }

    public static RefinementRelationDraftDO toObject(RefinementRelationDraft entity) {
        if (entity == null) {
            return null;
        }
        return new RefinementRelationDraftDO(
                entity.getId(),
                entity.getDraftId(),
                entity.getRefinementTaskId(),
                entity.getRelationId(),
                entity.getRelationKey(),
                entity.getOriginType(),
                entity.getOperationType(),
                entity.getSourceEntityKey(),
                entity.getTargetEntityKey(),
                entity.getSourceName(),
                entity.getTargetName(),
                entity.getRelationType(),
                entity.getEvidence(),
                entity.getConfirmationStatus(),
                entity.getSourceRefsJson(),
                entity.getSortOrder(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt());
    }

    public static List<RefinementRelationDraft> toDomainList(List<RefinementRelationDraftDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream().map(RefinementRelationDraftPersistenceAssembler::toDomain).toList();
    }
}
