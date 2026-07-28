package com.thundax.kuzhambu.operations.application.health.support;

import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthProbe.OperationsHealthProbeOutcome;
import org.springframework.stereotype.Component;

@Component
public class LocalOperationsHealthProbe implements OperationsHealthProbe {

    static final String COMPONENT = "admin-server";
    static final String PROBE_SOURCE = "LOCAL";
    static final String PROBE_TARGET = "admin-server";

    @Override
    public String component() {
        return COMPONENT;
    }

    @Override
    public String probeSource() {
        return PROBE_SOURCE;
    }

    @Override
    public String probeTarget() {
        return PROBE_TARGET;
    }

    @Override
    public OperationsHealthProbeOutcome probe() {
        long startedAt = System.nanoTime();
        int latencyMs = (int) Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
        return new OperationsHealthProbeOutcome("UP", latencyMs, "local process reachable", "{\"probe\":\"local\"}");
    }
}
