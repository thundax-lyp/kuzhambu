package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialStatsRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphMaterialStatsPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialStatsMapper;
import java.time.Instant;
import java.util.List;
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
    public List<GraphMaterialStats> listByMaterialIds(List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectByMaterialIds(materialIds).stream()
                .map(GraphMaterialStatsPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public int upsert(GraphMaterialStats stats) {
        return mapper.upsert(GraphMaterialStatsPersistenceAssembler.toObject(stats));
    }

    @Override
    public int refresh(Long materialId, Instant calculatedAt) {
        if (materialId == null) {
            return 0;
        }
        return mapper.refresh(materialId, calculatedAt);
    }
}
