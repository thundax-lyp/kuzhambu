package com.thundax.kuzhambu.operations.infra.report.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.report.codec.ReportIdCodec;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import com.thundax.kuzhambu.operations.infra.report.persistence.assembler.ReportPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.report.persistence.dataobject.ReportDO;
import com.thundax.kuzhambu.operations.infra.report.persistence.mapper.ReportMapper;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
    public PageResult<ReportRecord> page(
            String reportType,
            String format,
            String reportStatus,
            Long requesterUserId,
            Date periodStart,
            Date periodEnd,
            int pageNo,
            int pageSize) {
        Page<ReportDO> page = new Page<>(pageNo, pageSize);
        IPage<ReportDO> dataObjectPage = mapper.selectPage(
                page, buildPageWrapper(reportType, format, reportStatus, requesterUserId, periodStart, periodEnd));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                ReportPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
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

    @Override
    public List<ReportId> listExpiredReportIds(Date requestedBefore, int limit) {
        return mapper
                .selectObjs(new QueryWrapper<ReportDO>()
                        .select("report_id")
                        .le(requestedBefore != null, "requested_at", requestedBefore)
                        .in("report_status", List.of("SUCCEEDED", "FAILED"))
                        .orderByAsc("requested_at")
                        .orderByAsc("report_id")
                        .last("LIMIT " + limit))
                .stream()
                .map(ReportRepositoryImpl::longValue)
                .map(ReportId::of)
                .toList();
    }

    private QueryWrapper<ReportDO> buildPageWrapper(
            String reportType,
            String format,
            String reportStatus,
            Long requesterUserId,
            Date periodStart,
            Date periodEnd) {
        QueryWrapper<ReportDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(reportType)) {
            wrapper.eq("report_type", reportType);
        }
        if (StringUtils.isNotBlank(format)) {
            wrapper.eq("format", format);
        }
        if (StringUtils.isNotBlank(reportStatus)) {
            wrapper.eq("report_status", reportStatus);
        }
        if (requesterUserId != null) {
            wrapper.eq("requester_user_id", requesterUserId);
        }
        if (periodStart != null) {
            wrapper.ge("period_start", periodStart);
        }
        if (periodEnd != null) {
            wrapper.le("period_end", periodEnd);
        }
        wrapper.orderByDesc("requested_at");
        return wrapper;
    }

    private static Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }
}
