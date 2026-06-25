package com.thundax.kuzhambu.operations.domain.report.repository;

import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;

public interface ReportRepository {

    ReportRecord getById(ReportId id);

    ReportId insert(ReportRecord record);

    int update(ReportRecord record);

    int deleteById(ReportId id);
}
