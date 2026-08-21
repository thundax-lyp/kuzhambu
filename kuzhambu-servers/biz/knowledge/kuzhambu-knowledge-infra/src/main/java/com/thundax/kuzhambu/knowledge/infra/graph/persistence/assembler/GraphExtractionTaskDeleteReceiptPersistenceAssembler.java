package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTaskDeleteReceipt;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDeleteReceiptDO;

public final class GraphExtractionTaskDeleteReceiptPersistenceAssembler {
    private GraphExtractionTaskDeleteReceiptPersistenceAssembler() {}

    public static GraphExtractionTaskDeleteReceiptDO toObject(GraphExtractionTaskDeleteReceipt entity) {
        if (entity == null) {
            return null;
        }
        return new GraphExtractionTaskDeleteReceiptDO(
                null,
                entity.getOperatorId(),
                entity.getIdempotencyKey(),
                GraphExtractionTaskIdCodec.toValue(entity.getDeletedTaskId()),
                entity.getCompletedAt());
    }

    public static GraphExtractionTaskDeleteReceipt toDomain(GraphExtractionTaskDeleteReceiptDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphExtractionTaskDeleteReceipt(
                dataObject.getOperatorId(),
                dataObject.getIdempotencyKey(),
                GraphExtractionTaskIdCodec.toDomain(dataObject.getDeletedTaskId()),
                dataObject.getCompletedAt());
    }
}
