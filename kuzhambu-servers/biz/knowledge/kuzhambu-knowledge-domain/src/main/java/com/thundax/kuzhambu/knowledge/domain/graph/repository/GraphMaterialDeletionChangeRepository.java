package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;

public interface GraphMaterialDeletionChangeRepository {
    GraphMaterialDeletionChange getById(GraphMaterialDeletionChangeId id);

    GraphMaterialDeletionChange getByLatestMaterialRef(ContentRef materialRef);

    PageResult<GraphMaterialDeletionChange> page(GraphMaterialDeletionStatus status, int pageNo, int pageSize);

    GraphMaterialDeletionChangeId insert(GraphMaterialDeletionChange change);

    GraphMaterialDeletionChange updateIfLockVersion(GraphMaterialDeletionChange change, long expectedLockVersion);
}
