package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import java.util.List;

public interface GraphMaterialEdgeRepository {
    List<GraphMaterialEdge> listByMaterial(ContentRef materialRef);

    void batchReplaceByMaterial(ContentRef materialRef, List<GraphMaterialEdge> edges);
}
