package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import java.util.List;

public record GraphNodePublicationPlanResult(
        GraphMaterialNode materialNode,
        String action,
        GraphPublishedNode matchedNode,
        List<GraphValidationIssueResult> issues) {}
