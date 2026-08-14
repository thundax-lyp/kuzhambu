package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgePropertyId;
import java.util.List;

public interface GraphPublishedEdgePropertyRepository {
    GraphPublishedEdgeProperty getById(GraphPublishedEdgePropertyId id);

    List<GraphPublishedEdgeProperty> listByPublishedEdgeId(GraphPublishedEdgeId publishedEdgeId);

    GraphPublishedEdgePropertyId insert(GraphPublishedEdgeProperty property);

    void batchInsert(List<GraphPublishedEdgeProperty> properties);

    int update(GraphPublishedEdgeProperty property);

    int deleteById(GraphPublishedEdgePropertyId id);

    int deleteByPublishedEdgeId(GraphPublishedEdgeId publishedEdgeId);

    int deleteByPublishedEdgeIds(List<GraphPublishedEdgeId> publishedEdgeIds);
}
