package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "三才管理响应")
public class SancaiVolumeResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("categoryId")
    private Long categoryId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("volumeType")
    private String volumeType;
}
