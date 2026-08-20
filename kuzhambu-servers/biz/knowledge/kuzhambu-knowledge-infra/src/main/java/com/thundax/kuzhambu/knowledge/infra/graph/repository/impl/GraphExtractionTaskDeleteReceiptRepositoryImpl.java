package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTaskDeleteReceipt;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskDeleteReceiptRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphExtractionTaskDeleteReceiptPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphExtractionTaskDeleteReceiptMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class GraphExtractionTaskDeleteReceiptRepositoryImpl implements GraphExtractionTaskDeleteReceiptRepository {
    private final GraphExtractionTaskDeleteReceiptMapper mapper;

    public GraphExtractionTaskDeleteReceiptRepositoryImpl(GraphExtractionTaskDeleteReceiptMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphExtractionTaskDeleteReceipt getByIdempotencyKey(String idempotencyKey) {
        return GraphExtractionTaskDeleteReceiptPersistenceAssembler.toDomain(
                mapper.selectByIdempotencyKey(idempotencyKey));
    }

    @Override
    public GraphExtractionTaskDeleteReceipt getByIdempotencyKeyForUpdate(String idempotencyKey) {
        return GraphExtractionTaskDeleteReceiptPersistenceAssembler.toDomain(
                mapper.selectByIdempotencyKeyForUpdate(idempotencyKey));
    }

    @Override
    public boolean insert(GraphExtractionTaskDeleteReceipt receipt) {
        try {
            mapper.insert(GraphExtractionTaskDeleteReceiptPersistenceAssembler.toObject(receipt));
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }
}
