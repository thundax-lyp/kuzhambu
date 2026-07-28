package com.thundax.kuzhambu.ai.infra.invocation.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway.ArtifactDownloadException;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AiWorkerHttpGatewayTest {

    private static final String STREAM_COMPLETED_EVENT = "event:completed\n"
            + "data: {\"eventType\":\"completed\",\"status\":\"SUCCEEDED\",\"requestId\":\"req-1\",\"traceId\":\"trace-1\"}\n\n";

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void invokeShouldSendSignedWorkerRequestWithFallbackPath() throws IOException {
        AtomicReference<HttpExchange> captured = new AtomicReference<>();
        AtomicReference<String> capturedBody = new AtomicReference<>();
        startServer("/internal/ai/invoke", exchange -> {
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
                              "capability":"translate",
                              "result":{"format":"text","payload":"done"},
                              "usage":{"latencyMs":12,"inputTokens":3,"outputTokens":4,"costAmount":"0.01"}
                            }
                            """);
        });
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);

        AiInvokeResult result = client.invoke(command());

        assertTrue(result.isSucceeded());
        assertEquals("done", result.getResultPayload());
        assertEquals("/internal/ai/invoke", captured.get().getRequestURI().getPath());
        assertEquals("kuzhambu-ai-test", captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Service"));
        assertEquals("req-1", captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Request-Id"));
        String timestamp = captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Timestamp");
        assertNotNull(timestamp);
        assertEquals(
                new AiWorkerRequestSigner()
                        .sign("POST", "/internal/ai/invoke", timestamp, "req-1", capturedBody.get(), "worker-secret"),
                captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Signature"));
        assertTrue(capturedBody.get().contains("\"capability\":\"summary\""));
        assertTrue(capturedBody.get().contains("\"contentType\":\"entry\""));
    }

    @Test
    void invokeShouldAllowMissingContentReference() throws IOException {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        startServer("/internal/ai/invoke", exchange -> {
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, succeededResponse());
        });
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);
        AiInvokeCommand command = command();
        command.setContentRef(null);

        AiInvokeResult result = client.invoke(command);

        assertTrue(result.isSucceeded());
        assertTrue(capturedBody.get().contains("\"contentType\":null"));
        assertTrue(capturedBody.get().contains("\"contentId\":null"));
    }

    @Test
    void invokeShouldAllowContentTypeWithoutContentId() throws IOException {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        startServer("/internal/ai/invoke", exchange -> {
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, succeededResponse());
        });
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);
        AiInvokeCommand command = command();
        command.setContentRef(AiContentRef.ofNullable("entry", null));

        AiInvokeResult result = client.invoke(command);

        assertTrue(result.isSucceeded());
        assertTrue(capturedBody.get().contains("\"contentType\":\"entry\""));
        assertTrue(capturedBody.get().contains("\"contentId\":null"));
    }

    @Test
    void invokeShouldNormalizeWorkerHttpFailure() throws IOException {
        startServer("/internal/ai/invoke", exchange -> respond(exchange, 503, ""));
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);

        AiInvokeResult result = client.invoke(command());

        assertEquals("FAILED", result.getStatus());
        assertEquals("WORKER_UNAVAILABLE", result.getErrorType());
        assertEquals("Worker returned HTTP 503", result.getErrorMessage());
    }

    @Test
    void invokeShouldReadTopLevelWorkerErrorFields() throws IOException {
        startServer(
                "/internal/ai/invoke",
                exchange -> respond(
                        exchange,
                        200,
                        """
                                {
                                  "requestId":"req-1",
                                  "traceId":"trace-1",
                                  "status":"FAILED",
                                  "capability":"summary",
                                  "failureStage":"WORKER_REQUEST",
                                  "errorType":"MODEL_REQUEST_REJECTED",
                                  "errorMessage":"模型拒绝请求"
                                }
                                """));
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);

        AiInvokeResult result = client.invoke(command());

        assertEquals("FAILED", result.getStatus());
        assertEquals("WORKER_REQUEST", result.getFailureStage());
        assertEquals("MODEL_REQUEST_REJECTED", result.getErrorType());
        assertEquals("模型拒绝请求", result.getErrorMessage());
    }

    @Test
    void streamShouldUseUnifiedWorkerPathForInvocation() throws IOException {
        AtomicReference<HttpExchange> captured = new AtomicReference<>();
        AtomicReference<String> capturedBody = new AtomicReference<>();
        AtomicReference<AiStreamEventResult> capturedEvent = new AtomicReference<>();
        startServer("/internal/ai/stream", exchange -> {
            captured.set(exchange);
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, STREAM_COMPLETED_EVENT);
        });
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);

        client.stream(command(), capturedEvent::set);

        assertEquals("/internal/ai/stream", captured.get().getRequestURI().getPath());
        assertEquals("completed", capturedEvent.get().getEventType());
        String timestamp = captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Timestamp");
        assertNotNull(timestamp);
        assertEquals(
                new AiWorkerRequestSigner()
                        .sign("POST", "/internal/ai/stream", timestamp, "req-1", capturedBody.get(), "worker-secret"),
                captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Signature"));
    }

    @Test
    void streamShouldUseUnifiedStreamPath() throws IOException {
        AtomicReference<HttpExchange> captured = new AtomicReference<>();
        startServer("/internal/ai/stream", exchange -> {
            captured.set(exchange);
            respond(exchange, 200, STREAM_COMPLETED_EVENT);
        });
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);

        client.stream(command(), event -> {});

        assertEquals("/internal/ai/stream", captured.get().getRequestURI().getPath());
    }

    @Test
    void streamShouldParseDeltaAndCompletedEvents() throws IOException {
        startServer(
                "/internal/ai/stream",
                exchange -> respond(
                        exchange,
                        200,
                        "event:delta\n"
                                + "data: {\"eventId\":\"evt-1\",\"requestId\":\"req-1\",\"traceId\":\"trace-1\",\"stage\":\"model_stream\",\"deltaText\":\"片段一\"}\n\n"
                                + "event:completed\n"
                                + "data: {\"eventId\":\"evt-2\",\"requestId\":\"req-1\",\"traceId\":\"trace-1\",\"stage\":\"completed\",\"result\":{\"format\":\"MARKDOWN\",\"payload\":\"完整结果\"},\"extra\":{\"status\":\"SUCCEEDED\"}}\n\n"));
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);
        List<AiStreamEventResult> events = new ArrayList<>();

        client.stream(command(), events::add);

        assertEquals(2, events.size());
        assertEquals("delta", events.get(0).getEventType());
        assertEquals("model_stream", events.get(0).getStage());
        assertEquals("片段一", events.get(0).getDeltaText());
        assertEquals("completed", events.get(1).getEventType());
        assertEquals("MARKDOWN", events.get(1).getResultFormat());
        assertEquals("完整结果", events.get(1).getResultPayload());
        assertEquals("SUCCEEDED", events.get(1).getStatus());
    }

    @Test
    void streamShouldParseErrorEvent() throws IOException {
        startServer(
                "/internal/ai/stream",
                exchange -> respond(
                        exchange,
                        200,
                        "event:error\n"
                                + "data: {\"eventId\":\"evt-err\",\"requestId\":\"req-1\",\"traceId\":\"trace-1\",\"stage\":\"worker_stream\",\"error\":{\"type\":\"MODEL_TRANSPORT_FAILURE\",\"message\":\"模型服务不可用\"},\"extra\":{\"failureStage\":\"WORKER_STREAM\",\"status\":\"FAILED\"}}\n\n"));
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);
        AtomicReference<AiStreamEventResult> capturedEvent = new AtomicReference<>();

        client.stream(command(), capturedEvent::set);

        assertEquals("error", capturedEvent.get().getEventType());
        assertEquals("FAILED", capturedEvent.get().getStatus());
        assertEquals("MODEL_TRANSPORT_FAILURE", capturedEvent.get().getErrorType());
        assertEquals("模型服务不可用", capturedEvent.get().getErrorMessage());
        assertEquals("WORKER_STREAM", capturedEvent.get().getFailureStage());
    }

    @Test
    void streamShouldExposeEarlyEofToApplication() throws IOException {
        startServer(
                "/internal/ai/stream",
                exchange -> respond(
                        exchange,
                        200,
                        "event:delta\n"
                                + "data: {\"eventId\":\"evt-1\",\"requestId\":\"req-1\",\"traceId\":\"trace-1\",\"stage\":\"model_stream\",\"delta\":{\"text\":\"未完成片段\"}}\n\n"));
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(), new AiWorkerRequestSigner(), null);
        List<AiStreamEventResult> events = new ArrayList<>();

        client.stream(command(), events::add);

        assertEquals(1, events.size());
        assertEquals("delta", events.get(0).getEventType());
        assertEquals("未完成片段", events.get(0).getDeltaText());
    }

    @Test
    void downloadArtifactShouldRejectOversizeContentLength() throws IOException {
        AtomicReference<HttpExchange> captured = new AtomicReference<>();
        startServer("/artifact/large", exchange -> {
            captured.set(exchange);
            respondBinary(exchange, 200, "too-large".getBytes(StandardCharsets.UTF_8));
        });
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(4L), new AiWorkerRequestSigner(), null);

        ArtifactDownloadException exception = assertThrows(
                ArtifactDownloadException.class,
                () -> client.downloadArtifact(
                        RequestIdCodec.toDomain("req-1"), TraceIdCodec.toDomain("trace-1"), "/artifact/large"));

        assertTrue(hasMessage(exception, "Content-Length exceeds max size"));
        assertEquals("req-1", captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Request-Id"));
        assertEquals("trace-1", captured.get().getRequestHeaders().getFirst("X-Kuzhambu-Trace-Id"));
    }

    @Test
    void downloadArtifactShouldRejectOversizeWorkerSizeHeader() throws IOException {
        startServer("/artifact/declared-large", exchange -> {
            exchange.getResponseHeaders().set("X-Kuzhambu-Artifact-Size-Bytes", "9");
            respondBinary(exchange, 200, "ok".getBytes(StandardCharsets.UTF_8));
        });
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(4L), new AiWorkerRequestSigner(), null);

        ArtifactDownloadException exception = assertThrows(
                ArtifactDownloadException.class,
                () -> client.downloadArtifact(
                        RequestIdCodec.toDomain("req-1"),
                        TraceIdCodec.toDomain("trace-1"),
                        "/artifact/declared-large"));

        assertTrue(exception.getMessage().contains("X-Kuzhambu-Artifact-Size-Bytes exceeds max size"));
    }

    @Test
    void downloadArtifactShouldRejectOversizeActualBody() throws IOException {
        startServer("/artifact/chunked-large", exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("too-large".getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });
        AiWorkerHttpGateway client = new AiWorkerHttpGateway(properties(4L), new AiWorkerRequestSigner(), null);

        ArtifactDownloadException exception = assertThrows(
                ArtifactDownloadException.class,
                () -> client.downloadArtifact(
                        RequestIdCodec.toDomain("req-1"), TraceIdCodec.toDomain("trace-1"), "/artifact/chunked-large"));

        assertTrue(exception.getMessage().contains("artifact body exceeds max size"));
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

    private static String succeededResponse() {
        return """
                {
                  "requestId":"req-1",
                  "traceId":"trace-1",
                  "status":"SUCCEEDED",
                  "capability":"summary",
                  "result":{"format":"text","payload":"done"}
                }
                """;
    }

    private AiWorkerGatewayProperties properties() {
        return properties(50L * 1024L * 1024L);
    }

    private boolean hasMessage(Throwable throwable, String fragment) {
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(fragment)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private AiWorkerGatewayProperties properties(long maxArtifactSizeBytes) {
        AiWorkerGatewayProperties properties = new AiWorkerGatewayProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setInternalSecret("worker-secret");
        properties.setServiceName("kuzhambu-ai-test");
        properties.setTimeoutMs(3000);
        properties.setMaxArtifactSizeBytes(maxArtifactSizeBytes);
        return properties;
    }

    private AiInvokeCommand command() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias("classics_summary"));
        command.setWorkerCapability("summary");
        command.setOperation("translate");
        command.setContentRef(
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.ofNullable("entry", 10L));
        command.setServiceRole("default");
        command.setModelId(20L);
        command.setModelName("model-a");
        command.setPromptVersionId(30L);
        command.setRequestId("req-1");
        command.setTraceId("trace-1");
        command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"hello\"}]");
        command.setPromptVariablesJson("{}");
        command.setInputPayloadJson("{\"text\":\"hello\"}");
        command.setOutputSchemaJson("{\"type\":\"text\"}");
        return command;
    }

    private void respond(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        respondBinary(exchange, statusCode, bytes);
    }

    private void respondBinary(HttpExchange exchange, int statusCode, byte[] bytes) throws IOException {
        if (exchange.getResponseHeaders().getFirst("Content-Type") == null) {
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        }
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private interface ExchangeHandler {

        void handle(HttpExchange exchange) throws IOException;
    }
}
