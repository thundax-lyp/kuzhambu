package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementLineageRelationDraftDO;
import java.util.List;

public final class RefinementLineageRelationDraftPersistenceAssembler {

    private RefinementLineageRelationDraftPersistenceAssembler() {}

    public static RefinementLineageRelationDraft toDomain(RefinementLineageRelationDraftDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new RefinementLineageRelationDraft(
                dataObject.getId(),
                dataObject.getDraftId(),
                dataObject.getRefinementTaskId(),
                dataObject.getRelationId(),
                dataObject.getRelationKey(),
                dataObject.getOriginType(),
                dataObject.getOperationType(),
                dataObject.getSourceNodeKey(),
                dataObject.getTargetNodeKey(),
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

    public static RefinementLineageRelationDraftDO toObject(RefinementLineageRelationDraft entity) {
        if (entity == null) {
            return null;
        }
        return new RefinementLineageRelationDraftDO(
                entity.getId(),
                entity.getDraftId(),
                entity.getRefinementTaskId(),
                entity.getRelationId(),
                entity.getRelationKey(),
                entity.getOriginType(),
                entity.getOperationType(),
                entity.getSourceNodeKey(),
                entity.getTargetNodeKey(),
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

    public static List<RefinementLineageRelationDraft> toDomainList(
            List<RefinementLineageRelationDraftDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream().map(RefinementLineageRelationDraftPersistenceAssembler::toDomain).toList();
    }
}
