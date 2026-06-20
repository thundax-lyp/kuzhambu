package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response;

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
public class ClassicsSharePortalListItemResponse implements Serializable {
    @JsonProperty("shareLinkId")
    private Long shareLinkId;

    @JsonProperty("shareToken")
    private String shareToken;

    @JsonProperty("shareTitle")
    private String shareTitle;

    @JsonProperty("issuedAt")
    private Date issuedAt;

    @JsonProperty("expiresAt")
    private Date expiresAt;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("contentId")
    private Long contentId;

    @JsonProperty("contentVersionId")
    private Long contentVersionId;

    @JsonProperty("contentVersionNo")
    private Integer contentVersionNo;

    @JsonProperty("titleSnapshot")
    private String titleSnapshot;

    @JsonProperty("contentVisibilitySnapshot")
    private String contentVisibilitySnapshot;

    @JsonProperty("targetStatus")
    private String targetStatus;

    @JsonProperty("priority")
    private Integer priority;
}
