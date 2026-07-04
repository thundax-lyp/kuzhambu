package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSessionExport;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionExportRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler.QaPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionExportDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSessionExportMapper;
import org.springframework.stereotype.Repository;

@Repository
public class QaSessionExportRepositoryImpl implements QaSessionExportRepository {

    private final QaSessionExportMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public QaSessionExportRepositoryImpl(QaSessionExportMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(QaSessionExport entity) {
        QaSessionExportDO dataObject = QaPersistenceAssembler.toObject(entity);
        long nextId = idGenerator.nextId().value();
        dataObject.setId(nextId);
        if (dataObject.getExportId() == null) {
            dataObject.setExportId(nextId);
        }
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(QaSessionExport entity) {
        return mapper.updateById(QaPersistenceAssembler.toObject(entity));
    }

    @Override
    public QaSessionExport getByExportId(Long exportId) {
        if (exportId == null) {
            return null;
        }
        return QaPersistenceAssembler.toSessionExportDomain(mapper.selectOne(
                new QueryWrapper<QaSessionExportDO>().eq("export_id", exportId).last("limit 1")));
    }
}
