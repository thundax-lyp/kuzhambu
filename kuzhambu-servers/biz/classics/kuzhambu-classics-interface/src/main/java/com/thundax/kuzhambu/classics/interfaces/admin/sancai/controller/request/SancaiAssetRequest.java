package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiAssetRequest {
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
    private boolean currentUsed;

    @JsonProperty("draftJson")
    private String draftJson;

    @JsonProperty("scopeJson")
    private String scopeJson;

    @JsonProperty("status")
    private String status;

    @JsonProperty("entryCount")
    private int entryCount;

    @JsonProperty("visibilityRiskStatus")
    private String visibilityRiskStatus;
}
