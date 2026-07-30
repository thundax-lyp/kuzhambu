package com.thundax.kuzhambu.operations.application.health.query;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthTrendQuery {
    private String component;
    private String probeSource;
    private Instant periodStart;
    private Instant periodEnd;
    private String bucketType;
}
