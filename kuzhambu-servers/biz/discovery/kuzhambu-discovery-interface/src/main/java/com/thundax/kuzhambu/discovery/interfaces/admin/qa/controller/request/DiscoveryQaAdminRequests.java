package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

public final class DiscoveryQaAdminRequests {

    private DiscoveryQaAdminRequests() {}

    @Getter
    @Setter
    @Schema(name = "KnowledgeHealthRequest", description = "Discovery QA 知识库健康请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KnowledgeHealthRequest {}

    @Getter
    @Setter
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
    @Schema(name = "QaSessionPageRequest", description = "Discovery QA 会话分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionPageRequest extends PageRequest {

        @Schema(name = "title", description = "标题")
        @JsonProperty(value = "title")
        private String title;

        @Schema(name = "openedAtStart", description = "创建开始时间")
        @JsonProperty(value = "openedAtStart")
        private Instant openedAtStart;

        @Schema(name = "openedAtEnd", description = "创建结束时间")
        @JsonProperty(value = "openedAtEnd")
        private Instant openedAtEnd;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSessionGetRequest", description = "Discovery QA 会话详情请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionGetRequest {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        @NotBlank(message = "\"会话号\"不能为空")
        @Pattern(regexp = "[1-9]\\d*", message = "\"会话号\"必须为数字")
        private String sessionId;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSessionDeleteRequest", description = "Discovery QA 会话删除请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionDeleteRequest {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        @NotBlank(message = "\"会话号\"不能为空")
        @Pattern(regexp = "[1-9]\\d*", message = "\"会话号\"必须为数字")
        private String sessionId;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSessionExportRequest", description = "Discovery QA 会话导出请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionExportRequest {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        @NotBlank(message = "\"会话号\"不能为空")
        @Pattern(regexp = "[1-9]\\d*", message = "\"会话号\"必须为数字")
        private String sessionId;

        @Schema(name = "requesterUserId", description = "请求用户号")
        @JsonProperty(value = "requesterUserId")
        private Long requesterUserId;

        @Schema(name = "format", description = "导出格式")
        @JsonProperty(value = "format")
        private String format;
    }
}
