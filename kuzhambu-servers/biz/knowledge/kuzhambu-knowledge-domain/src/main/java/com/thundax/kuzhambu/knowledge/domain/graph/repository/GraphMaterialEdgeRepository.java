package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import java.util.List;

public interface GraphMaterialEdgeRepository {
    GraphMaterialEdge getById(GraphMaterialEdgeId id);

    List<GraphMaterialEdge> listByMaterial(ContentRef materialRef);

    GraphMaterialEdgeId insert(GraphMaterialEdge edge);

    void batchInsert(List<GraphMaterialEdge> edges);

    void batchUpdate(List<GraphMaterialEdge> edges);

    void deleteByIds(List<GraphMaterialEdgeId> ids);

    int update(GraphMaterialEdge edge);

    int deleteById(GraphMaterialEdgeId id);

    int deleteByMaterial(ContentRef materialRef);

    void batchReplaceByMaterial(ContentRef materialRef, List<GraphMaterialEdge> edges);
}
