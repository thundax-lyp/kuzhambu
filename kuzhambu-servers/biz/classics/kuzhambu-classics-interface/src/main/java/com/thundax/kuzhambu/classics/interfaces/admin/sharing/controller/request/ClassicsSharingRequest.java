package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import java.time.LocalDateTime;
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

    @JsonProperty("tokenHash")
    private String tokenHash;

    @JsonProperty("title")
    private String title;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty("status")
    private String status;

    @JsonProperty("visibilityRiskStatus")
    private String visibilityRiskStatus;

    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    @JsonProperty("targets")
    private List<ClassicsShareTarget> targets;
}
