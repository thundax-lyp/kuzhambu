package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;

public record GraphPublishedEdgeDeleteCommand(GraphPublishedEdgeId edgeId, long edgeLockVersion, String reason) {}
