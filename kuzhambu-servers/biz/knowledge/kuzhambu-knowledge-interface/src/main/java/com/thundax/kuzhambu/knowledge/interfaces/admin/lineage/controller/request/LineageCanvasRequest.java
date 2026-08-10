package com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema
public class LineageCanvasRequest {

    @JsonProperty("versionId")
    private Long versionId;

    @JsonProperty("focusNodeId")
    private Long focusNodeId;

    @JsonProperty("focusRelationId")
    private Long focusRelationId;

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("nodeType")
    private String nodeType;

    @JsonProperty("relationType")
    private String relationType;

    @JsonProperty("confirmationStatus")
    private String confirmationStatus;

    @JsonProperty("depth")
    private Integer depth;
}
