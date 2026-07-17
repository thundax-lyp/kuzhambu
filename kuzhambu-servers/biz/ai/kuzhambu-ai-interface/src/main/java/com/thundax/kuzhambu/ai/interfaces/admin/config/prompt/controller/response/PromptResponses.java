package com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

public final class PromptResponses {

    private PromptResponses() {}

    @Getter
    @Builder
    @Schema(name = "PromptTemplateResponse", description = "提示词模板响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateResponse implements Serializable {

        @JsonProperty(value = "id")
        private Long id;

        @JsonProperty(value = "scope")
        private String scope;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "name")
        private String name;

        @JsonProperty(value = "description")
        private String description;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "currentVersionNo")
        private Integer currentVersionNo;

        @JsonProperty(value = "registeredAt")
        private Instant registeredAt;
    }

    @Getter
    @Builder
    @Schema(name = "PromptVersionResponse", description = "提示词版本响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionResponse implements Serializable {

        @JsonProperty(value = "id")
        private Long id;

        @JsonProperty(value = "templateId")
        private Long templateId;

        @JsonProperty(value = "versionNo")
        private Integer versionNo;

        @JsonProperty(value = "messageTemplatesJson")
        private String messageTemplatesJson;

        @JsonProperty(value = "variablesSnapshotJson")
        private String variablesSnapshotJson;

        @JsonProperty(value = "outputSchemaJson")
        private String outputSchemaJson;

        @JsonProperty(value = "changeSummary")
        private String changeSummary;

        @JsonProperty(value = "registeredAt")
        private Instant registeredAt;
    }

    @Getter
    @Builder
    @Schema(name = "PromptVariableResponse", description = "提示词变量响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VariableResponse implements Serializable {

        @JsonProperty(value = "id")
        private Long id;

        @JsonProperty(value = "templateId")
        private Long templateId;

        @JsonProperty(value = "variableName")
        private String variableName;

        @JsonProperty(value = "required")
        private Boolean required;

        @JsonProperty(value = "description")
        private String description;

        @JsonProperty(value = "priority")
        private Integer priority;
    }
}
