package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public interface GraphPublishedNodeMaterialRepository {
    List<GraphPublishedNodeMaterial> listByMaterial(ContentRef materialRef);

    int insert(GraphPublishedNodeMaterial relation);

    int delete(GraphPublishedNodeId publishedNodeId, ContentRef materialRef);
}
