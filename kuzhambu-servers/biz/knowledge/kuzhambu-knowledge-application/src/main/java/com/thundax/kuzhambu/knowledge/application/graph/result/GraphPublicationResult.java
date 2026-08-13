package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import java.util.List;

public record GraphPublicationResult(
        ContentRef materialRef,
        GraphMaterialStatus materialStatus,
        int createdNodeCount,
        int reusedNodeCount,
        int createdEdgeCount,
        int reusedEdgeCount,
        List<GraphValidationIssueResult> issues) {}
