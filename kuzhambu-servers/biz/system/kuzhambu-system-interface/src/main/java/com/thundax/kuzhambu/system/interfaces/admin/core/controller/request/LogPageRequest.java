package com.thundax.kuzhambu.system.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "LogPageRequest", description = "日志分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogPageRequest extends PageRequest {

    @Schema(name = "title", description = "标题")
    @JsonProperty(value = "title")
    @Size(max = 50, message = "\"标题\"长度不能超过 50")
    private String title;

    @Schema(name = "userLoginName", description = "用户登录名")
    @JsonProperty(value = "userLoginName")
    @Size(max = 50, message = "\"用户登录名\"长度不能超过 50")
    private String userLoginName;

    @Schema(name = "userName", description = "用户名")
    @JsonProperty(value = "userName")
    @Size(max = 50, message = "\"用户名\"长度不能超过 50")
    private String userName;

    @Schema(name = "remoteAddr", description = "来源")
    @JsonProperty(value = "remoteAddr")
    @Size(max = 50, message = "\"来源\"长度不能超过 50")
    private String remoteAddr;

    @Schema(name = "requestUri", description = "请求地址")
    @JsonProperty(value = "requestUri")
    @Size(max = 500, message = "\"请求地址\"长度不能超过 500")
    private String requestUri;

    @Schema(name = "beginDate", description = "开始时间，格式: yyyy-MM-dd HH:mm:ss")
    @JsonProperty(value = "beginDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginDate;

    @Schema(name = "endDate", description = "结束时间，格式: yyyy-MM-dd HH:mm:ss")
    @JsonProperty(value = "endDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;
}
