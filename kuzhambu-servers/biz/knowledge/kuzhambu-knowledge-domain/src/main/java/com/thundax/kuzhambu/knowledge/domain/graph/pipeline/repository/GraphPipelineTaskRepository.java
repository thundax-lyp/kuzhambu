package com.thundax.kuzhambu.knowledge.domain.graph.pipeline.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.material.model.valueobject.GraphMaterialId;
import com.thundax.kuzhambu.knowledge.domain.graph.pipeline.model.entity.GraphPipelineTask;
import com.thundax.kuzhambu.knowledge.domain.graph.pipeline.model.valueobject.GraphPipelineTaskId;

public interface GraphPipelineTaskRepository {

    GraphPipelineTask getById(GraphPipelineTaskId id);

    GraphPipelineTask getRunningByMaterialId(GraphMaterialId materialId);

    GraphPipelineTaskId insert(GraphPipelineTask task);

    int update(GraphPipelineTask task);
}
