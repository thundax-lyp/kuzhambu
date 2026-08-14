package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;

public record GraphMaterialNodeDeleteCommand(
        ContentRef materialRef, GraphMaterialNodeId nodeId, long materialLockVersion) {}
