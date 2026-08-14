package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId;
import java.util.List;

public interface GraphPublishedNodePropertyRepository {
    GraphPublishedNodeProperty getById(GraphPublishedNodePropertyId id);

    List<GraphPublishedNodeProperty> listByPublishedNodeId(GraphPublishedNodeId publishedNodeId);

    GraphPublishedNodePropertyId insert(GraphPublishedNodeProperty property);

    void batchInsert(List<GraphPublishedNodeProperty> properties);

    int update(GraphPublishedNodeProperty property);

    int deleteById(GraphPublishedNodePropertyId id);

    int deleteByPublishedNodeId(GraphPublishedNodeId publishedNodeId);

    int deleteByPublishedNodeIds(List<GraphPublishedNodeId> publishedNodeIds);
}
