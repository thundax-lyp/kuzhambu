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
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupDetailQuery;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupQuery;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupPageResult;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthAlertStrategy;
import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import com.thundax.kuzhambu.operations.domain.cleanup.codec.CleanupItemIdCodec;
import com.thundax.kuzhambu.operations.domain.cleanup.codec.CleanupJobIdCodec;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupItem;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupJob;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupItemId;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import com.thundax.kuzhambu.operations.domain.cleanup.repository.CleanupJobRepository;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import com.thundax.kuzhambu.operations.domain.report.codec.ReportIdCodec;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import com.thundax.kuzhambu.operations.domain.task.codec.LongTaskSnapshotIdCodec;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CleanupApplicationServiceImplTest {

    @Test
    void executeShouldPersistSucceededCleanupJob() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        InMemoryBackupRepository backupRepository = new InMemoryBackupRepository();
        backupRepository.expiredBackupIds = List.of(BackupIdCodec.toDomain(101L), BackupIdCodec.toDomain(102L));
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
        backupRepository.expiredBackupIds = List.of(BackupIdCodec.toDomain(101L), BackupIdCodec.toDomain(102L));
        backupRepository.records.put(101L, new BackupRecord());
        backupRepository.records.put(102L, new BackupRecord());
        CleanupApplicationServiceImpl service =
                new CleanupApplicationServiceImpl(repository, backupRepository, new FakeClassicsFacade());

        OperationsCleanupDetailResult result = service.executeScheduled(new OperationsCleanupExecuteCommand(
                "EXPIRED_BACKUP", null, Instant.ofEpochMilli(1_719_630_400_000L), 30, 1));

        assertEquals(null, result.getRequesterUserId());
        assertEquals(1, result.getTotalCount());
        assertEquals(1, backupRepository.lastLimit);
        assertEquals(Instant.ofEpochMilli(1_717_038_400_000L), backupRepository.lastRequestedAt);
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

        service.executeScheduled(new OperationsCleanupExecuteCommand(
                "EXPIRED_SHARE", null, Instant.ofEpochMilli(1_719_630_400_000L), 90, 5));

        assertEquals(90, classicsFacade.lastListRequest.getRetentionDays());
        assertEquals(5, classicsFacade.lastListRequest.getLimit());
    }

    @Test
    void executeShouldCleanExpiredRuntimeTargetsAndPersistTargetTypes() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        InMemoryReportRepository reportRepository = new InMemoryReportRepository();
        reportRepository.expiredReportIds = List.of(ReportIdCodec.toDomain(301L));
        reportRepository.records.put(301L, new ReportRecord());
        CleanupApplicationServiceImpl service = new CleanupApplicationServiceImpl(
                repository,
                new InMemoryBackupRepository(),
                reportRepository,
                new InMemoryHealthCheckRepository(),
                new InMemoryLongTaskSnapshotRepository(),
                new FakeClassicsFacade(),
                null);

        OperationsCleanupDetailResult result =
                service.execute(new OperationsCleanupExecuteCommand("EXPIRED_REPORT", 1001L));

        assertEquals("SUCCEEDED", result.getCleanupStatus());
        assertEquals(1, result.getSuccessCount());
        CleanupItem item = repository.listItemsByJobId(result.getCleanupId()).get(0);
        assertEquals("report", item.getTargetType());
        assertEquals(301L, item.getTargetId());
        assertEquals(false, reportRepository.records.containsKey(301L));
    }

    @Test
    void executeShouldPersistFailureReasonForRuntimeTargetDeleteMiss() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        InMemoryLongTaskSnapshotRepository longTaskRepository = new InMemoryLongTaskSnapshotRepository();
        longTaskRepository.expiredSnapshotIds = List.of(LongTaskSnapshotIdCodec.toDomain(501L));
        CleanupApplicationServiceImpl service = new CleanupApplicationServiceImpl(
                repository,
                new InMemoryBackupRepository(),
                new InMemoryReportRepository(),
                new InMemoryHealthCheckRepository(),
                longTaskRepository,
                new FakeClassicsFacade(),
                null);

        OperationsCleanupDetailResult result =
                service.execute(new OperationsCleanupExecuteCommand("EXPIRED_LONG_TASK", 1001L));

        assertEquals("FAILED", result.getCleanupStatus());
        CleanupItem item = repository.listItemsByJobId(result.getCleanupId()).get(0);
        assertEquals("long-task", item.getTargetType());
        assertEquals("FAILED", item.getItemStatus());
        assertEquals("TARGET_NOT_FOUND", item.getFailureReason());
    }

    @Test
    void executeScheduledShouldApplyRetentionAndLimitToHealthCheckTargets() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        InMemoryHealthCheckRepository healthCheckRepository = new InMemoryHealthCheckRepository();
        healthCheckRepository.expiredCheckIds =
                List.of(HealthCheckIdCodec.toDomain(401L), HealthCheckIdCodec.toDomain(402L));
        healthCheckRepository.records.put(401L, new HealthCheckRecord());
        healthCheckRepository.records.put(402L, new HealthCheckRecord());
        CleanupApplicationServiceImpl service = new CleanupApplicationServiceImpl(
                repository,
                new InMemoryBackupRepository(),
                new InMemoryReportRepository(),
                healthCheckRepository,
                new InMemoryLongTaskSnapshotRepository(),
                new FakeClassicsFacade(),
                null);

        OperationsCleanupDetailResult result = service.executeScheduled(new OperationsCleanupExecuteCommand(
                "EXPIRED_HEALTH_CHECK", null, Instant.ofEpochMilli(1_719_630_400_000L), 30, 1));

        assertEquals("SUCCEEDED", result.getCleanupStatus());
        assertEquals(1, result.getTotalCount());
        assertEquals(1, healthCheckRepository.lastLimit);
        assertEquals(Instant.ofEpochMilli(1_717_038_400_000L), healthCheckRepository.lastCheckedBefore);
        assertEquals(
                "health-check",
                repository.listItemsByJobId(result.getCleanupId()).get(0).getTargetType());
    }

    @Test
    void executeShouldRejectUnsupportedCleanupType() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        CleanupApplicationServiceImpl service =
                new CleanupApplicationServiceImpl(repository, new InMemoryBackupRepository(), new FakeClassicsFacade());

        assertThrows(
                BizException.class, () -> service.execute(new OperationsCleanupExecuteCommand("UNSUPPORTED", 1001L)));
    }

    @Test
    void pageAndDetailShouldMapRepositoryRecords() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        CleanupJob cleanupJob = new CleanupJob(
                CleanupJobIdCodec.toDomain(9001L),
                "EXPIRED_BACKUP",
                "SUCCEEDED",
                3,
                2,
                1,
                null,
                1001L,
                Instant.ofEpochMilli(1_719_630_400_000L),
                Instant.ofEpochMilli(1_719_630_500_000L),
                List.of());
        repository.jobs.put(9001L, cleanupJob);

        CleanupApplicationServiceImpl service =
                new CleanupApplicationServiceImpl(repository, new InMemoryBackupRepository(), new FakeClassicsFacade());

        PageResult<OperationsCleanupPageResult> pageResult =
                service.page(new OperationsCleanupQuery("EXPIRED_BACKUP", "SUCCEEDED", 1001L), new PageQuery(1, 10));
        OperationsCleanupDetailResult detailResult =
                service.detail(new OperationsCleanupDetailQuery(CleanupJobIdCodec.toDomain(9001L)));

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
            CleanupJobId cleanupJobId = CleanupJobIdCodec.toDomain(nextCleanupId++);
            job.setId(cleanupJobId);
            job.setStartedAt(Instant.ofEpochMilli(1_719_000_000_000L));
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
            CleanupItemId itemId = CleanupItemIdCodec.toDomain(nextItemId++);
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
        public int deleteByJobId(CleanupJobId jobId) {
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
        private Instant lastRequestedAt;
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
            BackupId id = BackupIdCodec.toDomain((long) records.size() + 1L);
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
        public List<BackupId> listExpiredBackupIds(Instant now, int limit) {
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
        public ClassicsPublicContentsFacadeResponse listWorkbenchCategoryContents() {
            return null;
        }

        @Override
        public ClassicsPublicContentsFacadeResponse listWorkbenchVolumeContents() {
            return null;
        }

        @Override
        public ClassicsPublicContentsFacadeResponse listWorkbenchContents() {
            return null;
        }

        @Override
        public ClassicsPublicContentsFacadeResponse listWorkbenchContents(String categoryCode, String volumeCode) {
            return null;
        }

        @Override
        public ClassicsPublicContentFacadeResponse getWorkbenchContent(ClassicsPublicContentFacadeRequest request) {
            return null;
        }

        @Override
        public ClassicsQaKnowledgeFacadeResponse getQaKnowledge(ClassicsQaKnowledgeFacadeRequest request) {
            return null;
        }

        @Override
        public ClassicsQaKnowledgeFacadeResponse getWorkbenchQaKnowledge(ClassicsQaKnowledgeFacadeRequest request) {
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

    private static final class InMemoryReportRepository implements ReportRepository {
        private final Map<Long, ReportRecord> records = new LinkedHashMap<>();
        private List<ReportId> expiredReportIds = List.of();

        @Override
        public ReportRecord getById(ReportId id) {
            return id == null ? null : records.get(id.value());
        }

        @Override
        public PageResult<ReportRecord> page(
                String reportType,
                String format,
                String reportStatus,
                Long requesterUserId,
                Instant periodStart,
                Instant periodEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, records.size(), List.copyOf(records.values()));
        }

        @Override
        public ReportId insert(ReportRecord record) {
            ReportId id = ReportIdCodec.toDomain((long) records.size() + 1L);
            records.put(id.value(), record);
            return id;
        }

        @Override
        public int update(ReportRecord record) {
            return 0;
        }

        @Override
        public int deleteById(ReportId id) {
            return id != null && records.remove(id.value()) != null ? 1 : 0;
        }

        @Override
        public List<ReportId> listExpiredReportIds(Instant requestedBefore, int limit) {
            return expiredReportIds.stream().limit(limit).toList();
        }
    }

    private static final class InMemoryHealthCheckRepository implements HealthCheckRepository {
        private final Map<Long, HealthCheckRecord> records = new LinkedHashMap<>();
        private List<HealthCheckId> expiredCheckIds = List.of();
        private Instant lastCheckedBefore;
        private int lastLimit;

        @Override
        public HealthCheckRecord getById(HealthCheckId id) {
            return id == null ? null : records.get(id.value());
        }

        @Override
        public List<HealthCheckRecord> listLatestByComponent() {
            return List.copyOf(records.values());
        }

        @Override
        public PageResult<HealthCheckRecord> page(
                String component,
                String healthStatus,
                String probeSource,
                String probeTarget,
                Instant checkedAtStart,
                Instant checkedAtEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, records.size(), List.copyOf(records.values()));
        }

        @Override
        public List<HealthTrendBucket> listTrend(
                String component, String probeSource, Instant periodStart, Instant periodEnd, String bucketType) {
            return List.of();
        }

        @Override
        public HealthCheckId insert(HealthCheckRecord record) {
            HealthCheckId id = HealthCheckIdCodec.toDomain((long) records.size() + 1L);
            records.put(id.value(), record);
            return id;
        }

        @Override
        public int update(HealthCheckRecord record) {
            return 0;
        }

        @Override
        public int deleteById(HealthCheckId id) {
            return id != null && records.remove(id.value()) != null ? 1 : 0;
        }

        @Override
        public List<HealthCheckId> listExpiredCheckIds(Instant checkedBefore, int limit) {
            lastCheckedBefore = checkedBefore;
            lastLimit = limit;
            return expiredCheckIds.stream().limit(limit).toList();
        }
    }

    private static final class InMemoryLongTaskSnapshotRepository implements LongTaskSnapshotRepository {
        private final Map<Long, LongTaskSnapshot> records = new LinkedHashMap<>();
        private List<LongTaskSnapshotId> expiredSnapshotIds = List.of();

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
            LongTaskSnapshotId id = LongTaskSnapshotIdCodec.toDomain((long) records.size() + 1L);
            records.put(id.value(), snapshot);
            return id;
        }

        @Override
        public int update(LongTaskSnapshot snapshot) {
            return 0;
        }

        @Override
        public int deleteById(LongTaskSnapshotId id) {
            return id != null && records.remove(id.value()) != null ? 1 : 0;
        }

        @Override
        public List<LongTaskSnapshotId> listExpiredSnapshotIds(Instant snapshotBefore, int limit) {
            return expiredSnapshotIds.stream().limit(limit).toList();
        }
    }
}
