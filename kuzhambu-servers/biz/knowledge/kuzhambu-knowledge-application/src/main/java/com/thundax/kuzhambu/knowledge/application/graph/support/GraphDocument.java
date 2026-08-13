package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphDocument {
    private String schemaVersion;
    private List<GraphDocumentNode> nodes = new ArrayList<>();
    private List<GraphDocumentEdge> edges = new ArrayList<>();
}
