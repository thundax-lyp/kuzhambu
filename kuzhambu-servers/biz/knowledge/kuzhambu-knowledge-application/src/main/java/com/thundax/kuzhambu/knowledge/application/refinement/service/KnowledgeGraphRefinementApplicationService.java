package com.thundax.kuzhambu.knowledge.application.refinement.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityAnnotationQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementDetailQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementWorkbenchQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementApplyResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementEntityResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementRelationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementWorkbenchItemResult;

public interface KnowledgeGraphRefinementApplicationService {

    PageResult<RefinementWorkbenchItemResult> pageTasks(RefinementWorkbenchQuery query, PageQuery pageQuery);

    RefinementDetailResult openTask(Long graphVersionId, Long openedBy);

    RefinementDetailResult getTaskDetail(RefinementDetailQuery query);

    RefinementEntityResult upsertEntity(UpsertRefinementEntityCommand command);

    RefinementEntityResult confirmEntity(ConfirmRefinementEntityCommand command);

    void deleteEntity(DeleteRefinementEntityCommand command);

    RefinementRelationResult upsertRelation(UpsertRefinementRelationCommand command);

    RefinementRelationResult confirmRelation(ConfirmRefinementRelationCommand command);

    void deleteRelation(DeleteRefinementRelationCommand command);

    RefinementLineageNodeResult upsertLineageNode(UpsertRefinementLineageNodeCommand command);

    RefinementLineageNodeResult confirmLineageNode(ConfirmRefinementLineageNodeCommand command);

    void deleteLineageNode(DeleteRefinementLineageNodeCommand command);

    RefinementLineageRelationResult upsertLineageRelation(UpsertRefinementLineageRelationCommand command);

    RefinementLineageRelationResult confirmLineageRelation(ConfirmRefinementLineageRelationCommand command);

    void deleteLineageRelation(DeleteRefinementLineageRelationCommand command);

    QualityAnnotationResult upsertAnnotation(UpsertQualityAnnotationCommand command);

    PageResult<QualityAnnotationResult> pageAnnotations(QualityAnnotationQuery query, PageQuery pageQuery);

    void deleteAnnotation(DeleteQualityAnnotationCommand command);

    RefinementApplyResult applyTask(Long refinementTaskId, Long appliedBy);

    QualitySummaryResult qualitySummary(Long refinementTaskId);
}
