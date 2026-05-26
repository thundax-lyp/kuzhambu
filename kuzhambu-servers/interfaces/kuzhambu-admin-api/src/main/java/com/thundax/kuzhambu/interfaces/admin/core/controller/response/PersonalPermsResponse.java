package com.thundax.kuzhambu.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "PersonalPermsResponse", description = "当前用户权限响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalPermsResponse implements Serializable {

    @Schema(name = "perms", description = "权限编码集合")
    @JsonProperty(value = "perms")
    private Set<String> perms;
}
