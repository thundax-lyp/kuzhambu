package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialStatsRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class GraphMaterialStatsRefresher {
    private final GraphMaterialRepository materialRepository;
    private final GraphMaterialStatsRepository statsRepository;
    private final Clock clock;

    public GraphMaterialStatsRefresher(
            GraphMaterialRepository materialRepository, GraphMaterialStatsRepository statsRepository) {
        this(materialRepository, statsRepository, Clock.systemUTC());
    }

    GraphMaterialStatsRefresher(
            GraphMaterialRepository materialRepository, GraphMaterialStatsRepository statsRepository, Clock clock) {
        this.materialRepository = materialRepository;
        this.statsRepository = statsRepository;
        this.clock = clock;
    }

    public void refresh(ContentRef materialRef) {
        if (materialRef == null) {
            return;
        }
        GraphMaterial material = materialRepository.getByContentRef(materialRef);
        refresh(material);
    }

    public void refresh(GraphMaterial material) {
        if (material == null || material.getId() == null) {
            return;
        }
        statsRepository.refresh(material.getId(), Instant.now(clock));
    }
}
