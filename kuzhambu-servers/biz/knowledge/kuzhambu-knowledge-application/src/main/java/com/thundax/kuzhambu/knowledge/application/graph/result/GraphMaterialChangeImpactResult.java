package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import java.util.List;

public record GraphMaterialChangeImpactResult(
        List<GraphMaterialNode> nodes,
        List<GraphMaterialEdge> edges,
        List<GraphValidationIssueResult> issues,
        boolean executable) {}
