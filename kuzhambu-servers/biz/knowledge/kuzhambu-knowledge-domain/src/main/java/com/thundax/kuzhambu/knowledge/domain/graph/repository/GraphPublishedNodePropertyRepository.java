package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public interface GraphPublishedNodePropertyRepository {
    List<GraphPublishedNodeProperty> listByPublishedNodeId(GraphPublishedNodeId publishedNodeId);

    int insert(GraphPublishedNodeProperty property);

    int update(GraphPublishedNodeProperty property);
}
