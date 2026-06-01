package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

public final class AiRefinementResponses {

    private AiRefinementResponses() {}

    @Getter
    @Builder
    @Schema(name = "AiCandidateResultResponse", description = "AI候选结果响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateResultResponse implements Serializable {

        @JsonProperty(value = "callId")
        private Long callId;

        @JsonProperty(value = "candidateId")
        private Long candidateId;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "resultFormat")
        private String resultFormat;

        @JsonProperty(value = "resultPayload")
        private String resultPayload;

        @JsonProperty(value = "errorType")
        private String errorType;

        @JsonProperty(value = "errorMessage")
        private String errorMessage;
    }
}
