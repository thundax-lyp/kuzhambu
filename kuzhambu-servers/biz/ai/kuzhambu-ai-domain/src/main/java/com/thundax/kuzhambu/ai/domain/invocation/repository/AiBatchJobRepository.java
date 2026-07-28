package com.thundax.kuzhambu.ai.domain.invocation.repository;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.invocation.model.query.AiBatchJobQuery;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import java.util.List;

public interface AiBatchJobRepository {

    AiBatchJob get(AiBatchJobId batchId);

    AiBatchJobId insert(AiBatchJob batchJob);

    int update(AiBatchJob batchJob);

    List<AiBatchJob> listJobs(AiBatchJobQuery query);

    long countJobs(AiBatchJobQuery query);
}
