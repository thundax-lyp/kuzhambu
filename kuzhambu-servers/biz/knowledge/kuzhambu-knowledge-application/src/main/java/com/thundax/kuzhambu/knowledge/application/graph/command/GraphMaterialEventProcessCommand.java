package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;

public record GraphMaterialEventProcessCommand(GraphMaterialEventId eventId, long lockVersion) {}
