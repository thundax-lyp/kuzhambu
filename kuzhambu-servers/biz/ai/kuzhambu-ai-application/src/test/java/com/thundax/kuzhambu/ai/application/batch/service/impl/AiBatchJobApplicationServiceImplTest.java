package com.thundax.kuzhambu.ai.application.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.batch.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.batch.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.batch.repository.AiBatchJobRepository;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.split.model.entity.EntrySplitCandidate;
import com.thundax.kuzhambu.ai.domain.vision.model.entity.ImageUnderstandingResult;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AiBatchJobApplicationServiceImplTest {

    @Test
    public void createShouldNormalizeLegacyImageAnalysisCapability() {
        FakeRepository repository = new FakeRepository(null);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(repository);

        service.create(new AiBatchJobCreateCommand("classics", "image_analysis", "SANCAI_ENTRY", 1, null));

        assertEquals(AiBusinessCapability.CLASSICS_IMAGE_DESCRIBE, repository.inserted.getCapability());
    }

    @Test
    public void createShouldNormalizeLegacyVisualCapability() {
        FakeRepository repository = new FakeRepository(null);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(repository);

        service.create(new AiBatchJobCreateCommand("classics", "visual", "SANCAI_ENTRY", 1, null));

        assertEquals(AiBusinessCapability.CLASSICS_VISUAL_DESCRIBE, repository.inserted.getCapability());
    }

    @ParameterizedTest
    @CsvSource({
        "relation_extraction, KNOWLEDGE_RELATION_EXTRACT",
        "knowledge_graph, KNOWLEDGE_GRAPH_EXTRACT",
        "lineage_extraction, KNOWLEDGE_LINEAGE_EXTRACT"
    })
    public void createShouldNormalizeLegacyKnowledgeCapability(
            String legacyCapability, AiBusinessCapability expectedCapability) {
        FakeRepository repository = new FakeRepository(null);
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(repository);

        service.create(new AiBatchJobCreateCommand("knowledge", legacyCapability, "SANCAI_ENTRY", 1, null));

        assertEquals(expectedCapability, repository.inserted.getCapability());
    }

    @Test
    public void recordSuccessAfterCancelShouldConsumeCancelledSlot() {
        AiBatchJob job = cancelledJob();
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(new FakeRepository(job));

        AiBatchJobResult result = service.recordSuccess(1L);

        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        assertEquals(7, result.getCancelledCount());
        assertEquals(10, result.getSuccessCount() + result.getFailedCount() + result.getCancelledCount());
    }

    @Test
    public void recordFailureAfterCancelShouldConsumeCancelledSlot() {
        AiBatchJob job = cancelledJob();
        AiBatchJobApplicationServiceImpl service = new AiBatchJobApplicationServiceImpl(new FakeRepository(job));

        AiBatchJobResult result = service.recordFailure(1L, "{\"failed\":true}");

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

        AiBatchJobResult result = service.recordSuccess(1L);

        assertEquals(9, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        assertEquals(0, result.getCancelledCount());
        assertEquals(10, result.getSuccessCount() + result.getFailedCount() + result.getCancelledCount());
    }

    private AiBatchJob cancelledJob() {
        AiBatchJob job = new AiBatchJob();
        job.setId(AiBatchJobId.of(1L));
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

        FakeRepository(AiBatchJob job) {
            this.job = job;
        }

        @Override
        public AiBatchJob get(Long batchId) {
            return job != null && batchId.equals(job.getId().value()) ? job : null;
        }

        @Override
        public Long insert(AiBatchJob batchJob) {
            inserted = batchJob;
            return 1L;
        }

        @Override
        public int update(AiBatchJob batchJob) {
            return 1;
        }

        @Override
        public Long insertImageUnderstanding(ImageUnderstandingResult result) {
            return null;
        }

        @Override
        public ImageUnderstandingResult getImageUnderstanding(Long storageObjectId, String contentHash) {
            return null;
        }

        @Override
        public Long insertEntrySplitCandidate(EntrySplitCandidate candidate) {
            return null;
        }

        @Override
        public List<EntrySplitCandidate> listEntrySplitCandidates(Long candidateId) {
            return Collections.emptyList();
        }
    }
}
