package com.thundax.kuzhambu.ai.application.batch.command;

import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiBatchJobCreateCommand {

    private String scope;
    private String capability;
    private String contentType;
    private int totalCount;
    private String failureSummaryJson;

    public AiBatchJob toEntity() {
        AiBatchJob job = new AiBatchJob();
        job.setScope(scope);
        job.setCapability(AiBusinessCapability.from(capability));
        job.setContentType(contentType);
        job.setTotalCount(totalCount);
        job.setFailureSummaryJson(failureSummaryJson);
        job.setRequestedAt(Instant.now());
        return job;
    }
}
