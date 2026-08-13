package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;

public record GraphEdgePublicationDecisionCommand(
        GraphMaterialEdgeId materialEdgeId, String action, GraphPublishedEdgeId targetEdgeId, long targetLockVersion) {}
