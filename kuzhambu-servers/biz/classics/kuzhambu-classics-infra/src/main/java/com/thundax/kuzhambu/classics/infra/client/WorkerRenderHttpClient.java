package com.thundax.kuzhambu.classics.infra.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.infra.client.dto.WorkerRenderDtos;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class WorkerRenderHttpClient implements WorkerRenderClient {

    private static final String CLASSICS_EXPORT_PATH = "/internal/render/classics-export";
    private static final String SANCAI_SHOWCASE_PATH = "/internal/render/sancai-showcase";
    private static final String ERROR_INTERNAL_FAILURE = "INTERNAL_FAILURE";
    private static final String ERROR_WORKER_PROTOCOL_FAILURE = "WORKER_PROTOCOL_FAILURE";
    private static final String ERROR_WORKER_TIMEOUT = "WORKER_TIMEOUT";
    private static final String ERROR_WORKER_UNAVAILABLE = "WORKER_UNAVAILABLE";
    private static final String ERROR_WORKER_RESPONSE_PARSE_FAILURE = "WORKER_RESPONSE_PARSE_FAILURE";

    private final WorkerRenderProperties properties;
    private final WorkerRenderSignatureSupport signatureSupport;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WorkerRenderHttpClient(WorkerRenderProperties properties, WorkerRenderSignatureSupport signatureSupport) {
        this.properties = properties;
        this.signatureSupport = signatureSupport;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }

    @Override
    public WorkerRenderDtos.WorkerRenderResponse renderClassicsExport(WorkerRenderDtos.WorkerRenderRequest request) {
        return render(CLASSICS_EXPORT_PATH, request);
    }

    @Override
    public WorkerRenderDtos.WorkerRenderResponse renderSancaiShowcase(WorkerRenderDtos.WorkerRenderRequest request) {
        return render(SANCAI_SHOWCASE_PATH, request);
    }

    private WorkerRenderDtos.WorkerRenderResponse render(String path, WorkerRenderDtos.WorkerRenderRequest request) {
        try {
            String body = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = buildPostRequest(path, request.getRequestId(), request.getTraceId(), body);
            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (!isSuccessful(response.statusCode())) {
                return httpFailure(response.statusCode(), response.body());
            }
            return objectMapper.readValue(response.body(), WorkerRenderDtos.WorkerRenderResponse.class);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return failure(ERROR_WORKER_PROTOCOL_FAILURE, ex.getMessage());
        } catch (HttpTimeoutException ex) {
            return failure(ERROR_WORKER_TIMEOUT, "Worker request timed out");
        } catch (IOException ex) {
            return failure(ERROR_WORKER_UNAVAILABLE, "Worker is unavailable");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return failure(ERROR_WORKER_TIMEOUT, "Worker request was interrupted");
        } catch (RuntimeException ex) {
            return failure(ERROR_INTERNAL_FAILURE, ex.getMessage());
        }
    }

    private HttpRequest buildPostRequest(String path, String requestId, String traceId, String body) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = signatureSupport.sign("POST", path, timestamp, requestId, body, properties.getInternalSecret());
        return HttpRequest.newBuilder(uri(path))
                .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .header("X-Kuzhambu-Service", properties.getServiceName())
                .header("X-Kuzhambu-Request-Id", requestId)
                .header("X-Kuzhambu-Trace-Id", traceId)
                .header("X-Kuzhambu-Timestamp", timestamp)
                .header("X-Kuzhambu-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
    }

    private URI uri(String path) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Worker base url is not configured");
        }
        return URI.create(baseUrl.replaceAll("/+$", "") + path);
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private WorkerRenderDtos.WorkerRenderResponse httpFailure(int statusCode, String body) {
        WorkerRenderDtos.WorkerRenderResponse response = readWorkerResponse(body);
        if (response != null && response.getError() != null && response.getError().getType() != null) {
            return response;
        }
        return failure(httpErrorType(statusCode), "Worker returned HTTP " + statusCode);
    }

    private WorkerRenderDtos.WorkerRenderResponse readWorkerResponse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, WorkerRenderDtos.WorkerRenderResponse.class);
        } catch (JsonProcessingException ex) {
            return failure(ERROR_WORKER_RESPONSE_PARSE_FAILURE, "Failed to parse worker error response");
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

    private WorkerRenderDtos.WorkerRenderResponse failure(String errorType, String message) {
        WorkerRenderDtos.WorkerRenderResponse response = new WorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("FAILED");
        response.setRenderType("UNKNOWN");
        WorkerRenderDtos.WorkerRenderError error = new WorkerRenderDtos.WorkerRenderError();
        error.setType(errorType);
        error.setMessage(message);
        response.setError(error);
        return response;
    }
}
