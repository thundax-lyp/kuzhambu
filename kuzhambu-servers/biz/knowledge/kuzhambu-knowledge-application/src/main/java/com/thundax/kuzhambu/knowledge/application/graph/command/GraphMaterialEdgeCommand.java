package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;

public record GraphMaterialEdgeCommand(GraphMaterialEdge edge, long materialLockVersion) {}
