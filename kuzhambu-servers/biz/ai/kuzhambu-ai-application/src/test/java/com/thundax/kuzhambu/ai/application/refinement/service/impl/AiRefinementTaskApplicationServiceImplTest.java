package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.domain.refinement.repository.AiRefinementTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class AiRefinementTaskApplicationServiceImplTest {

    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    @Test
    void addTaskShouldMarkSancaiImageAnalysisStreamAndPersistSucceededCandidate() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(new AiCandidateResult(
                101L,
                201L,
                "SUCCEEDED",
                AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE.value(),
                null,
                "MARKDOWN",
                "候选正文",
                null,
                null));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService, DIRECT_EXECUTOR);

        AiRefinementTask accepted = service.addTask(command(AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE.value()));
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
                AiBusinessCapability.CLASSICS_IMAGE_GENERATE.value(),
                "WORKER_STREAM",
                null,
                null,
                "WORKER_PROTOCOL_FAILURE",
                "Worker stream ended without completed event"));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService, DIRECT_EXECUTOR);

        AiRefinementTask accepted = service.addTask(command(AiBusinessCapability.CLASSICS_IMAGE_GENERATE.value()));
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
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(new AiCandidateResult(
                101L,
                201L,
                "SUCCEEDED",
                AiBusinessCapability.CLASSICS_TRANSLATE.value(),
                null,
                "TEXT",
                "译文",
                null,
                null));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService, DIRECT_EXECUTOR);

        TransactionSynchronizationManager.initSynchronization();
        AiRefinementTask accepted;
        try {
            accepted = service.addTask(command(AiBusinessCapability.CLASSICS_TRANSLATE.value()));
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

    @Test
    void addTaskShouldNormalizeLegacyCapabilityBeforePersistAndExecute() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(new AiCandidateResult(
                101L,
                201L,
                "SUCCEEDED",
                AiBusinessCapability.CLASSICS_TRANSLATE.value(),
                null,
                "TEXT",
                "译文",
                null,
                null));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService, DIRECT_EXECUTOR);

        AiRefinementRequestCommand command = command("translate");
        AiRefinementTask accepted = service.addTask(command);
        AiRefinementTask completed = awaitTerminal(repository, accepted.getTaskId());

        assertEquals(AiBusinessCapability.CLASSICS_TRANSLATE.value(), command.getCapability());
        assertEquals(AiBusinessCapability.CLASSICS_TRANSLATE.value(), completed.getCapability());
        assertEquals("SUCCEEDED", completed.getStatus());
    }

    @Test
    void addTaskShouldAcceptBusinessPayloadAndPersistResolvedPromptConfig() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(new AiCandidateResult(
                101L,
                201L,
                "SUCCEEDED",
                AiBusinessCapability.CLASSICS_SUMMARY.value(),
                null,
                "TEXT",
                "摘要",
                null,
                null));
        refinementService.setResolvedPromptConfig("PRIMARY", 2001L, "gpt-4o", 940106L);
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService, DIRECT_EXECUTOR);
        AiRefinementRequestCommand command = command(AiBusinessCapability.CLASSICS_SUMMARY.value());
        command.setServiceRole(null);
        command.setModelId(null);
        command.setModelName(null);
        command.setPromptVersionId(null);
        command.setPromptMessagesJson(null);

        AiRefinementTask accepted = service.addTask(command);
        AiRefinementTask completed = awaitTerminal(repository, accepted.getTaskId());

        assertNull(accepted.getPromptVersionId());
        assertEquals("SUCCEEDED", completed.getStatus());
        assertEquals("PRIMARY", completed.getServiceRole());
        assertEquals(2001L, completed.getModelId());
        assertEquals("gpt-4o", completed.getModelName());
        assertEquals(940106L, completed.getPromptVersionId());
    }

    @Test
    void conditionalUpdateShouldKeepCancelledWhenWorkerCompletionArrivesLate() {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        AiRefinementTask task = task(AiBusinessCapability.CLASSICS_TRANSLATE.value(), "RUNNING");
        repository.insertWithTaskId(task);
        AiRefinementTask staleRunningTask = repository.get(task.getTaskId());
        staleRunningTask.markSucceeded(101L, 201L, "TEXT", "译文", Instant.now());
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(new AiCandidateResult(
                101L,
                201L,
                "SUCCEEDED",
                AiBusinessCapability.CLASSICS_TRANSLATE.value(),
                null,
                "TEXT",
                "译文",
                null,
                null));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService, DIRECT_EXECUTOR);

        service.cancelTask(task.getTaskId(), task.getRequestedBy());
        int updated = repository.updateWhenStatusIn(staleRunningTask, List.of("RUNNING"));

        assertEquals(0, updated);
        assertEquals("CANCELLED", repository.get(task.getTaskId()).getStatus());
        assertNull(repository.get(task.getTaskId()).getCallId());
    }

    @Test
    void cancelTaskShouldPublishStreamTerminalEvent() throws Exception {
        RecordingTaskRepository repository = new RecordingTaskRepository();
        AiRefinementTask task = task(AiBusinessCapability.CLASSICS_IMAGE_GENERATE.value(), "RUNNING");
        task.setStreamEnabled(true);
        repository.insertWithTaskId(task);
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(null);
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(repository, refinementService, DIRECT_EXECUTOR);
        List<String> statuses = new ArrayList<>();
        CompletableFuture<Void> subscription =
                CompletableFuture.runAsync(() -> service.streamTaskEvents(task.getTaskId(), event -> {
                    if (event.isError()) {
                        statuses.add(event.getStatus());
                    }
                }));

        Thread.sleep(20L);
        service.cancelTask(task.getTaskId(), task.getRequestedBy());
        subscription.get(1L, TimeUnit.SECONDS);

        assertEquals(List.of("CANCELLED"), statuses);
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

    private AiRefinementTask task(String capability, String status) {
        AiRefinementTask task = new AiRefinementTask();
        task.setTaskId(9001L);
        task.setScope("classics");
        task.setCapability(capability);
        task.setContentType("SANCAI_ENTRY");
        task.setContentId(10L);
        task.setObjectId(20L);
        task.setRequestedBy(30L);
        task.setRequestId("req-1");
        task.setTraceId("trace-1");
        task.setStatus(status);
        task.setServiceRole("PRIMARY");
        task.setModelId(40L);
        task.setModelName("model-a");
        task.setPromptVersionId(50L);
        task.setRequestedAt(Instant.now());
        task.setStartedAt(Instant.now());
        return task;
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
        private String resolvedServiceRole;
        private Long resolvedModelId;
        private String resolvedModelName;
        private Long resolvedPromptVersionId;

        StubRefinementApplicationService(AiCandidateResult result) {
            this.result = result;
        }

        void setResolvedPromptConfig(String serviceRole, Long modelId, String modelName, Long promptVersionId) {
            this.resolvedServiceRole = serviceRole;
            this.resolvedModelId = modelId;
            this.resolvedModelName = modelName;
            this.resolvedPromptVersionId = promptVersionId;
        }

        @Override
        public AiCandidateResult translate(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        @Override
        public AiCandidateResult summarize(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        @Override
        public AiCandidateResult generateTags(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        @Override
        public AiCandidateResult generateQa(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        @Override
        public AiCandidateResult analyzeImage(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        @Override
        public AiCandidateResult fuseVisualContext(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        @Override
        public AiCandidateResult generateImage(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        @Override
        public AiCandidateResult describeVisual(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        @Override
        public AiCandidateResult splitEntry(AiRefinementRequestCommand command) {
            applyResolvedPromptConfig(command);
            return result;
        }

        private void applyResolvedPromptConfig(AiRefinementRequestCommand command) {
            if (resolvedPromptVersionId == null) {
                return;
            }
            command.setServiceRole(resolvedServiceRole);
            command.setModelId(resolvedModelId);
            command.setModelName(resolvedModelName);
            command.setPromptVersionId(resolvedPromptVersionId);
        }
    }

    private static class RecordingTaskRepository implements AiRefinementTaskRepository {

        private final AtomicLong sequence = new AtomicLong(1000L);
        private final Map<Long, AiRefinementTask> tasks = new ConcurrentHashMap<>();

        @Override
        public AiRefinementTask get(Long taskId) {
            return copy(tasks.get(taskId));
        }

        @Override
        public Long insert(AiRefinementTask task) {
            long taskId = sequence.incrementAndGet();
            task.setTaskId(taskId);
            tasks.put(taskId, copy(task));
            return taskId;
        }

        void insertWithTaskId(AiRefinementTask task) {
            tasks.put(task.getTaskId(), copy(task));
        }

        @Override
        public int update(AiRefinementTask task) {
            tasks.put(task.getTaskId(), copy(task));
            return 1;
        }

        @Override
        public int updateWhenStatusIn(AiRefinementTask task, Collection<String> statuses) {
            AiRefinementTask current = tasks.get(task.getTaskId());
            if (current == null || statuses == null || !statuses.contains(current.getStatus())) {
                return 0;
            }
            tasks.put(task.getTaskId(), copy(task));
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
        public List<AiRefinementTask> listActiveTasks() {
            return List.of();
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

    private static AiRefinementTask copy(AiRefinementTask task) {
        if (task == null) {
            return null;
        }
        return new AiRefinementTask(
                task.getId(),
                task.getTaskId(),
                task.getScope(),
                task.getCapability(),
                task.getContentType(),
                task.getContentId(),
                task.getObjectId(),
                task.getRequestedBy(),
                task.getRequestId(),
                task.getTraceId(),
                task.getStatus(),
                task.getServiceRole(),
                task.getModelId(),
                task.getModelName(),
                task.getPromptVersionId(),
                task.getCallId(),
                task.getCandidateId(),
                task.getResultFormat(),
                task.getResultPreview(),
                task.getFailureStage(),
                task.getErrorType(),
                task.getErrorMessage(),
                task.isStreamEnabled(),
                task.getRequestedAt(),
                task.getStartedAt(),
                task.getCompletedAt(),
                task.getCancelledAt());
    }
}
