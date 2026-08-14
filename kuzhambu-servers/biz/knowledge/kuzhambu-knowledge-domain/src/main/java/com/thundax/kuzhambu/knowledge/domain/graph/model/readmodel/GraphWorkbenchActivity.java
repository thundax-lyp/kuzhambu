package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.time.Instant;

public record GraphWorkbenchActivity(String type, ContentRef contentRef, Instant occurredAt, String summary) {}
