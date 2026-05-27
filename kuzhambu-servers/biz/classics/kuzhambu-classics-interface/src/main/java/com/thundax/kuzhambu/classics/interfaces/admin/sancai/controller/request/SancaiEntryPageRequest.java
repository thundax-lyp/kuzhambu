package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiEntryPageRequest extends PageRequest {
    @JsonProperty("volumeId") private Long volumeId;
    @JsonProperty("keyword") private String keyword;
    @JsonProperty("lifecycleStatus") private String lifecycleStatus;
    @JsonProperty("visibility") private String visibility;
    @JsonProperty("translationStatus") private String translationStatus;
    @JsonProperty("imageStatus") private String imageStatus;
    @JsonProperty("visualAssetStatus") private String visualAssetStatus;
    @JsonProperty("refinementStatus") private String refinementStatus;
    @JsonProperty("sortDirection") private String sortDirection;
}
