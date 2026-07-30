package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaMessageIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
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
    public QaMessage getById(QaMessageId id) {
        if (id == null) {
            return null;
        }
        return QaPersistenceAssembler.toMessageDomain(mapper.selectById(QaMessageIdCodec.toValue(id)));
    }

    @Override
    public List<QaMessage> listBySessionId(QaSessionId sessionId) {
        if (sessionId == null) {
            return List.of();
        }
        return QaPersistenceAssembler.toMessageDomainList(mapper.selectList(new QueryWrapper<QaMessageDO>()
                .eq("session_id", QaSessionIdCodec.toValue(sessionId))
                .orderByAsc("sent_at")));
    }

    @Override
    public QaMessageId save(QaMessage entity) {
        QaMessageDO dataObject = QaPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return QaMessageIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(QaMessage entity) {
        return mapper.updateById(QaPersistenceAssembler.toObject(entity));
    }
}
