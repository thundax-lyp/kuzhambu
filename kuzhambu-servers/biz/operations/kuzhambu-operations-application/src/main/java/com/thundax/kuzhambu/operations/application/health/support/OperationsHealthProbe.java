package com.thundax.kuzhambu.operations.application.health.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface OperationsHealthProbe {

    String component();

    String probeSource();

    String probeTarget();

    OperationsHealthProbeResult probe();

    @Getter
    @AllArgsConstructor
    class OperationsHealthProbeResult {
        private final String healthStatus;
        private final Integer latencyMs;
        private final String message;
        private final String detailsJson;
    }
}
