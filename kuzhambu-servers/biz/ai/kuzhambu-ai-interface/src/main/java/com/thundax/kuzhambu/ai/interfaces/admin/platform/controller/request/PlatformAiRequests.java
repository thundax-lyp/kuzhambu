package com.thundax.kuzhambu.ai.interfaces.admin.platform.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public final class PlatformAiRequests {

    private PlatformAiRequests() {}

    @Getter
    @Setter
    @Schema(name = "PlatformAiInvokeRequest", description = "平台AI调用请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvokeRequest implements Serializable {

        @Size(max = 64)
        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "contentId")
        private Long contentId;

        @JsonProperty(value = "objectId")
        private Long objectId;

        @JsonProperty(value = "serviceId")
        private Long serviceId;

        @Size(max = 64)
        @JsonProperty(value = "serviceRole")
        private String serviceRole;

        @NotNull
        @JsonProperty(value = "modelId")
        private Long modelId;

        @Size(max = 128)
        @JsonProperty(value = "modelName")
        private String modelName;

        @JsonProperty(value = "promptVersionId")
        private Long promptVersionId;

        @NotBlank
        @Size(max = 128)
        @JsonProperty(value = "requestId")
        private String requestId;

        @NotBlank
        @Size(max = 128)
        @JsonProperty(value = "traceId")
        private String traceId;

        @NotBlank
        @JsonProperty(value = "promptMessagesJson")
        private String promptMessagesJson;

        @JsonProperty(value = "promptVariablesJson")
        private String promptVariablesJson;

        @Size(max = 128)
        @JsonProperty(value = "promptHash")
        private String promptHash;

        @NotBlank
        @JsonProperty(value = "inputPayloadJson")
        private String inputPayloadJson;

        @JsonProperty(value = "outputSchemaJson")
        private String outputSchemaJson;

        @JsonProperty(value = "forceJson")
        private Boolean forceJson;

        @Size(max = 32)
        @JsonProperty(value = "locale")
        private String locale;

        @JsonProperty(value = "allowFallback")
        private Boolean allowFallback;

        @JsonProperty(value = "createCandidate")
        private Boolean createCandidate;
    }
}
