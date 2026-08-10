package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "三才管理请求")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiEntryPageRequest extends PageRequest {
    @Schema(description = "categoryId")
    @JsonProperty("categoryId")
    private Long categoryId;

    @Schema(description = "volumeId")
    @JsonProperty("volumeId")
    private Long volumeId;

    @Schema(description = "keyword")
    @JsonProperty("keyword")
    private String keyword;

    @Schema(description = "lifecycleStatus")
    @JsonProperty("lifecycleStatus")
    private String lifecycleStatus;

    @Schema(description = "translationStatus")
    @JsonProperty("translationStatus")
    private String translationStatus;

    @Schema(description = "imageStatus")
    @JsonProperty("imageStatus")
    private String imageStatus;

    @Schema(description = "visualAssetStatus")
    @JsonProperty("visualAssetStatus")
    private String visualAssetStatus;

    @Schema(description = "refinementStatus")
    @JsonProperty("refinementStatus")
    private String refinementStatus;

    @Schema(description = "sortDirection")
    @JsonProperty("sortDirection")
    private String sortDirection;
}
