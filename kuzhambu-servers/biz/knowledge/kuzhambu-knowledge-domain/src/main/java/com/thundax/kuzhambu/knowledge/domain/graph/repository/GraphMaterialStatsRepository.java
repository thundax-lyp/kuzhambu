package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;

public interface GraphMaterialStatsRepository {
    GraphMaterialStats getByMaterialId(Long materialId);

    int upsert(GraphMaterialStats stats);
}
