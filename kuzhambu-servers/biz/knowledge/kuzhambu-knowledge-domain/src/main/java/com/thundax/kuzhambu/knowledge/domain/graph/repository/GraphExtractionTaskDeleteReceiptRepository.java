package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTaskDeleteReceipt;

public interface GraphExtractionTaskDeleteReceiptRepository {
    GraphExtractionTaskDeleteReceipt getByIdempotencyKey(String idempotencyKey);

    GraphExtractionTaskDeleteReceipt getByIdempotencyKeyForUpdate(String idempotencyKey);

    boolean insert(GraphExtractionTaskDeleteReceipt receipt);
}
