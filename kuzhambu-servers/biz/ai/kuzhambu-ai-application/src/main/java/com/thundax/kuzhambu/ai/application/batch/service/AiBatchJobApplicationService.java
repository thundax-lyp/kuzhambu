package com.thundax.kuzhambu.ai.application.batch.service;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;

public interface AiBatchJobApplicationService {

    AiBatchJobResult get(Long batchId);

    Long create(AiBatchJobCreateCommand command);

    boolean canDispatchNextUnit(Long batchId);

    AiBatchJobResult recordSuccess(Long batchId);

    AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson);

    AiBatchJobResult cancel(Long batchId);
}
