package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.util.List;

public record GraphMaterialNodeMergeQuery(
        ContentRef materialRef, GraphMaterialNodeId retainedNodeId, List<GraphMaterialNodeId> mergedNodeIds) {}
