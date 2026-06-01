package com.thundax.kuzhambu.ai.application.batch.service.impl;

import com.thundax.kuzhambu.ai.application.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.batch.repository.AiBatchJobRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiBatchJobApplicationServiceImpl implements AiBatchJobApplicationService {

    private static final String STATUS_CANCELLED = "CANCELLED";

    private final AiBatchJobRepository aiBatchJobRepository;

    public AiBatchJobApplicationServiceImpl(AiBatchJobRepository aiBatchJobRepository) {
        this.aiBatchJobRepository = aiBatchJobRepository;
    }

    @Override
    public AiBatchJobResult get(Long batchId) {
        return AiBatchJobResult.from(getRequired(batchId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AiBatchJobCreateCommand command) {
        validateCreateCommand(command);
        return aiBatchJobRepository.saveBatchJob(command.toEntity());
    }

    @Override
    public boolean canDispatchNextUnit(Long batchId) {
        AiBatchJob job = getRequired(batchId);
        return !STATUS_CANCELLED.equals(job.getStatus())
                && job.getSuccessCount() + job.getFailedCount() + job.getCancelledCount() < job.getTotalCount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordSuccess(Long batchId) {
        AiBatchJob job = getRequired(batchId);
        if (STATUS_CANCELLED.equals(job.getStatus())) {
            job.setSuccessCount(job.getSuccessCount() + 1);
        } else {
            job.recordSuccess();
        }
        aiBatchJobRepository.updateBatchJob(job);
        return AiBatchJobResult.from(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson) {
        AiBatchJob job = getRequired(batchId);
        job.setFailureSummaryJson(failureSummaryJson);
        if (STATUS_CANCELLED.equals(job.getStatus())) {
            job.setFailedCount(job.getFailedCount() + 1);
        } else {
            job.recordFailure();
        }
        aiBatchJobRepository.updateBatchJob(job);
        return AiBatchJobResult.from(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult cancel(Long batchId) {
        AiBatchJob job = getRequired(batchId);
        int finishedCount = job.getSuccessCount() + job.getFailedCount();
        job.setCancelledCount(Math.max(0, job.getTotalCount() - finishedCount));
        job.cancel(Instant.now());
        aiBatchJobRepository.updateBatchJob(job);
        return AiBatchJobResult.from(job);
    }

    private AiBatchJob getRequired(Long batchId) {
        if (batchId == null) {
            throw new BizException("AI batchId is required");
        }
        AiBatchJob job = aiBatchJobRepository.getBatchJob(batchId);
        if (job == null) {
            throw new BizException("AI batch job not found: " + batchId);
        }
        return job;
    }

    private void validateCreateCommand(AiBatchJobCreateCommand command) {
        if (command == null
                || isBlank(command.getScope())
                || isBlank(command.getCapability())
                || isBlank(command.getContentType())
                || command.getTotalCount() <= 0) {
            throw new BizException("AI batch job create command is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
