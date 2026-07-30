package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler.QaPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSourceDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSourceMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class QaSourceRepositoryImpl implements QaSourceRepository {

    private final QaSourceMapper mapper;

    public QaSourceRepositoryImpl(QaSourceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<QaSource> listByMessageId(Long messageId) {
        if (messageId == null) {
            return List.of();
        }
        return QaPersistenceAssembler.toSourceDomainList(mapper.selectList(
                new QueryWrapper<QaSourceDO>().eq("message_id", messageId).orderByAsc("source_rank")));
    }

    @Override
    public Long save(QaSource entity) {
        QaSourceDO dataObject = QaPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int deleteByMessageId(Long messageId) {
        if (messageId == null) {
            return 0;
        }
        return mapper.delete(new QueryWrapper<QaSourceDO>().eq("message_id", messageId));
    }
}
