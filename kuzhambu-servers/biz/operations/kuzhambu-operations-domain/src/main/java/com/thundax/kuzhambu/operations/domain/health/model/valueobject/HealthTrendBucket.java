package com.thundax.kuzhambu.operations.domain.health.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthTrendBucket {

    private String bucket;
    private Long upCount;
    private Long degradedCount;
    private Long downCount;
    private Long avgLatencyMs;
}
