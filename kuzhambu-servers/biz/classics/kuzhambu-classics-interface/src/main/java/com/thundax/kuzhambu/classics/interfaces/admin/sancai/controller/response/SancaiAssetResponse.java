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
public class SancaiAssetResponse implements Serializable {
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

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("draftJson")
    private String draftJson;

    @JsonProperty("status")
    private String status;

    @JsonProperty("scopeJson")
    private String scopeJson;
}
