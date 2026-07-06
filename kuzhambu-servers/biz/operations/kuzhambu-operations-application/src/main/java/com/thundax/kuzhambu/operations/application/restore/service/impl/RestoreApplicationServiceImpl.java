package com.thundax.kuzhambu.operations.application.restore.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupScriptExecutor;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupSupportModels.OperationsBackupArtifactResult;
import com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestoreDetailQuery;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestorePageQuery;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreDetailResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreExecuteResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestorePageResult;
import com.thundax.kuzhambu.operations.application.restore.service.RestoreApplicationService;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupStatus;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupType;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.domain.restore.model.enums.RestoreMode;
import com.thundax.kuzhambu.operations.domain.restore.model.enums.RestoreStatus;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import com.thundax.kuzhambu.operations.domain.restore.repository.RestoreRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class RestoreApplicationServiceImpl implements RestoreApplicationService {

    private static final long RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1000;

    private final RestoreRepository restoreRepository;
    private final BackupRepository backupRepository;
    private final OperationsBackupScriptExecutor scriptExecutor;

    public RestoreApplicationServiceImpl(
            RestoreRepository restoreRepository,
            BackupRepository backupRepository,
            OperationsBackupScriptExecutor scriptExecutor) {
        this.restoreRepository = restoreRepository;
        this.backupRepository = backupRepository;
        this.scriptExecutor = scriptExecutor;
    }

    @Override
    public OperationsRestoreExecuteResult execute(OperationsRestoreExecuteCommand command) {
        validateExecuteCommand(command);
        BackupRecord sourceBackup = backupRepository.getById(command.getBackupId());
        if (sourceBackup == null) {
            throw new IllegalArgumentException("Operations restore source backup does not exist.");
        }
        if (!BackupStatus.SUCCEEDED.value().equalsIgnoreCase(sourceBackup.getBackupStatus())) {
            throw new IllegalArgumentException("Operations restore source backup must be in SUCCEEDED status.");
        }
        String sourceBaseName = stripSqlSuffix(sourceBackup.getFileName());
        if (StringUtils.isBlank(sourceBaseName)) {
            throw new IllegalArgumentException("Operations restore source backup filename is invalid.");
        }

        Date startedAt = new Date();
        String preRestoreTimestamp = formatTimestamp(startedAt);
        BackupRecord preRestoreRecord =
                buildPreRestoreRecord(command.getRequesterUserId(), startedAt, preRestoreTimestamp);
        BackupId preRestoreBackupId = backupRepository.insert(preRestoreRecord);
        preRestoreRecord.setId(preRestoreBackupId);

        RestoreRecord restoreRecord = new RestoreRecord(
                null,
                command.getBackupId().value(),
                preRestoreBackupId.value(),
                RestoreMode.REAL.value(),
                RestoreStatus.RUNNING.value(),
                Boolean.TRUE,
                null,
                null,
                null,
                command.getRequesterUserId(),
                startedAt,
                null);
        RestoreId restoreId = restoreRepository.insert(restoreRecord);
        restoreRecord.setId(restoreId);
        try {
            scriptExecutor.executeRestore(sourceBaseName, preRestoreTimestamp);
            updatePreRestoreSuccess(preRestoreRecord, preRestoreTimestamp);
            restoreRecord.setRestoreStatus(RestoreStatus.SUCCEEDED.value());
            restoreRecord.setCompletedAt(new Date());
            restoreRepository.update(restoreRecord);
        } catch (RuntimeException exception) {
            updatePreRestoreAfterFailure(preRestoreRecord, exception);
            restoreRecord.setRestoreStatus(RestoreStatus.FAILED.value());
            restoreRecord.setFailureReason(truncateFailureReason(exception.getMessage()));
            restoreRecord.setCompletedAt(new Date());
            restoreRepository.update(restoreRecord);
        }
        return toExecuteResult(restoreRepository.getById(restoreId));
    }

    @Override
    public PageResult<OperationsRestorePageResult> page(OperationsRestorePageQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<RestoreRecord> recordPage = restoreRepository.page(
                query == null ? null : query.getBackupId(),
                null,
                query == null ? null : query.getRestoreStatus(),
                query == null ? null : query.getRequesterUserId(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<OperationsRestorePageResult> results =
                recordPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList());
        return PageResult.of(recordPage.getPageNo(), recordPage.getPageSize(), recordPage.getTotalCount(), results);
    }

    @Override
    public OperationsRestoreDetailResult detail(OperationsRestoreDetailQuery query) {
        RestoreRecord record = restoreRepository.getById(query == null ? null : query.getRestoreId());
        return toDetailResult(record);
    }

    private BackupRecord buildPreRestoreRecord(Long requesterUserId, Date startedAt, String preRestoreTimestamp) {
        return new BackupRecord(
                null,
                BackupType.PRE_RESTORE.value(),
                BackupStatus.RUNNING.value(),
                null,
                BackupType.PRE_RESTORE.filePrefix() + "_" + preRestoreTimestamp + ".sql",
                null,
                null,
                null,
                requesterUserId,
                startedAt,
                null,
                new Date(startedAt.getTime() + RETENTION_MILLIS));
    }

    private void updatePreRestoreSuccess(BackupRecord preRestoreRecord, String preRestoreTimestamp) {
        OperationsBackupArtifactResult artifact =
                scriptExecutor.loadArtifact(BackupType.PRE_RESTORE.filePrefix() + "_" + preRestoreTimestamp);
        preRestoreRecord.setFileName(artifact.getFileName());
        preRestoreRecord.setFileSizeBytes(artifact.getFileSizeBytes());
        preRestoreRecord.setChecksum(artifact.getChecksum());
        preRestoreRecord.setBackupStatus(BackupStatus.SUCCEEDED.value());
        preRestoreRecord.setCompletedAt(new Date());
        backupRepository.update(preRestoreRecord);
    }

    private void updatePreRestoreAfterFailure(BackupRecord preRestoreRecord, RuntimeException restoreException) {
        try {
            OperationsBackupArtifactResult artifact =
                    scriptExecutor.loadArtifact(stripSqlSuffix(preRestoreRecord.getFileName()));
            preRestoreRecord.setFileName(artifact.getFileName());
            preRestoreRecord.setFileSizeBytes(artifact.getFileSizeBytes());
            preRestoreRecord.setChecksum(artifact.getChecksum());
            preRestoreRecord.setBackupStatus(BackupStatus.SUCCEEDED.value());
            preRestoreRecord.setCompletedAt(new Date());
            backupRepository.update(preRestoreRecord);
        } catch (RuntimeException preRestoreException) {
            preRestoreRecord.setBackupStatus(BackupStatus.FAILED.value());
            preRestoreRecord.setFailureReason(truncateFailureReason(restoreException.getMessage()));
            preRestoreRecord.setCompletedAt(new Date());
            backupRepository.update(preRestoreRecord);
        }
    }

    private OperationsRestoreExecuteResult toExecuteResult(RestoreRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsRestoreExecuteResult(
                record.getId(),
                record.getBackupId(),
                record.getPreRestoreBackupId(),
                record.getRestoreStatus(),
                record.getWriteBlockEnabled(),
                record.getFailureReason(),
                record.getStartedAt(),
                record.getCompletedAt());
    }

    private OperationsRestorePageResult toPageResult(RestoreRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsRestorePageResult(
                record.getId(),
                record.getBackupId(),
                record.getPreRestoreBackupId(),
                record.getRestoreStatus(),
                record.getWriteBlockEnabled(),
                record.getFailureReason(),
                record.getRequesterUserId(),
                record.getStartedAt(),
                record.getCompletedAt());
    }

    private OperationsRestoreDetailResult toDetailResult(RestoreRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsRestoreDetailResult(
                record.getId(),
                record.getBackupId(),
                record.getPreRestoreBackupId(),
                record.getRestoreStatus(),
                record.getWriteBlockEnabled(),
                record.getFailureReason(),
                record.getRequesterUserId(),
                record.getStartedAt(),
                record.getCompletedAt());
    }

    private void validateExecuteCommand(OperationsRestoreExecuteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Operations restore execute command must not be null.");
        }
        if (command.getBackupId() == null) {
            throw new IllegalArgumentException("Operations restore backupId must not be null.");
        }
        if (command.getRequesterUserId() == null) {
            throw new IllegalArgumentException("Operations restore requesterUserId must not be null.");
        }
    }

    private String stripSqlSuffix(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        return fileName.endsWith(".sql") ? fileName.substring(0, fileName.length() - 4) : fileName;
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
