package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler.QaPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaMessageDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaMessageMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class QaMessageRepositoryImpl implements QaMessageRepository {

    private final QaMessageMapper mapper;

    public QaMessageRepositoryImpl(QaMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public QaMessage getById(Long id) {
        if (id == null) {
            return null;
        }
        return QaPersistenceAssembler.toMessageDomain(mapper.selectById(id));
    }

    @Override
    public List<QaMessage> listBySessionId(Long sessionId) {
        if (sessionId == null) {
            return List.of();
        }
        return QaPersistenceAssembler.toMessageDomainList(mapper.selectList(
                new QueryWrapper<QaMessageDO>().eq("session_id", sessionId).orderByAsc("sent_at")));
    }

    @Override
    public Long save(QaMessage entity) {
        QaMessageDO dataObject = QaPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(QaMessage entity) {
        return mapper.updateById(QaPersistenceAssembler.toObject(entity));
    }
}
