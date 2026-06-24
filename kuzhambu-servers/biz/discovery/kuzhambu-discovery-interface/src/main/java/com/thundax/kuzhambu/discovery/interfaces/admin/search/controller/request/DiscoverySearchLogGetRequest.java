package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DiscoverySearchLogGetRequest", description = "Discovery 搜索日志详情请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchLogGetRequest {

    @Schema(name = "searchLogId", description = "搜索日志号")
    @JsonProperty(value = "searchLogId")
    @NotBlank(message = "\"搜索日志号\"不能为空")
    private String searchLogId;
}
