package com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "门户图谱素材查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphPortalMaterialRequest {
    @NotBlank
    @Size(max = 64)
    private String contentType;

    @NotBlank
    @Pattern(regexp = "^\\d+$")
    private String contentRefId;
}
