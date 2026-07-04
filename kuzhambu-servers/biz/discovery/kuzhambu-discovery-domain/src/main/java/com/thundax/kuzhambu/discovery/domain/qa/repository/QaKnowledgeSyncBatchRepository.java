package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncBatch;

public interface QaKnowledgeSyncBatchRepository {

    QaKnowledgeSyncBatch getByBatchId(Long batchId);

    Long save(QaKnowledgeSyncBatch entity);

    int update(QaKnowledgeSyncBatch entity);
}
