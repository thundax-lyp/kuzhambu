package com.thundax.kuzhambu.operations.application.restore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupExecutionGuard;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupScriptExecutor;
import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupSupportModels.OperationsBackupArtifactResult;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthAlertStrategy;
import com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestoreDetailQuery;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestorePageQuery;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreDetailResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreExecuteResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestorePageResult;
import com.thundax.kuzhambu.operations.application.restore.support.OperationsRestoreWriteBlocker;
import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import com.thundax.kuzhambu.operations.domain.restore.codec.RestoreIdCodec;
import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.domain.restore.model.enums.RestoreMode;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import com.thundax.kuzhambu.operations.domain.restore.repository.RestoreRepository;
import com.thundax.kuzhambu.operations.domain.task.codec.LongTaskSnapshotIdCodec;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
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
                        BackupIdCodec.toDomain(9001L),
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
        SuccessfulRestoreScriptExecutor scriptExecutor = new SuccessfulRestoreScriptExecutor();
        RestoreApplicationServiceImpl service = service(restoreRepository, backupRepository, scriptExecutor);

        OperationsRestoreExecuteResult result = service.execute(
                new OperationsRestoreExecuteCommand(BackupIdCodec.toDomain(9001L), RestoreMode.REAL.value(), 1001L));

        assertNotNull(result.getRestoreId());
        assertEquals(RestoreMode.REAL.value(), result.getRestoreMode());
        assertEquals("SUCCEEDED", result.getRestoreStatus());
        assertEquals(1, scriptExecutor.realRestoreCount);
        assertNotNull(result.getWriteBlockStartedAt());
        assertNotNull(result.getWriteBlockReleasedAt());
        assertNotNull(result.getPreRestoreBackupId());
        assertEquals(
                "SUCCEEDED",
                backupRepository.records.get(result.getPreRestoreBackupId()).getBackupStatus());
    }

    @Test
    void executeShouldTrackRestoreResultInLongTaskSnapshot() {
        InMemoryBackupRepository backupRepository = backupRepositoryWithSucceededSource();
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        InMemoryLongTaskSnapshotRepository taskRepository = new InMemoryLongTaskSnapshotRepository();
        RestoreApplicationServiceImpl service = new RestoreApplicationServiceImpl(
                restoreRepository,
                backupRepository,
                new SuccessfulRestoreScriptExecutor(),
                new OperationsBackupExecutionGuard(),
                new OperationsRestoreWriteBlocker(),
                null,
                taskRepository);

        OperationsRestoreExecuteResult result = service.execute(
                new OperationsRestoreExecuteCommand(BackupIdCodec.toDomain(9001L), RestoreMode.REAL.value(), 1001L));

        assertEquals(1, taskRepository.records.size());
        LongTaskSnapshot snapshot = taskRepository.records.values().iterator().next();
        assertEquals("operations", snapshot.getSourceDomain());
        assertEquals("RESTORE", snapshot.getTaskType());
        assertEquals("restore:" + result.getRestoreId().value(), snapshot.getTaskKey());
        assertEquals("SUCCEEDED", snapshot.getTaskStatus());
        assertEquals(1, snapshot.getSuccessCount());
        assertEquals(0, snapshot.getFailedCount());
        assertNotNull(snapshot.getCompletedAt());
    }

    @Test
    void executeShouldPersistSucceededRestoreDrill() {
        InMemoryBackupRepository backupRepository = backupRepositoryWithSucceededSource();
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        SuccessfulRestoreScriptExecutor scriptExecutor = new SuccessfulRestoreScriptExecutor();
        RestoreApplicationServiceImpl service = service(restoreRepository, backupRepository, scriptExecutor);

        OperationsRestoreExecuteResult result = service.execute(
                new OperationsRestoreExecuteCommand(BackupIdCodec.toDomain(9001L), RestoreMode.DRILL.value(), 1001L));

        assertEquals(RestoreMode.DRILL.value(), result.getRestoreMode());
        assertEquals("SUCCEEDED", result.getRestoreStatus());
        assertEquals(0, scriptExecutor.realRestoreCount);
        assertEquals(1, scriptExecutor.drillRestoreCount);
    }

    @Test
    void executeShouldKeepPreRestoreRecordWhenRestoreFails() {
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        backupRepository.records.put(
                9001L,
                new BackupRecord(
                        BackupIdCodec.toDomain(9001L),
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
        OperationsHealthAlertStrategy alertStrategy = mock(OperationsHealthAlertStrategy.class);
        RestoreApplicationServiceImpl service = new RestoreApplicationServiceImpl(
                restoreRepository,
                backupRepository,
                new FailedRestoreScriptExecutor(),
                new OperationsBackupExecutionGuard(),
                new OperationsRestoreWriteBlocker(),
                alertStrategy);

        OperationsRestoreExecuteResult result = service.execute(
                new OperationsRestoreExecuteCommand(BackupIdCodec.toDomain(9001L), RestoreMode.REAL.value(), 1001L));

        assertEquals("FAILED", result.getRestoreStatus());
        assertNotNull(result.getWriteBlockReleasedAt());
        assertNotNull(result.getPreRestoreBackupId());
        assertEquals(
                "SUCCEEDED",
                backupRepository.records.get(result.getPreRestoreBackupId()).getBackupStatus());
        verify(alertStrategy).recordRestoreFailed(result.getRestoreId().value(), 9001L, "restore failed");
    }

    @Test
    void executeShouldTrackFailedRestoreInLongTaskSnapshot() {
        InMemoryBackupRepository backupRepository = backupRepositoryWithSucceededSource();
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        InMemoryLongTaskSnapshotRepository taskRepository = new InMemoryLongTaskSnapshotRepository();
        RestoreApplicationServiceImpl service = new RestoreApplicationServiceImpl(
                restoreRepository,
                backupRepository,
                new FailedRestoreScriptExecutor(),
                new OperationsBackupExecutionGuard(),
                new OperationsRestoreWriteBlocker(),
                null,
                taskRepository);

        service.execute(
                new OperationsRestoreExecuteCommand(BackupIdCodec.toDomain(9001L), RestoreMode.REAL.value(), 1001L));

        LongTaskSnapshot snapshot = taskRepository.records.values().iterator().next();
        assertEquals("FAILED", snapshot.getTaskStatus());
        assertEquals(0, snapshot.getSuccessCount());
        assertEquals(1, snapshot.getFailedCount());
        assertEquals("restore failed", snapshot.getFailureReason());
    }

    @Test
    void executeShouldNotRunScriptWhenWriteBlockEnableFails() {
        InMemoryBackupRepository backupRepository = backupRepositoryWithSucceededSource();
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        SuccessfulRestoreScriptExecutor scriptExecutor = new SuccessfulRestoreScriptExecutor();
        RestoreApplicationServiceImpl service = new RestoreApplicationServiceImpl(
                restoreRepository,
                backupRepository,
                scriptExecutor,
                new OperationsBackupExecutionGuard(),
                new FailingRestoreWriteBlocker());

        OperationsRestoreExecuteResult result = service.execute(
                new OperationsRestoreExecuteCommand(BackupIdCodec.toDomain(9001L), RestoreMode.REAL.value(), 1001L));

        assertEquals("FAILED", result.getRestoreStatus());
        assertEquals(0, scriptExecutor.realRestoreCount);
        assertEquals(0, scriptExecutor.drillRestoreCount);
    }

    @Test
    void executeShouldKeepBackupGuardDuringRestore() {
        InMemoryBackupRepository backupRepository = backupRepositoryWithSucceededSource();
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        OperationsBackupExecutionGuard guard = new OperationsBackupExecutionGuard();
        GuardAssertingRestoreScriptExecutor scriptExecutor = new GuardAssertingRestoreScriptExecutor(guard);
        RestoreApplicationServiceImpl service = new RestoreApplicationServiceImpl(
                restoreRepository, backupRepository, scriptExecutor, guard, new OperationsRestoreWriteBlocker());

        OperationsRestoreExecuteResult result = service.execute(
                new OperationsRestoreExecuteCommand(BackupIdCodec.toDomain(9001L), RestoreMode.REAL.value(), 1001L));

        assertEquals("SUCCEEDED", result.getRestoreStatus());
        assertFalse(scriptExecutor.backupEnteredDuringRestore);
    }

    @Test
    void pageAndDetailShouldMapRepositoryRecords() {
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        InMemoryRestoreRepository restoreRepository = new InMemoryRestoreRepository();
        Date writeBlockStartedAt = new Date(1_719_630_410_000L);
        Date writeBlockReleasedAt = new Date(1_719_630_490_000L);
        restoreRepository.records.put(
                9101L,
                new RestoreRecord(
                        RestoreIdCodec.toDomain(9101L),
                        9001L,
                        9201L,
                        RestoreMode.DRILL.value(),
                        "SUCCEEDED",
                        Boolean.TRUE,
                        writeBlockStartedAt,
                        writeBlockReleasedAt,
                        null,
                        1001L,
                        new Date(1_719_630_400_000L),
                        new Date(1_719_630_500_000L)));
        RestoreApplicationServiceImpl service =
                service(restoreRepository, backupRepository, new SuccessfulRestoreScriptExecutor());

        PageResult<OperationsRestorePageResult> pageResult = service.page(
                new OperationsRestorePageQuery(9001L, RestoreMode.DRILL.value(), "SUCCEEDED", 1001L),
                new PageQuery(1, 10));
        OperationsRestoreDetailResult detailResult =
                service.detail(new OperationsRestoreDetailQuery(RestoreIdCodec.toDomain(9101L)));

        assertEquals(1, pageResult.getRecords().size());
        assertEquals(9101L, pageResult.getRecords().get(0).getRestoreId().value());
        assertEquals(RestoreMode.DRILL.value(), pageResult.getRecords().get(0).getRestoreMode());
        assertEquals(writeBlockStartedAt, pageResult.getRecords().get(0).getWriteBlockStartedAt());
        assertEquals(writeBlockReleasedAt, detailResult.getWriteBlockReleasedAt());
        assertEquals(9201L, detailResult.getPreRestoreBackupId());
    }

    private RestoreApplicationServiceImpl service(
            InMemoryRestoreRepository restoreRepository,
            InMemoryBackupRepository backupRepository,
            OperationsBackupScriptExecutor scriptExecutor) {
        return new RestoreApplicationServiceImpl(
                restoreRepository,
                backupRepository,
                scriptExecutor,
                new OperationsBackupExecutionGuard(),
                new OperationsRestoreWriteBlocker());
    }

    private static InMemoryBackupRepository backupRepositoryWithSucceededSource() {
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        backupRepository.records.put(
                9001L,
                new BackupRecord(
                        BackupIdCodec.toDomain(9001L),
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
        return backupRepository;
    }

    private static class SuccessfulRestoreScriptExecutor implements OperationsBackupScriptExecutor {
        protected int realRestoreCount;
        protected int drillRestoreCount;

        @Override
        public OperationsBackupArtifactResult executeBackup(
                com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupType backupType, String timestamp) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void executeRestore(String backupBaseName, String preRestoreTimestamp) {
            realRestoreCount++;
        }

        @Override
        public void executeRestoreDrill(String backupBaseName, String preRestoreTimestamp) {
            drillRestoreCount++;
        }

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
            realRestoreCount++;
            throw new IllegalStateException("restore failed");
        }
    }

    private static final class GuardAssertingRestoreScriptExecutor extends SuccessfulRestoreScriptExecutor {
        private final OperationsBackupExecutionGuard guard;
        private boolean backupEnteredDuringRestore;

        private GuardAssertingRestoreScriptExecutor(OperationsBackupExecutionGuard guard) {
            this.guard = guard;
        }

        @Override
        public void executeRestore(String backupBaseName, String preRestoreTimestamp) {
            super.executeRestore(backupBaseName, preRestoreTimestamp);
            backupEnteredDuringRestore = guard.tryEnterBackup();
            if (backupEnteredDuringRestore) {
                guard.exit();
            }
        }
    }

    private static final class FailingRestoreWriteBlocker extends OperationsRestoreWriteBlocker {
        @Override
        public Date enable(RestoreId restoreId) {
            throw new IllegalStateException("write block failed");
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
            BackupId id = BackupIdCodec.toDomain(nextId++);
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
                Long backupId,
                String restoreMode,
                String restoreStatus,
                Long requesterUserId,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, records.size(), List.copyOf(records.values()));
        }

        @Override
        public RestoreId insert(RestoreRecord record) {
            RestoreId id = RestoreIdCodec.toDomain(nextId++);
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

    private static final class InMemoryLongTaskSnapshotRepository implements LongTaskSnapshotRepository {
        private long nextId = 9301L;
        private final Map<Long, LongTaskSnapshot> records = new LinkedHashMap<>();

        @Override
        public LongTaskSnapshot getById(LongTaskSnapshotId id) {
            return id == null ? null : records.get(id.value());
        }

        @Override
        public PageResult<LongTaskSnapshot> page(
                String sourceDomain, String taskType, String taskStatus, int pageNo, int pageSize) {
            return PageResult.of(pageNo, pageSize, records.size(), List.copyOf(records.values()));
        }

        @Override
        public LongTaskSnapshotId insert(LongTaskSnapshot snapshot) {
            LongTaskSnapshotId id = LongTaskSnapshotIdCodec.toDomain(nextId++);
            snapshot.setId(id);
            records.put(id.value(), snapshot);
            return id;
        }

        @Override
        public int update(LongTaskSnapshot snapshot) {
            records.put(snapshot.getId().value(), snapshot);
            return 1;
        }

        @Override
        public int deleteById(LongTaskSnapshotId id) {
            return records.remove(id.value()) == null ? 0 : 1;
        }
    }
}
