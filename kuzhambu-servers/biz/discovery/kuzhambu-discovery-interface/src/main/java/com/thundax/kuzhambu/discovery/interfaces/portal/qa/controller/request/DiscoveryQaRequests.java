package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public final class DiscoveryQaRequests {

    private DiscoveryQaRequests() {}

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaOpenSessionRequest", description = "Discovery Portal 问答创建会话请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenSessionRequest {

        @Schema(name = "ownerUserId", description = "拥有者用户号")
        @JsonProperty(value = "ownerUserId")
        @NotNull(message = "\"拥有者用户号\"不能为空")
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

        @Schema(name = "requestId", description = "请求号")
        @JsonProperty(value = "requestId")
        private String requestId;

        @Schema(name = "traceId", description = "链路号")
        @JsonProperty(value = "traceId")
        private String traceId;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSessionPageRequest", description = "Discovery Portal 问答会话分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionPageRequest {

        @Schema(name = "ownerUserId", description = "拥有者用户号")
        @JsonProperty(value = "ownerUserId")
        @NotNull(message = "\"拥有者用户号\"不能为空")
        private Long ownerUserId;

        @Schema(name = "pageNo", description = "页码")
        @JsonProperty(value = "pageNo")
        private Integer pageNo;

        @Schema(name = "pageSize", description = "每页条数")
        @JsonProperty(value = "pageSize")
        private Integer pageSize;

        @Schema(name = "limit", description = "最大返回条数")
        @JsonProperty(value = "limit")
        private Integer limit;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSessionGetRequest", description = "Discovery Portal 问答会话详情请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionGetRequest {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        @NotNull(message = "\"会话号\"不能为空")
        private Long sessionId;

        @Schema(name = "ownerUserId", description = "拥有者用户号")
        @JsonProperty(value = "ownerUserId")
        @NotNull(message = "\"拥有者用户号\"不能为空")
        private Long ownerUserId;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaSessionDeleteRequest", description = "Discovery Portal 问答会话删除请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QaSessionDeleteRequest {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        @NotNull(message = "\"会话号\"不能为空")
        private Long sessionId;

        @Schema(name = "ownerUserId", description = "拥有者用户号")
        @JsonProperty(value = "ownerUserId")
        @NotNull(message = "\"拥有者用户号\"不能为空")
        private Long ownerUserId;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaChatCompletionsRequest", description = "Discovery Portal 问答提问请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionsRequest {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        @NotNull(message = "\"会话号\"不能为空")
        private Long sessionId;

        @Schema(name = "model", description = "知识库名")
        @JsonProperty(value = "model")
        private String model;

        @Schema(name = "messages", description = "OpenAI-compatible 消息列表")
        @JsonProperty(value = "messages")
        private List<ChatMessage> messages;

        @Schema(name = "stream", description = "是否流式返回")
        @JsonProperty(value = "stream")
        private Boolean stream;

        @Schema(name = "metadata", description = "元数据")
        @JsonProperty(value = "metadata")
        private Map<String, Object> metadata;

        @Schema(name = "options", description = "模型参数")
        @JsonProperty(value = "options")
        private Map<String, Object> options;

        @Schema(name = "requestId", description = "请求号")
        @JsonProperty(value = "requestId")
        private String requestId;

        @Schema(name = "traceId", description = "链路号")
        @JsonProperty(value = "traceId")
        private String traceId;
    }

    @Getter
    @Setter
    @Schema(name = "DiscoveryQaChatCompletionMessage", description = "聊天消息")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatMessage {

        @Schema(name = "role", description = "角色")
        @JsonProperty(value = "role")
        @NotBlank(message = "\"角色\"不能为空")
        private String role;

        @Schema(name = "content", description = "内容")
        @JsonProperty(value = "content")
        @NotBlank(message = "\"内容\"不能为空")
        private String content;
    }
}
