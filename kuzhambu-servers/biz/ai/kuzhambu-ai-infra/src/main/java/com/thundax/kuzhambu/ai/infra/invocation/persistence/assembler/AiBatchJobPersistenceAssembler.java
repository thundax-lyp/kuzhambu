package com.thundax.kuzhambu.ai.infra.invocation.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiBatchJobDO;

public final class AiBatchJobPersistenceAssembler {

    private AiBatchJobPersistenceAssembler() {}

    public static AiBatchJobDO toObject(AiBatchJob job) {
        if (job == null) {
            return null;
        }
        AiBatchJobDO dataObject = new AiBatchJobDO();
        dataObject.setId(AiBatchJobIdCodec.toValue(job.getId()));
        dataObject.setScope(job.getScope());
        dataObject.setCapability(
                job.getCapability() == null ? null : job.getCapability().value());
        dataObject.setContentType(job.getContentType());
        dataObject.setContentId(job.getContentId());
        dataObject.setStatus(job.getStatus() == null ? null : job.getStatus().name());
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
                AiBatchJobIdCodec.toDomain(dataObject.getId()),
                dataObject.getScope(),
                dataObject.getCapability() == null ? null : AiBusinessCapability.from(dataObject.getCapability()),
                dataObject.getContentType(),
                dataObject.getContentId(),
                dataObject.getStatus() == null ? null : AiBatchJobStatus.from(dataObject.getStatus()),
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
