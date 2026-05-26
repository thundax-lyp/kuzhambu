package com.thundax.kuzhambu.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "LogResponse", description = "日志响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogResponse implements Serializable {

    @Schema(name = "id", description = "日志ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    private String remarks;

    @Schema(name = "createDate", description = "创建时间")
    @JsonProperty(value = "createDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @Schema(name = "type", description = "类型")
    @JsonProperty(value = "type")
    private String type;

    @Schema(name = "title", description = "标题")
    @JsonProperty(value = "title")
    private String title;

    @Schema(name = "remoteAddr", description = "来源地址")
    @JsonProperty(value = "remoteAddr")
    private String remoteAddr;

    @Schema(name = "userAgent", description = "UA")
    @JsonProperty(value = "userAgent")
    private String userAgent;

    @Schema(name = "method", description = "HTTP方法")
    @JsonProperty(value = "method")
    private String method;

    @Schema(name = "requestUri", description = "访问地址")
    @JsonProperty(value = "requestUri")
    private String requestUri;

    @Schema(name = "requestParams", description = "访问参数")
    @JsonProperty(value = "requestParams")
    private String requestParams;

    @Schema(name = "createUser", description = "创建人")
    @JsonProperty(value = "createUser")
    private LogUserResponse createUser;
}
