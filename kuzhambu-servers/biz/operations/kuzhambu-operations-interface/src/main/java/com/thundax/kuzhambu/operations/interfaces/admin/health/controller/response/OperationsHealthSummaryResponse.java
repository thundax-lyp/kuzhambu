package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "OperationsHealthSummaryResponse", description = "Operations 健康摘要响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsHealthSummaryResponse {
    private Long checkId;
    private String component;
    private String healthStatus;
    private Integer latencyMs;
    private String message;
    private String probeSource;
    private String probeTarget;
    private Instant checkedAt;
}
