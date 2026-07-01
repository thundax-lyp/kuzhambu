package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementTaskCleanupApplicationService.CleanupResult;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.domain.refinement.repository.AiRefinementTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiRefinementTaskCleanupApplicationServiceImplTest {

    @Test
    void cleanupShouldExpireRunningTasksAndDeleteTerminalTasks() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.expiredRunningTasks.add(task(1001L, "PENDING"));
        repository.expiredRunningTasks.add(task(1002L, "RUNNING"));
        repository.deletedTerminalCount = 3;

        AiRefinementTaskCleanupApplicationServiceImpl service =
                new AiRefinementTaskCleanupApplicationServiceImpl(repository) {
                    @Override
                    protected Instant now() {
                        return Instant.parse("2026-07-01T12:00:00Z");
                    }
                };

        CleanupResult result = service.cleanupExpiredTasks();

        assertEquals(2, result.expiredRunningCount());
        assertEquals(3, result.deletedTerminalCount());
        assertEquals(2, repository.updatedTasks.size());
        assertEquals("FAILED", repository.updatedTasks.get(0).getStatus());
        assertEquals("TASK_EXPIRED", repository.updatedTasks.get(0).getErrorType());
        assertEquals("WORKER_RESULT", repository.updatedTasks.get(0).getFailureStage());
        assertEquals(Instant.parse("2026-07-01T00:00:00Z"), repository.listExpiredRunningThreshold);
        assertEquals(Instant.parse("2026-07-01T00:00:00Z"), repository.deletedTerminalThreshold);
    }

    @Test
    void cleanupShouldHandleEmptyExpiredTaskList() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        repository.deletedTerminalCount = 1;

        AiRefinementTaskCleanupApplicationServiceImpl service =
                new AiRefinementTaskCleanupApplicationServiceImpl(repository) {
                    @Override
                    protected Instant now() {
                        return Instant.parse("2026-07-01T12:00:00Z");
                    }
                };

        CleanupResult result = service.cleanupExpiredTasks();

        assertEquals(0, result.expiredRunningCount());
        assertEquals(1, result.deletedTerminalCount());
        assertEquals(0, repository.updatedTasks.size());
    }

    private static AiRefinementTask task(Long taskId, String status) {
        AiRefinementTask task = new AiRefinementTask();
        task.setTaskId(taskId);
        task.setStatus(status);
        task.setRequestedAt(Instant.parse("2026-06-30T00:00:00Z"));
        return task;
    }

    private static final class RecordingTaskRepository implements AiRefinementTaskRepository {

        private final List<AiRefinementTask> expiredRunningTasks = new ArrayList<>();
        private final List<AiRefinementTask> updatedTasks = new ArrayList<>();
        private Instant listExpiredRunningThreshold;
        private Instant deletedTerminalThreshold;
        private int deletedTerminalCount;

        @Override
        public AiRefinementTask getTask(Long taskId) {
            return null;
        }

        @Override
        public Long saveTask(AiRefinementTask task) {
            return null;
        }

        @Override
        public int updateTask(AiRefinementTask task) {
            updatedTasks.add(task);
            return 1;
        }

        @Override
        public List<AiRefinementTask> listTasks(
                String capability,
                String status,
                String contentType,
                Long contentId,
                Long requestedBy,
                Integer pageNo,
                Integer pageSize) {
            return List.of();
        }

        @Override
        public long countTasks(String capability, String status, String contentType, Long contentId, Long requestedBy) {
            return 0;
        }

        @Override
        public List<AiRefinementTask> listExpiredRunningTasks(Instant threshold) {
            listExpiredRunningThreshold = threshold;
            return expiredRunningTasks;
        }

        @Override
        public int deleteExpiredTerminalTasks(Instant threshold) {
            deletedTerminalThreshold = threshold;
            return deletedTerminalCount;
        }
    }
}
