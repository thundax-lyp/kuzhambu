package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncBatch;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaKnowledgeSyncBatchRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaKnowledgeSyncBatchDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaKnowledgeSyncBatchMapper;
import org.springframework.stereotype.Repository;

@Repository
public class QaKnowledgeSyncBatchRepositoryImpl implements QaKnowledgeSyncBatchRepository {

    private final QaKnowledgeSyncBatchMapper mapper;

    public QaKnowledgeSyncBatchRepositoryImpl(QaKnowledgeSyncBatchMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public QaKnowledgeSyncBatch getById(Long id) {
        if (id == null) {
            return null;
        }
        return toDomain(mapper.selectById(id));
    }

    @Override
    public Long save(QaKnowledgeSyncBatch entity) {
        QaKnowledgeSyncBatchDO dataObject = toObject(entity);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(QaKnowledgeSyncBatch entity) {
        return mapper.updateById(toObject(entity));
    }

    private QaKnowledgeSyncBatchDO toObject(QaKnowledgeSyncBatch entity) {
        if (entity == null) {
            return null;
        }
        QaKnowledgeSyncBatchDO dataObject = new QaKnowledgeSyncBatchDO();
        dataObject.setId(entity.getId());
        dataObject.setTriggerType(entity.getTriggerType());
        dataObject.setProvider(entity.getProvider());
        dataObject.setTotalCount(entity.getTotalCount());
        dataObject.setSuccessCount(entity.getSuccessCount());
        dataObject.setFailureCount(entity.getFailureCount());
        dataObject.setStartedAt(entity.getStartedAt());
        dataObject.setFinishedAt(entity.getFinishedAt());
        return dataObject;
    }

    private QaKnowledgeSyncBatch toDomain(QaKnowledgeSyncBatchDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaKnowledgeSyncBatch entity = new QaKnowledgeSyncBatch();
        entity.setId(dataObject.getId());
        entity.setTriggerType(dataObject.getTriggerType());
        entity.setProvider(dataObject.getProvider());
        entity.setTotalCount(dataObject.getTotalCount());
        entity.setSuccessCount(dataObject.getSuccessCount());
        entity.setFailureCount(dataObject.getFailureCount());
        entity.setStartedAt(dataObject.getStartedAt());
        entity.setFinishedAt(dataObject.getFinishedAt());
        return entity;
    }
}
