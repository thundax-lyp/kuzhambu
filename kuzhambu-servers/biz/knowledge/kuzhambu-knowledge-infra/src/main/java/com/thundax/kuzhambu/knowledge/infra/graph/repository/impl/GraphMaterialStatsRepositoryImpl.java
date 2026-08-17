package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialStatsRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphMaterialStatsPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialStatsMapper;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialStatsRepositoryImpl implements GraphMaterialStatsRepository {
    private final GraphMaterialStatsMapper mapper;

    public GraphMaterialStatsRepositoryImpl(GraphMaterialStatsMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphMaterialStats getByMaterialId(Long materialId) {
        return GraphMaterialStatsPersistenceAssembler.toDomain(mapper.selectById(materialId));
    }

    @Override
    public int upsert(GraphMaterialStats stats) {
        return mapper.upsert(GraphMaterialStatsPersistenceAssembler.toObject(stats));
    }
}
