package com.thundax.kuzhambu.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thundax.kuzhambu.common.web.response.OptionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "RoleOptionsResponse", description = "角色选项响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleOptionsResponse implements Serializable {

    @Schema(name = "statusOptions", description = "角色状态选项")
    private List<OptionResponse> statusOptions = new ArrayList<>();

    @Schema(name = "privilegeOptions", description = "角色权限选项")
    private List<OptionResponse> privilegeOptions = new ArrayList<>();
}
