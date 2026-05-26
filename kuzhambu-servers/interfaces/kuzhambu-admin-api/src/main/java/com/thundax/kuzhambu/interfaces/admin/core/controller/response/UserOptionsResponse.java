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
@Schema(name = "UserOptionsResponse", description = "用户选项响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserOptionsResponse implements Serializable {

    @Schema(name = "statusOptions", description = "用户状态选项")
    private List<OptionResponse> statusOptions = new ArrayList<>();

    @Schema(name = "rankOptions", description = "用户等级选项")
    private List<OptionResponse> rankOptions = new ArrayList<>();
}
