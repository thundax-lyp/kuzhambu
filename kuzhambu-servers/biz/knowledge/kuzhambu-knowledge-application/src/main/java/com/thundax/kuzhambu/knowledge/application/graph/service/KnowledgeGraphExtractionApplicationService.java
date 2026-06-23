package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public interface KnowledgeGraphExtractionApplicationService {

    GraphExtractionTaskResult requestRelationExtraction(RequestRelationExtractionCommand command);

    GraphExtractionTaskResult requestGraphExtraction(RequestGraphExtractionCommand command);

    GraphExtractionTaskResult requestLineageExtraction(RequestLineageExtractionCommand command);

    PageResult<GraphExtractionTaskResult> pageTasks(
            String taskType, String status, String sourceContentType, Long sourceContentId, PageQuery pageQuery);

    GraphExtractionTaskResult getTaskDetail(GraphExtractionTaskId taskId);

    GraphExtractionTaskResult applyTaskCandidate(GraphExtractionTaskId taskId);
}
