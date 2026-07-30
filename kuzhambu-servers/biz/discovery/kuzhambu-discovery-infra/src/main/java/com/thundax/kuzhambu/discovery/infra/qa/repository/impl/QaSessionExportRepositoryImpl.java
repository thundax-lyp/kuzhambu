package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSessionExport;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionExportRepository;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler.QaPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionExportDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaSessionExportMapper;
import org.springframework.stereotype.Repository;

@Repository
public class QaSessionExportRepositoryImpl implements QaSessionExportRepository {

    private final QaSessionExportMapper mapper;

    public QaSessionExportRepositoryImpl(QaSessionExportMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(QaSessionExport entity) {
        QaSessionExportDO dataObject = QaPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(QaSessionExport entity) {
        return mapper.updateById(QaPersistenceAssembler.toObject(entity));
    }

    @Override
    public QaSessionExport getById(Long id) {
        if (id == null) {
            return null;
        }
        return QaPersistenceAssembler.toSessionExportDomain(mapper.selectById(id));
    }
}
