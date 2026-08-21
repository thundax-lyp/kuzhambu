package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTaskDeleteReceipt;

public interface GraphExtractionTaskDeleteReceiptRepository {
    GraphExtractionTaskDeleteReceipt getByOperatorIdAndIdempotencyKey(Long operatorId, String idempotencyKey);

    GraphExtractionTaskDeleteReceipt getByOperatorIdAndIdempotencyKeyForUpdate(Long operatorId, String idempotencyKey);

    boolean insert(GraphExtractionTaskDeleteReceipt receipt);
}
