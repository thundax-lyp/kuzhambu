package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionDecisionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionPrecheckCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialDeletionChangeQuery;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionTask;

public interface GraphMaterialDeletionApplicationService {

    GraphMaterialDeletionChange precheck(GraphMaterialDeletionPrecheckCommand command);

    GraphMaterialDeletionTask decide(GraphMaterialDeletionDecisionCommand command);

    PageResult<GraphMaterialDeletionChange> pageChanges(GraphMaterialDeletionChangeQuery query, PageQuery pageQuery);
}
