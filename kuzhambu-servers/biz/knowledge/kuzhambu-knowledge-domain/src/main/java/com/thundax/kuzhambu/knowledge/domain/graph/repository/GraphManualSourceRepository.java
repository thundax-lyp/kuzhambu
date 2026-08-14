package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphManualSource;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphManualSourceId;
import java.util.List;

public interface GraphManualSourceRepository {

    GraphManualSource getById(GraphManualSourceId id);

    List<GraphManualSource> listByTarget(String targetType, Long targetId);

    GraphManualSourceId insert(GraphManualSource source);
}
