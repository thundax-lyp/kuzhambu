package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;

public record GraphPublishedEdgeQuery(
        String keyword, String relationType, GraphPublishedStatus status, GraphSourceType source) {}
