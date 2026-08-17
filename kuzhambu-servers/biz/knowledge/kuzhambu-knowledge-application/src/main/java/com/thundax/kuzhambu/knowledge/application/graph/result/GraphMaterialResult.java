package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import java.util.List;

public record GraphMaterialResult(
        GraphMaterialSourceResult source,
        GraphMaterial material,
        GraphMaterialStats materialStats,
        List<GraphMaterialNode> nodes,
        List<GraphMaterialEdge> edges,
        GraphExtractionTaskResult taskSummary) {}
