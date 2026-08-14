package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId;

public record GraphMaterialDeletionTaskRetryCommand(GraphMaterialDeletionTaskId taskId, long lockVersion) {}
