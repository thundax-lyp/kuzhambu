package com.thundax.kuzhambu.operations.application.health.result;

import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthPageResult {
    private HealthCheckId checkId;
    private String component;
    private String healthStatus;
    private Integer latencyMs;
    private String message;
    private String probeSource;
    private String probeTarget;
    private String detailsJson;
    private Instant checkedAt;
}
