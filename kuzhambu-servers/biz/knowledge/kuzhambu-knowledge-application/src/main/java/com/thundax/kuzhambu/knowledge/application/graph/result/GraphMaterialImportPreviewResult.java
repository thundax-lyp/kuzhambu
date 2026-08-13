package com.thundax.kuzhambu.knowledge.application.graph.result;

import java.util.List;

public record GraphMaterialImportPreviewResult(
        GraphMaterialResult importedGraph,
        int createdNodeCount,
        int updatedNodeCount,
        int createdEdgeCount,
        int updatedEdgeCount,
        List<GraphValidationIssueResult> issues,
        boolean importable) {}
