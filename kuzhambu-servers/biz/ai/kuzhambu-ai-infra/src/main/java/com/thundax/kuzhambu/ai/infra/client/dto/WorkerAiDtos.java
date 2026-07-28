package com.thundax.kuzhambu.ai.infra.client.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class WorkerAiDtos {

    private WorkerAiDtos() {}

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WorkerAiRequest {

        private String requestId;
        private String traceId;
        private String callerDomain;
        private String operation;
        private String capability;
        private String scope;
        private ModelConfig modelConfig;
        private Prompt prompt;
        private Input input;
        private JsonNode outputSchema;
        private Options options;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ModelConfig {

        private String serviceRole;
        private String apiSource;
        private String baseUrl;
        private String apiKey;
        private String modelName;
        private List<String> capabilityTags;
        private JsonNode parameters;
        private Long timeoutMs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Prompt {

        private String templateId;
        private String promptVersionId;
        private Integer versionNo;
        private JsonNode messages;
        private JsonNode variables;
        private String promptHash;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Input {

        private String contentType;
        private String contentId;
        private JsonNode payload;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Options {

        private boolean stream;
        private boolean forceJson;
        private String locale;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WorkerAiResponse {

        private String requestId;
        private String traceId;
        private String status;
        private String capability;
        private Result result;
        private Usage usage;
        private String failureStage;
        private Boolean fallbackUsed;
        private JsonNode artifactReference;
        private JsonNode warnings;
        private String errorType;
        private String errorMessage;
        private Error error;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Result {

        private String format;
        private JsonNode payload;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Usage {

        private Integer latencyMs;
        private Integer inputTokens;
        private Integer outputTokens;
        private String costAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Error {

        private String type;
        private String code;
        private String message;
        private Boolean retryable;
        private JsonNode detail;
    }
}
