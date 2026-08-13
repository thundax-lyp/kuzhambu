package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import java.util.List;

public interface GraphMaterialEventRepository {
    GraphMaterialEvent getById(GraphMaterialEventId id);

    GraphMaterialEvent getByMaterialRef(ContentRef materialRef);

    List<GraphMaterialEvent> listByStatus(GraphMaterialEventStatus status);

    int insert(GraphMaterialEvent event);

    int update(GraphMaterialEvent event);
}
