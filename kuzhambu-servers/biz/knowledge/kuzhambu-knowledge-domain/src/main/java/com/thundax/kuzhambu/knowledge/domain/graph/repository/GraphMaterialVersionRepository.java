package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialVersion;
import java.util.List;

public interface GraphMaterialVersionRepository {
    List<GraphMaterialVersion> listByMaterial(ContentRef materialRef);

    GraphMaterialVersion getByMaterialAndVersionNo(ContentRef materialRef, long versionNo);

    int insert(GraphMaterialVersion version);
}
