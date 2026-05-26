package com.thundax.kuzhambu.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "PersonalAvatarResponse", description = "当前用户头像响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalAvatarResponse implements Serializable {

    @Schema(name = "avatar", description = "头像链接地址")
    @JsonProperty(value = "avatar")
    private String avatar;
}
