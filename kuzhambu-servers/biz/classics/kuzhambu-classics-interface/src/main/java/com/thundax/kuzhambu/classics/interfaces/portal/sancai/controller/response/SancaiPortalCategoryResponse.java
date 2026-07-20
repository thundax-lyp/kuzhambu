package com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "三才图会门户门类响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SancaiPortalCategoryResponse implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("categoryType")
    private String categoryType;

    @JsonProperty("publicEntryCount")
    private Long publicEntryCount;

    @JsonProperty("illustratedEntryCount")
    private Long illustratedEntryCount;

    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    @JsonProperty("thumbnailTitle")
    private String thumbnailTitle;
}
