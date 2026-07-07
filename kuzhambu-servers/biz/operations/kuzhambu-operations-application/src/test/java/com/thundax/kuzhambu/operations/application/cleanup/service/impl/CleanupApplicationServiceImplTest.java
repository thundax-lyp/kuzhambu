package com.thundax.kuzhambu.operations.application.cleanup.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.ClassicsCleanupTargetsFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsCleanupExecutionFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsCleanupTargetsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupDetailQuery;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupPageQuery;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupPageResult;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthAlertStrategy;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupItem;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupJob;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupItemId;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import com.thundax.kuzhambu.operations.domain.cleanup.repository.CleanupJobRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CleanupApplicationServiceImplTest {

    @Test
    void executeShouldPersistSucceededCleanupJob() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        backupRepository.expiredBackupIds = List.of(BackupId.of(101L), BackupId.of(102L));
        backupRepository.records.put(101L, new BackupRecord());
        backupRepository.records.put(102L, new BackupRecord());
        CleanupApplicationServiceImpl service =
                new CleanupApplicationServiceImpl(repository, backupRepository, new FakeClassicsFacade());

        OperationsCleanupDetailResult result =
                service.execute(new OperationsCleanupExecuteCommand("EXPIRED_BACKUP", 1001L));

        assertNotNull(result.getCleanupId());
        assertEquals("EXPIRED_BACKUP", result.getCleanupType());
        assertEquals("SUCCEEDED", result.getCleanupStatus());
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(2, repository.listItemsByJobId(result.getCleanupId()).size());
        assertEquals(
                "SUCCEEDED",
                repository.listItemsByJobId(result.getCleanupId()).get(0).getItemStatus());
        assertEquals(false, backupRepository.records.containsKey(101L));
    }

    @Test
    void executeShouldPersistFailedClassicsCleanupItem() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        FakeClassicsFacade classicsFacade = new FakeClassicsFacade();
        classicsFacade.targets = List.of(ClassicsCleanupTargetsFacadeResponse.Target.builder()
                .targetType("share")
                .targetId(201L)
                .build());
        classicsFacade.executionResults = List.of(ClassicsCleanupExecutionFacadeResponse.ItemResult.builder()
                .targetType("share")
                .targetId(201L)
                .success(false)
                .failureReason("TARGET_NOT_FOUND")
                .build());
        OperationsHealthAlertStrategy alertStrategy = mock(OperationsHealthAlertStrategy.class);
        CleanupApplicationServiceImpl service = new CleanupApplicationServiceImpl(
                repository, new InMemoryBackupRepository(), classicsFacade, alertStrategy);

        OperationsCleanupDetailResult result =
                service.execute(new OperationsCleanupExecuteCommand("EXPIRED_SHARE", 1001L));

        assertEquals("FAILED", result.getCleanupStatus());
        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        CleanupItem item = repository.listItemsByJobId(result.getCleanupId()).get(0);
        assertEquals("FAILED", item.getItemStatus());
        assertEquals("TARGET_NOT_FOUND", item.getFailureReason());
        verify(alertStrategy).recordCleanupFailed(result.getCleanupId().value(), "cleanup failed items: 1");
    }

    @Test
    void executeScheduledShouldAllowSystemRequesterAndApplyPolicyLimit() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        backupRepository.expiredBackupIds = List.of(BackupId.of(101L), BackupId.of(102L));
        backupRepository.records.put(101L, new BackupRecord());
        backupRepository.records.put(102L, new BackupRecord());
        CleanupApplicationServiceImpl service =
                new CleanupApplicationServiceImpl(repository, backupRepository, new FakeClassicsFacade());

        OperationsCleanupDetailResult result = service.executeScheduled(
                new OperationsCleanupExecuteCommand("EXPIRED_BACKUP", null, new Date(1_719_630_400_000L), 30, 1));

        assertEquals(null, result.getRequesterUserId());
        assertEquals(1, result.getTotalCount());
        assertEquals(1, backupRepository.lastLimit);
        assertEquals(new Date(1_717_038_400_000L), backupRepository.lastRequestedAt);
    }

    @Test
    void executeScheduledShouldPassRetentionAndLimitToClassicsFacade() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        FakeClassicsFacade classicsFacade = new FakeClassicsFacade();
        classicsFacade.targets = List.of(ClassicsCleanupTargetsFacadeResponse.Target.builder()
                .targetType("share")
                .targetId(201L)
                .build());
        classicsFacade.executionResults = List.of(ClassicsCleanupExecutionFacadeResponse.ItemResult.builder()
                .targetType("share")
                .targetId(201L)
                .success(true)
                .build());
        CleanupApplicationServiceImpl service =
                new CleanupApplicationServiceImpl(repository, new InMemoryBackupRepository(), classicsFacade);

        service.executeScheduled(
                new OperationsCleanupExecuteCommand("EXPIRED_SHARE", null, new Date(1_719_630_400_000L), 90, 5));

        assertEquals(90, classicsFacade.lastListRequest.getRetentionDays());
        assertEquals(5, classicsFacade.lastListRequest.getLimit());
    }

    @Test
    void executeShouldRejectUnsupportedCleanupType() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        CleanupApplicationServiceImpl service =
                new CleanupApplicationServiceImpl(repository, new InMemoryBackupRepository(), new FakeClassicsFacade());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(new OperationsCleanupExecuteCommand("UNSUPPORTED", 1001L)));
    }

    @Test
    void pageAndDetailShouldMapRepositoryRecords() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        CleanupJob cleanupJob = new CleanupJob(
                CleanupJobId.of(9001L),
                "EXPIRED_BACKUP",
                "SUCCEEDED",
                3,
                2,
                1,
                null,
                1001L,
                new Date(1_719_630_400_000L),
                new Date(1_719_630_500_000L),
                List.of());
        repository.jobs.put(9001L, cleanupJob);

        CleanupApplicationServiceImpl service =
                new CleanupApplicationServiceImpl(repository, new InMemoryBackupRepository(), new FakeClassicsFacade());

        PageResult<OperationsCleanupPageResult> pageResult = service.page(
                new OperationsCleanupPageQuery("EXPIRED_BACKUP", "SUCCEEDED", 1001L), new PageQuery(1, 10));
        OperationsCleanupDetailResult detailResult =
                service.detail(new OperationsCleanupDetailQuery(CleanupJobId.of(9001L)));

        assertEquals(1, pageResult.getRecords().size());
        assertEquals(9001L, pageResult.getRecords().get(0).getCleanupId().value());
        assertEquals("SUCCEEDED", pageResult.getRecords().get(0).getCleanupStatus());
        assertEquals(3, detailResult.getTotalCount());
    }

    private static final class InMemoryCleanupJobRepository implements CleanupJobRepository {
        private long nextCleanupId = 9101L;
        private long nextItemId = 9201L;
        private final Map<Long, CleanupJob> jobs = new LinkedHashMap<>();
        private final Map<Long, List<CleanupItem>> itemsByCleanupId = new LinkedHashMap<>();

        @Override
        public CleanupJob getById(CleanupJobId id) {
            CleanupJob job = id == null ? null : jobs.get(id.value());
            if (job != null) {
                job.setCleanupItems(listItemsByJobId(id));
            }
            return job;
        }

        @Override
        public CleanupJobId insert(CleanupJob job) {
            CleanupJobId cleanupJobId = CleanupJobId.of(nextCleanupId++);
            job.setId(cleanupJobId);
            job.setStartedAt(new Date(1_719_000_000_000L));
            jobs.put(cleanupJobId.value(), job);
            return cleanupJobId;
        }

        @Override
        public int update(CleanupJob job) {
            jobs.put(job.getId().value(), job);
            return 1;
        }

        @Override
        public int deleteById(CleanupJobId id) {
            jobs.remove(id.value());
            itemsByCleanupId.remove(id.value());
            return 1;
        }

        @Override
        public List<CleanupItem> listItemsByJobId(CleanupJobId jobId) {
            return itemsByCleanupId.getOrDefault(jobId.value(), List.of());
        }

        @Override
        public CleanupItemId insertItem(CleanupItem item) {
            CleanupItemId itemId = CleanupItemId.of(nextItemId++);
            item.setId(itemId);
            itemsByCleanupId
                    .computeIfAbsent(item.getCleanupId(), key -> new java.util.ArrayList<>())
                    .add(item);
            return itemId;
        }

        @Override
        public int updateItem(CleanupItem item) {
            List<CleanupItem> items = itemsByCleanupId.get(item.getCleanupId());
            if (items == null || items.isEmpty()) {
                return 0;
            }
            CleanupItem target = items.stream()
                    .filter(candidate -> candidate.getId() != null
                            && item.getId() != null
                            && candidate.getId().value().equals(item.getId().value()))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                return 0;
            }
            target.setFailureReason(item.getFailureReason());
            target.setItemStatus(item.getItemStatus());
            target.setProcessedAt(item.getProcessedAt());
            return 1;
        }

        @Override
        public int deleteItemsByJobId(CleanupJobId jobId) {
            return itemsByCleanupId.remove(jobId.value()) == null ? 0 : 1;
        }

        @Override
        public PageResult<CleanupJob> page(
                String cleanupType, String cleanupStatus, Long requesterUserId, int pageNo, int pageSize) {
            return PageResult.of(pageNo, pageSize, jobs.size(), List.copyOf(jobs.values()));
        }
    }

    private static final class InMemoryBackupRepository implements BackupRepository {
        private final Map<Long, BackupRecord> records = new LinkedHashMap<>();
        private List<BackupId> expiredBackupIds = List.of();
        private Date lastRequestedAt;
        private int lastLimit;

        @Override
        public BackupRecord getById(BackupId id) {
            return id == null ? null : records.get(id.value());
        }

        @Override
        public BackupRecord getByFileName(String fileName) {
            return null;
        }

        @Override
        public PageResult<BackupRecord> page(
                String backupType, String backupStatus, Long requesterUserId, int pageNo, int pageSize) {
            return PageResult.of(pageNo, pageSize, records.size(), List.copyOf(records.values()));
        }

        @Override
        public BackupId insert(BackupRecord record) {
            BackupId id = BackupId.of((long) records.size() + 1L);
            records.put(id.value(), record);
            return id;
        }

        @Override
        public int update(BackupRecord record) {
            return 0;
        }

        @Override
        public int deleteById(BackupId id) {
            return id != null && records.remove(id.value()) != null ? 1 : 0;
        }

        @Override
        public List<BackupId> listExpiredBackupIds(Date now, int limit) {
            lastRequestedAt = now;
            lastLimit = limit;
            return expiredBackupIds.stream().limit(limit).toList();
        }
    }

    private static final class FakeClassicsFacade implements ClassicsFacade {
        private List<ClassicsCleanupTargetsFacadeResponse.Target> targets = List.of();
        private List<ClassicsCleanupExecutionFacadeResponse.ItemResult> executionResults = List.of();
        private ClassicsCleanupTargetsFacadeRequest lastListRequest;

        @Override
        public ClassicsSummaryFacadeResponse summary(ClassicsSummaryFacadeRequest request) {
            return null;
        }

        @Override
        public ClassicsPublicContentsFacadeResponse listPublicContents() {
            return null;
        }

        @Override
        public ClassicsPublicContentFacadeResponse getPublicContent(ClassicsPublicContentFacadeRequest request) {
            return null;
        }

        @Override
        public ClassicsQaKnowledgeFacadeResponse getQaKnowledge(ClassicsQaKnowledgeFacadeRequest request) {
            return null;
        }

        @Override
        public ClassicsCleanupTargetsFacadeResponse listCleanupTargets(ClassicsCleanupTargetsFacadeRequest request) {
            lastListRequest = request;
            return ClassicsCleanupTargetsFacadeResponse.builder()
                    .cleanupType(request.getCleanupType())
                    .supported(true)
                    .targets(targets)
                    .build();
        }

        @Override
        public ClassicsCleanupExecutionFacadeResponse executeCleanupTargets(
                ClassicsCleanupTargetsFacadeRequest request) {
            return ClassicsCleanupExecutionFacadeResponse.builder()
                    .cleanupType(request.getCleanupType())
                    .supported(true)
                    .itemResults(new ArrayList<>(executionResults))
                    .build();
        }
    }
}
