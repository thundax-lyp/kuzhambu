package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;

public record GraphPublishedNodeQuery(
        String keyword, GraphNodeType nodeType, GraphPublishedStatus status, GraphSourceType source) {}
