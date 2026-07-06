package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response;

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
public class OperationsHealthTrendResponse {
    private String bucket;
    private Long upCount;
    private Long degradedCount;
    private Long downCount;
    private Long avgLatencyMs;
}
