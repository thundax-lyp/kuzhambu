package com.thundax.kuzhambu.knowledge.application.graph.result;

public record GraphWorkbenchOverviewResult(
        long publishedNodeCount,
        long publishedEdgeCount,
        long coveredMaterialCount,
        long isolatedNodeCount,
        long missingCoreRelationNodeCount) {}
