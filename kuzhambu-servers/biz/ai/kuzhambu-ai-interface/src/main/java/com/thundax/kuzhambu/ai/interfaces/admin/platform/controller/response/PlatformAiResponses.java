package com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

public final class PlatformAiResponses {

    private PlatformAiResponses() {}

    @Getter
    @Builder
    @Schema(name = "PlatformAiInvokeResponse", description = "平台AI调用响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvokeResponse implements Serializable {

        @JsonProperty(value = "callId")
        private Long callId;

        @JsonProperty(value = "candidateId")
        private Long candidateId;

        @JsonProperty(value = "requestId")
        private String requestId;

        @JsonProperty(value = "traceId")
        private String traceId;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "resultFormat")
        private String resultFormat;

        @JsonProperty(value = "resultPayload")
        private String resultPayload;

        @JsonProperty(value = "artifactReferenceJson")
        private String artifactReferenceJson;

        @JsonProperty(value = "warningsJson")
        private String warningsJson;

        @JsonProperty(value = "errorType")
        private String errorType;

        @JsonProperty(value = "errorMessage")
        private String errorMessage;

        @JsonProperty(value = "failureStage")
        private String failureStage;
    }
}
