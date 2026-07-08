package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiAssetRequest extends PageRequest {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("entryId")
    private Long entryId;

    @JsonProperty("storageObjectId")
    private Long storageObjectId;

    @JsonProperty("imageType")
    private String imageType;

    @JsonProperty("title")
    private String title;

    @JsonProperty("currentUsed")
    private Boolean currentUsed;

    @JsonProperty("draftJson")
    private String draftJson;

    @JsonProperty("scopeJson")
    private String scopeJson;

    @JsonProperty("scopeTitle")
    private String scopeTitle;

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("status")
    private String status;

    @JsonProperty("entryCount")
    private Integer entryCount;

    @JsonProperty("visibilityRiskStatus")
    private String visibilityRiskStatus;

    @JsonProperty("requestedAtStart")
    private Date requestedAtStart;

    @JsonProperty("requestedAtEnd")
    private Date requestedAtEnd;

    @JsonProperty("privateConfirmed")
    private Boolean privateConfirmed;

    @JsonProperty("visualAssetId")
    private Long visualAssetId;

    @JsonProperty("versionNo")
    private Integer versionNo;

    @JsonProperty("sourceImageStorageObjectId")
    private Long sourceImageStorageObjectId;

    @JsonProperty("generatedImageStorageObjectId")
    private Long generatedImageStorageObjectId;

    @JsonProperty("textWeight")
    private Integer textWeight;

    @JsonProperty("imageWeight")
    private Integer imageWeight;

    @JsonProperty("imageAnalysisMarkdown")
    private String imageAnalysisMarkdown;

    @JsonProperty("fusionDescription")
    private String fusionDescription;

    @JsonProperty("visualDescription")
    private String visualDescription;

    @JsonProperty("generationParamsJson")
    private String generationParamsJson;
}
