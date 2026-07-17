package com.thundax.kuzhambu.ai.infra.batch.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.AiBatchJobDO;

public final class AiBatchJobPersistenceAssembler {

    private AiBatchJobPersistenceAssembler() {}

    public static AiBatchJobDO toObject(AiBatchJob job) {
        if (job == null) {
            return null;
        }
        AiBatchJobDO dataObject = new AiBatchJobDO();
        dataObject.setId(job.getId());
        dataObject.setBatchId(job.getBatchId());
        dataObject.setScope(job.getScope());
        dataObject.setCapability(job.getCapability());
        dataObject.setContentType(job.getContentType());
        dataObject.setStatus(job.getStatus());
        dataObject.setTotalCount(job.getTotalCount());
        dataObject.setSuccessCount(job.getSuccessCount());
        dataObject.setFailedCount(job.getFailedCount());
        dataObject.setCancelledCount(job.getCancelledCount());
        dataObject.setFailureSummaryJson(job.getFailureSummaryJson());
        dataObject.setRequestedAt(job.getRequestedAt());
        dataObject.setCancelledAt(job.getCancelledAt());
        dataObject.setCompletedAt(job.getCompletedAt());
        return dataObject;
    }

    public static AiBatchJob toDomain(AiBatchJobDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiBatchJob(
                dataObject.getId(),
                dataObject.getBatchId(),
                dataObject.getScope(),
                dataObject.getCapability(),
                dataObject.getContentType(),
                dataObject.getStatus(),
                dataObject.getTotalCount() == null ? 0 : dataObject.getTotalCount(),
                dataObject.getSuccessCount() == null ? 0 : dataObject.getSuccessCount(),
                dataObject.getFailedCount() == null ? 0 : dataObject.getFailedCount(),
                dataObject.getCancelledCount() == null ? 0 : dataObject.getCancelledCount(),
                dataObject.getFailureSummaryJson(),
                dataObject.getRequestedAt(),
                dataObject.getCancelledAt(),
                dataObject.getCompletedAt());
    }
}
