package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphPublishedEdge {
    private GraphPublishedEdgeId id;
    private GraphEdgeKey edgeKey;
    private GraphPublishedNodeId sourceNodeId;
    private GraphPublishedNodeId targetNodeId;
    private String relationType;
    private String qualifiersJson;
    private GraphPublishedStatus status;
    private long lockVersion;
}
