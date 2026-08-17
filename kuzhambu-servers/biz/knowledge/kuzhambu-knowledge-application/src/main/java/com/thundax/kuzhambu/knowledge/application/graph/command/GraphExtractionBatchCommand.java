package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;

public record GraphExtractionBatchCommand(List<ContentRef> materialRefs, String idempotencyKey, Long requestedBy) {}
