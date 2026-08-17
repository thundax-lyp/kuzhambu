package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import java.util.List;

public interface GraphMaterialStatsRepository {
    GraphMaterialStats getByMaterialId(Long materialId);

    List<GraphMaterialStats> listByMaterialIds(List<Long> materialIds);

    int upsert(GraphMaterialStats stats);
}
