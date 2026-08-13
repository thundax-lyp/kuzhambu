package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;

public interface GraphMaterialRepository {
    GraphMaterial getByContentRef(ContentRef contentRef);

    int insert(GraphMaterial material);

    int update(GraphMaterial material);
}
