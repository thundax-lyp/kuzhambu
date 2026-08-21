package com.thundax.kuzhambu.knowledge.application.graph.result;

public record GraphPortalOverviewResult(
        long publishedNodeCount, long publishedEdgeCount, long coveredMaterialCount, long isolatedNodeCount) {}
