package com.thundax.kuzhambu.ai.domain.invocation.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import org.junit.jupiter.api.Test;

class AiBatchJobTest {

    @Test
    void completeIfFinishedShouldMarkSucceededWhenAllUnitsSucceeded() {
        AiBatchJob job = job(2);

        job.recordSuccess();
        job.recordSuccess();

        assertEquals(AiBatchJobStatus.SUCCEEDED, job.getStatus());
    }

    @Test
    void completeIfFinishedShouldMarkFailedWhenAllUnitsFailed() {
        AiBatchJob job = job(2);

        job.recordFailure();
        job.recordFailure();

        assertEquals(AiBatchJobStatus.FAILED, job.getStatus());
    }

    @Test
    void completeIfFinishedShouldMarkPartialWhenSucceededAndFailedUnitsCoexist() {
        AiBatchJob job = job(2);

        job.recordSuccess();
        job.recordFailure();

        assertEquals(AiBatchJobStatus.PARTIAL, job.getStatus());
    }

    @Test
    void recordPartialShouldMarkSingleUnitPartial() {
        AiBatchJob job = job(1);

        job.recordPartial();

        assertEquals(AiBatchJobStatus.PARTIAL, job.getStatus());
        assertEquals(1, job.getSuccessCount());
    }

    private AiBatchJob job(int totalCount) {
        AiBatchJob job = new AiBatchJob();
        job.setTotalCount(totalCount);
        return job;
    }
}
