package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;

public record GraphNodePublicationDecisionCommand(
        GraphMaterialNodeId materialNodeId, String action, GraphPublishedNodeId targetNodeId, long targetLockVersion) {}
