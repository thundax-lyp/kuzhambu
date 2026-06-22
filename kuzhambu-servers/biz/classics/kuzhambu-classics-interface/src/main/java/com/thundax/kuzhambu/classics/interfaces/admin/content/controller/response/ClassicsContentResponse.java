package com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response;

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
public class ClassicsContentResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("contentId")
    private Long contentId;

    @JsonProperty("tagNameSnapshot")
    private String tagNameSnapshot;

    @JsonProperty("question")
    private String question;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("status")
    private String status;

    @JsonProperty("exportKind")
    private String exportKind;

    @JsonProperty("exportFormat")
    private String exportFormat;

    @JsonProperty("scopeType")
    private String scopeType;

    @JsonProperty("scopeJson")
    private String scopeJson;

    @JsonProperty("requestedAt")
    private Date requestedAt;

    @JsonProperty("expiresAt")
    private Date expiresAt;

    @JsonProperty("storageObjectId")
    private Long storageObjectId;

    @JsonProperty("itemCount")
    private Integer itemCount;

    @JsonProperty("assetCount")
    private Integer assetCount;

    @JsonProperty("visibilityRiskStatus")
    private String visibilityRiskStatus;

    @JsonProperty("contentChanged")
    private Boolean contentChanged;

    @JsonProperty("contentUrl")
    private String contentUrl;

    @JsonProperty("downloadUrl")
    private String downloadUrl;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiCandidateApplyResponse {
        @JsonProperty("contentType")
        private String contentType;

        @JsonProperty("contentId")
        private Long contentId;

        @JsonProperty("versionId")
        private Long versionId;

        @JsonProperty("versionNo")
        private Integer versionNo;
    }
}
