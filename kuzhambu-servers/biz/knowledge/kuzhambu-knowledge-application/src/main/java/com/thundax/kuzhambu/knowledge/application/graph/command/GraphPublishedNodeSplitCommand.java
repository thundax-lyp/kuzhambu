package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId;
import java.util.List;

public record GraphPublishedNodeSplitCommand(
        GraphPublishedNodeId sourceNodeId,
        GraphPublishedNode splitNode,
        List<GraphPublishedNodePropertyId> movedPropertyIds,
        List<GraphPublishedNodePropertyId> copiedPropertyIds,
        List<GraphPublishedEdgeId> reassignedEdgeIds,
        List<GraphPublishedEdge> copiedEdges,
        List<ContentRef> movedMaterialRefs,
        List<ContentRef> copiedMaterialRefs,
        long sourceNodeLockVersion,
        String reason) {}
