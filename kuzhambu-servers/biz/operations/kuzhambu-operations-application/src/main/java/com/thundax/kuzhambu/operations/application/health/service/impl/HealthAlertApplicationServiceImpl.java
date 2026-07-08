package com.thundax.kuzhambu.operations.application.health.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand;
import com.thundax.kuzhambu.operations.application.backup.service.BackupApplicationService;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertAckCommand;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertRecoverCommand;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthAlertPageQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthAlertPageResult;
import com.thundax.kuzhambu.operations.application.health.service.HealthAlertApplicationService;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthRecoveryLinkFactory;
import com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand;
import com.thundax.kuzhambu.operations.application.restore.service.RestoreApplicationService;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class HealthAlertApplicationServiceImpl implements HealthAlertApplicationService {

    private static final String ALERT_STATUS_ACKED = "ACKED";
    private static final String ALERT_STATUS_RECOVERED = "RECOVERED";
    private static final Pattern BACKUP_ID_PATTERN = Pattern.compile("\"backupId\"\\s*:\\s*(\\d+)");

    private final HealthAlertRepository healthAlertRepository;
    private final BackupApplicationService backupApplicationService;
    private final RestoreApplicationService restoreApplicationService;

    public HealthAlertApplicationServiceImpl(HealthAlertRepository healthAlertRepository) {
        this(healthAlertRepository, null, null);
    }

    @Autowired
    public HealthAlertApplicationServiceImpl(
            HealthAlertRepository healthAlertRepository,
            BackupApplicationService backupApplicationService,
            RestoreApplicationService restoreApplicationService) {
        this.healthAlertRepository = healthAlertRepository;
        this.backupApplicationService = backupApplicationService;
        this.restoreApplicationService = restoreApplicationService;
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
                query == null ? null : query.getLatestCheckId(),
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
        executeRecoveryAction(record, command);
        record.setAlertStatus(ALERT_STATUS_RECOVERED);
        record.setRecoveredAt(new Date());
        record.setFailureReason(null);
        healthAlertRepository.update(record);
    }

    private void executeRecoveryAction(HealthAlertRecord record, OperationsHealthAlertRecoverCommand command) {
        String action = record.getRecoveryAction();
        if (OperationsHealthRecoveryLinkFactory.ACTION_RUN_MANUAL_BACKUP.equals(action)) {
            if (backupApplicationService == null) {
                throw new IllegalStateException("Operations manual backup recovery action is not available.");
            }
            backupApplicationService.execute(new OperationsBackupExecuteCommand(command.getRecoveredByUserId()));
            return;
        }
        if (OperationsHealthRecoveryLinkFactory.ACTION_RUN_RESTORE.equals(action)) {
            Long backupId = parseBackupId(record.getRecoveryTarget());
            if (backupId == null) {
                throw new IllegalArgumentException("Operations restore recovery target must contain backupId.");
            }
            if (restoreApplicationService == null) {
                throw new IllegalStateException("Operations restore recovery action is not available.");
            }
            restoreApplicationService.execute(
                    new OperationsRestoreExecuteCommand(BackupId.of(backupId), command.getRecoveredByUserId()));
        }
    }

    private Long parseBackupId(String recoveryTarget) {
        if (StringUtils.isBlank(recoveryTarget)) {
            return null;
        }
        Matcher matcher = BACKUP_ID_PATTERN.matcher(recoveryTarget);
        return matcher.find() ? Long.valueOf(matcher.group(1)) : null;
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
