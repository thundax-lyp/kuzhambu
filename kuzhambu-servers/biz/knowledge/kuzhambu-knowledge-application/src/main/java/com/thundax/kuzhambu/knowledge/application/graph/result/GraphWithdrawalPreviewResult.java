package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import java.util.List;

public record GraphWithdrawalPreviewResult(
        ContentRef materialRef,
        int nodeMappingCount,
        int edgeMappingCount,
        List<GraphPublishedNode> governedNodes,
        List<GraphPublishedEdge> governedEdges) {}
