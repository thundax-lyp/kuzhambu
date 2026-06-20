package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response;

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
public class ClassicsSharePortalResponse implements Serializable {
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

    @JsonProperty("targets")
    private List<ClassicsSharePortalTargetResponse> targets;
}
