package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;

public record GraphMaterialPageResult(
        GraphMaterialSourceResult source,
        GraphMaterial material,
        GraphMaterialStats materialStats,
        GraphExtractionTaskResult latestTask) {}
