package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsSharingRequest extends PageRequest {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty("status")
    private String status;

    @JsonProperty("visibilityRiskStatus")
    private String visibilityRiskStatus;

    @JsonProperty("expiresAt")
    private Instant expiresAt;

    @JsonProperty("issuedAfter")
    private Instant issuedAfter;

    @JsonProperty("issuedBefore")
    private Instant issuedBefore;

    @JsonProperty("shareLinkId")
    private Long shareLinkId;

    @JsonProperty("shareTargetId")
    private Long shareTargetId;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("targets")
    private List<ClassicsShareTargetRequest> targets;
}
