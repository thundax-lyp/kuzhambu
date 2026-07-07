package com.thundax.kuzhambu.operations.application.health.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthPageQuery;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthTrendQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthPageResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthTrendResult;
import com.thundax.kuzhambu.operations.application.health.service.HealthCheckApplicationService;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthAlertStrategy;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class HealthCheckApplicationServiceImpl implements HealthCheckApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckApplicationServiceImpl.class);

    private final HealthCheckRepository healthCheckRepository;
    private final OperationsHealthAlertStrategy healthAlertStrategy;

    public HealthCheckApplicationServiceImpl(
            HealthCheckRepository healthCheckRepository, OperationsHealthAlertStrategy healthAlertStrategy) {
        this.healthCheckRepository = healthCheckRepository;
        this.healthAlertStrategy = healthAlertStrategy;
    }

    @Override
    public List<OperationsHealthSummaryResult> summary() {
        evaluateStaleAlerts();
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
                query == null ? null : query.getProbeSource(),
                query == null ? null : query.getProbeTarget(),
                query == null ? null : query.getCheckedAtStart(),
                query == null ? null : query.getCheckedAtEnd(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<OperationsHealthPageResult> results =
                recordPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList());
        return PageResult.of(recordPage.getPageNo(), recordPage.getPageSize(), recordPage.getTotalCount(), results);
    }

    @Override
    public List<OperationsHealthTrendResult> trend(OperationsHealthTrendQuery query) {
        OperationsHealthTrendQuery effectiveQuery = query == null ? new OperationsHealthTrendQuery() : query;
        return healthCheckRepository
                .listTrend(
                        effectiveQuery.getComponent(),
                        effectiveQuery.getProbeSource(),
                        effectiveQuery.getPeriodStart(),
                        effectiveQuery.getPeriodEnd(),
                        effectiveQuery.getBucketType())
                .stream()
                .map(this::toTrendResult)
                .collect(Collectors.toList());
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

    private OperationsHealthTrendResult toTrendResult(HealthTrendBucket bucket) {
        if (bucket == null) {
            return null;
        }
        return new OperationsHealthTrendResult(
                bucket.getBucket(),
                bucket.getUpCount(),
                bucket.getDegradedCount(),
                bucket.getDownCount(),
                bucket.getAvgLatencyMs());
    }

    private void evaluateStaleAlerts() {
        try {
            healthAlertStrategy.evaluateStaleAlerts();
        } catch (RuntimeException exception) {
            LOGGER.warn("Operations stale health alert evaluation failed", exception);
        }
    }
}
