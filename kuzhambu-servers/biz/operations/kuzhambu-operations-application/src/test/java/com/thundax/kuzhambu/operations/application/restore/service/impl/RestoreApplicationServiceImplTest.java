package com.thundax.kuzhambu.operations.application.restore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import com.thundax.kuzhambu.operations.domain.restore.repository.RestoreRepository;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RestoreApplicationServiceImplTest {

    @Test
    void executeShouldPersistSucceededRestoreAndPreRestoreSnapshot() {
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        backupRepository.records.put(
                9001L,
                new BackupRecord(
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
                        new Date(1_722_222_400_000L)));
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        RestoreApplicationServiceImpl service = new RestoreApplicationServiceImpl(
                restoreRepository, backupRepository, new SuccessfulRestoreScriptExecutor());

        OperationsRestoreExecuteResult result =
                service.execute(new OperationsRestoreExecuteCommand(BackupId.of(9001L), 1001L));

        assertNotNull(result.getRestoreId());
        assertEquals("SUCCEEDED", result.getRestoreStatus());
        assertNotNull(result.getPreRestoreBackupId());
        assertEquals(
                "SUCCEEDED",
                backupRepository.records.get(result.getPreRestoreBackupId()).getBackupStatus());
    }

    @Test
    void executeShouldKeepPreRestoreRecordWhenRestoreFails() {
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        backupRepository.records.put(
                9001L,
                new BackupRecord(
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
                        new Date(1_722_222_400_000L)));
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        RestoreApplicationServiceImpl service = new RestoreApplicationServiceImpl(
                restoreRepository, backupRepository, new FailedRestoreScriptExecutor());

        OperationsRestoreExecuteResult result =
                service.execute(new OperationsRestoreExecuteCommand(BackupId.of(9001L), 1001L));

        assertEquals("FAILED", result.getRestoreStatus());
        assertNotNull(result.getPreRestoreBackupId());
        assertEquals(
                "SUCCEEDED",
                backupRepository.records.get(result.getPreRestoreBackupId()).getBackupStatus());
    }

    @Test
    void pageAndDetailShouldMapRepositoryRecords() {
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        restoreRepository.records.put(
                9101L,
                new RestoreRecord(
                        RestoreId.of(9101L),
                        9001L,
                        9201L,
                        "SUCCEEDED",
                        Boolean.TRUE,
                        null,
                        1001L,
                        new Date(1_719_630_400_000L),
                        new Date(1_719_630_500_000L)));
        RestoreApplicationServiceImpl service = new RestoreApplicationServiceImpl(
                restoreRepository, backupRepository, new SuccessfulRestoreScriptExecutor());

        PageResult<OperationsRestorePageResult> pageResult =
                service.page(new OperationsRestorePageQuery(9001L, "SUCCEEDED", 1001L), new PageQuery(1, 10));
        OperationsRestoreDetailResult detailResult =
                service.detail(new OperationsRestoreDetailQuery(RestoreId.of(9101L)));

        assertEquals(1, pageResult.getRecords().size());
        assertEquals(9101L, pageResult.getRecords().get(0).getRestoreId().value());
        assertEquals(9201L, detailResult.getPreRestoreBackupId());
    }

    private static class SuccessfulRestoreScriptExecutor implements OperationsBackupScriptExecutor {

        @Override
        public OperationsBackupArtifactResult executeBackup(
                com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupType backupType, String timestamp) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void executeRestore(String backupBaseName, String preRestoreTimestamp) {}

        @Override
        public OperationsBackupArtifactResult loadArtifact(String baseName) {
            return new OperationsBackupArtifactResult(
                    baseName,
                    baseName + ".sql",
                    Path.of("/backup/kuzhambu/" + baseName + ".sql"),
                    2048L,
                    "sha256-prerestore",
                    baseName + ".storage.tar.gz",
                    "sha256-storage");
        }
    }

    private static final class FailedRestoreScriptExecutor extends SuccessfulRestoreScriptExecutor {
        @Override
        public void executeRestore(String backupBaseName, String preRestoreTimestamp) {
            throw new IllegalStateException("restore failed");
        }
    }

    private static final class InMemoryBackupRepository implements BackupRepository {
        private long nextId = 9201L;
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

    private static final class InMemoryRestoreRepository implements RestoreRepository {
        private long nextId = 9101L;
        private final Map<Long, RestoreRecord> records = new LinkedHashMap<>();

        @Override
        public RestoreRecord getById(RestoreId id) {
            return id == null ? null : records.get(id.value());
        }

        @Override
        public PageResult<RestoreRecord> page(
                Long backupId, String restoreStatus, Long requesterUserId, int pageNo, int pageSize) {
            return PageResult.of(pageNo, pageSize, records.size(), List.copyOf(records.values()));
        }

        @Override
        public RestoreId insert(RestoreRecord record) {
            RestoreId id = RestoreId.of(nextId++);
            record.setId(id);
            records.put(id.value(), record);
            return id;
        }

        @Override
        public int update(RestoreRecord record) {
            records.put(record.getId().value(), record);
            return 1;
        }

        @Override
        public int deleteById(RestoreId id) {
            return records.remove(id.value()) == null ? 0 : 1;
        }
    }
}
