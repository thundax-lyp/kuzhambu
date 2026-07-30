package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthPageResponse {
    private Long checkId;
    private String component;
    private String healthStatus;
    private Integer latencyMs;
    private String message;
    private String probeSource;
    private String probeTarget;
    private String detailsJson;
    private Instant checkedAt;
}
