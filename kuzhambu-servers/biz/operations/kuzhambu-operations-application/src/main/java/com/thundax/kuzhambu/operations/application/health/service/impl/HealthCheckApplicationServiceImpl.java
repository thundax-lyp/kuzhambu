package com.thundax.kuzhambu.operations.application.health.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthPageQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthPageResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.application.health.service.HealthCheckApplicationService;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class HealthCheckApplicationServiceImpl implements HealthCheckApplicationService {

    private final HealthCheckRepository healthCheckRepository;

    public HealthCheckApplicationServiceImpl(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    @Override
    public List<OperationsHealthSummaryResult> summary() {
        return healthCheckRepository.listLatestByComponent().stream()
                .map(this::toSummaryResult)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<OperationsHealthPageResult> page(OperationsHealthPageQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<HealthCheckRecord> recordPage = healthCheckRepository.page(
                query == null ? null : query.getComponent(),
                query == null ? null : query.getHealthStatus(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<OperationsHealthPageResult> results =
                recordPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList());
        return PageResult.of(recordPage.getPageNo(), recordPage.getPageSize(), recordPage.getTotalCount(), results);
    }

    private OperationsHealthSummaryResult toSummaryResult(HealthCheckRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsHealthSummaryResult(
                record.getId(),
                record.getComponent(),
                record.getHealthStatus(),
                record.getLatencyMs(),
                record.getMessage(),
                record.getProbeSource(),
                record.getProbeTarget(),
                record.getCheckedAt());
    }

    private OperationsHealthPageResult toPageResult(HealthCheckRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsHealthPageResult(
                record.getId(),
                record.getComponent(),
                record.getHealthStatus(),
                record.getLatencyMs(),
                record.getMessage(),
                record.getProbeSource(),
                record.getProbeTarget(),
                record.getDetailsJson(),
                record.getCheckedAt());
    }
}
