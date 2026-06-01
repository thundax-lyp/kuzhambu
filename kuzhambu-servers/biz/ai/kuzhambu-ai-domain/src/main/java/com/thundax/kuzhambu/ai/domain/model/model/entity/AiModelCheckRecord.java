package com.thundax.kuzhambu.ai.domain.model.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiModelCheckRecord {

    private Long id;
    private Long checkId;
    private Long modelId;
    private Long serviceId;
    private String modelName;
    private String status;
    private Integer latencyMs;
    private String errorType;
    private String errorMessage;
    private Instant checkedAt;

    public boolean isSucceeded() {
        return "SUCCEEDED".equals(status);
    }
}
