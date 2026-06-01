package com.thundax.kuzhambu.ai.interfaces.admin.prompt.controller.request;

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

public final class PromptRequests {

    private PromptRequests() {}

    @Getter
    @Setter
    @Schema(name = "PromptTemplateIdRequest", description = "提示词模板ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateIdRequest implements Serializable {

        @NotNull
        @Schema(name = "templateId", description = "模板ID")
        @JsonProperty(value = "templateId")
        private Long templateId;
    }

    @Getter
    @Setter
    @Schema(name = "PromptTemplateQueryRequest", description = "提示词模板查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateQueryRequest implements Serializable {

        @Size(max = 64)
        @Schema(name = "scope", description = "业务范围")
        @JsonProperty(value = "scope")
        private String scope;

        @Size(max = 64)
        @Schema(name = "capability", description = "能力编码")
        @JsonProperty(value = "capability")
        private String capability;
    }

    @Getter
    @Setter
    @Schema(name = "PromptTemplateSaveRequest", description = "提示词模板保存请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateSaveRequest implements Serializable {

        @Schema(name = "templateId", description = "模板ID")
        @JsonProperty(value = "templateId")
        private Long templateId;

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

        @NotBlank
        @Size(max = 128)
        @Schema(name = "name", description = "模板名称")
        @JsonProperty(value = "name")
        private String name;

        @Size(max = 500)
        @Schema(name = "description", description = "模板说明")
        @JsonProperty(value = "description")
        private String description;

        @Size(max = 32)
        @Schema(name = "status", description = "模板状态")
        @JsonProperty(value = "status")
        private String status;

        @NotBlank
        @Schema(name = "messageTemplatesJson", description = "消息模板JSON")
        @JsonProperty(value = "messageTemplatesJson")
        private String messageTemplatesJson;

        @Schema(name = "variablesSnapshotJson", description = "变量快照JSON")
        @JsonProperty(value = "variablesSnapshotJson")
        private String variablesSnapshotJson;

        @Schema(name = "outputSchemaJson", description = "输出结构JSON")
        @JsonProperty(value = "outputSchemaJson")
        private String outputSchemaJson;

        @Size(max = 500)
        @Schema(name = "changeSummary", description = "变更说明")
        @JsonProperty(value = "changeSummary")
        private String changeSummary;

        @Schema(name = "variables", description = "变量定义")
        @JsonProperty(value = "variables")
        private List<VariableItemRequest> variables;
    }

    @Getter
    @Setter
    @Schema(name = "PromptVariableItemRequest", description = "提示词变量项")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VariableItemRequest implements Serializable {

        @NotBlank
        @Size(max = 64)
        @Schema(name = "variableName", description = "变量名")
        @JsonProperty(value = "variableName")
        private String variableName;

        @Schema(name = "required", description = "是否必填")
        @JsonProperty(value = "required")
        private Boolean required;

        @Size(max = 500)
        @Schema(name = "description", description = "变量说明")
        @JsonProperty(value = "description")
        private String description;

        @Schema(name = "priority", description = "排序值")
        @JsonProperty(value = "priority")
        private Integer priority;
    }

    @Getter
    @Setter
    @Schema(name = "PromptVersionCompareRequest", description = "提示词版本对比请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionCompareRequest implements Serializable {

        @NotNull
        @Schema(name = "templateId", description = "模板ID")
        @JsonProperty(value = "templateId")
        private Long templateId;

        @Schema(name = "leftVersionNo", description = "左侧版本号")
        @JsonProperty(value = "leftVersionNo")
        private int leftVersionNo;

        @Schema(name = "rightVersionNo", description = "右侧版本号")
        @JsonProperty(value = "rightVersionNo")
        private int rightVersionNo;
    }

    @Getter
    @Setter
    @Schema(name = "PromptVersionRollbackRequest", description = "提示词版本回滚请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionRollbackRequest implements Serializable {

        @NotNull
        @Schema(name = "templateId", description = "模板ID")
        @JsonProperty(value = "templateId")
        private Long templateId;

        @Schema(name = "versionNo", description = "版本号")
        @JsonProperty(value = "versionNo")
        private int versionNo;
    }

    @Getter
    @Setter
    @Schema(name = "PromptVariableValidateRequest", description = "提示词变量校验请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VariableValidateRequest implements Serializable {

        @NotNull
        @Schema(name = "templateId", description = "模板ID")
        @JsonProperty(value = "templateId")
        private Long templateId;

        @Schema(name = "providedNames", description = "已提供变量名")
        @JsonProperty(value = "providedNames")
        private List<String> providedNames;
    }

    @Getter
    @Setter
    @Schema(name = "PromptOptimizationRequest", description = "提示词优化建议请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OptimizationRequest implements Serializable {

        @NotNull
        @Schema(name = "templateId", description = "模板ID")
        @JsonProperty(value = "templateId")
        private Long templateId;

        @Size(max = 500)
        @Schema(name = "changeSummary", description = "变更说明")
        @JsonProperty(value = "changeSummary")
        private String changeSummary;
    }
}
