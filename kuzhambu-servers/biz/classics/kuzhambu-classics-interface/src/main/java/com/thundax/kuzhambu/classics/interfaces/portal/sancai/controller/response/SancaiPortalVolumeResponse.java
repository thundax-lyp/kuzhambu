package com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "三才图会门户卷响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SancaiPortalVolumeResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("categoryId")
    private Long categoryId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("volumeType")
    private String volumeType;
}
