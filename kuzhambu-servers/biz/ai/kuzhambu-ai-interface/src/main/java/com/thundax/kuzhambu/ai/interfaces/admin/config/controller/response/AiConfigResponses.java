package com.thundax.kuzhambu.ai.interfaces.admin.config.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public final class AiConfigResponses {

    private AiConfigResponses() {}

    @Getter
    @Builder
    @Schema(name = "AiIdResponse", description = "AI资源ID响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdResponse implements Serializable {

        @JsonProperty(value = "id")
        private Long id;
    }

    @Getter
    @Builder
    @Schema(name = "AiModelResponse", description = "AI模型响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelResponse implements Serializable {

        @JsonProperty(value = "id")
        private Long id;

        @JsonProperty(value = "apiSource")
        private String apiSource;

        @JsonProperty(value = "baseUrl")
        private String baseUrl;

        @JsonProperty(value = "apiKeyConfigured")
        private Boolean apiKeyConfigured;

        @JsonProperty(value = "modelName")
        private String modelName;

        @JsonProperty(value = "displayName")
        private String displayName;

        @JsonProperty(value = "capabilities")
        private List<String> capabilities;

        @JsonProperty(value = "defaultParamsJson")
        private String defaultParamsJson;

        @JsonProperty(value = "description")
        private String description;

        @JsonProperty(value = "enabled")
        private Boolean enabled;

        @JsonProperty(value = "registeredAt")
        private Instant registeredAt;
    }

    @Getter
    @Builder
    @Schema(name = "AiCapabilityResponse", description = "AI能力响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CapabilityResponse implements Serializable {

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "name")
        private String name;

        @JsonProperty(value = "requiredTags")
        private List<String> requiredTags;

        @JsonProperty(value = "outputMode")
        private String outputMode;

        @JsonProperty(value = "enabled")
        private Boolean enabled;

        @JsonProperty(value = "priority")
        private Integer priority;
    }

    @Getter
    @Builder
    @Schema(name = "AiCapabilityMappingResponse", description = "AI能力映射响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CapabilityMappingResponse implements Serializable {

        @JsonProperty(value = "mappingId")
        private Long mappingId;

        @JsonProperty(value = "scope")
        private String scope;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "modelId")
        private Long modelId;

        @JsonProperty(value = "enabled")
        private Boolean enabled;

        @JsonProperty(value = "configuredAt")
        private Instant configuredAt;
    }

    @Getter
    @Builder
    @Schema(name = "AiActionStatusResponse", description = "AI动作状态响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActionStatusResponse implements Serializable {

        @JsonProperty(value = "scope")
        private String scope;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "available")
        private Boolean available;

        @JsonProperty(value = "unavailableReason")
        private String unavailableReason;

        @JsonProperty(value = "checkedAt")
        private Instant checkedAt;
    }
}
