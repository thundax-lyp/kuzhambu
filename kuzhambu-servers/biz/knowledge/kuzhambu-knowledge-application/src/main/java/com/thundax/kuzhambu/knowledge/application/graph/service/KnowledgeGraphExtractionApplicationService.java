package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeEntityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeRelationResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public interface KnowledgeGraphExtractionApplicationService {

    GraphExtractionTaskResult requestRelationExtraction(RequestRelationExtractionCommand command);

    GraphExtractionTaskResult requestGraphExtraction(RequestGraphExtractionCommand command);

    GraphExtractionTaskResult requestLineageExtraction(RequestLineageExtractionCommand command);

    PageResult<GraphExtractionTaskResult> pageTasks(
            String taskType, String status, String sourceContentType, Long sourceContentId, PageQuery pageQuery);

    GraphExtractionTaskResult getTaskDetail(GraphExtractionTaskId taskId);

    PageResult<GraphVersionResult> pageVersions(
            String taskType, String status, String sourceContentType, Long sourceContentId, PageQuery pageQuery);

    GraphVersionResult getVersionDetail(Long versionId);

    PageResult<KnowledgeEntityResult> pageEntities(
            Long versionId, String keyword, String entityType, String confirmationStatus, PageQuery pageQuery);

    KnowledgeEntityResult getEntityDetail(Long entityId);

    PageResult<KnowledgeRelationResult> pageRelations(
            Long versionId, String keyword, String relationType, String confirmationStatus, PageQuery pageQuery);

    KnowledgeRelationResult getRelationDetail(Long relationId);

    GraphExtractionTaskResult applyTaskCandidate(GraphExtractionTaskId taskId);
}
