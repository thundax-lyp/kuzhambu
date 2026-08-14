package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import java.util.List;

public record GraphPublishedEdgeCommand(GraphPublishedEdge edge, List<GraphPublishedEdgeProperty> properties) {}
