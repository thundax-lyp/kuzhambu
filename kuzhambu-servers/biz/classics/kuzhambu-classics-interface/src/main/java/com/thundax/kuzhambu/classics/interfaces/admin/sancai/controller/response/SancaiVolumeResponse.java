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
public class SancaiVolumeResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("categoryId")
    private Long categoryId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("volumeType")
    private String volumeType;

    @JsonProperty("priority")
    private Integer priority;
}
