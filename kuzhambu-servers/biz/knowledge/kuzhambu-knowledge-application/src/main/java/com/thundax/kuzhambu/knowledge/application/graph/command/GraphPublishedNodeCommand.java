package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import java.util.List;

public record GraphPublishedNodeCommand(
        GraphPublishedNode node, List<GraphPublishedNodeProperty> properties, String reason) {}
