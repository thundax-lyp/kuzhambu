package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import java.util.List;

public record GraphEdgePublicationPlanResult(
        GraphMaterialEdge materialEdge,
        String action,
        GraphPublishedEdge matchedEdge,
        List<GraphValidationIssueResult> issues) {}
