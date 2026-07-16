package com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SancaiPortalCategoryResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("categoryType")
    private String categoryType;

    @JsonProperty("priority")
    private Integer priority;
}
