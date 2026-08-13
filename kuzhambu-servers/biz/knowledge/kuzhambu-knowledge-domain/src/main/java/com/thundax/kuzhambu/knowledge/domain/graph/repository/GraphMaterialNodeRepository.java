package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.util.List;

public interface GraphMaterialNodeRepository {
    GraphMaterialNode getById(GraphMaterialNodeId id);

    List<GraphMaterialNode> listByMaterial(ContentRef materialRef);

    GraphMaterialNodeId insert(GraphMaterialNode node);

    void batchInsert(List<GraphMaterialNode> nodes);

    void batchUpdate(List<GraphMaterialNode> nodes);

    void deleteByIds(List<GraphMaterialNodeId> ids);

    int update(GraphMaterialNode node);

    int deleteById(GraphMaterialNodeId id);

    int deleteByMaterial(ContentRef materialRef);

    void batchReplaceByMaterial(ContentRef materialRef, List<GraphMaterialNode> nodes);
}
