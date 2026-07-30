package com.thundax.kuzhambu.operations.domain.report.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import java.time.Instant;
import java.util.List;

public interface ReportRepository {

    ReportRecord getById(ReportId id);

    PageResult<ReportRecord> page(
            String reportType,
            String format,
            String reportStatus,
            Long requesterUserId,
            Instant periodStart,
            Instant periodEnd,
            int pageNo,
            int pageSize);

    ReportId insert(ReportRecord record);

    int update(ReportRecord record);

    int deleteById(ReportId id);

    default List<ReportId> listExpiredReportIds(Instant requestedBefore, int limit) {
        return List.of();
    }
}
