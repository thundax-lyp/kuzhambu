package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;

public record GraphPublicationCommand(
        ContentRef materialRef,
        long materialLockVersion,
        List<GraphNodePublicationDecisionCommand> nodeDecisions,
        List<GraphEdgePublicationDecisionCommand> edgeDecisions) {}
