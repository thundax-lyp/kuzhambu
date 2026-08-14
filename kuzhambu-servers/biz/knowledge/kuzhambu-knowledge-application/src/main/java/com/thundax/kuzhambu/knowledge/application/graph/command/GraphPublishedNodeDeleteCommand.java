package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;

public record GraphPublishedNodeDeleteCommand(
        GraphPublishedNodeId nodeId, boolean cascadeEdges, long nodeLockVersion, String reason) {}
