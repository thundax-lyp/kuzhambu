package com.thundax.kuzhambu.operations.infra.report.persistence.assembler;

import com.thundax.kuzhambu.operations.domain.report.codec.ReportIdCodec;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.infra.report.persistence.dataobject.ReportDO;
import java.util.ArrayList;
import java.util.List;

public final class ReportPersistenceAssembler {

    private ReportPersistenceAssembler() {}

    public static ReportDO toObject(ReportRecord entity) {
        return entity == null
                ? null
                : new ReportDO(
                        null,
                        ReportIdCodec.toValue(entity.getId()),
                        entity.getReportType(),
                        entity.getFormat(),
                        entity.getPeriodStart(),
                        entity.getPeriodEnd(),
                        entity.getStorageObjectId(),
                        entity.getReportStatus(),
                        entity.getFailureReason(),
                        entity.getRequesterUserId(),
                        entity.getRequestedAt(),
                        entity.getCompletedAt());
    }

    public static ReportRecord toDomain(ReportDO dataObject) {
        return dataObject == null
                ? null
                : new ReportRecord(
                        ReportIdCodec.toDomain(dataObject.getReportId()),
                        dataObject.getReportType(),
                        dataObject.getFormat(),
                        dataObject.getPeriodStart(),
                        dataObject.getPeriodEnd(),
                        dataObject.getStorageObjectId(),
                        dataObject.getReportStatus(),
                        dataObject.getFailureReason(),
                        dataObject.getRequesterUserId(),
                        dataObject.getRequestedAt(),
                        dataObject.getCompletedAt());
    }

    public static List<ReportRecord> toDomainList(List<ReportDO> dataObjects) {
        List<ReportRecord> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toDomain(item)));
        }
        return entities;
    }
}
