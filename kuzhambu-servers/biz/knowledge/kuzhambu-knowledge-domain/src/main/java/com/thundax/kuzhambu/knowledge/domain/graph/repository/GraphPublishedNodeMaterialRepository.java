package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public interface GraphPublishedNodeMaterialRepository {
    List<GraphPublishedNodeMaterial> listByPublishedNodeId(GraphPublishedNodeId publishedNodeId);

    List<GraphPublishedNodeMaterial> listByMaterial(ContentRef materialRef);

    int insert(GraphPublishedNodeMaterial relation);

    int deleteByPublishedNodeIdAndMaterialRef(GraphPublishedNodeId publishedNodeId, ContentRef materialRef);

    int deleteByMaterial(ContentRef materialRef);
}
