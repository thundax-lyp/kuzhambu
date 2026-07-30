package com.thundax.kuzhambu.operations.application.restore.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupExecutionGuard;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupScriptExecutor;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupSupportModels.OperationsBackupArtifact;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthAlertStrategy;
import com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestoreDetailQuery;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestorePageQuery;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreDetailResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreExecuteResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestorePageResult;
import com.thundax.kuzhambu.operations.application.restore.service.RestoreApplicationService;
import com.thundax.kuzhambu.operations.application.restore.support.OperationsRestoreWriteBlocker;
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
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class RestoreApplicationServiceImpl implements RestoreApplicationService {

    private static final long RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1000;
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.of("Asia/Shanghai"));
    private static final String TASK_SOURCE_DOMAIN = "operations";
    private static final String TASK_TYPE_RESTORE = "RESTORE";
    private static final String TASK_STATUS_RUNNING = "RUNNING";
    private static final String TASK_STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String TASK_STATUS_FAILED = "FAILED";

    private final RestoreRepository restoreRepository;
    private final BackupRepository backupRepository;
    private final OperationsBackupScriptExecutor scriptExecutor;
    private final OperationsBackupExecutionGuard executionGuard;
    private final OperationsRestoreWriteBlocker writeBlocker;
    private final OperationsHealthAlertStrategy healthAlertStrategy;
    private final LongTaskSnapshotRepository longTaskSnapshotRepository;

    public RestoreApplicationServiceImpl(
            RestoreRepository restoreRepository,
            BackupRepository backupRepository,
            OperationsBackupScriptExecutor scriptExecutor,
            OperationsBackupExecutionGuard executionGuard,
            OperationsRestoreWriteBlocker writeBlocker) {
        this(restoreRepository, backupRepository, scriptExecutor, executionGuard, writeBlocker, null, null);
    }

    public RestoreApplicationServiceImpl(
            RestoreRepository restoreRepository,
            BackupRepository backupRepository,
            OperationsBackupScriptExecutor scriptExecutor,
            OperationsBackupExecutionGuard executionGuard,
            OperationsRestoreWriteBlocker writeBlocker,
            OperationsHealthAlertStrategy healthAlertStrategy) {
        this(
                restoreRepository,
                backupRepository,
                scriptExecutor,
                executionGuard,
                writeBlocker,
                healthAlertStrategy,
                null);
    }

    @Autowired
    public RestoreApplicationServiceImpl(
            RestoreRepository restoreRepository,
            BackupRepository backupRepository,
            OperationsBackupScriptExecutor scriptExecutor,
            OperationsBackupExecutionGuard executionGuard,
            OperationsRestoreWriteBlocker writeBlocker,
            OperationsHealthAlertStrategy healthAlertStrategy,
            LongTaskSnapshotRepository longTaskSnapshotRepository) {
        this.restoreRepository = restoreRepository;
        this.backupRepository = backupRepository;
        this.scriptExecutor = scriptExecutor;
        this.executionGuard = executionGuard;
        this.writeBlocker = writeBlocker;
        this.healthAlertStrategy = healthAlertStrategy;
        this.longTaskSnapshotRepository = longTaskSnapshotRepository;
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
        RestoreMode restoreMode = resolveRestoreMode(command);
        if (!executionGuard.tryEnterRestore()) {
            throw new IllegalStateException("Operations restore skipped because another backup or restore is running.");
        }

        try {
            return executeWithGuard(command, sourceBaseName, restoreMode);
        } finally {
            executionGuard.exit();
        }
    }

    @Override
    public PageResult<OperationsRestorePageResult> page(OperationsRestorePageQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<RestoreRecord> recordPage = restoreRepository.page(
                query == null ? null : query.getBackupId(),
                query == null ? null : query.getRestoreMode(),
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

    private OperationsRestoreExecuteResult executeWithGuard(
            OperationsRestoreExecuteCommand command, String sourceBaseName, RestoreMode restoreMode) {
        Instant startedAt = Instant.now();
        String preRestoreTimestamp = formatTimestamp(startedAt);
        BackupRecord preRestoreRecord =
                buildPreRestoreRecord(command.getRequesterUserId(), startedAt, preRestoreTimestamp);
        BackupId preRestoreBackupId = backupRepository.insert(preRestoreRecord);
        preRestoreRecord.setId(preRestoreBackupId);

        RestoreRecord restoreRecord = new RestoreRecord(
                null,
                command.getBackupId().value(),
                preRestoreBackupId.value(),
                restoreMode.value(),
                RestoreStatus.RUNNING.value(),
                Boolean.FALSE,
                null,
                null,
                null,
                command.getRequesterUserId(),
                startedAt,
                null);
        RestoreId restoreId = restoreRepository.insert(restoreRecord);
        restoreRecord.setId(restoreId);
        LongTaskSnapshot taskSnapshot = createRestoreTaskSnapshot(restoreRecord);

        boolean writeBlockEnabled = false;
        try {
            restoreRecord.setWriteBlockStartedAt(writeBlocker.enable(restoreId));
            restoreRecord.setWriteBlockEnabled(Boolean.TRUE);
            writeBlockEnabled = true;
            restoreRepository.update(restoreRecord);
            executeRestoreScript(restoreMode, sourceBaseName, preRestoreTimestamp);
            updatePreRestoreSuccess(preRestoreRecord, preRestoreTimestamp);
            restoreRecord.setRestoreStatus(RestoreStatus.SUCCEEDED.value());
        } catch (RuntimeException exception) {
            if (writeBlockEnabled) {
                updatePreRestoreAfterFailure(preRestoreRecord, exception);
            } else {
                markPreRestoreFailed(preRestoreRecord, exception);
            }
            restoreRecord.setRestoreStatus(RestoreStatus.FAILED.value());
            restoreRecord.setFailureReason(truncateFailureReason(exception.getMessage()));
        } finally {
            if (writeBlockEnabled) {
                restoreRecord.setWriteBlockReleasedAt(writeBlocker.disable(restoreId));
            }
            restoreRecord.setCompletedAt(Instant.now());
            restoreRepository.update(restoreRecord);
            updateRestoreTaskSnapshot(taskSnapshot, restoreRecord);
        }
        if (RestoreStatus.FAILED.value().equals(restoreRecord.getRestoreStatus())) {
            recordRestoreFailure(restoreRecord);
        }
        return toExecuteResult(restoreRepository.getById(restoreId));
    }

    private void recordRestoreFailure(RestoreRecord record) {
        if (healthAlertStrategy != null && record != null && record.getId() != null) {
            healthAlertStrategy.recordRestoreFailed(
                    record.getId().value(), record.getBackupId(), record.getFailureReason());
        }
    }

    private LongTaskSnapshot createRestoreTaskSnapshot(RestoreRecord record) {
        if (longTaskSnapshotRepository == null || record == null || record.getId() == null) {
            return null;
        }
        Instant snapshotAt = Instant.now();
        LongTaskSnapshot snapshot = new LongTaskSnapshot(
                null,
                TASK_SOURCE_DOMAIN,
                TASK_TYPE_RESTORE,
                taskKey(record.getId()),
                TASK_STATUS_RUNNING,
                1,
                0,
                0,
                null,
                record.getRequesterUserId(),
                record.getStartedAt(),
                null,
                snapshotAt);
        LongTaskSnapshotId snapshotId = longTaskSnapshotRepository.insert(snapshot);
        snapshot.setId(snapshotId);
        return snapshot;
    }

    private void updateRestoreTaskSnapshot(LongTaskSnapshot snapshot, RestoreRecord record) {
        if (longTaskSnapshotRepository == null || snapshot == null || record == null) {
            return;
        }
        boolean succeeded = RestoreStatus.SUCCEEDED.value().equals(record.getRestoreStatus());
        snapshot.setTaskStatus(succeeded ? TASK_STATUS_SUCCEEDED : TASK_STATUS_FAILED);
        snapshot.setSuccessCount(succeeded ? 1 : 0);
        snapshot.setFailedCount(succeeded ? 0 : 1);
        snapshot.setFailureReason(record.getFailureReason());
        snapshot.setCompletedAt(record.getCompletedAt());
        snapshot.setSnapshotAt(Instant.now());
        longTaskSnapshotRepository.update(snapshot);
    }

    private String taskKey(RestoreId restoreId) {
        return "restore:" + restoreId.value();
    }

    private void executeRestoreScript(RestoreMode restoreMode, String sourceBaseName, String preRestoreTimestamp) {
        if (RestoreMode.DRILL == restoreMode) {
            scriptExecutor.executeRestoreDrill(sourceBaseName, preRestoreTimestamp);
            return;
        }
        scriptExecutor.executeRestore(sourceBaseName, preRestoreTimestamp);
    }

    private BackupRecord buildPreRestoreRecord(Long requesterUserId, Instant startedAt, String preRestoreTimestamp) {
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
                startedAt.plusMillis(RETENTION_MILLIS));
    }

    private void updatePreRestoreSuccess(BackupRecord preRestoreRecord, String preRestoreTimestamp) {
        OperationsBackupArtifact artifact =
                scriptExecutor.loadArtifact(BackupType.PRE_RESTORE.filePrefix() + "_" + preRestoreTimestamp);
        preRestoreRecord.setFileName(artifact.getFileName());
        preRestoreRecord.setFileSizeBytes(artifact.getFileSizeBytes());
        preRestoreRecord.setChecksum(artifact.getChecksum());
        preRestoreRecord.setBackupStatus(BackupStatus.SUCCEEDED.value());
        preRestoreRecord.setCompletedAt(java.time.Instant.now());
        backupRepository.update(preRestoreRecord);
    }

    private void updatePreRestoreAfterFailure(BackupRecord preRestoreRecord, RuntimeException restoreException) {
        try {
            OperationsBackupArtifact artifact =
                    scriptExecutor.loadArtifact(stripSqlSuffix(preRestoreRecord.getFileName()));
            preRestoreRecord.setFileName(artifact.getFileName());
            preRestoreRecord.setFileSizeBytes(artifact.getFileSizeBytes());
            preRestoreRecord.setChecksum(artifact.getChecksum());
            preRestoreRecord.setBackupStatus(BackupStatus.SUCCEEDED.value());
            preRestoreRecord.setCompletedAt(java.time.Instant.now());
            backupRepository.update(preRestoreRecord);
        } catch (RuntimeException preRestoreException) {
            preRestoreRecord.setBackupStatus(BackupStatus.FAILED.value());
            preRestoreRecord.setFailureReason(truncateFailureReason(restoreException.getMessage()));
            preRestoreRecord.setCompletedAt(java.time.Instant.now());
            backupRepository.update(preRestoreRecord);
        }
    }

    private void markPreRestoreFailed(BackupRecord preRestoreRecord, RuntimeException exception) {
        preRestoreRecord.setBackupStatus(BackupStatus.FAILED.value());
        preRestoreRecord.setFailureReason(truncateFailureReason(exception.getMessage()));
        preRestoreRecord.setCompletedAt(java.time.Instant.now());
        backupRepository.update(preRestoreRecord);
    }

    private OperationsRestoreExecuteResult toExecuteResult(RestoreRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsRestoreExecuteResult(
                record.getId(),
                record.getBackupId(),
                record.getPreRestoreBackupId(),
                record.getRestoreMode(),
                record.getRestoreStatus(),
                record.getWriteBlockEnabled(),
                record.getWriteBlockStartedAt(),
                record.getWriteBlockReleasedAt(),
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
                record.getRestoreMode(),
                record.getRestoreStatus(),
                record.getWriteBlockEnabled(),
                record.getWriteBlockStartedAt(),
                record.getWriteBlockReleasedAt(),
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
                record.getRestoreMode(),
                record.getRestoreStatus(),
                record.getWriteBlockEnabled(),
                record.getWriteBlockStartedAt(),
                record.getWriteBlockReleasedAt(),
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

    private RestoreMode resolveRestoreMode(OperationsRestoreExecuteCommand command) {
        return StringUtils.isBlank(command.getRestoreMode())
                ? RestoreMode.REAL
                : RestoreMode.from(command.getRestoreMode());
    }

    private String stripSqlSuffix(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        return fileName.endsWith(".sql") ? fileName.substring(0, fileName.length() - 4) : fileName;
    }

    private String formatTimestamp(Instant date) {
        return BACKUP_TIMESTAMP_FORMATTER.format(date);
    }

    private String truncateFailureReason(String failureReason) {
        if (failureReason == null) {
            return null;
        }
        return failureReason.length() > 1000 ? failureReason.substring(0, 1000) : failureReason;
    }
}
