package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialVersionId;
import java.util.List;

public interface GraphMaterialVersionRepository {
    List<GraphMaterialVersion> listByMaterial(ContentRef materialRef);

    GraphMaterialVersion getByMaterialAndVersionNo(ContentRef materialRef, long versionNo);

    long maxVersionNo(ContentRef materialRef);

    GraphMaterialVersionId insert(GraphMaterialVersion version);

    int deleteByMaterial(ContentRef materialRef);
}
