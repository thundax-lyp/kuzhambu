package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import java.time.Instant;
import java.util.List;

public interface GraphMaterialStatsRepository {
    GraphMaterialStats getByMaterialId(Long materialId);

    List<GraphMaterialStats> listByMaterialIds(List<Long> materialIds);

    int update(GraphMaterialStats stats);

    int updateCalculatedAt(Long materialId, Instant calculatedAt);
}
