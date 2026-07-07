package com.thundax.kuzhambu.operations.application.health.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.thundax.kuzhambu.operations.application.health.configure.OperationsExternalHealthProbeProperties.Target;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthProbe.OperationsHealthProbeResult;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HttpOperationsHealthProbeTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void probeShouldReturnUpWhenExpectedStatusIsFast() throws Exception {
        withServer(200, 0, url -> {
            OperationsHealthProbeResult result = new HttpOperationsHealthProbe(target(url, 200, 1000), 1000).probe();

            assertEquals(OperationsHealthCollector.HEALTH_STATUS_UP, result.getHealthStatus());
            assertEquals("http status 200", result.getMessage());
            assertEquals(
                    200,
                    OBJECT_MAPPER
                            .readTree(result.getDetailsJson())
                            .get("actualStatus")
                            .asInt());
        });
    }

    @Test
    void probeShouldReturnDegradedWhenExpectedStatusIsSlow() throws Exception {
        withServer(200, 50, url -> {
            OperationsHealthProbeResult result = new HttpOperationsHealthProbe(target(url, 200, 1), 1000).probe();

            assertEquals(OperationsHealthCollector.HEALTH_STATUS_DEGRADED, result.getHealthStatus());
            assertTrue(result.getMessage().contains("latency degraded"));
        });
    }

    @Test
    void probeShouldReturnDownWhenStatusDoesNotMatch() throws Exception {
        withServer(503, 0, url -> {
            OperationsHealthProbeResult result = new HttpOperationsHealthProbe(target(url, 200, 1000), 1000).probe();

            assertEquals(OperationsHealthCollector.HEALTH_STATUS_DOWN, result.getHealthStatus());
            assertEquals(
                    "INVALID_STATUS",
                    OBJECT_MAPPER
                            .readTree(result.getDetailsJson())
                            .get("errorType")
                            .asText());
        });
    }

    @Test
    void probeShouldReturnDownWhenRequestFails() throws Exception {
        OperationsHealthProbeResult result =
                new HttpOperationsHealthProbe(target("http://127.0.0.1:1/internal/health", 200, 1000), 50).probe();

        assertEquals(OperationsHealthCollector.HEALTH_STATUS_DOWN, result.getHealthStatus());
        assertEquals("http probe failed", result.getMessage());
        assertEquals(
                "IO_ERROR",
                OBJECT_MAPPER.readTree(result.getDetailsJson()).get("errorType").asText());
    }

    private static Target target(String url, int expectedStatus, int degradedLatencyMs) {
        Target target = new Target();
        target.setComponent("admin");
        target.setUrl(url);
        target.setExpectedStatus(expectedStatus);
        target.setDegradedLatencyMs(degradedLatencyMs);
        return target;
    }

    private static void withServer(int statusCode, long delayMs, ServerAssertion assertion) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal/health", exchange -> {
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        try {
            server.start();
            assertion.assertServer("http://127.0.0.1:" + server.getAddress().getPort() + "/internal/health");
        } finally {
            server.stop(0);
        }
    }

    @FunctionalInterface
    private interface ServerAssertion {
        void assertServer(String url) throws IOException;
    }
}
