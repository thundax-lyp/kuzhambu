package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public interface GraphPublishedNodeMaterialRepository {
    List<GraphPublishedNodeMaterial> listByPublishedNodeId(GraphPublishedNodeId publishedNodeId);

    List<GraphPublishedNodeMaterial> listByPublishedNodeIds(List<GraphPublishedNodeId> publishedNodeIds);

    List<GraphPublishedNodeMaterial> listByMaterial(ContentRef materialRef);

    List<GraphPublishedNodeMaterial> listByMaterials(List<ContentRef> materialRefs);

    int insert(GraphPublishedNodeMaterial relation);

    void batchInsert(List<GraphPublishedNodeMaterial> relations);

    int deleteByPublishedNodeId(GraphPublishedNodeId publishedNodeId);

    int deleteByPublishedNodeIds(List<GraphPublishedNodeId> publishedNodeIds);

    int deleteByPublishedNodeIdAndMaterialRef(GraphPublishedNodeId publishedNodeId, ContentRef materialRef);

    int deleteByMaterial(ContentRef materialRef);

    long countDistinctMaterials();
}
