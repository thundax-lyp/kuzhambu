package com.thundax.kuzhambu.ai.interfaces.admin.config.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class AiConfigRequests {

    private AiConfigRequests() {}

    @Getter
    @Setter
    @Schema(name = "AiServiceIdRequest", description = "AI服务ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceIdRequest implements Serializable {

        @NotNull
        @Schema(name = "serviceId", description = "服务ID")
        @JsonProperty(value = "serviceId")
        private Long serviceId;
    }

    @Getter
    @Setter
    @Schema(name = "AiServiceRoleRequest", description = "AI服务角色请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceRoleRequest implements Serializable {

        @NotBlank
        @Size(max = 32)
        @Schema(name = "serviceRole", description = "服务角色")
        @JsonProperty(value = "serviceRole")
        private String serviceRole;
    }

    @Getter
    @Setter
    @Schema(name = "AiServiceConfigSaveRequest", description = "AI服务配置保存请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceConfigSaveRequest implements Serializable {

        @NotNull
        @Schema(name = "serviceId", description = "服务ID")
        @JsonProperty(value = "serviceId")
        private Long serviceId;

        @NotBlank
        @Size(max = 32)
        @Schema(name = "serviceRole", description = "服务角色")
        @JsonProperty(value = "serviceRole")
        private String serviceRole;

        @NotBlank
        @Size(max = 64)
        @Schema(name = "apiSource", description = "API来源")
        @JsonProperty(value = "apiSource")
        private String apiSource;

        @NotBlank
        @Size(max = 500)
        @Schema(name = "baseUrl", description = "模型服务基础地址")
        @JsonProperty(value = "baseUrl")
        private String baseUrl;

        @Size(max = 1000)
        @Schema(name = "encryptedApiKey", description = "加密后的API Key")
        @JsonProperty(value = "encryptedApiKey")
        private String encryptedApiKey;

        @Schema(name = "enabled", description = "是否启用")
        @JsonProperty(value = "enabled")
        private Boolean enabled;

        @Size(max = 32)
        @Schema(name = "status", description = "服务状态")
        @JsonProperty(value = "status")
        private String status;
    }

    @Getter
    @Setter
    @Schema(name = "AiModelIdRequest", description = "AI模型ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelIdRequest implements Serializable {

        @NotNull
        @Schema(name = "modelId", description = "模型ID")
        @JsonProperty(value = "modelId")
        private Long modelId;
    }

    @Getter
    @Setter
    @Schema(name = "AiModelListRequest", description = "AI模型列表请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelListRequest implements Serializable {

        @Schema(name = "serviceId", description = "服务ID")
        @JsonProperty(value = "serviceId")
        private Long serviceId;

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

        @Schema(name = "modelId", description = "模型ID")
        @JsonProperty(value = "modelId")
        private Long modelId;

        @NotNull
        @Schema(name = "serviceId", description = "服务ID")
        @JsonProperty(value = "serviceId")
        private Long serviceId;

        @NotBlank
        @Size(max = 128)
        @Schema(name = "modelName", description = "模型名称")
        @JsonProperty(value = "modelName")
        private String modelName;

        @Size(max = 128)
        @Schema(name = "displayName", description = "展示名称")
        @JsonProperty(value = "displayName")
        private String displayName;

        @Schema(name = "capabilityTags", description = "能力标签")
        @JsonProperty(value = "capabilityTags")
        private List<String> capabilityTags;

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
    @Schema(name = "AiModelCheckRecordRequest", description = "AI模型检测记录请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelCheckRecordRequest implements Serializable {

        @Schema(name = "checkId", description = "检测ID")
        @JsonProperty(value = "checkId")
        private Long checkId;

        @NotNull
        @Schema(name = "modelId", description = "模型ID")
        @JsonProperty(value = "modelId")
        private Long modelId;

        @NotNull
        @Schema(name = "serviceId", description = "服务ID")
        @JsonProperty(value = "serviceId")
        private Long serviceId;

        @NotBlank
        @Size(max = 128)
        @Schema(name = "modelName", description = "模型名称")
        @JsonProperty(value = "modelName")
        private String modelName;

        @NotBlank
        @Size(max = 32)
        @Schema(name = "status", description = "检测状态")
        @JsonProperty(value = "status")
        private String status;

        @Schema(name = "latencyMs", description = "延迟毫秒")
        @JsonProperty(value = "latencyMs")
        private Integer latencyMs;

        @Size(max = 64)
        @Schema(name = "errorType", description = "错误类型")
        @JsonProperty(value = "errorType")
        private String errorType;

        @Size(max = 500)
        @Schema(name = "errorMessage", description = "错误信息")
        @JsonProperty(value = "errorMessage")
        private String errorMessage;

        @Schema(name = "checkedAt", description = "检测时间")
        @JsonProperty(value = "checkedAt")
        private Instant checkedAt;
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
