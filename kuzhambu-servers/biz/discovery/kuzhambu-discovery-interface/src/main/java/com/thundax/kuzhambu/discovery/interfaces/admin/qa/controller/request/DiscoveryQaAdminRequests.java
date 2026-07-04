package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public final class DiscoveryQaAdminRequests {

    private DiscoveryQaAdminRequests() {}

    @Getter
    @Setter
    @ToString
    @Schema(name = "KnowledgeHealthRequest", description = "Discovery QA 知识库健康请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KnowledgeHealthRequest {}

    @Getter
    @Setter
    @ToString
    @Schema(name = "KnowledgeRebuildRequest", description = "Discovery QA 知识库重建请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KnowledgeRebuildRequest {

        @Schema(name = "requestId", description = "请求ID")
        @JsonProperty(value = "requestId")
        private String requestId;

        @Schema(name = "traceId", description = "追踪ID")
        @JsonProperty(value = "traceId")
        private String traceId;
    }

    @Getter
    @Setter
    @ToString
    @Schema(name = "KnowledgeSyncRequest", description = "Discovery QA 知识同步请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KnowledgeSyncRequest {

        @Schema(name = "contentType", description = "内容类型")
        @JsonProperty(value = "contentType")
        @NotNull(message = "\"内容类型\"不能为空")
        private String contentType;

        @Schema(name = "contentId", description = "内容ID")
        @JsonProperty(value = "contentId")
        @NotNull(message = "\"内容ID\"不能为空")
        private Long contentId;

        @Schema(name = "currentVersionNo", description = "当前版本号")
        @JsonProperty(value = "currentVersionNo")
        private Integer currentVersionNo;

        @Schema(name = "requestId", description = "请求ID")
        @JsonProperty(value = "requestId")
        private String requestId;

        @Schema(name = "traceId", description = "追踪ID")
        @JsonProperty(value = "traceId")
        private String traceId;
    }

    @Getter
    @Setter
    @ToString
    @Schema(name = "KnowledgeSyncPageRequest", description = "Discovery QA 知识同步分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KnowledgeSyncPageRequest extends PageRequest {

        @Schema(name = "contentType", description = "内容类型")
        @JsonProperty(value = "contentType")
        private String contentType;

        @Schema(name = "syncStatus", description = "同步状态")
        @JsonProperty(value = "syncStatus")
        private String syncStatus;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSessionGetRequest", description = "Discovery QA 会话详情请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionGetRequest {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        @NotNull(message = "\"会话号\"不能为空")
        private Long sessionId;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSourceListRequest", description = "Discovery QA 来源列表请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSourceListRequest {

        @Schema(name = "messageId", description = "消息号")
        @JsonProperty(value = "messageId")
        @NotNull(message = "\"消息号\"不能为空")
        private Long messageId;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaTraceGetRequest", description = "Discovery QA 检索轨迹请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaTraceGetRequest {

        @Schema(name = "traceId", description = "轨迹号")
        @JsonProperty(value = "traceId")
        @NotNull(message = "\"轨迹号\"不能为空")
        private Long traceId;
    }
}
