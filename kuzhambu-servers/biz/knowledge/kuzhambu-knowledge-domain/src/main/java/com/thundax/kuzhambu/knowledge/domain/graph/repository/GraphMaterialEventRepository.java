package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import java.time.Instant;
import java.util.List;

public interface GraphMaterialEventRepository {
    GraphMaterialEvent getById(GraphMaterialEventId id);

    GraphMaterialEvent getByMaterialRefAndType(ContentRef materialRef, GraphMaterialEventType type);

    PageResult<GraphMaterialEvent> page(
            ContentRef materialRef,
            GraphMaterialEventType type,
            GraphMaterialEventStatus status,
            int pageNo,
            int pageSize);

    List<GraphMaterialEvent> listByStatus(GraphMaterialEventStatus status, int limit);

    List<GraphMaterialEvent> listProcessingBefore(Instant changedBefore, int limit);

    GraphMaterialEventId insert(GraphMaterialEvent event);

    int updateIfLockVersion(GraphMaterialEvent event, long expectedLockVersion);
}
