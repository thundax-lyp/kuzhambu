package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public final class DiscoveryQaAdminResponses {

    private DiscoveryQaAdminResponses() {}

    @Getter
    @Builder
    @Schema(name = "DiscoveryQaSessionDetailResponse", description = "Discovery QA 会话详情响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionDetailResponse implements Serializable {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        private Long sessionId;

        @Schema(name = "ownerUserId", description = "拥有者用户号")
        @JsonProperty(value = "ownerUserId")
        private Long ownerUserId;

        @Schema(name = "title", description = "标题")
        @JsonProperty(value = "title")
        private String title;

        @Schema(name = "scope", description = "作用域")
        @JsonProperty(value = "scope")
        private String scope;

        @Schema(name = "contextMode", description = "上下文模式")
        @JsonProperty(value = "contextMode")
        private String contextMode;

        @Schema(name = "contextContentType", description = "上下文内容类型")
        @JsonProperty(value = "contextContentType")
        private String contextContentType;

        @Schema(name = "contextContentId", description = "上下文内容标识")
        @JsonProperty(value = "contextContentId")
        private Long contextContentId;

        @Schema(name = "status", description = "状态")
        @JsonProperty(value = "status")
        private String status;

        @Schema(name = "openedAt", description = "创建时间")
        @JsonProperty(value = "openedAt")
        private Long openedAt;

        @Schema(name = "lastMessageAt", description = "最后消息时间")
        @JsonProperty(value = "lastMessageAt")
        private Long lastMessageAt;

        @Schema(name = "messages", description = "会话消息")
        @JsonProperty(value = "messages")
        private List<QaMessageResponse> messages;
    }

    @Getter
    @Builder
    @Schema(name = "DiscoveryQaMessageResponse", description = "Discovery QA 消息响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaMessageResponse implements Serializable {

        @Schema(name = "messageId", description = "消息号")
        @JsonProperty(value = "messageId")
        private Long messageId;

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        private Long sessionId;

        @Schema(name = "role", description = "角色")
        @JsonProperty(value = "role")
        private String role;

        @Schema(name = "content", description = "内容")
        @JsonProperty(value = "content")
        private String content;

        @Schema(name = "messageStatus", description = "消息状态")
        @JsonProperty(value = "messageStatus")
        private String messageStatus;

        @Schema(name = "contextTurnCount", description = "上下文轮次")
        @JsonProperty(value = "contextTurnCount")
        private Integer contextTurnCount;

        @Schema(name = "failureReason", description = "失败原因")
        @JsonProperty(value = "failureReason")
        private String failureReason;

        @Schema(name = "sentAt", description = "发送时间")
        @JsonProperty(value = "sentAt")
        private Date sentAt;

        @Schema(name = "answeredAt", description = "回答时间")
        @JsonProperty(value = "answeredAt")
        private Date answeredAt;
    }

    @Getter
    @Builder
    @Schema(name = "DiscoveryQaSourceResponse", description = "Discovery QA 来源响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSourceResponse implements Serializable {

        @Schema(name = "sourceId", description = "来源号")
        @JsonProperty(value = "sourceId")
        private Long sourceId;

        @Schema(name = "contentType", description = "内容类型")
        @JsonProperty(value = "contentType")
        private String contentType;

        @Schema(name = "contentId", description = "内容号")
        @JsonProperty(value = "contentId")
        private Long contentId;

        @Schema(name = "knowledgeBase", description = "知识库")
        @JsonProperty(value = "knowledgeBase")
        private String knowledgeBase;

        @Schema(name = "titleSnapshot", description = "标题快照")
        @JsonProperty(value = "titleSnapshot")
        private String titleSnapshot;

        @Schema(name = "locationLabel", description = "位置标签")
        @JsonProperty(value = "locationLabel")
        private String locationLabel;

        @Schema(name = "snippet", description = "摘录")
        @JsonProperty(value = "snippet")
        private String snippet;

        @Schema(name = "sourceRank", description = "来源顺序")
        @JsonProperty(value = "sourceRank")
        private Integer sourceRank;

        @Schema(name = "score", description = "来源得分")
        @JsonProperty(value = "score")
        private java.math.BigDecimal score;

        @Schema(name = "sourceStatus", description = "来源状态")
        @JsonProperty(value = "sourceStatus")
        private String sourceStatus;
    }

    @Getter
    @Builder
    @Schema(name = "DiscoveryQaTraceResponse", description = "Discovery QA 轨迹响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaTraceResponse implements Serializable {

        @Schema(name = "traceId", description = "轨迹号")
        @JsonProperty(value = "traceId")
        private Long traceId;

        @Schema(name = "messageId", description = "消息号")
        @JsonProperty(value = "messageId")
        private Long messageId;

        @Schema(name = "rawQuestion", description = "原始问题")
        @JsonProperty(value = "rawQuestion")
        private String rawQuestion;

        @Schema(name = "rewrittenQuestion", description = "改写问题")
        @JsonProperty(value = "rewrittenQuestion")
        private String rewrittenQuestion;

        @Schema(name = "scope", description = "作用域")
        @JsonProperty(value = "scope")
        private String scope;

        @Schema(name = "filtersJson", description = "过滤条件 JSON")
        @JsonProperty(value = "filtersJson")
        private String filtersJson;

        @Schema(name = "expandedTermsJson", description = "扩展词 JSON")
        @JsonProperty(value = "expandedTermsJson")
        private String expandedTermsJson;

        @Schema(name = "linkedEntitiesJson", description = "关联实体 JSON")
        @JsonProperty(value = "linkedEntitiesJson")
        private String linkedEntitiesJson;

        @Schema(name = "candidateCount", description = "候选数")
        @JsonProperty(value = "candidateCount")
        private Integer candidateCount;

        @Schema(name = "contextSnapshot", description = "上下文快照")
        @JsonProperty(value = "contextSnapshot")
        private String contextSnapshot;

        @Schema(name = "retrievedAt", description = "检索时间")
        @JsonProperty(value = "retrievedAt")
        private Date retrievedAt;
    }
}
