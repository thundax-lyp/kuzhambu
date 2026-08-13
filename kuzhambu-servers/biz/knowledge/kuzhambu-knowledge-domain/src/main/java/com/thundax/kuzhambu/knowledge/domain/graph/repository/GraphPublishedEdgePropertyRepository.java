package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import java.util.List;

public interface GraphPublishedEdgePropertyRepository {
    List<GraphPublishedEdgeProperty> listByPublishedEdgeId(GraphPublishedEdgeId publishedEdgeId);

    int insert(GraphPublishedEdgeProperty property);

    int update(GraphPublishedEdgeProperty property);
}
