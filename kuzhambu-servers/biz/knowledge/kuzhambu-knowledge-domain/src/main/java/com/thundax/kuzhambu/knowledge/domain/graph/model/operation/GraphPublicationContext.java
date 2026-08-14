package com.thundax.kuzhambu.knowledge.domain.graph.model.operation;

import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record GraphPublicationContext(
        GraphMaterialGraph materialGraph,
        Map<GraphMaterialNodeId, GraphPublishedNode> matchedNodesByMaterialNodeId,
        Map<GraphMaterialEdgeId, GraphPublishedEdge> matchedEdgesByMaterialEdgeId,
        Map<GraphPublishedNodeId, List<GraphPublishedNodeProperty>> existingNodeProperties,
        Map<GraphPublishedEdgeId, List<GraphPublishedEdgeProperty>> existingEdgeProperties,
        Instant modifiedAt) {}
