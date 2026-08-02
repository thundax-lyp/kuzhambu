package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
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

    @JsonProperty("transitionStatus")
    private String transitionStatus;

    @JsonProperty("currentPublicationJobId")
    private Long currentPublicationJobId;

    @JsonProperty("translationStatus")
    private String translationStatus;

    @JsonProperty("imageStatus")
    private String imageStatus;

    @JsonProperty("visualAssetStatus")
    private String visualAssetStatus;

    @JsonProperty("refinementStatus")
    private String refinementStatus;

    @JsonProperty("currentVersionId")
    private Long currentVersionId;

    @JsonProperty("currentVersionNo")
    private Integer currentVersionNo;

    @JsonProperty("currentVersionedAt")
    private Instant currentVersionedAt;

    @JsonProperty("contentUpdatedAt")
    private Instant contentUpdatedAt;

    @JsonProperty("versionDirty")
    private Boolean versionDirty;

    @JsonProperty("tags")
    private List<ClassicsContentResponse> tags;
}
