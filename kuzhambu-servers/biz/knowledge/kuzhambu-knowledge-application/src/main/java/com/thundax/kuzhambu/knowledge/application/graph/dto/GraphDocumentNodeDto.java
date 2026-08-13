package com.thundax.kuzhambu.knowledge.application.graph.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphDocumentNodeDto {
    private String id;
    private String nodeType;
    private String name;
    private JsonNode aliases;
    private String description;
    private String identityQualifier;
    private JsonNode period;
    private JsonNode categoryCodes;
    private JsonNode imageRefs;
    private JsonNode properties;
}
