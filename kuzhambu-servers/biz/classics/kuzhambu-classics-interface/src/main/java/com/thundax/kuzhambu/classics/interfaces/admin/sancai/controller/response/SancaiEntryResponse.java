package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiEntryResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("volumeId")
    private Long volumeId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("originalText")
    private String originalText;

    @JsonProperty("translationText")
    private String translationText;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("lifecycleStatus")
    private String lifecycleStatus;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty("translationStatus")
    private String translationStatus;

    @JsonProperty("imageStatus")
    private String imageStatus;

    @JsonProperty("visualAssetStatus")
    private String visualAssetStatus;

    @JsonProperty("refinementStatus")
    private String refinementStatus;

    @JsonProperty("priority")
    private Integer priority;
}
