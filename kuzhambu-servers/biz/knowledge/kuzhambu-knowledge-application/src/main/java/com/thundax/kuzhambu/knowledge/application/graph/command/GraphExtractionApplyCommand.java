package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;

public record GraphExtractionApplyCommand(ContentRef materialRef, Long candidateId, long materialLockVersion) {}
