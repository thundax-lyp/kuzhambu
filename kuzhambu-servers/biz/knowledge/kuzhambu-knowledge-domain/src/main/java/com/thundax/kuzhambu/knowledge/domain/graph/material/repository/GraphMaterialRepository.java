package com.thundax.kuzhambu.knowledge.domain.graph.material.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.material.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.material.model.valueobject.GraphMaterialId;

public interface GraphMaterialRepository {

    GraphMaterial getById(GraphMaterialId id);

    GraphMaterial getByContentRef(ContentRef contentRef);

    GraphMaterialId insert(GraphMaterial material);

    int update(GraphMaterial material);
}
