package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public final class DiscoveryQaResponses {

    private DiscoveryQaResponses() {}

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaOpenSessionResponse", description = "Discovery Portal 问答创建会话响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenSessionResponse implements Serializable {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        private Long sessionId;

        @Schema(name = "ownerUserId", description = "拥有者用户号")
        @JsonProperty(value = "ownerUserId")
        private Long ownerUserId;

        @Schema(name = "title", description = "会话标题")
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

        @Schema(name = "status", description = "会话状态")
        @JsonProperty(value = "status")
        private String status;

        @Schema(name = "openedAt", description = "创建时间")
        @JsonProperty(value = "openedAt")
        private Long openedAt;

        @Schema(name = "lastMessageAt", description = "最后消息时间")
        @JsonProperty(value = "lastMessageAt")
        private Long lastMessageAt;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaChatCompletionsResponse", description = "Discovery Portal 问答提问响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionsResponse implements Serializable {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        private Long sessionId;

        @Schema(name = "questionMessageId", description = "用户消息号")
        @JsonProperty(value = "questionMessageId")
        private Long questionMessageId;

        @Schema(name = "answerMessageId", description = "回答消息号")
        @JsonProperty(value = "answerMessageId")
        private Long answerMessageId;

        @Schema(name = "question", description = "问题")
        @JsonProperty(value = "question")
        private String question;

        @Schema(name = "answer", description = "回答")
        @JsonProperty(value = "answer")
        private String answer;

        @Schema(name = "answerStatus", description = "回答状态")
        @JsonProperty(value = "answerStatus")
        private String answerStatus;

        @Schema(name = "failureReason", description = "失败原因")
        @JsonProperty(value = "failureReason")
        private String failureReason;

        @Schema(name = "usage", description = "用量")
        @JsonProperty(value = "usage")
        private ChatCompletionUsage usage;

        @Schema(name = "sources", description = "来源列表")
        @JsonProperty(value = "sources")
        private List<QaSourceResponse> sources;

        @Schema(name = "choices", description = "返回选项")
        @JsonProperty(value = "choices")
        private List<ChatCompletionChoice> choices;

        @Schema(name = "raw", description = "原始响应")
        @JsonProperty(value = "raw")
        private Map<String, Object> raw;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaChatCompletionChoice", description = "聊天补全候选")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionChoice implements Serializable {

        @Schema(name = "index", description = "下标")
        @JsonProperty(value = "index")
        private Integer index;

        @Schema(name = "message", description = "回复消息")
        @JsonProperty(value = "message")
        private ChatCompletionMessage message;

        @Schema(name = "finishReason", description = "完成原因")
        @JsonProperty(value = "finishReason")
        private String finishReason;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaChatCompletionMessage", description = "聊天补全消息")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionMessage implements Serializable {

        @Schema(name = "role", description = "角色")
        @JsonProperty(value = "role")
        private String role;

        @Schema(name = "content", description = "内容")
        @JsonProperty(value = "content")
        private String content;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaChatCompletionUsage", description = "token 用量")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionUsage implements Serializable {

        @Schema(name = "promptTokens", description = "提示词 tokens")
        @JsonProperty(value = "promptTokens")
        private Integer promptTokens;

        @Schema(name = "completionTokens", description = "输出 tokens")
        @JsonProperty(value = "completionTokens")
        private Integer completionTokens;

        @Schema(name = "totalTokens", description = "总 tokens")
        @JsonProperty(value = "totalTokens")
        private Integer totalTokens;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSourceResponse", description = "Discovery Portal 问答来源响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSourceResponse implements Serializable {

        @Schema(name = "sourceId", description = "来源号")
        @JsonProperty(value = "sourceId")
        private String sourceId;

        @Schema(name = "contentType", description = "内容类型")
        @JsonProperty(value = "contentType")
        private String contentType;

        @Schema(name = "contentId", description = "内容号")
        @JsonProperty(value = "contentId")
        private String contentId;

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
        private BigDecimal score;

        @Schema(name = "sourceStatus", description = "来源状态")
        @JsonProperty(value = "sourceStatus")
        private String sourceStatus;
    }
}
