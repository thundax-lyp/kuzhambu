package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId;
import java.util.List;

public interface GraphMaterialDeletionTaskRepository {
    GraphMaterialDeletionTask getById(GraphMaterialDeletionTaskId id);

    GraphMaterialDeletionTask getByIdempotencyKey(String idempotencyKey);

    PageResult<GraphMaterialDeletionTask> page(GraphMaterialDeletionStatus status, int pageNo, int pageSize);

    List<GraphMaterialDeletionTask> listByStatus(GraphMaterialDeletionStatus status, int limit);

    GraphMaterialDeletionTaskId insert(GraphMaterialDeletionTask task);

    GraphMaterialDeletionTask updateIfLockVersion(GraphMaterialDeletionTask task, long expectedLockVersion);
}
