package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.util.List;

public interface GraphMaterialNodeRepository {
    GraphMaterialNode getById(GraphMaterialNodeId id);

    List<GraphMaterialNode> listByMaterial(ContentRef materialRef);

    int insert(GraphMaterialNode node);

    int update(GraphMaterialNode node);

    int deleteById(GraphMaterialNodeId id);

    void batchReplaceByMaterial(ContentRef materialRef, List<GraphMaterialNode> nodes);
}
