package com.thundax.kuzhambu.classics.infra.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import java.util.Map;

public final class WorkerRenderHttpAssembler {

    private final ObjectMapper objectMapper;

    public WorkerRenderHttpAssembler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
    }

    public WorkerRenderHttpDtos.WorkerRenderRequest toHttpRequest(WorkerRenderDtos.WorkerRenderRequest request) {
        if (request == null) {
            return null;
        }
        WorkerRenderHttpDtos.WorkerRenderRequest httpRequest = new WorkerRenderHttpDtos.WorkerRenderRequest();
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

    public WorkerRenderDtos.WorkerRenderResponse toDomainResponse(WorkerRenderHttpDtos.WorkerRenderResponse response) {
        if (response == null) {
            return null;
        }
        WorkerRenderDtos.WorkerRenderResponse domainResponse = new WorkerRenderDtos.WorkerRenderResponse();
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

    public WorkerRenderDtos.WorkerRenderResponse failure(String errorType, String message) {
        WorkerRenderDtos.WorkerRenderResponse response = new WorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("FAILED");
        response.setRenderType("UNKNOWN");
        WorkerRenderDtos.WorkerRenderError error = new WorkerRenderDtos.WorkerRenderError();
        error.setType(errorType);
        error.setMessage(message);
        response.setError(error);
        return response;
    }

    private WorkerRenderHttpDtos.Template toHttpTemplate(WorkerRenderDtos.Template template) {
        if (template == null) {
            return null;
        }
        WorkerRenderHttpDtos.Template httpTemplate = new WorkerRenderHttpDtos.Template();
        httpTemplate.setTemplateId(template.getTemplateId());
        httpTemplate.setTemplateVersion(template.getTemplateVersion());
        return httpTemplate;
    }

    private WorkerRenderHttpDtos.Output toHttpOutput(WorkerRenderDtos.Output output) {
        if (output == null) {
            return null;
        }
        WorkerRenderHttpDtos.Output httpOutput = new WorkerRenderHttpDtos.Output();
        httpOutput.setFormat(output.getFormat());
        httpOutput.setFilenameHint(output.getFilenameHint());
        httpOutput.setLocale(output.getLocale());
        return httpOutput;
    }

    private WorkerRenderHttpDtos.Input toHttpInput(WorkerRenderDtos.Input input) {
        if (input == null) {
            return null;
        }
        WorkerRenderHttpDtos.Input httpInput = new WorkerRenderHttpDtos.Input();
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

    private WorkerRenderHttpDtos.Options toHttpOptions(WorkerRenderDtos.Options options) {
        if (options == null) {
            return null;
        }
        WorkerRenderHttpDtos.Options httpOptions = new WorkerRenderHttpDtos.Options();
        httpOptions.setStream(options.isStream());
        httpOptions.setIncludeMetadata(options.isIncludeMetadata());
        return httpOptions;
    }

    private WorkerRenderDtos.Artifact toDomainArtifact(WorkerRenderHttpDtos.Artifact artifact) {
        if (artifact == null) {
            return null;
        }
        WorkerRenderDtos.Artifact domainArtifact = new WorkerRenderDtos.Artifact();
        domainArtifact.setFormat(artifact.getFormat());
        domainArtifact.setFilename(artifact.getFilename());
        domainArtifact.setContentType(artifact.getContentType());
        domainArtifact.setEncoding(artifact.getEncoding());
        domainArtifact.setContent(artifact.getContent());
        domainArtifact.setSizeBytes(artifact.getSizeBytes());
        domainArtifact.setSha256(artifact.getSha256());
        return domainArtifact;
    }

    private WorkerRenderDtos.Summary toDomainSummary(WorkerRenderHttpDtos.Summary summary) {
        if (summary == null) {
            return null;
        }
        WorkerRenderDtos.Summary domainSummary = new WorkerRenderDtos.Summary();
        domainSummary.setItemCount(summary.getItemCount());
        domainSummary.setWarnings(summary.getWarnings());
        return domainSummary;
    }

    private WorkerRenderDtos.Usage toDomainUsage(WorkerRenderHttpDtos.Usage usage) {
        if (usage == null) {
            return null;
        }
        WorkerRenderDtos.Usage domainUsage = new WorkerRenderDtos.Usage();
        domainUsage.setLatencyMs(usage.getLatencyMs());
        return domainUsage;
    }

    private WorkerRenderDtos.WorkerRenderError toDomainError(WorkerRenderHttpDtos.WorkerRenderError error) {
        if (error == null) {
            return null;
        }
        WorkerRenderDtos.WorkerRenderError domainError = new WorkerRenderDtos.WorkerRenderError();
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
}
