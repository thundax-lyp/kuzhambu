package com.thundax.kuzhambu.ai.application.model.command;

import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiModelCheckCommand {

    private Long checkId;
    private Long modelId;
    private Long serviceId;
    private String modelName;
    private String status;
    private Integer latencyMs;
    private String errorType;
    private String errorMessage;
    private Instant checkedAt;

    public AiModelCheckRecord toRecord() {
        Instant effectiveCheckedAt = checkedAt == null ? Instant.now() : checkedAt;
        return new AiModelCheckRecord(
                null,
                checkId,
                modelId,
                serviceId,
                modelName,
                status,
                latencyMs,
                errorType,
                errorMessage,
                effectiveCheckedAt);
    }
}
