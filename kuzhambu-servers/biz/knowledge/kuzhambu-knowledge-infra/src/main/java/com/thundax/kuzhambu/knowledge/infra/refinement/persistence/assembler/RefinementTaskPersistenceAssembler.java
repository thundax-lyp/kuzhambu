package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.refinement.codec.RefinementTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementTaskDO;
import java.util.List;

public final class RefinementTaskPersistenceAssembler {

    private RefinementTaskPersistenceAssembler() {}

    public static RefinementTask toDomain(RefinementTaskDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new RefinementTask(
                dataObject.getId(),
                RefinementTaskIdCodec.toDomain(dataObject.getRefinementTaskId()),
                dataObject.getTaskType(),
                dataObject.getSourceContentType(),
                dataObject.getSourceContentId(),
                dataObject.getSourceCategoryCode(),
                dataObject.getSourceCategoryName(),
                dataObject.getGraphVersionId(),
                dataObject.getStatus(),
                dataObject.getOpenedBy(),
                dataObject.getOpenedAt(),
                dataObject.getSubmittedBy(),
                dataObject.getSubmittedAt(),
                dataObject.getAppliedBy(),
                dataObject.getAppliedAt(),
                dataObject.getCancelledBy(),
                dataObject.getCancelledAt());
    }

    public static RefinementTaskDO toObject(RefinementTask entity) {
        if (entity == null) {
            return null;
        }
        return new RefinementTaskDO(
                entity.getId(),
                entity.getRefinementTaskId() == null
                        ? null
                        : entity.getRefinementTaskId().value(),
                entity.getTaskType(),
                entity.getSourceContentType(),
                entity.getSourceContentId(),
                entity.getSourceCategoryCode(),
                entity.getSourceCategoryName(),
                entity.getGraphVersionId(),
                entity.getStatus(),
                entity.getOpenedBy(),
                entity.getOpenedAt(),
                entity.getSubmittedBy(),
                entity.getSubmittedAt(),
                entity.getAppliedBy(),
                entity.getAppliedAt(),
                entity.getCancelledBy(),
                entity.getCancelledAt());
    }

    public static List<RefinementTask> toDomainList(List<RefinementTaskDO> dataObjects) {
        return dataObjects == null
                ? List.of()
                : dataObjects.stream()
                        .map(RefinementTaskPersistenceAssembler::toDomain)
                        .toList();
    }
}
