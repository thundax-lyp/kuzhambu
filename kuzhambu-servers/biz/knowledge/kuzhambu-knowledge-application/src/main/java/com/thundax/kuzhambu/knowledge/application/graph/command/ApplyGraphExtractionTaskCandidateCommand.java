package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public record ApplyGraphExtractionTaskCandidateCommand(GraphExtractionTaskId taskId, String applyMode) {}
