package com.thundax.kuzhambu.operations.infra.report.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.operations.domain.report.codec.ReportIdCodec;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import com.thundax.kuzhambu.operations.infra.report.persistence.assembler.ReportPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.report.persistence.dataobject.ReportDO;
import com.thundax.kuzhambu.operations.infra.report.persistence.mapper.ReportMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private final ReportMapper mapper;

    public ReportRepositoryImpl(ReportMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ReportRecord getById(ReportId id) {
        return ReportPersistenceAssembler.toDomain(mapper.selectOne(
                new LambdaQueryWrapper<ReportDO>().eq(ReportDO::getReportId, ReportIdCodec.toValue(id))));
    }

    @Override
    public ReportId insert(ReportRecord record) {
        ReportDO dataObject = ReportPersistenceAssembler.toObject(record);
        mapper.insert(dataObject);
        return ReportIdCodec.toDomain(dataObject.getReportId());
    }

    @Override
    public int update(ReportRecord record) {
        ReportDO dataObject = ReportPersistenceAssembler.toObject(record);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<ReportDO>()
                        .eq(ReportDO::getReportId, dataObject.getReportId())
                        .set(ReportDO::getReportType, dataObject.getReportType())
                        .set(ReportDO::getFormat, dataObject.getFormat())
                        .set(ReportDO::getPeriodStart, dataObject.getPeriodStart())
                        .set(ReportDO::getPeriodEnd, dataObject.getPeriodEnd())
                        .set(ReportDO::getRequestId, dataObject.getRequestId())
                        .set(ReportDO::getTraceId, dataObject.getTraceId())
                        .set(ReportDO::getTemplateVersion, dataObject.getTemplateVersion())
                        .set(ReportDO::getStorageObjectId, dataObject.getStorageObjectId())
                        .set(ReportDO::getArtifactFilename, dataObject.getArtifactFilename())
                        .set(ReportDO::getReportStatus, dataObject.getReportStatus())
                        .set(ReportDO::getFailureReason, dataObject.getFailureReason())
                        .set(ReportDO::getRequesterUserId, dataObject.getRequesterUserId())
                        .set(ReportDO::getRequestedAt, dataObject.getRequestedAt())
                        .set(ReportDO::getCompletedAt, dataObject.getCompletedAt()));
    }

    @Override
    public int deleteById(ReportId id) {
        return mapper.delete(new LambdaQueryWrapper<ReportDO>().eq(ReportDO::getReportId, ReportIdCodec.toValue(id)));
    }
}
