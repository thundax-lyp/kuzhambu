package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;

public record GraphMaterialEdgeDeleteCommand(
        ContentRef materialRef, GraphMaterialEdgeId edgeId, long materialLockVersion) {}
