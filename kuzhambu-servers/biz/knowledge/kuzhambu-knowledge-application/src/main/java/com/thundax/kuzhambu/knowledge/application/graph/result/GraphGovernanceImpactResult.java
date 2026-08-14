package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import java.util.List;

public record GraphGovernanceImpactResult(
        String impactToken,
        List<GraphPublishedNode> nodes,
        List<GraphPublishedEdge> edges,
        List<GraphPublishedNodeMaterial> nodeMaterials,
        List<GraphPublishedEdgeMaterial> edgeMaterials,
        List<GraphValidationIssueResult> issues,
        boolean executable) {}
