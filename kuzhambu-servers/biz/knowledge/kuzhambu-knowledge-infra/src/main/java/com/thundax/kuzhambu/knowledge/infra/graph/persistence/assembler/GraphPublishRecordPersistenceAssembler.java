package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishRecord;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishRecordDO;

public final class GraphPublishRecordPersistenceAssembler {
    private GraphPublishRecordPersistenceAssembler() {}

    public static GraphPublishRecordDO toObject(GraphPublishRecord record, Long materialId) {
        if (record == null) {
            return null;
        }
        return new GraphPublishRecordDO(
                record.getId(),
                materialId,
                record.getStatus(),
                record.getPreviewSummaryJson(),
                record.getConflictDecisionsJson(),
                record.getResultSummaryJson(),
                record.getRequestedAt(),
                record.getCompletedAt());
    }

    public static GraphPublishRecord toDomain(GraphPublishRecordDO dataObject, ContentRef materialRef) {
        if (dataObject == null) {
            return null;
        }
        return new GraphPublishRecord(
                dataObject.getId(),
                materialRef,
                dataObject.getStatus(),
                dataObject.getPreviewSummaryJson(),
                dataObject.getConflictDecisionsJson(),
                dataObject.getResultSummaryJson(),
                dataObject.getRequestedAt(),
                dataObject.getCompletedAt());
    }
}
