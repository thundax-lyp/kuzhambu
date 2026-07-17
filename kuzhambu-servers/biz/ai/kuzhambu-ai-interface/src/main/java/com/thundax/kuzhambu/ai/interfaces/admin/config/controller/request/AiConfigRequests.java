package com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class AiConfigRequests {

    private AiConfigRequests() {}

    @Getter
    @Setter
    @Schema(name = "AiModelIdRequest", description = "AI模型ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelIdRequest implements Serializable {

        @NotNull
        @Schema(name = "id", description = "模型ID")
        @JsonProperty(value = "id")
        private Long id;
    }

    @Getter
    @Setter
    @Schema(name = "AiModelListRequest", description = "AI模型列表请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelListRequest implements Serializable {

        @Schema(name = "apiSource", description = "API来源")
        @JsonProperty(value = "apiSource")
        private String apiSource;

        @Schema(name = "enabled", description = "是否启用")
        @JsonProperty(value = "enabled")
        private Boolean enabled;
    }

    @Getter
    @Setter
    @Schema(name = "AiModelSaveRequest", description = "AI模型保存请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelSaveRequest implements Serializable {

        @Schema(name = "id", description = "模型ID")
        @JsonProperty(value = "id")
        private Long id;

        @NotBlank
        @Schema(name = "apiSource", description = "API来源")
        @JsonProperty(value = "apiSource")
        private String apiSource;

        @NotBlank
        @Size(max = 512)
        @Schema(name = "baseUrl", description = "服务地址")
        @JsonProperty(value = "baseUrl")
        private String baseUrl;

        @Schema(name = "apiKey", description = "API Key")
        @JsonProperty(value = "apiKey")
        private String apiKey;

        @NotBlank
        @Size(max = 128)
        @Schema(name = "modelName", description = "模型名称")
        @JsonProperty(value = "modelName")
        private String modelName;

        @Size(max = 128)
        @Schema(name = "displayName", description = "展示名称")
        @JsonProperty(value = "displayName")
        private String displayName;

        @Schema(name = "capabilities", description = "模型能力")
        @JsonProperty(value = "capabilities")
        private List<String> capabilities;

        @Schema(name = "defaultParamsJson", description = "默认参数JSON")
        @JsonProperty(value = "defaultParamsJson")
        private String defaultParamsJson;

        @Size(max = 500)
        @Schema(name = "description", description = "描述")
        @JsonProperty(value = "description")
        private String description;

        @Schema(name = "enabled", description = "是否启用")
        @JsonProperty(value = "enabled")
        private Boolean enabled;
    }

    @Getter
    @Setter
    @Schema(name = "AiCapabilityQueryRequest", description = "AI能力查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CapabilityQueryRequest implements Serializable {

        @Size(max = 64)
        @Schema(name = "scope", description = "业务范围")
        @JsonProperty(value = "scope")
        private String scope;

        @Size(max = 64)
        @Schema(name = "capability", description = "能力编码")
        @JsonProperty(value = "capability")
        private String capability;

        @Schema(name = "enabled", description = "是否启用")
        @JsonProperty(value = "enabled")
        private Boolean enabled;
    }

    @Getter
    @Setter
    @Schema(name = "AiActionStatusListRequest", description = "AI动作状态列表请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActionStatusListRequest implements Serializable {

        @Size(max = 64)
        @Schema(name = "scope", description = "业务范围")
        @JsonProperty(value = "scope")
        private String scope;

        @Size(max = 64)
        @Schema(name = "capability", description = "能力编码")
        @JsonProperty(value = "capability")
        private String capability;

        @Schema(name = "available", description = "是否可用")
        @JsonProperty(value = "available")
        private Boolean available;
    }

    @Getter
    @Setter
    @Schema(name = "AiCapabilityMappingSaveRequest", description = "AI能力映射保存请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CapabilityMappingSaveRequest implements Serializable {

        @Schema(name = "mappingId", description = "映射ID")
        @JsonProperty(value = "mappingId")
        private Long mappingId;

        @NotBlank
        @Size(max = 64)
        @Schema(name = "scope", description = "业务范围")
        @JsonProperty(value = "scope")
        private String scope;

        @NotBlank
        @Size(max = 64)
        @Schema(name = "capability", description = "能力编码")
        @JsonProperty(value = "capability")
        private String capability;

        @NotNull
        @Schema(name = "modelId", description = "模型ID")
        @JsonProperty(value = "modelId")
        private Long modelId;

        @Schema(name = "enabled", description = "是否启用")
        @JsonProperty(value = "enabled")
        private Boolean enabled;
    }
}
