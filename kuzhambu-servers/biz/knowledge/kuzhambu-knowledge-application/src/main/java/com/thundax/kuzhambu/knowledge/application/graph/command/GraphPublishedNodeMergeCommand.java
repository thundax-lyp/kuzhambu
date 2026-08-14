package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public record GraphPublishedNodeMergeCommand(
        GraphPublishedNodeId retainedNodeId, List<GraphPublishedNodeId> mergedNodeIds, long retainedNodeLockVersion) {}
