package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler.QaPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaRetrievalTraceDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaRetrievalTraceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class QaRetrievalTraceRepositoryImpl implements QaRetrievalTraceRepository {

    private final QaRetrievalTraceMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public QaRetrievalTraceRepositoryImpl(QaRetrievalTraceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public QaRetrievalTrace getByTraceId(Long traceId) {
        if (traceId == null) {
            return null;
        }
        return QaPersistenceAssembler.toTraceDomain(mapper.selectOne(
                new QueryWrapper<QaRetrievalTraceDO>().eq("trace_id", traceId).last("limit 1")));
    }

    @Override
    public QaRetrievalTrace getByMessageId(Long messageId) {
        if (messageId == null) {
            return null;
        }
        return QaPersistenceAssembler.toTraceDomain(mapper.selectOne(new QueryWrapper<QaRetrievalTraceDO>()
                .eq("message_id", messageId)
                .last("limit 1")));
    }

    @Override
    public Long save(QaRetrievalTrace entity) {
        QaRetrievalTraceDO dataObject = QaPersistenceAssembler.toObject(entity);
        long nextId = idGenerator.nextId().value();
        dataObject.setId(nextId);
        if (dataObject.getTraceId() == null) {
            dataObject.setTraceId(nextId);
        }
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(QaRetrievalTrace entity) {
        return mapper.updateById(QaPersistenceAssembler.toObject(entity));
    }
}
