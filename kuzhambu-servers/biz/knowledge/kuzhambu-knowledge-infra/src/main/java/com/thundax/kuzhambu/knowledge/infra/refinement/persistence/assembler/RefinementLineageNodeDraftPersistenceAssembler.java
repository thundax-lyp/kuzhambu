package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementLineageNodeDraftDO;
import java.util.List;

public final class RefinementLineageNodeDraftPersistenceAssembler {

    private RefinementLineageNodeDraftPersistenceAssembler() {}

    public static RefinementLineageNodeDraft toDomain(RefinementLineageNodeDraftDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new RefinementLineageNodeDraft(
                dataObject.getId(),
                dataObject.getDraftId(),
                dataObject.getRefinementTaskId(),
                dataObject.getNodeId(),
                dataObject.getNodeKey(),
                dataObject.getOriginType(),
                dataObject.getOperationType(),
                dataObject.getName(),
                dataObject.getNodeType(),
                dataObject.getGeneration(),
                dataObject.getGender(),
                dataObject.getConfirmationStatus(),
                dataObject.getSourceRefsJson(),
                dataObject.getSortOrder(),
                dataObject.getCreatedBy(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedBy(),
                dataObject.getUpdatedAt());
    }

    public static RefinementLineageNodeDraftDO toObject(RefinementLineageNodeDraft entity) {
        if (entity == null) {
            return null;
        }
        return new RefinementLineageNodeDraftDO(
                entity.getId(),
                entity.getDraftId(),
                entity.getRefinementTaskId(),
                entity.getNodeId(),
                entity.getNodeKey(),
                entity.getOriginType(),
                entity.getOperationType(),
                entity.getName(),
                entity.getNodeType(),
                entity.getGeneration(),
                entity.getGender(),
                entity.getConfirmationStatus(),
                entity.getSourceRefsJson(),
                entity.getSortOrder(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt());
    }

    public static List<RefinementLineageNodeDraft> toDomainList(List<RefinementLineageNodeDraftDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream().map(RefinementLineageNodeDraftPersistenceAssembler::toDomain).toList();
    }
}
