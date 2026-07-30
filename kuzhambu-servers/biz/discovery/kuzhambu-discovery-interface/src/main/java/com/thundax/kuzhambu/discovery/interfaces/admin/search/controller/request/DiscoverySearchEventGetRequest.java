package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DiscoverySearchEventGetRequest", description = "Discovery 检索统计事件详情请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchEventGetRequest {

    @Schema(name = "id", description = "检索统计事件 ID")
    @JsonProperty(value = "id")
    @NotBlank(message = "\"检索统计事件号\"不能为空")
    @Pattern(regexp = "[1-9]\\d*", message = "\"检索统计事件号\"必须为数字")
    private String id;
}
