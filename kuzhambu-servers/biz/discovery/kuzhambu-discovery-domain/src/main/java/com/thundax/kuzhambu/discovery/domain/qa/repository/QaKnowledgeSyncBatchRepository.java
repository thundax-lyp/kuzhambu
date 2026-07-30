package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncBatch;

public interface QaKnowledgeSyncBatchRepository {

    QaKnowledgeSyncBatch getById(Long id);

    default QaKnowledgeSyncBatch getByBatchId(Long batchId) {
        return getById(batchId);
    }

    Long save(QaKnowledgeSyncBatch entity);

    int update(QaKnowledgeSyncBatch entity);
}
