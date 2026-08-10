package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.ApplyGraphExtractionTaskCandidateCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.CancelGraphExtractionBatchCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RegenerateGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphExtractionTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphVersionQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeEntityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.KnowledgeRelationQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchCancelResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeEntityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeRelationResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphVersionId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.KnowledgeEntityId;

public interface KnowledgeGraphExtractionApplicationService {

    GraphExtractionTaskResult requestRelationExtraction(RequestRelationExtractionCommand command);

    GraphExtractionTaskResult requestGraphExtraction(RequestGraphExtractionCommand command);

    GraphExtractionTaskResult requestLineageExtraction(RequestLineageExtractionCommand command);

    GraphExtractionTaskResult regenerateTask(RegenerateGraphExtractionCommand command);

    PageResult<GraphExtractionTaskResult> pageTasks(GraphExtractionTaskQuery query, PageQuery pageQuery);

    GraphExtractionTaskResult getTaskDetail(GraphExtractionTaskId taskId);

    GraphExtractionBatchCancelResult cancelBatch(CancelGraphExtractionBatchCommand command);

    PageResult<GraphVersionResult> pageVersions(GraphVersionQuery query, PageQuery pageQuery);

    GraphVersionResult getVersionDetail(GraphVersionId versionId);

    PageResult<KnowledgeEntityResult> pageEntities(KnowledgeEntityQuery query, PageQuery pageQuery);

    KnowledgeEntityResult getEntityDetail(KnowledgeEntityId entityId);

    PageResult<KnowledgeRelationResult> pageRelations(KnowledgeRelationQuery query, PageQuery pageQuery);

    KnowledgeRelationResult getRelationDetail(KnowledgeRelationQuery query);

    PageResult<KnowledgeLineageNodeResult> pageLineageNodes(
            Long versionId, String keyword, String nodeType, String confirmationStatus, PageQuery pageQuery);

    KnowledgeLineageNodeResult getLineageNodeDetail(Long nodeId);

    PageResult<KnowledgeLineageRelationResult> pageLineageRelations(
            Long versionId, String keyword, String relationType, String confirmationStatus, PageQuery pageQuery);

    KnowledgeLineageRelationResult getLineageRelationDetail(Long relationId);

    default GraphExtractionTaskResult applyTaskCandidate(GraphExtractionTaskId taskId) {
        return applyTaskCandidate(new ApplyGraphExtractionTaskCandidateCommand(taskId, null));
    }

    GraphExtractionTaskResult applyTaskCandidate(ApplyGraphExtractionTaskCandidateCommand command);
}
