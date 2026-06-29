package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsSharingResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("shareToken")
    private String shareToken;

    @JsonProperty("shareUrl")
    private String shareUrl;

    @JsonProperty("title")
    private String title;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty("status")
    private String status;

    @JsonProperty("issuedAt")
    private Date issuedAt;

    @JsonProperty("expiresAt")
    private Date expiresAt;

    @JsonProperty("accessCount")
    private Long accessCount;

    @JsonProperty("targets")
    private List<Target> targets;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccessRecord implements Serializable {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("shareLinkId")
        private Long shareLinkId;

        @JsonProperty("shareTargetId")
        private Long shareTargetId;

        @JsonProperty("accessedAt")
        private Date accessedAt;

        @JsonProperty("accessResult")
        private String accessResult;

        @JsonProperty("clientSnapshot")
        private String clientSnapshot;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Target implements Serializable {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("contentType")
        private String contentType;

        @JsonProperty("contentId")
        private Long contentId;

        @JsonProperty("contentVersionId")
        private Long contentVersionId;

        @JsonProperty("contentVersionNo")
        private Integer contentVersionNo;

        @JsonProperty("currentContentVersionId")
        private Long currentContentVersionId;

        @JsonProperty("currentContentVersionNo")
        private Integer currentContentVersionNo;

        @JsonProperty("contentChangedAfterShare")
        private Boolean contentChangedAfterShare;

        @JsonProperty("titleSnapshot")
        private String titleSnapshot;

        @JsonProperty("contentVisibilitySnapshot")
        private String contentVisibilitySnapshot;

        @JsonProperty("targetStatus")
        private String targetStatus;

        @JsonProperty("priority")
        private Integer priority;
    }
}
