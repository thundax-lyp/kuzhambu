package com.thundax.kuzhambu.operations.infra.report.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.operations.domain.report.client.OperationsWorkerRenderClient;
import com.thundax.kuzhambu.operations.domain.report.client.dto.OperationsWorkerRenderDtos;
import com.thundax.kuzhambu.operations.infra.report.configure.OperationsWorkerRenderProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OperationsWorkerRenderHttpClient implements OperationsWorkerRenderClient {

    private static final String OPERATIONS_REPORT_PATH = "/internal/render/operations-report";
    private static final String ERROR_INTERNAL_FAILURE = "INTERNAL_FAILURE";
    private static final String ERROR_WORKER_PROTOCOL_FAILURE = "WORKER_PROTOCOL_FAILURE";
    private static final String ERROR_WORKER_TIMEOUT = "WORKER_TIMEOUT";
    private static final String ERROR_WORKER_UNAVAILABLE = "WORKER_UNAVAILABLE";

    private final OperationsWorkerRenderProperties properties;
    private final OperationsWorkerRenderSignatureSupport signatureSupport;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OperationsWorkerRenderHttpClient(
            OperationsWorkerRenderProperties properties, OperationsWorkerRenderSignatureSupport signatureSupport) {
        this.properties = properties;
        this.signatureSupport = signatureSupport;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }

    @Override
    public OperationsWorkerRenderDtos.WorkerRenderResponse renderOperationsReport(
            OperationsWorkerRenderDtos.WorkerRenderRequest request) {
        try {
            WorkerRenderRequest httpRequestBody = toHttpRequest(request);
            String body = objectMapper.writeValueAsString(httpRequestBody);
            HttpRequest httpRequest =
                    buildPostRequest(OPERATIONS_REPORT_PATH, request.getRequestId(), request.getTraceId(), body);
            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (!isSuccessful(response.statusCode())) {
                return httpFailure(response.statusCode(), response.body());
            }
            WorkerRenderResponse httpResponse = objectMapper.readValue(response.body(), WorkerRenderResponse.class);
            return toDomainResponse(httpResponse);
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
        String signature =
                signatureSupport.sign("POST", path, timestamp, requestId, body, properties.getInternalSecret());
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

    private OperationsWorkerRenderDtos.WorkerRenderResponse httpFailure(int statusCode, String body) {
        WorkerRenderResponse response = readWorkerResponse(body);
        if (response != null
                && response.getError() != null
                && response.getError().getType() != null) {
            return toDomainResponse(response);
        }
        return failure(httpErrorType(statusCode), "Worker returned HTTP " + statusCode);
    }

    private WorkerRenderResponse readWorkerResponse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, WorkerRenderResponse.class);
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

    private OperationsWorkerRenderDtos.WorkerRenderResponse failure(String errorType, String message) {
        OperationsWorkerRenderDtos.WorkerRenderResponse response =
                new OperationsWorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("FAILED");
        response.setRenderType("UNKNOWN");
        OperationsWorkerRenderDtos.WorkerRenderError error = new OperationsWorkerRenderDtos.WorkerRenderError();
        error.setType(errorType);
        error.setMessage(message);
        response.setError(error);
        return response;
    }

    private WorkerRenderRequest toHttpRequest(OperationsWorkerRenderDtos.WorkerRenderRequest request) {
        if (request == null) {
            return null;
        }
        WorkerRenderRequest httpRequest = new WorkerRenderRequest();
        httpRequest.setRequestId(request.getRequestId());
        httpRequest.setTraceId(request.getTraceId());
        httpRequest.setCallerDomain(request.getCallerDomain());
        httpRequest.setOperation(request.getOperation());
        httpRequest.setRenderType(request.getRenderType());
        httpRequest.setTemplate(toHttpTemplate(request.getTemplate()));
        httpRequest.setOutput(toHttpOutput(request.getOutput()));
        httpRequest.setInput(toHttpInput(request.getInput()));
        httpRequest.setOptions(toHttpOptions(request.getOptions()));
        return httpRequest;
    }

    private Template toHttpTemplate(OperationsWorkerRenderDtos.Template template) {
        if (template == null) {
            return null;
        }
        Template httpTemplate = new Template();
        httpTemplate.setTemplateId(template.getTemplateId());
        httpTemplate.setTemplateVersion(template.getTemplateVersion());
        return httpTemplate;
    }

    private Output toHttpOutput(OperationsWorkerRenderDtos.Output output) {
        if (output == null) {
            return null;
        }
        Output httpOutput = new Output();
        httpOutput.setFormat(output.getFormat());
        httpOutput.setFilenameHint(output.getFilenameHint());
        httpOutput.setLocale(output.getLocale());
        return httpOutput;
    }

    private Input toHttpInput(OperationsWorkerRenderDtos.Input input) {
        if (input == null) {
            return null;
        }
        Input httpInput = new Input();
        httpInput.setSnapshotId(input.getSnapshotId());
        httpInput.setContentType(input.getContentType());
        httpInput.setPayload(parsePayloadJson(input.getPayloadJson()));
        return httpInput;
    }

    private JsonNode parsePayloadJson(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(payloadJson);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private Options toHttpOptions(OperationsWorkerRenderDtos.Options options) {
        if (options == null) {
            return null;
        }
        Options httpOptions = new Options();
        httpOptions.setStream(options.isStream());
        httpOptions.setIncludeMetadata(options.isIncludeMetadata());
        return httpOptions;
    }

    private OperationsWorkerRenderDtos.WorkerRenderResponse toDomainResponse(WorkerRenderResponse response) {
        if (response == null) {
            return null;
        }
        OperationsWorkerRenderDtos.WorkerRenderResponse domainResponse =
                new OperationsWorkerRenderDtos.WorkerRenderResponse();
        domainResponse.setRequestId(response.getRequestId());
        domainResponse.setTraceId(response.getTraceId());
        domainResponse.setStatus(response.getStatus());
        domainResponse.setRenderType(response.getRenderType());
        domainResponse.setArtifact(toDomainArtifact(response.getArtifact()));
        domainResponse.setSummary(toDomainSummary(response.getSummary()));
        domainResponse.setUsage(toDomainUsage(response.getUsage()));
        domainResponse.setError(toDomainError(response.getError()));
        return domainResponse;
    }

    private OperationsWorkerRenderDtos.Artifact toDomainArtifact(Artifact artifact) {
        if (artifact == null) {
            return null;
        }
        OperationsWorkerRenderDtos.Artifact domainArtifact = new OperationsWorkerRenderDtos.Artifact();
        domainArtifact.setFormat(artifact.getFormat());
        domainArtifact.setFilename(artifact.getFilename());
        domainArtifact.setContentType(artifact.getContentType());
        domainArtifact.setEncoding(artifact.getEncoding());
        domainArtifact.setContent(artifact.getContent());
        domainArtifact.setSizeBytes(artifact.getSizeBytes());
        domainArtifact.setSha256(artifact.getSha256());
        return domainArtifact;
    }

    private OperationsWorkerRenderDtos.Summary toDomainSummary(Summary summary) {
        if (summary == null) {
            return null;
        }
        OperationsWorkerRenderDtos.Summary domainSummary = new OperationsWorkerRenderDtos.Summary();
        domainSummary.setItemCount(summary.getItemCount());
        domainSummary.setWarnings(summary.getWarnings());
        return domainSummary;
    }

    private OperationsWorkerRenderDtos.Usage toDomainUsage(Usage usage) {
        if (usage == null) {
            return null;
        }
        OperationsWorkerRenderDtos.Usage domainUsage = new OperationsWorkerRenderDtos.Usage();
        domainUsage.setLatencyMs(usage.getLatencyMs());
        return domainUsage;
    }

    private OperationsWorkerRenderDtos.WorkerRenderError toDomainError(WorkerRenderError error) {
        if (error == null) {
            return null;
        }
        OperationsWorkerRenderDtos.WorkerRenderError domainError = new OperationsWorkerRenderDtos.WorkerRenderError();
        domainError.setType(error.getType());
        domainError.setCode(error.getCode());
        domainError.setMessage(error.getMessage());
        domainError.setRetryable(error.getRetryable());
        if (error.getDetail() != null) {
            domainError.setDetail(
                    objectMapper.convertValue(error.getDetail(), new TypeReference<Map<String, Object>>() {}));
        }
        return domainError;
    }

    private static class WorkerRenderRequest {
        private String requestId;
        private String traceId;
        private String callerDomain;
        private String operation;
        private String renderType;
        private Template template;
        private Output output;
        private Input input;
        private Options options;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public String getCallerDomain() {
            return callerDomain;
        }

        public void setCallerDomain(String callerDomain) {
            this.callerDomain = callerDomain;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getRenderType() {
            return renderType;
        }

        public void setRenderType(String renderType) {
            this.renderType = renderType;
        }

        public Template getTemplate() {
            return template;
        }

        public void setTemplate(Template template) {
            this.template = template;
        }

        public Output getOutput() {
            return output;
        }

        public void setOutput(Output output) {
            this.output = output;
        }

        public Input getInput() {
            return input;
        }

        public void setInput(Input input) {
            this.input = input;
        }

        public Options getOptions() {
            return options;
        }

        public void setOptions(Options options) {
            this.options = options;
        }
    }

    private static class Template {
        private String templateId;
        private String templateVersion;

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getTemplateVersion() {
            return templateVersion;
        }

        public void setTemplateVersion(String templateVersion) {
            this.templateVersion = templateVersion;
        }
    }

    private static class Output {
        private String format;
        private String filenameHint;
        private String locale;

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getFilenameHint() {
            return filenameHint;
        }

        public void setFilenameHint(String filenameHint) {
            this.filenameHint = filenameHint;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }
    }

    private static class Input {
        private String snapshotId;
        private String contentType;
        private JsonNode payload;

        public String getSnapshotId() {
            return snapshotId;
        }

        public void setSnapshotId(String snapshotId) {
            this.snapshotId = snapshotId;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public JsonNode getPayload() {
            return payload;
        }

        public void setPayload(JsonNode payload) {
            this.payload = payload;
        }
    }

    private static class Options {
        private boolean stream;
        private boolean includeMetadata;

        public boolean isStream() {
            return stream;
        }

        public void setStream(boolean stream) {
            this.stream = stream;
        }

        public boolean isIncludeMetadata() {
            return includeMetadata;
        }

        public void setIncludeMetadata(boolean includeMetadata) {
            this.includeMetadata = includeMetadata;
        }
    }

    private static class WorkerRenderResponse {
        private String requestId;
        private String traceId;
        private String status;
        private String renderType;
        private Artifact artifact;
        private Summary summary;
        private Usage usage;
        private WorkerRenderError error;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRenderType() {
            return renderType;
        }

        public void setRenderType(String renderType) {
            this.renderType = renderType;
        }

        public Artifact getArtifact() {
            return artifact;
        }

        public void setArtifact(Artifact artifact) {
            this.artifact = artifact;
        }

        public Summary getSummary() {
            return summary;
        }

        public void setSummary(Summary summary) {
            this.summary = summary;
        }

        public Usage getUsage() {
            return usage;
        }

        public void setUsage(Usage usage) {
            this.usage = usage;
        }

        public WorkerRenderError getError() {
            return error;
        }

        public void setError(WorkerRenderError error) {
            this.error = error;
        }
    }

    private static class Artifact {
        private String format;
        private String filename;
        private String contentType;
        private String encoding;
        private String content;
        private Long sizeBytes;
        private String sha256;

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getSizeBytes() {
            return sizeBytes;
        }

        public void setSizeBytes(Long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }

        public String getSha256() {
            return sha256;
        }

        public void setSha256(String sha256) {
            this.sha256 = sha256;
        }
    }

    private static class Summary {
        private Integer itemCount;
        private List<String> warnings;

        public Integer getItemCount() {
            return itemCount;
        }

        public void setItemCount(Integer itemCount) {
            this.itemCount = itemCount;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
    }

    private static class Usage {
        private Integer latencyMs;

        public Integer getLatencyMs() {
            return latencyMs;
        }

        public void setLatencyMs(Integer latencyMs) {
            this.latencyMs = latencyMs;
        }
    }

    private static class WorkerRenderError {
        private String type;
        private String code;
        private String message;
        private Boolean retryable;
        private JsonNode detail;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getRetryable() {
            return retryable;
        }

        public void setRetryable(Boolean retryable) {
            this.retryable = retryable;
        }

        public JsonNode getDetail() {
            return detail;
        }

        public void setDetail(JsonNode detail) {
            this.detail = detail;
        }
    }
}
