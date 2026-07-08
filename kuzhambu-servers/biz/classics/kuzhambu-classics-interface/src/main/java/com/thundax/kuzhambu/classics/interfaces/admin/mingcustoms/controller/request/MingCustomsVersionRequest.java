package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MingCustomsVersionRequest {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("versionId")
    private Long versionId;
}
