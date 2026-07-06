package com.thundax.kuzhambu.operations.application.health.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthTrendResult {
    private String bucket;
    private Long upCount;
    private Long degradedCount;
    private Long downCount;
    private Long avgLatencyMs;
}
