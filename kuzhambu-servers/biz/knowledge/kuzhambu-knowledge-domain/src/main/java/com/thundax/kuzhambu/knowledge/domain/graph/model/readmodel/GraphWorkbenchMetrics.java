package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

public record GraphWorkbenchMetrics(
        long publishedNodeCount,
        long publishedEdgeCount,
        long coveredMaterialCount,
        long isolatedNodeCount,
        long missingCoreRelationNodeCount) {}
