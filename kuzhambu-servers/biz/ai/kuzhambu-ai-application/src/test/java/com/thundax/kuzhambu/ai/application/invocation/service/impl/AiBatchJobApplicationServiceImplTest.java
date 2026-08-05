package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobFailureCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsQuery;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.query.AiBatchJobQuery;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiBatchJobRepository;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AiBatchJobApplicationServiceImplTest {

    @Test
    public void createShouldUseImageAnalysisCapabilityCode() {
        FakeRepository repository = new FakeRepository(null);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(repository);

        service.create(new AiBatchJobCreateCommand(
                "classics",
                AiBusinessCapability.from(AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE.value()),
                AiContentRef.ofNullable("SANCAI_ENTRY", 3001L),
                1,
                null));

        assertEquals(AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE, repository.inserted.getCapability());
    }

    @Test
    public void createShouldUseVisualCapabilityCode() {
        FakeRepository repository = new FakeRepository(null);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(repository);

        service.create(new AiBatchJobCreateCommand(
                "classics",
                AiBusinessCapability.from(AiBusinessCapability.CLASSICS_VISUAL_DESCRIBE.value()),
                AiContentRef.ofNullable("SANCAI_ENTRY", 3001L),
                1,
                null));

        assertEquals(AiBusinessCapability.CLASSICS_VISUAL_DESCRIBE, repository.inserted.getCapability());
    }

    @Test
    public void createShouldUseFusionCapabilityCodeAndKeepContentId() {
        FakeRepository repository = new FakeRepository(null);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(repository);

        service.create(new AiBatchJobCreateCommand(
                "classics",
                AiBusinessCapability.from(AiBusinessCapability.CLASSICS_IMAGE_PROMPT_FUSION.value()),
                AiContentRef.ofNullable("SANCAI_ENTRY", 3001L),
                1,
                null));

        assertEquals(AiBusinessCapability.CLASSICS_IMAGE_PROMPT_FUSION, repository.inserted.getCapability());
        assertEquals(3001L, repository.inserted.getContentId());
    }

    @ParameterizedTest
    @CsvSource({
        "KNOWLEDGE_RELATION_EXTRACT, KNOWLEDGE_RELATION_EXTRACT",
        "KNOWLEDGE_GRAPH_EXTRACT, KNOWLEDGE_GRAPH_EXTRACT",
        "KNOWLEDGE_LINEAGE_EXTRACT, KNOWLEDGE_LINEAGE_EXTRACT"
    })
    public void createShouldUseKnowledgeCapabilityCode(String capabilityCode, AiBusinessCapability expectedCapability) {
        FakeRepository repository = new FakeRepository(null);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(repository);

        service.create(new AiBatchJobCreateCommand(
                "knowledge",
                AiBusinessCapability.from(capabilityCode),
                AiContentRef.ofNullable("SANCAI_ENTRY", null),
                1,
                null));

        assertEquals(expectedCapability, repository.inserted.getCapability());
    }

    @Test
    public void recordSuccessAfterCancelShouldConsumeCancelledSlot() {
        AiBatchJob job = cancelledJob();
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(new FakeRepository(job));

        AiBatchJobResult result = service.recordSuccess(new RecordAiBatchJobCommand(new AiBatchJobId(1L)));

        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        assertEquals(7, result.getCancelledCount());
        assertEquals(10, result.getSuccessCount() + result.getFailedCount() + result.getCancelledCount());
    }

    @Test
    public void recordFailureAfterCancelShouldConsumeCancelledSlot() {
        AiBatchJob job = cancelledJob();
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(new FakeRepository(job));

        AiBatchJobResult result =
                service.recordFailure(new RecordAiBatchJobFailureCommand(new AiBatchJobId(1L), "{\"failed\":true}"));

        assertEquals(1, result.getSuccessCount());
        assertEquals(2, result.getFailedCount());
        assertEquals(7, result.getCancelledCount());
        assertEquals("{\"failed\":true}", result.getFailureSummaryJson());
        assertEquals(10, result.getSuccessCount() + result.getFailedCount() + result.getCancelledCount());
    }

    @Test
    public void recordSuccessAfterCancelShouldNotExceedTotalCount() {
        AiBatchJob job = cancelledJob();
        job.setSuccessCount(9);
        job.setFailedCount(1);
        job.setCancelledCount(0);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(new FakeRepository(job));

        AiBatchJobResult result = service.recordSuccess(new RecordAiBatchJobCommand(new AiBatchJobId(1L)));

        assertEquals(9, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        assertEquals(0, result.getCancelledCount());
        assertEquals(10, result.getSuccessCount() + result.getFailedCount() + result.getCancelledCount());
    }

    @Test
    public void cancelShouldKeepSucceededJobUnchanged() {
        AiBatchJob job = new AiBatchJob();
        job.setId(AiBatchJobIdCodec.toDomain(1L));
        job.setScope("classics");
        job.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
        job.setContentType("SANCAI_ENTRY");
        job.setStatus(AiBatchJobStatus.SUCCEEDED);
        job.setTotalCount(1);
        job.setSuccessCount(1);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(new FakeRepository(job));

        AiBatchJobResult result = service.cancel(new CancelAiBatchJobCommand(new AiBatchJobId(1L)));

        assertEquals(AiBatchJobStatus.SUCCEEDED, result.getStatus());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getCancelledCount());
    }

    @Test
    public void recordSuccessIfRunningShouldReloadTerminalJobWhenConditionalUpdateLosesRace() {
        AiBatchJob job = new AiBatchJob();
        job.setId(AiBatchJobIdCodec.toDomain(1L));
        job.setScope("classics");
        job.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
        job.setContentType("SANCAI_ENTRY");
        job.setStatus(AiBatchJobStatus.RUNNING);
        job.setTotalCount(1);
        FakeRepository repository = new FakeRepository(job);
        repository.failNextConditionalUpdateWith(AiBatchJobStatus.CANCELLED);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(repository);

        AiBatchJobResult result = service.recordSuccessIfRunning(new RecordAiBatchJobCommand(new AiBatchJobId(1L)));

        assertEquals(AiBatchJobStatus.CANCELLED, result.getStatus());
        assertEquals(0, result.getSuccessCount());
    }

    @Test
    public void recordPartialIfRunningShouldPreservePartialTerminalState() {
        AiBatchJob job = new AiBatchJob();
        job.setId(AiBatchJobIdCodec.toDomain(1L));
        job.setScope("classics");
        job.setCapability(AiBusinessCapability.CLASSICS_IMAGE_GENERATE);
        job.setContentType("SANCAI_ENTRY");
        job.setStatus(AiBatchJobStatus.RUNNING);
        job.setTotalCount(1);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(new FakeRepository(job));

        AiBatchJobResult result = service.recordPartialIfRunning(
                new RecordAiBatchJobFailureCommand(new AiBatchJobId(1L), "{\"errorType\":\"MODEL_SEMANTIC_FAILURE\"}"));

        assertEquals(AiBatchJobStatus.PARTIAL, result.getStatus());
        assertEquals(1, result.getSuccessCount());
        assertEquals("{\"errorType\":\"MODEL_SEMANTIC_FAILURE\"}", result.getFailureSummaryJson());
    }

    @Test
    public void pageShouldFilterByBatchContentIdWithoutInvocationLog() {
        AiBatchJob job = new AiBatchJob();
        job.setId(AiBatchJobIdCodec.toDomain(1L));
        job.setScope("classics");
        job.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
        job.setContentType("SANCAI_ENTRY");
        job.setContentId(3001L);
        job.setStatus(AiBatchJobStatus.RUNNING);
        job.setTotalCount(1);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(new FakeRepository(job));

        PageResult<AiBatchJobResult> page = service.page(new PageAiBatchJobsQuery(
                "classics",
                AiBusinessCapability.CLASSICS_SUMMARY,
                null,
                AiContentRef.ofNullable("SANCAI_ENTRY", 3001L),
                new PageQuery(1, 10)));

        assertEquals(1, page.getTotalCount());
        assertEquals(3001L, page.getRecords().get(0).getContentRef().contentId());
    }

    private AiBatchJob cancelledJob() {
        AiBatchJob job = new AiBatchJob();
        job.setId(AiBatchJobIdCodec.toDomain(1L));
        job.setScope("knowledge");
        job.setCapability(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT);
        job.setContentType("KNOWLEDGE_ENTITY");
        job.setStatus(AiBatchJobStatus.CANCELLED);
        job.setTotalCount(10);
        job.setSuccessCount(1);
        job.setFailedCount(1);
        job.setCancelledCount(8);
        return job;
    }

    private static class FakeRepository implements AiBatchJobRepository {

        private final AiBatchJob job;
        private AiBatchJob inserted;
        private AiBatchJobStatus raceStatus;

        FakeRepository(AiBatchJob job) {
            this.job = job;
        }

        @Override
        public AiBatchJob get(AiBatchJobId batchId) {
            return job != null && batchId.equals(job.getId()) ? copy(job) : null;
        }

        @Override
        public AiBatchJobId insert(AiBatchJob batchJob) {
            inserted = batchJob;
            return AiBatchJobIdCodec.toDomain(1L);
        }

        @Override
        public int update(AiBatchJob batchJob) {
            copyInto(job, batchJob);
            return 1;
        }

        @Override
        public int updateIfStatus(AiBatchJob batchJob, AiBatchJobStatus expectedStatus) {
            if (raceStatus != null) {
                job.setStatus(raceStatus);
                raceStatus = null;
                return 0;
            }
            if (job.getStatus() != expectedStatus) {
                return 0;
            }
            copyInto(job, batchJob);
            return 1;
        }

        @Override
        public List<AiBatchJob> listRunningJobsRequestedBefore(
                String scope, List<AiBusinessCapability> capabilities, java.time.Instant requestedBefore, int limit) {
            if (job == null || job.getStatus() != AiBatchJobStatus.RUNNING) {
                return List.of();
            }
            return List.of(copy(job));
        }

        @Override
        public List<AiBatchJob> listJobs(AiBatchJobQuery query) {
            return matches(query) ? List.of(copy(job)) : List.of();
        }

        @Override
        public long countJobs(AiBatchJobQuery query) {
            return matches(query) ? 1 : 0;
        }

        private boolean matches(AiBatchJobQuery query) {
            if (job == null) {
                return false;
            }
            return query == null
                    || query.getContentId() == null
                    || Objects.equals(query.getContentId(), job.getContentId());
        }

        private void failNextConditionalUpdateWith(AiBatchJobStatus status) {
            raceStatus = status;
        }

        private void copyInto(AiBatchJob target, AiBatchJob source) {
            target.setStatus(source.getStatus());
            target.setSuccessCount(source.getSuccessCount());
            target.setFailedCount(source.getFailedCount());
            target.setCancelledCount(source.getCancelledCount());
            target.setFailureSummaryJson(source.getFailureSummaryJson());
            target.setCancelledAt(source.getCancelledAt());
            target.setCompletedAt(source.getCompletedAt());
        }

        private AiBatchJob copy(AiBatchJob source) {
            AiBatchJob target = new AiBatchJob();
            target.setId(source.getId());
            target.setScope(source.getScope());
            target.setCapability(source.getCapability());
            target.setContentType(source.getContentType());
            target.setContentId(source.getContentId());
            target.setStatus(source.getStatus());
            target.setTotalCount(source.getTotalCount());
            target.setSuccessCount(source.getSuccessCount());
            target.setFailedCount(source.getFailedCount());
            target.setCancelledCount(source.getCancelledCount());
            target.setFailureSummaryJson(source.getFailureSummaryJson());
            target.setRequestedAt(source.getRequestedAt());
            target.setCancelledAt(source.getCancelledAt());
            target.setCompletedAt(source.getCompletedAt());
            return target;
        }
    }
}
