package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsBatchShareCreateRequest {
    @JsonProperty("titlePrefix")
    private String titlePrefix;

    @JsonProperty("visibility")
    @NotBlank(message = "visibility不能为空")
    private String visibility;

    @JsonProperty("status")
    private String status;

    @JsonProperty("visibilityRiskStatus")
    private String visibilityRiskStatus;

    @JsonProperty("expiresAt")
    private Instant expiresAt;

    @JsonProperty("privateContentConfirmed")
    private boolean privateContentConfirmed;

    @JsonProperty("targets")
    @Valid
    @NotEmpty(message = "targets不能为空")
    private List<ClassicsShareTargetRequest> targets;
}
