package com.thundax.kuzhambu.operations.application.cleanup.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupDetailQuery;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupPageQuery;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupPageResult;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupItem;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupJob;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupItemId;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import com.thundax.kuzhambu.operations.domain.cleanup.repository.CleanupJobRepository;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CleanupApplicationServiceImplTest {

    @Test
    void executeShouldPersistSucceededCleanupJob() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        CleanupApplicationServiceImpl service = new CleanupApplicationServiceImpl(repository);

        OperationsCleanupDetailResult result =
                service.execute(new OperationsCleanupExecuteCommand("EXPIRED_BACKUP", 1001L));

        assertNotNull(result.getCleanupId());
        assertEquals("EXPIRED_BACKUP", result.getCleanupType());
        assertEquals("SUCCEEDED", result.getCleanupStatus());
        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());
    }

    @Test
    void executeShouldRejectUnsupportedCleanupType() {
        InMemoryCleanupJobRepository repository = new InMemoryCleanupJobRepository();
        CleanupApplicationServiceImpl service = new CleanupApplicationServiceImpl(repository);

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

        CleanupApplicationServiceImpl service = new CleanupApplicationServiceImpl(repository);

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
            return id == null ? null : jobs.get(id.value());
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
}
