package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaOwnerRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler.QaPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSessionMapper;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class QaSessionRepositoryImpl implements QaSessionRepository {

    private static final String REMOVED_AT_COLUMN = "removed_at";

    private final QaSessionMapper mapper;

    public QaSessionRepositoryImpl(QaSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public QaSession getById(QaSessionId id) {
        if (id == null) {
            return null;
        }
        return QaPersistenceAssembler.toSessionDomain(mapper.selectById(QaSessionIdCodec.toValue(id)));
    }

    @Override
    public List<QaSession> listByOpenedAtRange(Instant openedAtStart, Instant openedAtEnd) {
        return QaPersistenceAssembler.toSessionDomainList(mapper.selectByOpenedAtRange(openedAtStart, openedAtEnd));
    }

    @Override
    public PageResult<QaSession> page(
            String title, Instant openedAtStart, Instant openedAtEnd, int pageNo, int pageSize) {
        Page<QaSessionDO> page = new Page<>(pageNo, pageSize);
        IPage<QaSessionDO> dataObjectPage =
                mapper.selectPage(page, buildPageWrapper(title, openedAtStart, openedAtEnd));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                QaPersistenceAssembler.toSessionDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public List<QaSession> listByOwnerUserId(QaOwnerRef owner, Integer limit) {
        if (owner == null || owner.getOwnerType() == null || owner.getOwnerId() == null) {
            return List.of();
        }
        QueryWrapper<QaSessionDO> wrapper = new QueryWrapper<QaSessionDO>()
                .eq("owner_type", owner.getOwnerType())
                .eq("owner_id", owner.getOwnerId())
                .isNull(REMOVED_AT_COLUMN)
                .orderByDesc("last_message_at");
        if (limit != null && limit > 0) {
            wrapper.last("limit " + limit);
        }
        return QaPersistenceAssembler.toSessionDomainList(mapper.selectList(wrapper));
    }

    private QueryWrapper<QaSessionDO> buildPageWrapper(String title, Instant openedAtStart, Instant openedAtEnd) {
        QueryWrapper<QaSessionDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(title)) {
            wrapper.like("title", title);
        }
        if (openedAtStart != null) {
            wrapper.ge("opened_at", openedAtStart);
        }
        if (openedAtEnd != null) {
            wrapper.le("opened_at", openedAtEnd);
        }
        wrapper.orderByDesc("opened_at").orderByDesc("id");
        return wrapper;
    }

    @Override
    public QaSessionId save(QaSession entity) {
        QaSessionDO dataObject = QaPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return QaSessionIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(QaSession entity) {
        return mapper.updateById(QaPersistenceAssembler.toObject(entity));
    }

    @Override
    public int markRemoved(QaSessionId id, Instant removedAt) {
        if (id == null || removedAt == null) {
            return 0;
        }
        return mapper.markRemoved(QaSessionIdCodec.toValue(id), removedAt);
    }
}
