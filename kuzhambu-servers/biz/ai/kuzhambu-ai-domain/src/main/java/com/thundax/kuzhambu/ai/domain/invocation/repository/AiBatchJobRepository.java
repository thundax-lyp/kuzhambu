package com.thundax.kuzhambu.ai.domain.invocation.repository;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.query.AiBatchJobQuery;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import java.time.Instant;
import java.util.List;

public interface AiBatchJobRepository {

    AiBatchJob get(AiBatchJobId batchId);

    AiBatchJobId insert(AiBatchJob batchJob);

    int update(AiBatchJob batchJob);

    int updateIfStatus(AiBatchJob batchJob, AiBatchJobStatus expectedStatus);

    List<AiBatchJob> listRunningJobsRequestedBefore(
            String scope, List<AiBusinessCapability> capabilities, Instant requestedBefore, int limit);

    List<AiBatchJob> listJobs(AiBatchJobQuery query);

    long countJobs(AiBatchJobQuery query);
}
