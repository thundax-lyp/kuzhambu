package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import java.util.List;

public record GraphMaterialResult(
        GraphMaterial material, List<GraphMaterialNode> nodes, List<GraphMaterialEdge> edges) {}
