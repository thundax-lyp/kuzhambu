package com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "CaptchaRefreshResponse", description = "图形验证码刷新响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaptchaRefreshResponse implements Serializable {

    @Schema(name = "refreshed", description = "是否已刷新")
    @JsonProperty(value = "refreshed")
    private Boolean refreshed;
}
