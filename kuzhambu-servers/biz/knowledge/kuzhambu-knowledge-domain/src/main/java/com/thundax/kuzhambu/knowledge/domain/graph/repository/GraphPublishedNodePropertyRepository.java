package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId;
import java.util.List;

public interface GraphPublishedNodePropertyRepository {
    GraphPublishedNodeProperty getById(GraphPublishedNodePropertyId id);

    List<GraphPublishedNodeProperty> listByPublishedNodeId(GraphPublishedNodeId publishedNodeId);

    int insert(GraphPublishedNodeProperty property);

    int update(GraphPublishedNodeProperty property);

    int deleteById(GraphPublishedNodePropertyId id);
}
