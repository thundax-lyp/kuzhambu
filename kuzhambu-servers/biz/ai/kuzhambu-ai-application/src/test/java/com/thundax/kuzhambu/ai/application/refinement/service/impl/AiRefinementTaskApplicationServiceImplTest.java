package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.domain.refinement.repository.AiRefinementTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class AiRefinementTaskApplicationServiceImplTest {

    @Test
    void addTaskShouldMarkSancaiImageAnalysisStreamAndPersistSucceededCandidate() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(
                new AiCandidateResult(101L, 201L, "SUCCEEDED", "image_analysis", null, "MARKDOWN", "候选正文", null, null));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService);

        AiRefinementTask accepted = service.addTask(command("image_analysis"));
        AiRefinementTask completed = awaitTerminal(repository, accepted.getTaskId());

        assertTrue(accepted.isStreamEnabled());
        assertEquals("SUCCEEDED", completed.getStatus());
        assertTrue(completed.isStreamEnabled());
        assertEquals(101L, completed.getCallId());
        assertEquals(201L, completed.getCandidateId());
        assertEquals("MARKDOWN", completed.getResultFormat());
        assertEquals("候选正文", completed.getResultPreview());
    }

    @Test
    void addTaskShouldKeepStreamFailureWithoutCandidate() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(new AiCandidateResult(
                101L,
                201L,
                "FAILED",
                "image_gen",
                "WORKER_STREAM",
                null,
                null,
                "WORKER_PROTOCOL_FAILURE",
                "Worker stream ended without completed event"));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService);

        AiRefinementTask accepted = service.addTask(command("image_gen"));
        AiRefinementTask completed = awaitTerminal(repository, accepted.getTaskId());

        assertTrue(completed.isStreamEnabled());
        assertEquals("FAILED", completed.getStatus());
        assertEquals(101L, completed.getCallId());
        assertNull(completed.getCandidateId());
        assertEquals("WORKER_STREAM", completed.getFailureStage());
        assertEquals("WORKER_PROTOCOL_FAILURE", completed.getErrorType());
        assertEquals("Worker stream ended without completed event", completed.getErrorMessage());
    }

    @Test
    void addTaskShouldExecuteOnlyAfterTransactionCommitWhenSynchronizationIsActive() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(
                new AiCandidateResult(101L, 201L, "SUCCEEDED", "translate", null, "TEXT", "译文", null, null));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService);

        TransactionSynchronizationManager.initSynchronization();
        AiRefinementTask accepted;
        try {
            accepted = service.addTask(command("translate"));
            assertEquals("PENDING", repository.get(accepted.getTaskId()).getStatus());
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(synchronization -> synchronization.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        AiRefinementTask completed = awaitTerminal(repository, accepted.getTaskId());
        assertEquals("SUCCEEDED", completed.getStatus());
        assertEquals("译文", completed.getResultPreview());
    }

    private AiRefinementRequestCommand command(String capability) {
        AiRefinementRequestCommand command = new AiRefinementRequestCommand();
        command.setCapability(capability);
        command.setScope("classics");
        command.setOperation(capability);
        command.setContentType("SANCAI_ENTRY");
        command.setContentId(10L);
        command.setObjectId(20L);
        command.setRequestedBy(30L);
        command.setModelId(40L);
        command.setModelName("model-a");
        command.setPromptVersionId(50L);
        command.setRequestId("req-1");
        command.setTraceId("trace-1");
        command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"hello\"}]");
        command.setInputPayloadJson("{\"text\":\"hello\"}");
        return command;
    }

    private AiRefinementTask awaitTerminal(RecordingTaskRepository repository, Long taskId) {
        long deadline = System.currentTimeMillis() + 3000L;
        AiRefinementTask task = repository.get(taskId);
        while (System.currentTimeMillis() < deadline) {
            task = repository.get(taskId);
            if (task != null && ("SUCCEEDED".equals(task.getStatus()) || "FAILED".equals(task.getStatus()))) {
                return task;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("interrupted while waiting for task completion", exception);
            }
        }
        assertNotNull(task);
        return task;
    }

    private static class StubRefinementApplicationService implements AiRefinementApplicationService {

        private final AiCandidateResult result;

        StubRefinementApplicationService(AiCandidateResult result) {
            this.result = result;
        }

        @Override
        public AiCandidateResult translate(AiRefinementRequestCommand command) {
            return result;
        }

        @Override
        public AiCandidateResult summarize(AiRefinementRequestCommand command) {
            return result;
        }

        @Override
        public AiCandidateResult generateTags(AiRefinementRequestCommand command) {
            return result;
        }

        @Override
        public AiCandidateResult generateQa(AiRefinementRequestCommand command) {
            return result;
        }

        @Override
        public AiCandidateResult analyzeImage(AiRefinementRequestCommand command) {
            return result;
        }

        @Override
        public AiCandidateResult fuseVisualContext(AiRefinementRequestCommand command) {
            return result;
        }

        @Override
        public AiCandidateResult generateImage(AiRefinementRequestCommand command) {
            return result;
        }

        @Override
        public AiCandidateResult describeVisual(AiRefinementRequestCommand command) {
            return result;
        }

        @Override
        public AiCandidateResult splitEntry(AiRefinementRequestCommand command) {
            return result;
        }
    }

    private static class RecordingTaskRepository implements AiRefinementTaskRepository {

        private final AtomicLong sequence = new AtomicLong(1000L);
        private final Map<Long, AiRefinementTask> tasks = new ConcurrentHashMap<>();

        @Override
        public AiRefinementTask get(Long taskId) {
            return tasks.get(taskId);
        }

        @Override
        public Long insert(AiRefinementTask task) {
            long taskId = sequence.incrementAndGet();
            task.setTaskId(taskId);
            tasks.put(taskId, task);
            return taskId;
        }

        @Override
        public int update(AiRefinementTask task) {
            tasks.put(task.getTaskId(), task);
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
            return new ArrayList<>(tasks.values());
        }

        @Override
        public long countTasks(String capability, String status, String contentType, Long contentId, Long requestedBy) {
            return tasks.size();
        }

        @Override
        public List<AiRefinementTask> listExpiredRunningTasks(Instant threshold) {
            return List.of();
        }

        @Override
        public int deleteExpiredTerminalTasks(Instant threshold) {
            return 0;
        }
    }
}
