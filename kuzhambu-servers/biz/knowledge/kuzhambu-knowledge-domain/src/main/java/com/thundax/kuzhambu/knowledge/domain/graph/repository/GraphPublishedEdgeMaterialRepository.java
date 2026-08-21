package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import java.util.List;

public interface GraphPublishedEdgeMaterialRepository {
    List<GraphPublishedEdgeMaterial> listByPublishedEdgeId(GraphPublishedEdgeId publishedEdgeId);

    List<GraphPublishedEdgeMaterial> listByPublishedEdgeIds(List<GraphPublishedEdgeId> publishedEdgeIds);

    List<GraphPublishedEdgeMaterial> listByMaterial(ContentRef materialRef);

    List<GraphPublishedEdgeMaterial> listByMaterials(List<ContentRef> materialRefs);

    int insert(GraphPublishedEdgeMaterial relation);

    void batchInsert(List<GraphPublishedEdgeMaterial> relations);

    int deleteByPublishedEdgeId(GraphPublishedEdgeId publishedEdgeId);

    int deleteByPublishedEdgeIds(List<GraphPublishedEdgeId> publishedEdgeIds);

    int deleteByPublishedEdgeIdAndMaterialRef(GraphPublishedEdgeId publishedEdgeId, ContentRef materialRef);

    int deleteByMaterial(ContentRef materialRef);

    long countDistinctMaterials();
}
