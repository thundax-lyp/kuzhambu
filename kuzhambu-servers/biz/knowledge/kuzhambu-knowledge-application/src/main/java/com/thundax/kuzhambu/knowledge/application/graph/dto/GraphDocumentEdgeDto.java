package com.thundax.kuzhambu.knowledge.application.graph.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphDocumentEdgeDto {
    private String id;
    private String sourceId;
    private String targetId;
    private String relationType;
    private JsonNode qualifiers;
}
