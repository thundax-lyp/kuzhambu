package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiAssetResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("entryId")
    private Long entryId;

    @JsonProperty("visualAssetId")
    private Long visualAssetId;

    @JsonProperty("storageObjectId")
    private Long storageObjectId;

    @JsonProperty("imageType")
    private String imageType;

    @JsonProperty("title")
    private String title;

    @JsonProperty("currentUsed")
    private Boolean currentUsed;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("originalFilename")
    private String originalFilename;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("previewUrl")
    private String previewUrl;

    @JsonProperty("draftJson")
    private String draftJson;

    @JsonProperty("status")
    private String status;

    @JsonProperty("scopeJson")
    private String scopeJson;

    @JsonProperty("requestedAt")
    private Date requestedAt;

    @JsonProperty("entryCount")
    private Integer entryCount;

    @JsonProperty("visibilityRiskStatus")
    private String visibilityRiskStatus;

    @JsonProperty("contentUrl")
    private String contentUrl;

    @JsonProperty("downloadUrl")
    private String downloadUrl;

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

    @JsonProperty("sourcePreviewUrl")
    private String sourcePreviewUrl;

    @JsonProperty("sourceDownloadUrl")
    private String sourceDownloadUrl;

    @JsonProperty("generatedPreviewUrl")
    private String generatedPreviewUrl;

    @JsonProperty("generatedDownloadUrl")
    private String generatedDownloadUrl;
}
