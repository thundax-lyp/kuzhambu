package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;

public record GraphMaterialNodeSplitQuery(ContentRef materialRef, GraphMaterialNodeId sourceNodeId) {}
