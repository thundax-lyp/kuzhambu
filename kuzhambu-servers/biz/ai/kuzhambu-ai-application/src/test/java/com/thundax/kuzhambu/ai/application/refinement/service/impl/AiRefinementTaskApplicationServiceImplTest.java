package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.invocation.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.refinement.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class AiRefinementTaskApplicationServiceImplTest {

    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    @Test
    void addTaskShouldCreateSingleUnitBatchJobAndRecordSuccess() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
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
                new AiRefinementTaskApplicationServiceImpl(batchJobService, refinementService, null, DIRECT_EXECUTOR);

        AiRefinementTaskResult accepted = service.addTask(command(AiBusinessCapability.CLASSICS_TRANSLATE.value()));

        assertEquals(1, batchJobService.created.getTotalCount());
        assertEquals(AiBusinessCapability.CLASSICS_TRANSLATE.value(), accepted.getCapability());
        assertEquals("SUCCEEDED", batchJobService.get(accepted.getTaskId()).getStatus());
        assertEquals(1, batchJobService.get(accepted.getTaskId()).getSuccessCount());
        assertEquals(accepted.getTaskId(), refinementService.lastCommand.getBatchId());
    }

    @Test
    void addTaskShouldNormalizeLegacyCapabilityBeforeCreatingBatchJob() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
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
                new AiRefinementTaskApplicationServiceImpl(batchJobService, refinementService, null, DIRECT_EXECUTOR);
        AiRefinementRequestCommand command = command("translate");

        service.addTask(command);

        assertEquals(AiBusinessCapability.CLASSICS_TRANSLATE.value(), command.getCapability());
        assertEquals(AiBusinessCapability.CLASSICS_TRANSLATE.value(), batchJobService.created.getCapability());
    }

    @Test
    void addTaskShouldNormalizeLegacyFusionCapabilityAndInvokeFusionUseCase() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(new AiCandidateResult(
                101L,
                201L,
                "SUCCEEDED",
                AiBusinessCapability.CLASSICS_IMAGE_PROMPT_FUSION.value(),
                null,
                "TEXT",
                "融合说明",
                null,
                null));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(batchJobService, refinementService, null, DIRECT_EXECUTOR);
        AiRefinementRequestCommand command = command("fusion");

        service.addTask(command);

        assertEquals(AiBusinessCapability.CLASSICS_IMAGE_PROMPT_FUSION.value(), command.getCapability());
        assertEquals(
                AiBusinessCapability.CLASSICS_IMAGE_PROMPT_FUSION.value(), batchJobService.created.getCapability());
        assertEquals(1, refinementService.fusionInvokeCount);
    }

    @Test
    void addTaskShouldExecuteOnlyAfterTransactionCommitWhenSynchronizationIsActive() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
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
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(batchJobService, refinementService, null, DIRECT_EXECUTOR);

        TransactionSynchronizationManager.initSynchronization();
        AiRefinementTaskResult accepted;
        try {
            accepted = service.addTask(command(AiBusinessCapability.CLASSICS_SUMMARY.value()));
            assertEquals("RUNNING", batchJobService.get(accepted.getTaskId()).getStatus());
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(synchronization -> synchronization.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        assertEquals("SUCCEEDED", batchJobService.get(accepted.getTaskId()).getStatus());
    }

    @Test
    void addTaskShouldRecordFailureThroughBatchJobRule() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
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
                new AiRefinementTaskApplicationServiceImpl(batchJobService, refinementService, null, DIRECT_EXECUTOR);

        AiRefinementTaskResult accepted =
                service.addTask(command(AiBusinessCapability.CLASSICS_IMAGE_GENERATE.value()));
        AiBatchJobResult completed = batchJobService.get(accepted.getTaskId());

        assertTrue(accepted.isStreamEnabled());
        assertEquals("FAILED", completed.getStatus());
        assertEquals(1, completed.getFailedCount());
        assertTrue(completed.getFailureSummaryJson().contains("WORKER_PROTOCOL_FAILURE"));
    }

    private AiRefinementRequestCommand command(String capability) {
        AiRefinementRequestCommand command = new AiRefinementRequestCommand();
        command.setCapability(capability);
        command.setScope("classics");
        command.setOperation(capability);
        command.setContentType("SANCAI_ENTRY");
        command.setContentId(10L);
        command.setObjectId(20L);
        command.setModelId(40L);
        command.setModelName("model-a");
        command.setPromptVersionId(50L);
        command.setRequestId("req-1");
        command.setTraceId("trace-1");
        command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"hello\"}]");
        command.setInputPayloadJson("{\"text\":\"hello\"}");
        return command;
    }

    private static final class RecordingBatchJobService implements AiBatchJobApplicationService {

        private final AtomicLong sequence = new AtomicLong(1000L);
        private final List<AiBatchJobResult> jobs = new ArrayList<>();
        private AiBatchJobCreateCommand created;

        @Override
        public AiBatchJobResult get(Long batchId) {
            return jobs.stream()
                    .filter(job -> job.getBatchId().equals(batchId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResult<AiBatchJobResult> page(
                String scope,
                String capability,
                String status,
                String contentType,
                Long contentId,
                PageQuery pageQuery) {
            return PageResult.of(1, 10, jobs.size(), jobs);
        }

        @Override
        public Long create(AiBatchJobCreateCommand command) {
            created = command;
            long batchId = sequence.incrementAndGet();
            jobs.add(new AiBatchJobResult(
                    batchId,
                    command.getScope(),
                    command.getCapability(),
                    command.getContentType(),
                    command.getContentId(),
                    "RUNNING",
                    command.getTotalCount(),
                    0,
                    0,
                    0,
                    command.getFailureSummaryJson(),
                    Instant.parse("2026-01-01T00:00:00Z"),
                    null,
                    null));
            return batchId;
        }

        @Override
        public boolean canDispatchNextUnit(Long batchId) {
            return true;
        }

        @Override
        public AiBatchJobResult recordSuccess(Long batchId) {
            AiBatchJobResult job = get(batchId);
            AiBatchJobResult updated = copy(job, "SUCCEEDED", 1, 0, null);
            replace(updated);
            return updated;
        }

        @Override
        public AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson) {
            AiBatchJobResult job = get(batchId);
            AiBatchJobResult updated = copy(job, "FAILED", 0, 1, failureSummaryJson);
            replace(updated);
            return updated;
        }

        @Override
        public AiBatchJobResult cancel(Long batchId) {
            AiBatchJobResult job = get(batchId);
            AiBatchJobResult updated = copy(job, "CANCELLED", job.getSuccessCount(), job.getFailedCount(), null);
            replace(updated);
            return updated;
        }

        private AiBatchJobResult copy(
                AiBatchJobResult job, String status, int successCount, int failedCount, String failureSummaryJson) {
            return new AiBatchJobResult(
                    job.getBatchId(),
                    job.getScope(),
                    job.getCapability(),
                    job.getContentType(),
                    job.getContentId(),
                    status,
                    job.getTotalCount(),
                    successCount,
                    failedCount,
                    job.getCancelledCount(),
                    failureSummaryJson,
                    job.getRequestedAt(),
                    "CANCELLED".equals(status) ? Instant.parse("2026-01-01T00:01:00Z") : job.getCancelledAt(),
                    !"RUNNING".equals(status) ? Instant.parse("2026-01-01T00:02:00Z") : job.getCompletedAt());
        }

        private void replace(AiBatchJobResult updated) {
            jobs.removeIf(job -> job.getBatchId().equals(updated.getBatchId()));
            jobs.add(updated);
        }
    }

    private static class StubRefinementApplicationService implements AiRefinementApplicationService {

        private final AiCandidateResult result;
        private int fusionInvokeCount;
        private AiRefinementRequestCommand lastCommand;

        StubRefinementApplicationService(AiCandidateResult result) {
            this.result = result;
        }

        @Override
        public void snapshotInvokeConfig(AiRefinementRequestCommand command) {}

        @Override
        public void validateSnapshotInvokeConfig(AiRefinementRequestCommand command) {}

        @Override
        public AiCandidateResult translate(AiRefinementRequestCommand command) {
            lastCommand = command;
            return result;
        }

        @Override
        public AiCandidateResult summarize(AiRefinementRequestCommand command) {
            lastCommand = command;
            return result;
        }

        @Override
        public AiCandidateResult generateTags(AiRefinementRequestCommand command) {
            lastCommand = command;
            return result;
        }

        @Override
        public AiCandidateResult generateQa(AiRefinementRequestCommand command) {
            lastCommand = command;
            return result;
        }

        @Override
        public AiCandidateResult analyzeImage(AiRefinementRequestCommand command) {
            lastCommand = command;
            return result;
        }

        @Override
        public AiCandidateResult fuseVisualContext(AiRefinementRequestCommand command) {
            lastCommand = command;
            fusionInvokeCount++;
            return result;
        }

        @Override
        public AiCandidateResult generateImage(AiRefinementRequestCommand command) {
            lastCommand = command;
            return result;
        }

        @Override
        public AiCandidateResult describeVisual(AiRefinementRequestCommand command) {
            lastCommand = command;
            return result;
        }

        @Override
        public AiCandidateResult splitEntry(AiRefinementRequestCommand command) {
            lastCommand = command;
            return result;
        }
    }
}
