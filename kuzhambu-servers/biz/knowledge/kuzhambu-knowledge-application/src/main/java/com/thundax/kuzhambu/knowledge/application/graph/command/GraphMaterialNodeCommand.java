package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;

public record GraphMaterialNodeCommand(GraphMaterialNode node, long materialLockVersion) {}
