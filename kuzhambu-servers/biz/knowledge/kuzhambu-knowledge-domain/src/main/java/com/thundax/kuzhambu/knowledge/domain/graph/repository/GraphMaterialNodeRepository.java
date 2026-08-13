package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import java.util.List;

public interface GraphMaterialNodeRepository {
    List<GraphMaterialNode> listByMaterial(ContentRef materialRef);

    void batchReplaceByMaterial(ContentRef materialRef, List<GraphMaterialNode> nodes);
}
