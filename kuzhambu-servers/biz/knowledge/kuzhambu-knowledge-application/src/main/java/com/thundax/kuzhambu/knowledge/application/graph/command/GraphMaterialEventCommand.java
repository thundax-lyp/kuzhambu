package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;
import java.time.Instant;

public record GraphMaterialEventCommand(ContentRef materialRef, GraphMaterialEventType eventType, Instant changedAt) {}
