package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;

public record GraphMaterialEventQuery(
        ContentRef materialRef, GraphMaterialEventType eventType, GraphMaterialEventStatus status) {}
