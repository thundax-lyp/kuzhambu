package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;

public record GraphExtractionCommand(ContentRef materialRef, String idempotencyKey, Long requestedBy) {}
