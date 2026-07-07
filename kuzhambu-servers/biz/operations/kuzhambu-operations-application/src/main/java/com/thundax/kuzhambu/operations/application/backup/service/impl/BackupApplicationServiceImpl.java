package com.thundax.kuzhambu.operations.application.backup.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupDetailQuery;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupPageQuery;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupDetailResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupExecuteResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupPageResult;
import com.thundax.kuzhambu.operations.application.backup.service.BackupApplicationService;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupExecutionGuard;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupScriptExecutor;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupSupportModels.OperationsBackupArtifactResult;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthAlertStrategy;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupStatus;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupType;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class BackupApplicationServiceImpl implements BackupApplicationService {

    private static final long RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1000;
    private static final String RUNNING_FAILURE_REASON =
            "Operations backup skipped because another backup or restore is running.";

    private final BackupRepository backupRepository;
    private final OperationsBackupScriptExecutor scriptExecutor;
    private final OperationsBackupExecutionGuard executionGuard;
    private final OperationsHealthAlertStrategy healthAlertStrategy;

    public BackupApplicationServiceImpl(
            BackupRepository backupRepository,
            OperationsBackupScriptExecutor scriptExecutor,
            OperationsBackupExecutionGuard executionGuard) {
        this(backupRepository, scriptExecutor, executionGuard, null);
    }

    @Autowired
    public BackupApplicationServiceImpl(
            BackupRepository backupRepository,
            OperationsBackupScriptExecutor scriptExecutor,
            OperationsBackupExecutionGuard executionGuard,
            OperationsHealthAlertStrategy healthAlertStrategy) {
        this.backupRepository = backupRepository;
        this.scriptExecutor = scriptExecutor;
        this.executionGuard = executionGuard;
        this.healthAlertStrategy = healthAlertStrategy;
    }

    @Override
    public OperationsBackupExecuteResult execute(OperationsBackupExecuteCommand command) {
        validateExecuteCommand(command);
        return executeBackup(BackupType.MANUAL, command.getRequesterUserId(), false);
    }

    @Override
    public OperationsBackupExecuteResult executeAutoBackup() {
        return executeBackup(BackupType.AUTO, null, true);
    }

    private OperationsBackupExecuteResult executeBackup(
            BackupType backupType, Long requesterUserId, boolean autoBackup) {
        boolean entered = executionGuard.tryEnterBackup();
        if (!entered) {
            if (autoBackup) {
                return toExecuteResult(insertSkippedAutoBackup());
            }
            throw new IllegalStateException(RUNNING_FAILURE_REASON);
        }
        try {
            return doExecuteBackup(backupType, requesterUserId);
        } finally {
            executionGuard.exit();
        }
    }

    private OperationsBackupExecuteResult doExecuteBackup(BackupType backupType, Long requesterUserId) {
        Date startedAt = new Date();
        String timestamp = formatTimestamp(startedAt);
        BackupRecord record = new BackupRecord(
                null,
                backupType.value(),
                BackupStatus.RUNNING.value(),
                null,
                backupType.filePrefix() + "_" + timestamp + ".sql",
                null,
                null,
                null,
                requesterUserId,
                startedAt,
                null,
                new Date(startedAt.getTime() + RETENTION_MILLIS));
        BackupId backupId = backupRepository.insert(record);
        record.setId(backupId);
        try {
            OperationsBackupArtifactResult artifact = scriptExecutor.executeBackup(backupType, timestamp);
            record.setFileName(artifact.getFileName());
            record.setFileSizeBytes(artifact.getFileSizeBytes());
            record.setChecksum(artifact.getChecksum());
            record.setBackupStatus(BackupStatus.SUCCEEDED.value());
            record.setCompletedAt(new Date());
            backupRepository.update(record);
        } catch (RuntimeException exception) {
            record.setBackupStatus(BackupStatus.FAILED.value());
            record.setFailureReason(truncateFailureReason(exception.getMessage()));
            record.setCompletedAt(new Date());
            backupRepository.update(record);
            recordBackupFailure(record);
        }
        return toExecuteResult(backupRepository.getById(backupId));
    }

    private BackupRecord insertSkippedAutoBackup() {
        Date startedAt = new Date();
        String timestamp = formatTimestamp(startedAt);
        BackupRecord record = new BackupRecord(
                null,
                BackupType.AUTO.value(),
                BackupStatus.FAILED.value(),
                null,
                BackupType.AUTO.filePrefix() + "_" + timestamp + ".sql",
                null,
                null,
                RUNNING_FAILURE_REASON,
                null,
                startedAt,
                startedAt,
                new Date(startedAt.getTime() + RETENTION_MILLIS));
        BackupId backupId = backupRepository.insert(record);
        record.setId(backupId);
        recordBackupFailure(record);
        return record;
    }

    private void recordBackupFailure(BackupRecord record) {
        if (healthAlertStrategy != null && record != null && record.getId() != null) {
            healthAlertStrategy.recordBackupFailed(record.getId().value(), record.getFailureReason());
        }
    }

    @Override
    public PageResult<OperationsBackupPageResult> page(OperationsBackupPageQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<BackupRecord> recordPage = backupRepository.page(
                query == null ? null : query.getBackupType(),
                query == null ? null : query.getBackupStatus(),
                query == null ? null : query.getRequesterUserId(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<OperationsBackupPageResult> results =
                recordPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList());
        return PageResult.of(recordPage.getPageNo(), recordPage.getPageSize(), recordPage.getTotalCount(), results);
    }

    @Override
    public OperationsBackupDetailResult detail(OperationsBackupDetailQuery query) {
        BackupRecord record = backupRepository.getById(query == null ? null : query.getBackupId());
        return toDetailResult(record);
    }

    private OperationsBackupExecuteResult toExecuteResult(BackupRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsBackupExecuteResult(
                record.getId(),
                record.getBackupType(),
                record.getBackupStatus(),
                record.getFileName(),
                record.getFileSizeBytes(),
                record.getChecksum(),
                record.getFailureReason(),
                record.getStartedAt(),
                record.getCompletedAt(),
                record.getExpiresAt());
    }

    private OperationsBackupPageResult toPageResult(BackupRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsBackupPageResult(
                record.getId(),
                record.getBackupType(),
                record.getBackupStatus(),
                record.getFileName(),
                record.getFileSizeBytes(),
                record.getChecksum(),
                record.getFailureReason(),
                record.getRequesterUserId(),
                record.getStartedAt(),
                record.getCompletedAt(),
                record.getExpiresAt());
    }

    private OperationsBackupDetailResult toDetailResult(BackupRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsBackupDetailResult(
                record.getId(),
                record.getBackupType(),
                record.getBackupStatus(),
                record.getStorageObjectId(),
                record.getFileName(),
                record.getFileSizeBytes(),
                record.getChecksum(),
                record.getFailureReason(),
                record.getRequesterUserId(),
                record.getStartedAt(),
                record.getCompletedAt(),
                record.getExpiresAt());
    }

    private void validateExecuteCommand(OperationsBackupExecuteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Operations backup execute command must not be null.");
        }
        if (command.getRequesterUserId() == null) {
            throw new IllegalArgumentException("Operations backup requesterUserId must not be null.");
        }
    }

    private String formatTimestamp(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return formatter.format(date);
    }

    private String truncateFailureReason(String failureReason) {
        if (failureReason == null) {
            return null;
        }
        return failureReason.length() > 1000 ? failureReason.substring(0, 1000) : failureReason;
    }
}
