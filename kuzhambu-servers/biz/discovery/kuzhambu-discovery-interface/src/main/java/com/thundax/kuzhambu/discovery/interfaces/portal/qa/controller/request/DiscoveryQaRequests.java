package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Schema(name = "DiscoveryQaAskQuestionRequest", description = "Discovery Portal 问答提问请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AskQuestionRequest {

        @Schema(name = "sessionId", description = "会话号")
        @JsonProperty(value = "sessionId")
        @NotNull(message = "\"会话号\"不能为空")
        private Long sessionId;

        @Schema(name = "question", description = "问题")
        @JsonProperty(value = "question")
        @NotBlank(message = "\"问题\"不能为空")
        private String question;

        @Schema(name = "contextTurnCount", description = "上下文轮次")
        @JsonProperty(value = "contextTurnCount")
        private Integer contextTurnCount;

        @Schema(name = "operatorType", description = "操作者类型")
        @JsonProperty(value = "operatorType")
        private String operatorType;

        @Schema(name = "operatorId", description = "操作者号")
        @JsonProperty(value = "operatorId")
        private String operatorId;

        @Schema(name = "requestId", description = "请求号")
        @JsonProperty(value = "requestId")
        private String requestId;

        @Schema(name = "traceId", description = "链路号")
        @JsonProperty(value = "traceId")
        private String traceId;
    }
}
