package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import java.util.List;

public record GraphPublishedNodeDetailResult(
        GraphPublishedNode node,
        List<GraphPublishedNodeProperty> properties,
        List<GraphPublishedNodeMaterial> materials,
        List<GraphPublishedEdge> incidentEdges,
        List<GraphGovernanceOperationResult> operations) {}
