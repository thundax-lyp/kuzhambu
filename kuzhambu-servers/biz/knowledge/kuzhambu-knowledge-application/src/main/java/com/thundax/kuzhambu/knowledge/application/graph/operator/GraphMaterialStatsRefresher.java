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
    private Clock clock = Clock.systemUTC();

    public GraphMaterialStatsRefresher(
            GraphMaterialRepository materialRepository, GraphMaterialStatsRepository statsRepository) {
        this.materialRepository = materialRepository;
        this.statsRepository = statsRepository;
    }

    GraphMaterialStatsRefresher useClock(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
        return this;
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
        statsRepository.updateCalculatedAt(material.getId(), Instant.now(clock));
    }
}
