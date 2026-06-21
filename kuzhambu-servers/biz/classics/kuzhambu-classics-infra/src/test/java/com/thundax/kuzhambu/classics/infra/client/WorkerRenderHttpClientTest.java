package com.thundax.kuzhambu.classics.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.thundax.kuzhambu.classics.infra.client.dto.WorkerRenderDtos;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WorkerRenderHttpClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void renderShouldSendSignedWorkerRequestToClassicsExportPath() throws IOException {
        AtomicReference<HttpExchange> captured = new AtomicReference<>();
        AtomicReference<String> capturedBody = new AtomicReference<>();
        startServer("/internal/render/classics-export", exchange -> {
            captured.set(exchange);
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(
                    exchange,
                    200,
                    """
                    {
                      "requestId":"req-1",
                      "traceId":"trace-1",
                      "status":"SUCCEEDED",
                      "renderType":"CLASSICS_EXPORT",
                      "artifact":{
                        "format":"ZIP",
                        "filename":"classics-export.zip",
                        "contentType":"application/zip",
                        "encoding":"BASE64",
                        "content":"UEsDB",
                        "sizeBytes":16,
                        "sha256":"sha256:abc"
                      },
                      "summary":{"itemCount":3,"warnings":[]},
                      "usage":{"latencyMs":120}
                    }
                    """);
        });

        WorkerRenderHttpClient client = new WorkerRenderHttpClient(properties(), new WorkerRenderSignatureSupport());
        WorkerRenderDtos.WorkerRenderResponse response = client.renderClassicsExport(request());

        assertEquals("SUCCEEDED", response.getStatus());
        assertEquals("req-1", response.getRequestId());
        assertEquals("CLASSICS_EXPORT", response.getRenderType());
        assertNotNull(response.getArtifact());
        assertEquals(
                "/internal/render/classics-export",
                captured.get().getRequestURI().getPath());
        assertEquals(
                "kuzhambu-classics-test", captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Service"));
        assertEquals("req-1", captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Request-Id"));
        String timestamp = captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Timestamp");
        assertNotNull(timestamp);
        assertEquals(
                new WorkerRenderSignatureSupport()
                        .sign(
                                "POST",
                                "/internal/render/classics-export",
                                timestamp,
                                "req-1",
                                capturedBody.get(),
                                "worker-secret"),
                captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Signature"));
    }

    @Test
    void renderShouldNormalizeWorkerHttpFailure() throws IOException {
        startServer("/internal/render/sancai-showcase", exchange -> respond(exchange, 503, ""));
        WorkerRenderHttpClient client = new WorkerRenderHttpClient(properties(), new WorkerRenderSignatureSupport());

        WorkerRenderDtos.WorkerRenderResponse response = client.renderSancaiShowcase(request());

        assertEquals("FAILED", response.getStatus());
        assertEquals("WORKER_UNAVAILABLE", response.getError().getType());
        assertEquals("Worker returned HTTP 503", response.getError().getMessage());
    }

    private void startServer(String path, ExchangeHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(path, exchange -> {
            try {
                handler.handle(exchange);
            } catch (RuntimeException ex) {
                exchange.close();
                throw ex;
            }
        });
        server.start();
    }

    private WorkerRenderProperties properties() {
        WorkerRenderProperties properties = new WorkerRenderProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setInternalSecret("worker-secret");
        properties.setServiceName("kuzhambu-classics-test");
        properties.setTimeoutMs(3000);
        return properties;
    }

    private WorkerRenderDtos.WorkerRenderRequest request() {
        WorkerRenderDtos.WorkerRenderRequest request = new WorkerRenderDtos.WorkerRenderRequest();
        request.setRequestId("req-1");
        request.setTraceId("trace-1");
        request.setCallerDomain("CLASSICS");
        request.setOperation("export");
        request.setRenderType("CLASSICS_EXPORT");
        request.setTemplate(template());
        request.setOutput(output());
        request.setInput(input());
        request.setOptions(options());
        return request;
    }

    private WorkerRenderDtos.Template template() {
        WorkerRenderDtos.Template template = new WorkerRenderDtos.Template();
        template.setTemplateId("classics-export-default");
        template.setTemplateVersion("2026.06.01");
        return template;
    }

    private WorkerRenderDtos.Output output() {
        WorkerRenderDtos.Output output = new WorkerRenderDtos.Output();
        output.setFormat("ZIP");
        output.setFilenameHint("classics-export.zip");
        output.setLocale("zh-CN");
        return output;
    }

    private WorkerRenderDtos.Input input() {
        WorkerRenderDtos.Input input = new WorkerRenderDtos.Input();
        input.setSnapshotId("snapshot-1");
        input.setContentType("CLASSICS_EXPORT_SNAPSHOT");
        input.setPayload(null);
        return input;
    }

    private WorkerRenderDtos.Options options() {
        WorkerRenderDtos.Options options = new WorkerRenderDtos.Options();
        options.setStream(false);
        options.setIncludeMetadata(true);
        return options;
    }

    private void respond(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private interface ExchangeHandler {

        void handle(HttpExchange exchange) throws IOException;
    }
}
