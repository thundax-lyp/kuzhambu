package com.thundax.kuzhambu.knowledge.application.graph.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphDocumentDto {
    private String schemaVersion;
    private List<GraphDocumentNodeDto> nodes = new ArrayList<>();
    private List<GraphDocumentEdgeDto> edges = new ArrayList<>();
}
