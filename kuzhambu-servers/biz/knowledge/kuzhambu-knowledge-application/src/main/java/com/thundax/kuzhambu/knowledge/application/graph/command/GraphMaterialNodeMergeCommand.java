package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.util.List;

public record GraphMaterialNodeMergeCommand(
        ContentRef materialRef,
        GraphMaterialNodeId retainedNodeId,
        List<GraphMaterialNodeId> mergedNodeIds,
        long materialLockVersion) {}
