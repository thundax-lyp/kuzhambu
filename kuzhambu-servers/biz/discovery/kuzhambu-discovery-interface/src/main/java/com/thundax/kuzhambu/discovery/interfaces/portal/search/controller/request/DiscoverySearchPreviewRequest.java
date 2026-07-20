package com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DiscoverySearchPreviewRequest", description = "Discovery Portal 搜索预览请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchPreviewRequest implements Serializable {

    @NotBlank
    @Schema(name = "contentType", description = "内容类型")
    @JsonProperty(value = "contentType")
    private String contentType;

    @NotBlank
    @Schema(name = "contentId", description = "内容 ID")
    @JsonProperty(value = "contentId")
    private String contentId;
}
