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

        @Schema(name = "removedAt", description = "删除时间")
        @JsonProperty(value = "removedAt")
        private Long removedAt;

        @Schema(name = "messages", description = "会话消息")
        @JsonProperty(value = "messages")
        private List<QaMessageResponse> messages;
    }

    @Getter
    @Builder
    @Schema(name = "DiscoveryQaSessionExportResponse", description = "Discovery QA 会话导出响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionExportResponse implements Serializable {

        @Schema(name = "exportId", description = "导出号")
        @JsonProperty(value = "exportId")
        private Long exportId;

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        private Long sessionId;

        @Schema(name = "format", description = "导出格式")
        @JsonProperty(value = "format")
        private String format;

        @Schema(name = "storageObjectId", description = "Storage 对象号")
        @JsonProperty(value = "storageObjectId")
        private Long storageObjectId;

        @Schema(name = "exportStatus", description = "导出状态")
        @JsonProperty(value = "exportStatus")
        private String exportStatus;

        @Schema(name = "failureReason", description = "失败原因")
        @JsonProperty(value = "failureReason")
        private String failureReason;

        @Schema(name = "requestedAt", description = "请求时间")
        @JsonProperty(value = "requestedAt")
        private Long requestedAt;

        @Schema(name = "completedAt", description = "完成时间")
        @JsonProperty(value = "completedAt")
        private Long completedAt;

        @Schema(name = "filename", description = "文件名")
        @JsonProperty(value = "filename")
        private String filename;

        @Schema(name = "contentType", description = "内容类型")
        @JsonProperty(value = "contentType")
        private String contentType;
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
    @Schema(name = "DiscoveryQaKnowledgeHealthResponse", description = "Discovery QA 知识库健康响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaKnowledgeHealthResponse implements Serializable {

        @Schema(name = "knowledgeBaseName", description = "知识库名称")
        @JsonProperty(value = "knowledgeBaseName")
        private String knowledgeBaseName;

        @Schema(name = "status", description = "健康状态")
        @JsonProperty(value = "status")
        private String status;

        @Schema(name = "provider", description = "Provider")
        @JsonProperty(value = "provider")
        private String provider;

        @Schema(name = "checkedAt", description = "检查时间")
        @JsonProperty(value = "checkedAt")
        private Long checkedAt;

        @Schema(name = "failureReason", description = "失败原因")
        @JsonProperty(value = "failureReason")
        private String failureReason;

        @Schema(name = "raw", description = "原始响应")
        @JsonProperty(value = "raw")
        private Object raw;
    }

    @Getter
    @Builder
    @Schema(name = "DiscoveryQaSyncItemResponse", description = "Discovery QA 同步条目响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSyncItemResponse implements Serializable {

        @Schema(name = "sourceId", description = "来源号")
        @JsonProperty(value = "sourceId")
        private String sourceId;

        @Schema(name = "contentType", description = "内容类型")
        @JsonProperty(value = "contentType")
        private String contentType;

        @Schema(name = "contentId", description = "内容号")
        @JsonProperty(value = "contentId")
        private Long contentId;

        @Schema(name = "knowledgeBaseName", description = "知识库名称")
        @JsonProperty(value = "knowledgeBaseName")
        private String knowledgeBaseName;

        @Schema(name = "currentVersionNo", description = "当前版本号")
        @JsonProperty(value = "currentVersionNo")
        private Integer currentVersionNo;

        @Schema(name = "knowledgeRevision", description = "知识版本哈希")
        @JsonProperty(value = "knowledgeRevision")
        private String knowledgeRevision;

        @Schema(name = "provider", description = "Provider")
        @JsonProperty(value = "provider")
        private String provider;

        @Schema(name = "externalKnowledgeBaseId", description = "外部知识库ID")
        @JsonProperty(value = "externalKnowledgeBaseId")
        private String externalKnowledgeBaseId;

        @Schema(name = "externalKnowledgeItemId", description = "外部知识条目ID")
        @JsonProperty(value = "externalKnowledgeItemId")
        private String externalKnowledgeItemId;

        @Schema(name = "syncStatus", description = "同步状态")
        @JsonProperty(value = "syncStatus")
        private String syncStatus;

        @Schema(name = "failureReason", description = "失败原因")
        @JsonProperty(value = "failureReason")
        private String failureReason;

        @Schema(name = "syncedAt", description = "同步时间")
        @JsonProperty(value = "syncedAt")
        private Long syncedAt;

        @Schema(name = "createdAt", description = "创建时间")
        @JsonProperty(value = "createdAt")
        private Long createdAt;

        @Schema(name = "updatedAt", description = "更新时间")
        @JsonProperty(value = "updatedAt")
        private Long updatedAt;
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

        @Schema(name = "provider", description = "Provider")
        @JsonProperty(value = "provider")
        private String provider;

        @Schema(name = "externalKnowledgeBaseId", description = "外部知识库ID")
        @JsonProperty(value = "externalKnowledgeBaseId")
        private String externalKnowledgeBaseId;

        @Schema(name = "externalKnowledgeItemIds", description = "外部知识条目ID集合")
        @JsonProperty(value = "externalKnowledgeItemIds")
        private String externalKnowledgeItemIds;

        @Schema(name = "externalChatId", description = "外部会话ID")
        @JsonProperty(value = "externalChatId")
        private String externalChatId;

        @Schema(name = "providerRequestId", description = "Provider 请求ID")
        @JsonProperty(value = "providerRequestId")
        private String providerRequestId;

        @Schema(name = "latencyMs", description = "耗时（ms）")
        @JsonProperty(value = "latencyMs")
        private Long latencyMs;

        @Schema(name = "failureReason", description = "失败原因")
        @JsonProperty(value = "failureReason")
        private String failureReason;

        @Schema(name = "raw", description = "原始响应")
        @JsonProperty(value = "raw")
        private String raw;

        @Schema(name = "retrievedAt", description = "检索时间")
        @JsonProperty(value = "retrievedAt")
        private Date retrievedAt;
    }
}
