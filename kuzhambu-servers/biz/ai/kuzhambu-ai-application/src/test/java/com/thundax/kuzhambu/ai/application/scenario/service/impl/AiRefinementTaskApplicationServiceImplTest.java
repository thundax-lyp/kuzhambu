package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.invocation.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.scenario.result.AiRefinementTaskResult;
import com.thundax.kuzhambu.ai.application.scenario.service.AiRefinementApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
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

    @Test
    void addTaskShouldPreservePartialResultStatus() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
        StubRefinementApplicationService refinementService = new StubRefinementApplicationService(new AiCandidateResult(
                101L,
                201L,
                "PARTIAL",
                AiBusinessCapability.CLASSICS_IMAGE_GENERATE.value(),
                "WORKER_RESULT",
                "TEXT",
                "partial",
                "MODEL_SEMANTIC_FAILURE",
                "partial output"));
        AiRefinementTaskApplicationServiceImpl service =
                new AiRefinementTaskApplicationServiceImpl(batchJobService, refinementService, null, DIRECT_EXECUTOR);

        AiRefinementTaskResult accepted =
                service.addTask(command(AiBusinessCapability.CLASSICS_IMAGE_GENERATE.value()));
        AiBatchJobResult completed = batchJobService.get(accepted.getTaskId());

        assertEquals("PARTIAL", completed.getStatus());
        assertEquals(1, completed.getSuccessCount());
        assertTrue(completed.getFailureSummaryJson().contains("MODEL_SEMANTIC_FAILURE"));
    }

    @Test
    void taskResultShouldParseBatchFailureSummaryWhenInvocationAndCandidateAreMissing() {
        AiBatchJobResult job = new AiBatchJobResult(
                1001L,
                "classics",
                "classics_summary",
                "SANCAI_ENTRY",
                10L,
                "FAILED",
                1,
                0,
                1,
                0,
                "{\"failureStage\":\"WORKER_REQUEST\",\"errorType\":\"MODEL_CONFIG_INVALID\",\"errorMessage\":\"prompt invalid\"}",
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                Instant.parse("2026-01-01T00:01:00Z"));

        AiRefinementTaskResult result = AiRefinementTaskResult.fromBatchJob(job);

        assertEquals("WORKER_REQUEST", result.getFailureStage());
        assertEquals("MODEL_CONFIG_INVALID", result.getErrorType());
        assertEquals("prompt invalid", result.getErrorMessage());
    }

    @Test
    void expireOrphanedRunningTasksShouldFailStaleRefinementBatches() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
        Long batchId = batchJobService.create(
                new AiBatchJobCreateCommand("classics", "classics_summary", "SANCAI_ENTRY", 10L, 1, null));
        AiRefinementTaskApplicationServiceImpl service = new AiRefinementTaskApplicationServiceImpl(
                batchJobService, new StubRefinementApplicationService(null), null, DIRECT_EXECUTOR);

        service.expireOrphanedRunningTasks();

        AiBatchJobResult expired = batchJobService.get(batchId);
        assertEquals("FAILED", expired.getStatus());
        assertTrue(expired.getFailureSummaryJson().contains("TASK_ORPHANED"));
    }

    @Test
    void pageTasksShouldLoadInvocationAndCandidateByBatchIdsInBulk() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
        Long firstBatchId = batchJobService.create(
                new AiBatchJobCreateCommand("classics", "classics_summary", "SANCAI_ENTRY", 10L, 1, null));
        Long secondBatchId = batchJobService.create(
                new AiBatchJobCreateCommand("classics", "classics_tags", "SANCAI_ENTRY", 10L, 1, null));
        batchJobService.create(
                new AiBatchJobCreateCommand("knowledge", "knowledge_graph_extract", "SANCAI_ENTRY", 10L, 1, null));
        RecordingInvocationRepository invocationRepository =
                new RecordingInvocationRepository(firstBatchId, secondBatchId);
        AiRefinementTaskApplicationServiceImpl service = new AiRefinementTaskApplicationServiceImpl(
                batchJobService, new StubRefinementApplicationService(null), invocationRepository, DIRECT_EXECUTOR);

        PageResult<AiRefinementTaskResult> page = service.pageTasks(null, null, "SANCAI_ENTRY", 10L, new PageQuery());

        assertEquals(2, page.getRecords().size());
        assertEquals(1, invocationRepository.contentInvocationQueryCount);
        assertEquals(1, invocationRepository.contentCandidateQueryCount);
        assertEquals(301L, page.getRecords().get(0).getCandidateId());
        assertEquals(302L, page.getRecords().get(1).getCandidateId());
    }

    @Test
    void getTaskShouldRejectNonRefinementBatchJob() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
        Long batchId = batchJobService.create(
                new AiBatchJobCreateCommand("knowledge", "knowledge_graph_extract", "SANCAI_ENTRY", 10L, 1, null));
        AiRefinementTaskApplicationServiceImpl service = new AiRefinementTaskApplicationServiceImpl(
                batchJobService, new StubRefinementApplicationService(null), null, DIRECT_EXECUTOR);

        assertThrows(RuntimeException.class, () -> service.getTask(batchId));
    }

    @Test
    void cancelTaskShouldRejectNonRefinementBatchJob() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
        Long batchId = batchJobService.create(
                new AiBatchJobCreateCommand("knowledge", "knowledge_graph_extract", "SANCAI_ENTRY", 10L, 1, null));
        AiRefinementTaskApplicationServiceImpl service = new AiRefinementTaskApplicationServiceImpl(
                batchJobService, new StubRefinementApplicationService(null), null, DIRECT_EXECUTOR);

        assertThrows(RuntimeException.class, () -> service.cancelTask(batchId));
        assertEquals("RUNNING", batchJobService.get(batchId).getStatus());
    }

    @Test
    void pageTasksShouldKeepCandidateScopedToRequestedContentInMultiContentBatch() {
        RecordingBatchJobService batchJobService = new RecordingBatchJobService();
        Long batchId = batchJobService.create(
                new AiBatchJobCreateCommand("classics", "classics_summary", "SANCAI_ENTRY", null, 2, null));
        RecordingInvocationRepository invocationRepository = new RecordingInvocationRepository(
                List.of(
                        RecordingInvocationRepository.invocationLog(batchId, 202L, 20L, "2026-01-01T00:03:00Z"),
                        RecordingInvocationRepository.invocationLog(batchId, 201L, 10L, "2026-01-01T00:01:00Z")),
                List.of(
                        RecordingInvocationRepository.candidate(batchId, 302L, 202L, 20L, "2026-01-01T00:04:00Z"),
                        RecordingInvocationRepository.candidate(batchId, 301L, 201L, 10L, "2026-01-01T00:02:00Z")));
        AiRefinementTaskApplicationServiceImpl service = new AiRefinementTaskApplicationServiceImpl(
                batchJobService, new StubRefinementApplicationService(null), invocationRepository, DIRECT_EXECUTOR);

        PageResult<AiRefinementTaskResult> page = service.pageTasks(null, null, "SANCAI_ENTRY", 10L, new PageQuery());

        assertEquals(1, page.getRecords().size());
        assertEquals(10L, page.getRecords().get(0).getContentId());
        assertEquals(301L, page.getRecords().get(0).getCandidateId());
        assertEquals(201L, page.getRecords().get(0).getCallId());
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
            List<AiBatchJobResult> records = filtered(scope, capability == null ? List.of() : List.of(capability));
            return PageResult.of(1, 10, records.size(), records);
        }

        @Override
        public PageResult<AiBatchJobResult> pageByCapabilities(
                String scope,
                List<String> capabilities,
                String status,
                String contentType,
                Long contentId,
                PageQuery pageQuery) {
            List<AiBatchJobResult> records = filtered(scope, capabilities);
            return PageResult.of(1, 10, records.size(), records);
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
        public AiBatchJobResult recordSuccessIfRunning(Long batchId) {
            AiBatchJobResult job = get(batchId);
            if (!"RUNNING".equals(job.getStatus())) {
                return job;
            }
            return recordSuccess(batchId);
        }

        @Override
        public AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson) {
            AiBatchJobResult job = get(batchId);
            AiBatchJobResult updated = copy(job, "FAILED", 0, 1, failureSummaryJson);
            replace(updated);
            return updated;
        }

        @Override
        public AiBatchJobResult recordFailureIfRunning(Long batchId, String failureSummaryJson) {
            AiBatchJobResult job = get(batchId);
            if (!"RUNNING".equals(job.getStatus())) {
                return job;
            }
            return recordFailure(batchId, failureSummaryJson);
        }

        @Override
        public AiBatchJobResult recordPartialIfRunning(Long batchId, String failureSummaryJson) {
            AiBatchJobResult job = get(batchId);
            if (!"RUNNING".equals(job.getStatus())) {
                return job;
            }
            AiBatchJobResult updated = copy(job, "PARTIAL", 1, 0, failureSummaryJson);
            replace(updated);
            return updated;
        }

        @Override
        public int expireRunning(
                String scope,
                List<String> capabilities,
                Instant requestedBefore,
                String failureSummaryJson,
                int limit) {
            int expiredCount = 0;
            for (AiBatchJobResult job : List.copyOf(jobs)) {
                if ("RUNNING".equals(job.getStatus())
                        && scope.equals(job.getScope())
                        && capabilities.contains(job.getCapability())
                        && job.getRequestedAt().isBefore(requestedBefore)) {
                    recordFailure(job.getBatchId(), failureSummaryJson);
                    expiredCount++;
                }
            }
            return expiredCount;
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

        private List<AiBatchJobResult> filtered(String scope, List<String> capabilities) {
            List<AiBatchJobResult> records = new ArrayList<>();
            for (AiBatchJobResult job : jobs) {
                if ((scope == null || scope.equals(job.getScope()))
                        && (capabilities == null
                                || capabilities.isEmpty()
                                || capabilities.contains(job.getCapability()))) {
                    records.add(job);
                }
            }
            return records;
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

    private static final class RecordingInvocationRepository implements AiInvocationRepository {

        private final List<AiInvocationLog> invocationLogs;
        private final List<AiCandidate> candidates;
        private int batchInvocationQueryCount;
        private int batchCandidateQueryCount;
        private int contentInvocationQueryCount;
        private int contentCandidateQueryCount;

        RecordingInvocationRepository(Long firstBatchId, Long secondBatchId) {
            invocationLogs = List.of(invocationLog(firstBatchId, 201L), invocationLog(secondBatchId, 202L));
            candidates = List.of(candidate(firstBatchId, 301L, 201L), candidate(secondBatchId, 302L, 202L));
        }

        RecordingInvocationRepository(List<AiInvocationLog> invocationLogs, List<AiCandidate> candidates) {
            this.invocationLogs = invocationLogs;
            this.candidates = candidates;
        }

        @Override
        public AiInvocationLog getInvocationLog(AiCallId callId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AiCallId insertInvocationLog(AiInvocationLog invocationLog) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int updateInvocationLog(AiInvocationLog invocationLog) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AiInvocationLog> listInvocationLogs(Instant requestedAtStart, Instant requestedAtEnd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AiInvocationLog> listInvocationLogsByBatch(AiBatchJobId batchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AiInvocationLog> listInvocationLogsByBatches(List<AiBatchJobId> batchIds) {
            batchInvocationQueryCount++;
            return invocationLogs;
        }

        @Override
        public List<AiInvocationLog> listInvocationLogsByBatchesAndContent(
                List<AiBatchJobId> batchIds, AiContentRef contentRef) {
            contentInvocationQueryCount++;
            List<AiInvocationLog> records = new ArrayList<>();
            for (AiInvocationLog record : invocationLogs) {
                if (matchesContentRef(record.getContentRef(), contentRef)) {
                    records.add(record);
                }
            }
            return records;
        }

        @Override
        public PageResult<AiInvocationLog> pageInvocationLogs(
                String scope,
                AiBusinessCapability capability,
                AiContentRef contentRef,
                AiInvocationStatus status,
                String serviceRole,
                AiModelName modelName,
                Boolean fallbackUsed,
                Instant requestedAtStart,
                Instant requestedAtEnd,
                int pageNo,
                int pageSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AiInvocationLog> listInvocationLogs(
                String scope,
                AiBusinessCapability capability,
                String serviceRole,
                Instant requestedAtStart,
                Instant requestedAtEnd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AiCandidate getCandidate(AiCandidateId candidateId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AiCandidateId insertCandidate(AiCandidate candidate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AiCandidate> listCandidates(
                AiContentRef contentRef,
                AiTargetObjectId targetObjectId,
                AiBusinessCapability capability,
                AiCandidateStatus status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AiCandidate> listCandidatesByBatch(AiBatchJobId batchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AiCandidate> listCandidatesByBatches(List<AiBatchJobId> batchIds) {
            batchCandidateQueryCount++;
            return candidates;
        }

        @Override
        public List<AiCandidate> listCandidatesByBatchesAndContent(
                List<AiBatchJobId> batchIds, AiContentRef contentRef) {
            contentCandidateQueryCount++;
            List<AiCandidate> records = new ArrayList<>();
            for (AiCandidate record : candidates) {
                if (matchesContentRef(record.getContentRef(), contentRef)) {
                    records.add(record);
                }
            }
            return records;
        }

        private static AiInvocationLog invocationLog(Long batchId, Long callId) {
            return invocationLog(batchId, callId, 10L, "2026-01-01T00:01:00Z");
        }

        private static AiInvocationLog invocationLog(Long batchId, Long callId, Long contentId, String requestedAt) {
            AiInvocationLog invocationLog = new AiInvocationLog();
            invocationLog.setBatchId(AiBatchJobIdCodec.toDomain(batchId));
            invocationLog.setCallId(AiCallIdCodec.toDomain(callId));
            invocationLog.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
            invocationLog.setContentRef(AiContentRef.ofNullable("SANCAI_ENTRY", contentId));
            invocationLog.setStatus(AiInvocationStatus.SUCCEEDED);
            invocationLog.setRequestedAt(Instant.parse(requestedAt));
            return invocationLog;
        }

        private static AiCandidate candidate(Long batchId, Long candidateId, Long callId) {
            return candidate(batchId, candidateId, callId, 10L, "2026-01-01T00:02:00Z");
        }

        private static AiCandidate candidate(
                Long batchId, Long candidateId, Long callId, Long contentId, String requestedAt) {
            AiCandidate candidate = new AiCandidate();
            candidate.setId(AiCandidateIdCodec.toDomain(candidateId));
            candidate.setBatchId(AiBatchJobIdCodec.toDomain(batchId));
            candidate.setCallId(AiCallIdCodec.toDomain(callId));
            candidate.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
            candidate.setContentRef(AiContentRef.ofNullable("SANCAI_ENTRY", contentId));
            candidate.setResultFormat("TEXT");
            candidate.setResultPayload("result-" + candidateId);
            candidate.setStatus(AiCandidateStatus.PENDING);
            candidate.setRequestedAt(Instant.parse(requestedAt));
            return candidate;
        }

        private static boolean matchesContentRef(AiContentRef actual, AiContentRef expected) {
            if (expected == null) {
                return true;
            }
            return actual != null
                    && (expected.contentType() == null || expected.contentType().equals(actual.contentType()))
                    && (expected.contentId() == null || expected.contentId().equals(actual.contentId()));
        }
    }
}
