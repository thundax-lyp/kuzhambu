package com.thundax.kuzhambu.ai.infra.invocation.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway.ArtifactDownloadException;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway.DownloadedArtifact;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.support.AiWorkerModelConfigResolver;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.infra.invocation.gateway.dto.AiWorkerHttpPayloads;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class AiWorkerHttpGateway implements AiWorkerGateway {

    private static final String CALLER_DOMAIN = "AI";
    private static final String INVOKE_PATH = "/internal/ai/invoke";
    private static final String STREAM_PATH = "/internal/ai/stream";
    private static final String EVENT_ERROR = "error";
    private static final String ERROR_INTERNAL_FAILURE = "INTERNAL_FAILURE";
    private static final String ERROR_WORKER_PROTOCOL_FAILURE = "WORKER_PROTOCOL_FAILURE";
    private static final String ERROR_WORKER_TIMEOUT = "WORKER_TIMEOUT";
    private static final String ERROR_WORKER_UNAVAILABLE = "WORKER_UNAVAILABLE";

    private final AiWorkerGatewayProperties properties;
    private final AiWorkerRequestSigner requestSigner;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AiWorkerModelConfigResolver modelConfigResolver;

    public AiWorkerHttpGateway(
            AiWorkerGatewayProperties properties,
            AiWorkerRequestSigner requestSigner,
            AiWorkerModelConfigResolver modelConfigResolver) {
        this.properties = properties;
        this.requestSigner = requestSigner;
        this.modelConfigResolver = modelConfigResolver;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }

    @Override
    public AiInvokeResult invoke(AiInvokeCommand command) {
        try {
            String body = objectMapper.writeValueAsString(toRequest(command, false));
            String invokePath = resolveInvokePath(command);
            HttpRequest request = buildPostRequest(invokePath, command.getRequestId(), command.getTraceId(), body);
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (!isSuccessful(response.statusCode())) {
                return httpFailure(command, response.statusCode(), response.body());
            }
            return toInvokeResult(
                    command, objectMapper.readValue(response.body(), AiWorkerHttpPayloads.InvokeResponse.class));
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return failure(command, ERROR_WORKER_PROTOCOL_FAILURE, ex.getMessage());
        } catch (HttpTimeoutException ex) {
            return failure(command, ERROR_WORKER_TIMEOUT, "Worker request timed out");
        } catch (IOException ex) {
            return failure(command, ERROR_WORKER_UNAVAILABLE, "Worker is unavailable");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return failure(command, ERROR_WORKER_TIMEOUT, "Worker request was interrupted");
        } catch (RuntimeException ex) {
            return failure(command, ERROR_INTERNAL_FAILURE, ex.getMessage());
        }
    }

    @Override
    public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        try {
            String body = objectMapper.writeValueAsString(toRequest(command, true));
            String streamPath = resolveStreamPath(command);
            HttpRequest request = buildPostRequest(streamPath, command.getRequestId(), command.getTraceId(), body);
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (!isSuccessful(response.statusCode())) {
                emitError(
                        eventConsumer, command, httpFailure(command, response.statusCode(), readAll(response.body())));
                return;
            }
            readSse(response.body(), eventConsumer, command);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            emitError(eventConsumer, command, failure(command, ERROR_WORKER_PROTOCOL_FAILURE, ex.getMessage()));
        } catch (HttpTimeoutException ex) {
            emitError(eventConsumer, command, failure(command, ERROR_WORKER_TIMEOUT, "Worker stream timed out"));
        } catch (IOException ex) {
            emitError(
                    eventConsumer, command, failure(command, ERROR_WORKER_UNAVAILABLE, "Worker stream is unavailable"));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            emitError(eventConsumer, command, failure(command, ERROR_WORKER_TIMEOUT, "Worker stream was interrupted"));
        } catch (RuntimeException ex) {
            emitError(eventConsumer, command, failure(command, ERROR_INTERNAL_FAILURE, ex.getMessage()));
        }
    }

    @Override
    public DownloadedArtifact downloadArtifact(String requestId, String traceId, String downloadPath) {
        try {
            HttpRequest request = buildGetRequest(downloadPath, requestId, traceId);
            HttpResponse<byte[]> response = httpClient.send(request, boundedArtifactBodyHandler());
            if (!isSuccessful(response.statusCode())) {
                throw new ArtifactDownloadException("ARTIFACT_DOWNLOAD failed with HTTP " + response.statusCode());
            }
            long declaredSize = parseOptionalLong(
                    response.headers()
                            .firstValue("X-Kuzhambu-Artifact-Size-Bytes")
                            .orElse(null),
                    "X-Kuzhambu-Artifact-Size-Bytes");
            assertArtifactSizeWithinLimit(declaredSize, "X-Kuzhambu-Artifact-Size-Bytes");
            assertArtifactSizeWithinLimit(response.body().length, "artifact body");
            return new DownloadedArtifact(
                    response.body(),
                    response.headers().firstValue("Content-Type").orElse("application/octet-stream"),
                    resolveFilename(
                            response.headers().firstValue("Content-Disposition").orElse(null)),
                    response.headers().firstValue("X-Kuzhambu-Artifact-Sha256").orElse(null),
                    declaredSize >= 0L ? declaredSize : (long) response.body().length,
                    response.headers()
                            .firstValue("X-Kuzhambu-Artifact-Expires-At")
                            .map(this::toInstant)
                            .orElse(null));
        } catch (HttpTimeoutException ex) {
            throw new ArtifactDownloadException("ARTIFACT_DOWNLOAD timed out", ex);
        } catch (IOException ex) {
            throw new ArtifactDownloadException("ARTIFACT_DOWNLOAD unavailable", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ArtifactDownloadException("ARTIFACT_DOWNLOAD interrupted", ex);
        }
    }

    private HttpResponse.BodyHandler<byte[]> boundedArtifactBodyHandler() {
        return responseInfo -> {
            long contentLength =
                    responseInfo.headers().firstValueAsLong("Content-Length").orElse(-1L);
            assertArtifactSizeWithinLimit(contentLength, "Content-Length");
            return HttpResponse.BodySubscribers.ofByteArray();
        };
    }

    private void assertArtifactSizeWithinLimit(long sizeBytes, String source) {
        long maxSizeBytes = properties.getMaxArtifactSizeBytes();
        if (sizeBytes < 0L || maxSizeBytes <= 0L || sizeBytes <= maxSizeBytes) {
            return;
        }
        throw new ArtifactDownloadException(
                "ARTIFACT_DOWNLOAD " + source + " exceeds max size: " + sizeBytes + " > " + maxSizeBytes);
    }

    private long parseOptionalLong(String value, String headerName) {
        if (isBlank(value)) {
            return -1L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new ArtifactDownloadException("ARTIFACT_DOWNLOAD invalid " + headerName + ": " + value, ex);
        }
    }

    private String resolveInvokePath(AiInvokeCommand command) {
        return INVOKE_PATH;
    }

    private String resolveStreamPath(AiInvokeCommand command) {
        return STREAM_PATH;
    }

    private AiWorkerHttpPayloads.InvokeRequest toRequest(AiInvokeCommand command, boolean stream) {
        AiWorkerHttpPayloads.InvokeRequest request = new AiWorkerHttpPayloads.InvokeRequest();
        request.setRequestId(command.getRequestId());
        request.setTraceId(command.getTraceId());
        request.setCallerDomain(CALLER_DOMAIN);
        request.setOperation(command.getOperation());
        request.setCapability(defaultString(command.getWorkerCapability(), command.getCapability()));
        request.setScope(command.getScope());
        request.setModelConfig(modelConfig(command));
        request.setPrompt(prompt(command));
        request.setInput(input(command));
        request.setOutputSchema(jsonOrDefault(
                command.getOutputSchemaJson(), objectMapper.createObjectNode().put("type", "text")));
        request.setOptions(options(command, stream));
        return request;
    }

    private AiWorkerHttpPayloads.ModelConfig modelConfig(AiInvokeCommand command) {
        AiWorkerHttpPayloads.ModelConfig modelConfig = new AiWorkerHttpPayloads.ModelConfig();
        if (modelConfigResolver != null) {
            AiWorkerModelConfigResolver.ResolvedModelConfig resolved = modelConfigResolver.resolve(command);
            if (resolved != null) {
                modelConfig.setServiceRole(resolved.serviceRole());
                modelConfig.setApiSource(resolved.apiSource());
                modelConfig.setBaseUrl(resolved.baseUrl());
                modelConfig.setApiKey(resolved.apiKey());
                modelConfig.setModelName(resolved.modelName());
                modelConfig.setCapabilityTags(
                        resolved.capabilityTags() == null ? Collections.emptyList() : resolved.capabilityTags());
                modelConfig.setParameters(resolved.parameters());
                modelConfig.setTimeoutMs(
                        resolved.timeoutMs() == null ? properties.getTimeoutMs() : resolved.timeoutMs());
                return modelConfig;
            }
        }
        modelConfig.setServiceRole(command.getServiceRole());
        modelConfig.setModelName(command.getModelName());
        modelConfig.setCapabilityTags(Collections.emptyList());
        modelConfig.setParameters(objectMapper.createObjectNode());
        modelConfig.setTimeoutMs(properties.getTimeoutMs());
        return modelConfig;
    }

    private AiWorkerHttpPayloads.Prompt prompt(AiInvokeCommand command) {
        AiWorkerHttpPayloads.Prompt prompt = new AiWorkerHttpPayloads.Prompt();
        if (command.getPromptVersionId() != null) {
            prompt.setPromptVersionId(String.valueOf(command.getPromptVersionId()));
        }
        prompt.setMessages(jsonOrDefault(command.getPromptMessagesJson(), objectMapper.createArrayNode()));
        prompt.setVariables(jsonOrDefault(command.getPromptVariablesJson(), objectMapper.createObjectNode()));
        prompt.setPromptHash(command.getPromptHash());
        return prompt;
    }

    private AiWorkerHttpPayloads.Input input(AiInvokeCommand command) {
        AiWorkerHttpPayloads.Input input = new AiWorkerHttpPayloads.Input();
        input.setContentType(command.getContentType());
        if (command.getContentId() != null) {
            input.setContentId(String.valueOf(command.getContentId()));
        }
        input.setPayload(jsonOrDefault(command.getInputPayloadJson(), objectMapper.createObjectNode()));
        return input;
    }

    private AiWorkerHttpPayloads.Options options(AiInvokeCommand command, boolean stream) {
        AiWorkerHttpPayloads.Options options = new AiWorkerHttpPayloads.Options();
        options.setStream(stream);
        options.setForceJson(command.isForceJson());
        options.setLocale(command.getLocale());
        return options;
    }

    private HttpRequest buildPostRequest(String path, String requestId, String traceId, String body) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = requestSigner.sign("POST", path, timestamp, requestId, body, properties.getInternalSecret());
        return HttpRequest.newBuilder(uri(path))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json, text/event-stream")
                .header("X-Kuzhambu-Service", properties.getServiceName())
                .header("X-Kuzhambu-Request-Id", requestId)
                .header("X-Kuzhambu-Trace-Id", traceId)
                .header("X-Kuzhambu-Timestamp", timestamp)
                .header("X-Kuzhambu-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
    }

    private HttpRequest buildGetRequest(String path, String requestId, String traceId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = requestSigner.sign("GET", path, timestamp, requestId, "", properties.getInternalSecret());
        return HttpRequest.newBuilder(uri(path))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                .header("Accept", "application/octet-stream")
                .header("X-Kuzhambu-Service", properties.getServiceName())
                .header("X-Kuzhambu-Request-Id", requestId)
                .header("X-Kuzhambu-Trace-Id", traceId)
                .header("X-Kuzhambu-Timestamp", timestamp)
                .header("X-Kuzhambu-Signature", signature)
                .GET()
                .build();
    }

    private URI uri(String path) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Worker base url is not configured");
        }
        return URI.create(baseUrl.replaceAll("/+$", "") + path);
    }

    private AiInvokeResult toInvokeResult(AiInvokeCommand command, AiWorkerHttpPayloads.InvokeResponse response)
            throws JsonProcessingException {
        if (response == null || response.getStatus() == null) {
            return failure(command, ERROR_WORKER_PROTOCOL_FAILURE, "Worker returned invalid response");
        }
        AiInvokeResult result = new AiInvokeResult();
        result.setRequestId(defaultString(response.getRequestId(), command.getRequestId()));
        result.setTraceId(defaultString(response.getTraceId(), command.getTraceId()));
        result.setStatus(response.getStatus());
        result.setCapability(defaultString(response.getCapability(), command.getCapability()));
        result.setFailureStage(response.getFailureStage());
        result.setFallbackUsed(Boolean.TRUE.equals(response.getFallbackUsed()));
        if (response.getResult() != null) {
            result.setResultFormat(response.getResult().getFormat());
            result.setResultPayload(payloadToString(response.getResult().getPayload()));
        }
        if (response.getArtifactReference() != null
                && !response.getArtifactReference().isNull()) {
            result.setArtifactReferenceJson(payloadToString(response.getArtifactReference()));
        }
        result.setUsage(toUsage(response.getUsage()));
        if (response.getWarnings() != null && !response.getWarnings().isNull()) {
            result.setWarningsJson(objectMapper.writeValueAsString(response.getWarnings()));
        }
        if (response.getError() != null) {
            result.setErrorType(response.getError().getType());
            result.setErrorMessage(response.getError().getMessage());
        }
        return result;
    }

    private void readSse(InputStream inputStream, Consumer<AiStreamEventResult> eventConsumer, AiInvokeCommand command)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String eventType = null;
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    dispatchSseEvent(eventType, data.toString(), eventConsumer, command);
                    eventType = null;
                    data.setLength(0);
                    continue;
                }
                if (line.startsWith(":")) {
                    continue;
                }
                if (line.startsWith("event:")) {
                    eventType = line.substring("event:".length()).trim();
                    continue;
                }
                if (line.startsWith("data:")) {
                    if (data.length() > 0) {
                        data.append('\n');
                    }
                    data.append(line.substring("data:".length()).trim());
                }
            }
            dispatchSseEvent(eventType, data.toString(), eventConsumer, command);
        }
    }

    private void dispatchSseEvent(
            String eventType, String data, Consumer<AiStreamEventResult> eventConsumer, AiInvokeCommand command)
            throws JsonProcessingException {
        if (isBlank(data)) {
            return;
        }
        AiStreamEventResult event = toStreamEvent(eventType, objectMapper.readTree(data), command);
        if (eventConsumer != null) {
            eventConsumer.accept(event);
        }
    }

    private AiStreamEventResult toStreamEvent(String eventType, JsonNode node, AiInvokeCommand command) {
        AiStreamEventResult event = new AiStreamEventResult();
        event.setEventType(defaultString(eventType, text(node, "eventType")));
        event.setEventId(text(node, "eventId"));
        event.setRequestId(defaultString(text(node, "requestId"), command.getRequestId()));
        event.setTraceId(defaultString(text(node, "traceId"), command.getTraceId()));
        event.setStage(text(node, "stage"));
        event.setTimestamp(toInstant(text(node, "timestamp")));
        event.setDeltaText(defaultString(
                text(node, "deltaText"), payloadToString(node.path("delta").path("text"))));
        event.setStatus(defaultString(text(node, "status"), text(node.path("extra"), "status")));
        event.setFailureStage(text(node.path("extra"), "failureStage"));
        event.setFallbackUsed(Boolean.parseBoolean(text(node.path("extra"), "fallbackUsed")));
        if (!node.path("extra").path("artifactReference").isMissingNode()
                && !node.path("extra").path("artifactReference").isNull()) {
            event.setArtifactReferenceJson(payloadToString(node.path("extra").path("artifactReference")));
        }
        setResult(node, event);
        event.setUsage(toUsage(node.get("usage")));
        JsonNode error = node.get("error");
        if (error != null && !error.isNull()) {
            event.setErrorType(text(error, "type"));
            event.setErrorMessage(text(error, "message"));
        }
        return event;
    }

    private void setResult(JsonNode node, AiStreamEventResult event) {
        JsonNode result = node.get("result");
        if (result == null || result.isNull()) {
            return;
        }
        event.setResultFormat(text(result, "format"));
        event.setResultPayload(payloadToString(result.get("payload")));
    }

    private AiInvokeResult httpFailure(AiInvokeCommand command, int statusCode, String body) {
        AiWorkerHttpPayloads.InvokeResponse response = readWorkerResponse(body);
        if (response != null
                && response.getError() != null
                && !isBlank(response.getError().getType())) {
            return failure(
                    command, response.getError().getType(), response.getError().getMessage());
        }
        return failure(command, httpErrorType(statusCode), "Worker returned HTTP " + statusCode);
    }

    private AiWorkerHttpPayloads.InvokeResponse readWorkerResponse(String body) {
        if (isBlank(body)) {
            return null;
        }
        try {
            return objectMapper.readValue(body, AiWorkerHttpPayloads.InvokeResponse.class);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String httpErrorType(int statusCode) {
        if (statusCode == 408 || statusCode == 504) {
            return ERROR_WORKER_TIMEOUT;
        }
        if (statusCode >= 500) {
            return ERROR_WORKER_UNAVAILABLE;
        }
        return ERROR_WORKER_PROTOCOL_FAILURE;
    }

    private void emitError(
            Consumer<AiStreamEventResult> eventConsumer, AiInvokeCommand command, AiInvokeResult failure) {
        if (eventConsumer == null) {
            return;
        }
        AiStreamEventResult event = new AiStreamEventResult();
        event.setEventType(EVENT_ERROR);
        event.setRequestId(command.getRequestId());
        event.setTraceId(command.getTraceId());
        event.setStage(EVENT_ERROR);
        event.setTimestamp(Instant.now());
        event.setStatus("FAILED");
        event.setErrorType(failure.getErrorType());
        event.setErrorMessage(failure.getErrorMessage());
        eventConsumer.accept(event);
    }

    private AiInvokeResult failure(AiInvokeCommand command, String errorType, String errorMessage) {
        AiInvokeResult result =
                AiInvokeResult.failed(command.getRequestId(), command.getTraceId(), errorType, errorMessage, null);
        result.setCapability(command.getCapability());
        return result;
    }

    private JsonNode jsonOrDefault(String json, JsonNode defaultValue) {
        if (isBlank(json)) {
            return defaultValue;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid worker request JSON field", ex);
        }
    }

    private AiUsageSnapshot toUsage(AiWorkerHttpPayloads.Usage usage) {
        if (usage == null) {
            return AiUsageSnapshot.empty();
        }
        return new AiUsageSnapshot(
                valueOrZero(usage.getLatencyMs()),
                valueOrZero(usage.getInputTokens()),
                valueOrZero(usage.getOutputTokens()),
                decimalOrZero(usage.getCostAmount()));
    }

    private AiUsageSnapshot toUsage(JsonNode usage) {
        if (usage == null || usage.isNull()) {
            return AiUsageSnapshot.empty();
        }
        return new AiUsageSnapshot(
                usage.path("latencyMs").asInt(0),
                usage.path("inputTokens").asInt(0),
                usage.path("outputTokens").asInt(0),
                decimalOrZero(text(usage, "costAmount")));
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal decimalOrZero(String value) {
        if (isBlank(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private String payloadToString(JsonNode payload) {
        if (payload == null || payload.isNull() || payload.isMissingNode()) {
            return null;
        }
        if (payload.isTextual()) {
            return payload.asText();
        }
        return payload.toString();
    }

    private Instant toInstant(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        try (inputStream) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private String defaultString(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String resolveFilename(String contentDisposition) {
        if (isBlank(contentDisposition)) {
            return "artifact.bin";
        }
        String marker = "filename=\"";
        int start = contentDisposition.indexOf(marker);
        if (start < 0) {
            return contentDisposition;
        }
        int valueStart = start + marker.length();
        int valueEnd = contentDisposition.indexOf('"', valueStart);
        if (valueEnd < 0) {
            return contentDisposition.substring(valueStart);
        }
        return contentDisposition.substring(valueStart, valueEnd);
    }
}
