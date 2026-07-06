package com.thundax.kuzhambu.operations.application.backup.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupDetailQuery;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupPageQuery;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupDetailResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupExecuteResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupPageResult;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupExecutionGuard;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupScriptExecutor;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupSupportModels.OperationsBackupArtifactResult;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupType;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BackupApplicationServiceImplTest {

    @Test
    void executeShouldPersistSucceededBackupRecord() {
        InMemoryBackupRepository repository = new InMemoryBackupRepository();
        BackupApplicationServiceImpl service = new BackupApplicationServiceImpl(
                repository, new SuccessfulBackupScriptExecutor(), new OperationsBackupExecutionGuard());
        OperationsBackupExecuteCommand command = new OperationsBackupExecuteCommand(1001L);

        OperationsBackupExecuteResult result = service.execute(command);

        assertNotNull(result.getBackupId());
        assertEquals("MANUAL", result.getBackupType());
        assertEquals("SUCCEEDED", result.getBackupStatus());
        assertEquals("backup_20260629-120000.sql", result.getFileName());
        assertEquals(4096L, result.getFileSizeBytes());
        assertEquals("sha256-backup", result.getChecksum());
    }

    @Test
    void executeAutoBackupShouldPersistAutoBackupWithoutRequester() {
        InMemoryBackupRepository repository = new InMemoryBackupRepository();
        BackupApplicationServiceImpl service = new BackupApplicationServiceImpl(
                repository, new SuccessfulBackupScriptExecutor(), new OperationsBackupExecutionGuard());

        OperationsBackupExecuteResult result = service.executeAutoBackup();
        BackupRecord record = repository.getById(result.getBackupId());

        assertNotNull(result.getBackupId());
        assertEquals("AUTO", result.getBackupType());
        assertEquals("SUCCEEDED", result.getBackupStatus());
        assertNull(record.getRequesterUserId());
    }

    @Test
    void executeAutoBackupShouldPersistFailedRecordWhenScriptFails() {
        InMemoryBackupRepository repository = new InMemoryBackupRepository();
        BackupApplicationServiceImpl service = new BackupApplicationServiceImpl(
                repository, new FailingBackupScriptExecutor(), new OperationsBackupExecutionGuard());

        OperationsBackupExecuteResult result = service.executeAutoBackup();

        assertEquals("AUTO", result.getBackupType());
        assertEquals("FAILED", result.getBackupStatus());
        assertEquals("script failed", result.getFailureReason());
    }

    @Test
    void executeAutoBackupShouldPersistSkippedRecordWhenGuardIsOccupied() {
        InMemoryBackupRepository repository = new InMemoryBackupRepository();
        CountingBackupScriptExecutor scriptExecutor = new CountingBackupScriptExecutor();
        OperationsBackupExecutionGuard guard = new OperationsBackupExecutionGuard();
        guard.tryEnterRestore();
        BackupApplicationServiceImpl service = new BackupApplicationServiceImpl(repository, scriptExecutor, guard);

        OperationsBackupExecuteResult result = service.executeAutoBackup();

        assertEquals("AUTO", result.getBackupType());
        assertEquals("FAILED", result.getBackupStatus());
        assertEquals(
                "Operations backup skipped because another backup or restore is running.", result.getFailureReason());
        assertEquals(0, scriptExecutor.executeBackupCount);
        guard.exit();
    }

    @Test
    void executeManualBackupShouldRejectWhenGuardIsOccupied() {
        InMemoryBackupRepository repository = new InMemoryBackupRepository();
        CountingBackupScriptExecutor scriptExecutor = new CountingBackupScriptExecutor();
        OperationsBackupExecutionGuard guard = new OperationsBackupExecutionGuard();
        guard.tryEnterRestore();
        BackupApplicationServiceImpl service = new BackupApplicationServiceImpl(repository, scriptExecutor, guard);

        assertThrows(IllegalStateException.class, () -> service.execute(new OperationsBackupExecuteCommand(1001L)));
        assertEquals(0, scriptExecutor.executeBackupCount);
        guard.exit();
    }

    @Test
    void pageAndDetailShouldMapRepositoryRecords() {
        InMemoryBackupRepository repository = new InMemoryBackupRepository();
        BackupRecord record = new BackupRecord(
                BackupId.of(9001L),
                "MANUAL",
                "SUCCEEDED",
                null,
                "backup_20260629-120000.sql",
                4096L,
                "sha256-backup",
                null,
                1001L,
                new Date(1_719_630_400_000L),
                new Date(1_719_630_500_000L),
                new Date(1_722_222_400_000L));
        repository.records.put(9001L, record);
        BackupApplicationServiceImpl service = new BackupApplicationServiceImpl(
                repository, new SuccessfulBackupScriptExecutor(), new OperationsBackupExecutionGuard());

        PageResult<OperationsBackupPageResult> pageResult =
                service.page(new OperationsBackupPageQuery("MANUAL", "SUCCEEDED", 1001L), new PageQuery(1, 10));
        OperationsBackupDetailResult detailResult = service.detail(new OperationsBackupDetailQuery(BackupId.of(9001L)));

        assertEquals(1, pageResult.getRecords().size());
        assertEquals(9001L, pageResult.getRecords().get(0).getBackupId().value());
        assertEquals("backup_20260629-120000.sql", detailResult.getFileName());
    }

    private static class SuccessfulBackupScriptExecutor implements OperationsBackupScriptExecutor {

        @Override
        public OperationsBackupArtifactResult executeBackup(BackupType backupType, String timestamp) {
            return new OperationsBackupArtifactResult(
                    "backup_20260629-120000",
                    "backup_20260629-120000.sql",
                    Path.of("/backup/kuzhambu/backup_20260629-120000.sql"),
                    4096L,
                    "sha256-backup",
                    "backup_20260629-120000.storage.tar.gz",
                    "sha256-storage");
        }

        @Override
        public void executeRestore(String backupBaseName, String preRestoreTimestamp) {}

        @Override
        public void executeRestoreDrill(String backupBaseName, String preRestoreTimestamp) {}

        @Override
        public OperationsBackupArtifactResult loadArtifact(String baseName) {
            return executeBackup(BackupType.MANUAL, "20260629-120000");
        }
    }

    private static final class FailingBackupScriptExecutor implements OperationsBackupScriptExecutor {

        @Override
        public OperationsBackupArtifactResult executeBackup(BackupType backupType, String timestamp) {
            throw new IllegalStateException("script failed");
        }

        @Override
        public void executeRestore(String backupBaseName, String preRestoreTimestamp) {}

        @Override
        public void executeRestoreDrill(String backupBaseName, String preRestoreTimestamp) {}

        @Override
        public OperationsBackupArtifactResult loadArtifact(String baseName) {
            throw new IllegalStateException("script failed");
        }
    }

    private static final class CountingBackupScriptExecutor extends SuccessfulBackupScriptExecutor {
        private int executeBackupCount;

        @Override
        public OperationsBackupArtifactResult executeBackup(BackupType backupType, String timestamp) {
            executeBackupCount++;
            return super.executeBackup(backupType, timestamp);
        }
    }

    private static final class InMemoryBackupRepository implements BackupRepository {
        private long nextId = 9001L;
        private final Map<Long, BackupRecord> records = new LinkedHashMap<>();

        @Override
        public BackupRecord getById(BackupId id) {
            return id == null ? null : records.get(id.value());
        }

        @Override
        public BackupRecord getByFileName(String fileName) {
            return records.values().stream()
                    .filter(item ->
                            item.getFileName() != null && item.getFileName().equals(fileName))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResult<BackupRecord> page(
                String backupType, String backupStatus, Long requesterUserId, int pageNo, int pageSize) {
            return PageResult.of(pageNo, pageSize, records.size(), List.copyOf(records.values()));
        }

        @Override
        public BackupId insert(BackupRecord record) {
            BackupId id = BackupId.of(nextId++);
            record.setId(id);
            records.put(id.value(), record);
            return id;
        }

        @Override
        public int update(BackupRecord record) {
            records.put(record.getId().value(), record);
            return 1;
        }

        @Override
        public int deleteById(BackupId id) {
            return records.remove(id.value()) == null ? 0 : 1;
        }
    }
}
