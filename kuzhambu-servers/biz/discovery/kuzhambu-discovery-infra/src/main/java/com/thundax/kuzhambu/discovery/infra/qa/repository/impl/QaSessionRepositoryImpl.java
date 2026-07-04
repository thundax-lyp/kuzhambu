package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler.QaPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSessionMapper;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class QaSessionRepositoryImpl implements QaSessionRepository {

    private static final String REMOVED_AT_COLUMN = "removed_at";

    private final QaSessionMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public QaSessionRepositoryImpl(QaSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public QaSession getBySessionId(Long sessionId) {
        if (sessionId == null) {
            return null;
        }
        return QaPersistenceAssembler.toSessionDomain(mapper.selectOne(
                new QueryWrapper<QaSessionDO>().eq("session_id", sessionId).last("limit 1")));
    }

    @Override
    public List<QaSession> listByOpenedAtRange(Date openedAtStart, Date openedAtEnd) {
        return QaPersistenceAssembler.toSessionDomainList(mapper.selectByOpenedAtRange(openedAtStart, openedAtEnd));
    }

    @Override
    public List<QaSession> listByOwnerUserId(String ownerType, String ownerId, Integer limit) {
        if (ownerType == null || ownerId == null) {
            return List.of();
        }
        QueryWrapper<QaSessionDO> wrapper = new QueryWrapper<QaSessionDO>()
                .eq("owner_type", ownerType)
                .eq("owner_id", ownerId)
                .isNull(REMOVED_AT_COLUMN)
                .orderByDesc("last_message_at");
        if (limit != null && limit > 0) {
            wrapper.last("limit " + limit);
        }
        return QaPersistenceAssembler.toSessionDomainList(mapper.selectList(wrapper));
    }

    @Override
    public Long save(QaSession entity) {
        QaSessionDO dataObject = QaPersistenceAssembler.toObject(entity);
        long nextId = idGenerator.nextId().value();
        dataObject.setId(nextId);
        if (dataObject.getSessionId() == null) {
            dataObject.setSessionId(nextId);
        }
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(QaSession entity) {
        return mapper.updateById(QaPersistenceAssembler.toObject(entity));
    }

    @Override
    public int markRemoved(Long sessionId, Date removedAt) {
        if (sessionId == null || removedAt == null) {
            return 0;
        }
        return mapper.markRemoved(sessionId, removedAt);
    }
}
