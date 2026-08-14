package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.util.List;

public record GraphMaterialNodeSplitCommand(
        ContentRef materialRef,
        GraphMaterialNodeId sourceNodeId,
        GraphMaterialNode splitNode,
        List<GraphMaterialEdgeId> reassignedEdgeIds,
        long materialLockVersion) {}
