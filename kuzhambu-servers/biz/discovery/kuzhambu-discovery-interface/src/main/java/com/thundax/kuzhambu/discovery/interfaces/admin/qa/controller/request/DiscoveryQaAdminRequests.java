package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public final class DiscoveryQaAdminRequests {

    private DiscoveryQaAdminRequests() {}

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
