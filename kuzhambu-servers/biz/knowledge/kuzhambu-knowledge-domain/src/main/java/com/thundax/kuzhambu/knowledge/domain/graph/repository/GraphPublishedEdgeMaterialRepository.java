package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import java.util.List;

public interface GraphPublishedEdgeMaterialRepository {
    List<GraphPublishedEdgeMaterial> listByMaterial(ContentRef materialRef);

    int insert(GraphPublishedEdgeMaterial relation);

    int delete(GraphPublishedEdgeId publishedEdgeId, ContentRef materialRef);
}
