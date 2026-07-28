package com.thundax.kuzhambu.operations.application.health.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.operations.application.health.configure.OperationsExternalHealthProbeProperties.Target;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthProbe.OperationsHealthProbeOutcome;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpOperationsHealthProbe implements OperationsHealthProbe {

    static final String PROBE_SOURCE = "HTTP";
    static final String ERROR_TYPE_INVALID_STATUS = "INVALID_STATUS";
    static final String ERROR_TYPE_TIMEOUT = "TIMEOUT";
    static final String ERROR_TYPE_IO_ERROR = "IO_ERROR";

    private final Target target;
    private final int timeoutMs;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpOperationsHealthProbe(Target target, int timeoutMs) {
        this(
                target,
                timeoutMs,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(normalizeTimeout(timeoutMs)))
                        .build(),
                new ObjectMapper());
    }

    HttpOperationsHealthProbe(Target target, int timeoutMs, HttpClient httpClient, ObjectMapper objectMapper) {
        this.target = target;
        this.timeoutMs = normalizeTimeout(timeoutMs);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String component() {
        return target.getComponent();
    }

    @Override
    public String probeSource() {
        return PROBE_SOURCE;
    }

    @Override
    public String probeTarget() {
        return target.getUrl();
    }

    @Override
    public OperationsHealthProbeOutcome probe() {
        long startedAt = System.nanoTime();
        try {
            HttpResponse<Void> response = httpClient.send(buildRequest(), HttpResponse.BodyHandlers.discarding());
            int latencyMs = elapsedMillis(startedAt);
            int actualStatus = response.statusCode();
            if (actualStatus != target.getExpectedStatus()) {
                return down(
                        latencyMs,
                        "http status " + actualStatus + ", expected " + target.getExpectedStatus(),
                        details(actualStatus, latencyMs, ERROR_TYPE_INVALID_STATUS, null));
            }
            if (latencyMs > target.getDegradedLatencyMs()) {
                return new OperationsHealthProbeOutcome(
                        OperationsHealthCollector.HEALTH_STATUS_DEGRADED,
                        latencyMs,
                        "http status " + actualStatus + ", latency degraded",
                        details(actualStatus, latencyMs, null, null));
            }
            return new OperationsHealthProbeOutcome(
                    OperationsHealthCollector.HEALTH_STATUS_UP,
                    latencyMs,
                    "http status " + actualStatus,
                    details(actualStatus, latencyMs, null, null));
        } catch (HttpTimeoutException ex) {
            int latencyMs = elapsedMillis(startedAt);
            return down(latencyMs, "http probe timeout", details(null, latencyMs, ERROR_TYPE_TIMEOUT, ex.getMessage()));
        } catch (IOException ex) {
            int latencyMs = elapsedMillis(startedAt);
            return down(latencyMs, "http probe failed", details(null, latencyMs, ERROR_TYPE_IO_ERROR, ex.getMessage()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            int latencyMs = elapsedMillis(startedAt);
            return down(latencyMs, "http probe timeout", details(null, latencyMs, ERROR_TYPE_TIMEOUT, ex.getMessage()));
        }
    }

    private HttpRequest buildRequest() {
        return HttpRequest.newBuilder(URI.create(target.getUrl()))
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();
    }

    private OperationsHealthProbeOutcome down(Integer latencyMs, String message, String detailsJson) {
        return new OperationsHealthProbeOutcome(
                OperationsHealthCollector.HEALTH_STATUS_DOWN, latencyMs, message, detailsJson);
    }

    private String details(Integer actualStatus, Integer latencyMs, String errorType, String errorMessage) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("url", target.getUrl());
        details.put("expectedStatus", target.getExpectedStatus());
        details.put("actualStatus", actualStatus);
        details.put("timeoutMs", timeoutMs);
        details.put("degradedLatencyMs", target.getDegradedLatencyMs());
        details.put("latencyMs", latencyMs);
        details.put("errorType", errorType);
        details.put("errorMessage", errorMessage);
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException ex) {
            return "{\"url\":\"" + target.getUrl() + "\"}";
        }
    }

    private static int normalizeTimeout(int timeoutMs) {
        return timeoutMs > 0 ? timeoutMs : 3000;
    }

    private static int elapsedMillis(long startedAt) {
        return (int) Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }
}
