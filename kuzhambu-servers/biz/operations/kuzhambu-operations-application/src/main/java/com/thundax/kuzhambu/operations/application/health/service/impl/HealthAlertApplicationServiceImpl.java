package com.thundax.kuzhambu.operations.application.health.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertAckCommand;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertRecoverCommand;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthAlertPageQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthAlertPageResult;
import com.thundax.kuzhambu.operations.application.health.service.HealthAlertApplicationService;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class HealthAlertApplicationServiceImpl implements HealthAlertApplicationService {

    private static final String ALERT_STATUS_ACKED = "ACKED";
    private static final String ALERT_STATUS_RECOVERED = "RECOVERED";

    private final HealthAlertRepository healthAlertRepository;

    public HealthAlertApplicationServiceImpl(HealthAlertRepository healthAlertRepository) {
        this.healthAlertRepository = healthAlertRepository;
    }

    @Override
    public PageResult<OperationsHealthAlertPageResult> page(OperationsHealthAlertPageQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<HealthAlertRecord> recordPage = healthAlertRepository.page(
                query == null ? null : query.getComponent(),
                query == null ? null : query.getAlertLevel(),
                query == null ? null : query.getAlertStatus(),
                query == null ? null : query.getSourceRefType(),
                query == null ? null : query.getSourceRefId(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<OperationsHealthAlertPageResult> results =
                recordPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList());
        return PageResult.of(recordPage.getPageNo(), recordPage.getPageSize(), recordPage.getTotalCount(), results);
    }

    @Override
    public void ack(OperationsHealthAlertAckCommand command) {
        HealthAlertRecord record = requireAlert(command == null ? null : command.getAlertId());
        record.setAlertStatus(ALERT_STATUS_ACKED);
        record.setAckedAt(new Date());
        record.setAckedByUserId(command.getAckedByUserId());
        healthAlertRepository.update(record);
    }

    @Override
    public void recover(OperationsHealthAlertRecoverCommand command) {
        HealthAlertRecord record = requireAlert(command == null ? null : command.getAlertId());
        record.setAlertStatus(ALERT_STATUS_RECOVERED);
        record.setRecoveredAt(new Date());
        healthAlertRepository.update(record);
    }

    private HealthAlertRecord requireAlert(HealthAlertId alertId) {
        if (alertId == null) {
            throw new IllegalArgumentException("Operations health alert id must not be null.");
        }
        HealthAlertRecord record = healthAlertRepository.getById(alertId);
        if (record == null) {
            throw new IllegalArgumentException("Operations health alert does not exist.");
        }
        return record;
    }

    private OperationsHealthAlertPageResult toPageResult(HealthAlertRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsHealthAlertPageResult(
                record.getId(),
                record.getComponent(),
                record.getAlertType(),
                record.getAlertLevel(),
                record.getAlertStatus(),
                record.getSourceRefType(),
                record.getSourceRefId(),
                record.getLatestCheckId(),
                record.getMessage(),
                record.getSuggestion(),
                record.getRecoveryAction(),
                record.getRecoveryTarget(),
                record.getFirstTriggeredAt(),
                record.getLastTriggeredAt(),
                record.getAckedAt(),
                record.getAckedByUserId(),
                record.getRecoveredAt(),
                record.getFailureReason());
    }
}
