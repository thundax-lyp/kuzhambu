package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSessionExport;

public interface QaSessionExportRepository {

    Long save(QaSessionExport entity);

    int update(QaSessionExport entity);

    QaSessionExport getByExportId(Long exportId);
}
