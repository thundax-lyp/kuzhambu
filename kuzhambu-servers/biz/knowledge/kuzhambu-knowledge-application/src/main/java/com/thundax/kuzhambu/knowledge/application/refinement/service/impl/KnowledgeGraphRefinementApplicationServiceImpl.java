package com.thundax.kuzhambu.knowledge.application.refinement.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.page.PageRules;
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
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityAnnotationPageQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementDetailQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementWorkbenchPageQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementEntityOptionResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementEntityResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementProgressSummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementRelationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementWorkbenchItemResult;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeGraphRefinementApplicationService;
import com.thundax.kuzhambu.knowledge.application.refinement.support.KnowledgeRefinementManualKeySupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.QualitySummaryAggregationSupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.RefinementApplySupport;
import com.thundax.kuzhambu.knowledge.application.refinement.support.RefinementDraftBootstrapSupport;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.valueobject.RefinementTaskId;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityAnnotationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementEntityDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageNodeDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@BizExceptionBoundary
public class KnowledgeGraphRefinementApplicationServiceImpl implements KnowledgeGraphRefinementApplicationService {

    private final GraphVersionRepository graphVersionRepository;
    private final RefinementTaskRepository refinementTaskRepository;
    private final RefinementEntityDraftRepository entityDraftRepository;
    private final RefinementRelationDraftRepository relationDraftRepository;
    private final RefinementLineageNodeDraftRepository lineageNodeDraftRepository;
    private final RefinementLineageRelationDraftRepository lineageRelationDraftRepository;
    private final QualityAnnotationRepository qualityAnnotationRepository;
    private final RefinementDraftBootstrapSupport draftBootstrapSupport;
    private final RefinementApplySupport applySupport;
    private final QualitySummaryAggregationSupport qualitySummaryAggregationSupport;
    private final KnowledgeRefinementManualKeySupport manualKeySupport;

    public KnowledgeGraphRefinementApplicationServiceImpl(
            GraphVersionRepository graphVersionRepository,
            RefinementTaskRepository refinementTaskRepository,
            RefinementEntityDraftRepository entityDraftRepository,
            RefinementRelationDraftRepository relationDraftRepository,
            RefinementLineageNodeDraftRepository lineageNodeDraftRepository,
            RefinementLineageRelationDraftRepository lineageRelationDraftRepository,
            QualityAnnotationRepository qualityAnnotationRepository,
            RefinementDraftBootstrapSupport draftBootstrapSupport,
            RefinementApplySupport applySupport,
            QualitySummaryAggregationSupport qualitySummaryAggregationSupport,
            KnowledgeRefinementManualKeySupport manualKeySupport) {
        this.graphVersionRepository = graphVersionRepository;
        this.refinementTaskRepository = refinementTaskRepository;
        this.entityDraftRepository = entityDraftRepository;
        this.relationDraftRepository = relationDraftRepository;
        this.lineageNodeDraftRepository = lineageNodeDraftRepository;
        this.lineageRelationDraftRepository = lineageRelationDraftRepository;
        this.qualityAnnotationRepository = qualityAnnotationRepository;
        this.draftBootstrapSupport = draftBootstrapSupport;
        this.applySupport = applySupport;
        this.qualitySummaryAggregationSupport = qualitySummaryAggregationSupport;
        this.manualKeySupport = manualKeySupport;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RefinementWorkbenchItemResult> pageTasks(RefinementWorkbenchPageQuery query) {
        RefinementWorkbenchPageQuery effective = query == null
                ? new RefinementWorkbenchPageQuery(
                        null, null, null, null, null, PageRules.firstPageIndex(), PageRules.defaultPageSize())
                : query;
        PageResult<RefinementTask> page = refinementTaskRepository.page(
                effective.getTaskType(),
                effective.getSourceContentType(),
                effective.getSourceContentId(),
                effective.getSourceCategoryCode(),
                effective.getStatus(),
                effective.getPageNo(),
                effective.getPageSize());
        return PageResult.of(
                page.getPageNo(),
                page.getPageSize(),
                page.getTotalCount(),
                page.getRecords().stream().map(this::toWorkbenchItem).toList());
    }

    @Override
    public RefinementDetailResult openTask(Long graphVersionId, Long openedBy) {
        GraphVersion version = graphVersionRepository.getByVersionId(graphVersionId);
        RefinementTask existing = refinementTaskRepository.findLatestDraft(
                version.getTaskType(), version.getSourceContentType(), version.getSourceContentId(), graphVersionId);
        if (existing != null) {
            return detail(existing);
        }
        Date now = new Date();
        RefinementTask task = new RefinementTask(
                null,
                null,
                version.getTaskType(),
                version.getSourceContentType(),
                version.getSourceContentId(),
                version.getSourceCategoryCode(),
                version.getSourceCategoryName(),
                graphVersionId,
                "DRAFT",
                openedBy,
                now,
                null,
                null,
                null,
                null,
                null,
                null);
        Long taskId = refinementTaskRepository.save(task);
        task.setRefinementTaskId(RefinementTaskId.of(taskId));
        entityDraftRepository.saveOrUpdateBatch(
                draftBootstrapSupport.bootstrapEntityDrafts(taskId, graphVersionId, openedBy));
        relationDraftRepository.saveOrUpdateBatch(
                draftBootstrapSupport.bootstrapRelationDrafts(taskId, graphVersionId, openedBy));
        lineageNodeDraftRepository.saveOrUpdateBatch(
                draftBootstrapSupport.bootstrapLineageNodeDrafts(taskId, graphVersionId, openedBy));
        lineageRelationDraftRepository.saveOrUpdateBatch(
                draftBootstrapSupport.bootstrapLineageRelationDrafts(taskId, graphVersionId, openedBy));
        return detail(task);
    }

    @Override
    @Transactional(readOnly = true)
    public RefinementDetailResult getTaskDetail(RefinementDetailQuery query) {
        return detail(refinementTaskRepository.getByTaskId(
                RefinementTaskId.ofNullable(query == null ? null : query.getRefinementTaskId())));
    }

    @Override
    public RefinementEntityResult upsertEntity(UpsertRefinementEntityCommand command) {
        List<RefinementEntityDraft> drafts =
                new ArrayList<>(entityDraftRepository.listByTaskId(command.getRefinementTaskId()));
        RefinementEntityDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getEntityKey(), command.getEntityKey())
                        || idEquals(item.getEntityId(), command.getEntityId()))
                .findFirst()
                .orElseGet(() -> new RefinementEntityDraft());
        boolean created = draft.getDraftId() == null;
        fillEntityDraft(draft, command, created);
        entityDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public RefinementEntityResult confirmEntity(ConfirmRefinementEntityCommand command) {
        List<RefinementEntityDraft> drafts = entityDraftRepository.listByTaskId(command.getRefinementTaskId());
        RefinementEntityDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getEntityKey(), command.getEntityKey()))
                .findFirst()
                .orElseThrow();
        draft.setConfirmationStatus("MANUAL_CONFIRMED");
        draft.setOperationType("CONFIRMED");
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(new Date());
        entityDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public void deleteEntity(DeleteRefinementEntityCommand command) {
        List<RefinementEntityDraft> drafts = entityDraftRepository.listByTaskId(command.getRefinementTaskId());
        RefinementEntityDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getEntityKey(), command.getEntityKey()))
                .findFirst()
                .orElseThrow();
        draft.setOperationType("DELETED");
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(new Date());
        entityDraftRepository.saveOrUpdateBatch(List.of(draft));
    }

    @Override
    public RefinementRelationResult upsertRelation(UpsertRefinementRelationCommand command) {
        List<RefinementRelationDraft> drafts =
                new ArrayList<>(relationDraftRepository.listByTaskId(command.getRefinementTaskId()));
        RefinementRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.getRelationKey())
                        || idEquals(item.getRelationId(), command.getRelationId()))
                .findFirst()
                .orElseGet(RefinementRelationDraft::new);
        boolean created = draft.getDraftId() == null;
        fillRelationDraft(draft, command, created);
        relationDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public RefinementRelationResult confirmRelation(ConfirmRefinementRelationCommand command) {
        List<RefinementRelationDraft> drafts = relationDraftRepository.listByTaskId(command.getRefinementTaskId());
        RefinementRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.getRelationKey()))
                .findFirst()
                .orElseThrow();
        draft.setConfirmationStatus("MANUAL_CONFIRMED");
        draft.setOperationType("CONFIRMED");
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(new Date());
        relationDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public void deleteRelation(DeleteRefinementRelationCommand command) {
        List<RefinementRelationDraft> drafts = relationDraftRepository.listByTaskId(command.getRefinementTaskId());
        RefinementRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.getRelationKey()))
                .findFirst()
                .orElseThrow();
        draft.setOperationType("DELETED");
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(new Date());
        relationDraftRepository.saveOrUpdateBatch(List.of(draft));
    }

    @Override
    public RefinementLineageNodeResult upsertLineageNode(UpsertRefinementLineageNodeCommand command) {
        List<RefinementLineageNodeDraft> drafts =
                new ArrayList<>(lineageNodeDraftRepository.listByTaskId(command.getRefinementTaskId()));
        RefinementLineageNodeDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getNodeKey(), command.getNodeKey())
                        || idEquals(item.getNodeId(), command.getNodeId()))
                .findFirst()
                .orElseGet(RefinementLineageNodeDraft::new);
        boolean created = draft.getDraftId() == null;
        fillLineageNodeDraft(draft, command, created);
        lineageNodeDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public RefinementLineageNodeResult confirmLineageNode(ConfirmRefinementLineageNodeCommand command) {
        List<RefinementLineageNodeDraft> drafts =
                lineageNodeDraftRepository.listByTaskId(command.getRefinementTaskId());
        RefinementLineageNodeDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getNodeKey(), command.getNodeKey()))
                .findFirst()
                .orElseThrow();
        draft.setConfirmationStatus("MANUAL_CONFIRMED");
        draft.setOperationType("CONFIRMED");
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(new Date());
        lineageNodeDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public void deleteLineageNode(DeleteRefinementLineageNodeCommand command) {
        List<RefinementLineageNodeDraft> drafts =
                lineageNodeDraftRepository.listByTaskId(command.getRefinementTaskId());
        RefinementLineageNodeDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getNodeKey(), command.getNodeKey()))
                .findFirst()
                .orElseThrow();
        draft.setOperationType("DELETED");
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(new Date());
        lineageNodeDraftRepository.saveOrUpdateBatch(List.of(draft));
    }

    @Override
    public RefinementLineageRelationResult upsertLineageRelation(UpsertRefinementLineageRelationCommand command) {
        List<RefinementLineageRelationDraft> drafts =
                new ArrayList<>(lineageRelationDraftRepository.listByTaskId(command.getRefinementTaskId()));
        RefinementLineageRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.getRelationKey())
                        || idEquals(item.getRelationId(), command.getRelationId()))
                .findFirst()
                .orElseGet(RefinementLineageRelationDraft::new);
        boolean created = draft.getDraftId() == null;
        fillLineageRelationDraft(draft, command, created);
        lineageRelationDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public RefinementLineageRelationResult confirmLineageRelation(ConfirmRefinementLineageRelationCommand command) {
        List<RefinementLineageRelationDraft> drafts =
                lineageRelationDraftRepository.listByTaskId(command.getRefinementTaskId());
        RefinementLineageRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.getRelationKey()))
                .findFirst()
                .orElseThrow();
        draft.setConfirmationStatus("MANUAL_CONFIRMED");
        draft.setOperationType("CONFIRMED");
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(new Date());
        lineageRelationDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public void deleteLineageRelation(DeleteRefinementLineageRelationCommand command) {
        List<RefinementLineageRelationDraft> drafts =
                lineageRelationDraftRepository.listByTaskId(command.getRefinementTaskId());
        RefinementLineageRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.getRelationKey()))
                .findFirst()
                .orElseThrow();
        draft.setOperationType("DELETED");
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(new Date());
        lineageRelationDraftRepository.saveOrUpdateBatch(List.of(draft));
    }

    @Override
    public QualityAnnotationResult upsertAnnotation(UpsertQualityAnnotationCommand command) {
        QualityAnnotation annotation = new QualityAnnotation(
                null,
                command.getAnnotationId(),
                command.getObjectType(),
                command.getObjectKey(),
                command.getSourceContentType(),
                command.getSourceContentId(),
                command.getGraphVersionId(),
                command.getAnnotationStatus(),
                command.getAnnotationLabel(),
                command.getComment(),
                command.getOperatorId(),
                new Date(),
                command.getOperatorId(),
                new Date());
        qualityAnnotationRepository.saveOrUpdate(annotation);
        return new QualityAnnotationResult(
                annotation.getAnnotationId(),
                annotation.getObjectType(),
                annotation.getObjectKey(),
                annotation.getGraphVersionId(),
                annotation.getAnnotationStatus(),
                annotation.getAnnotationLabel(),
                annotation.getComment());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<QualityAnnotationResult> pageAnnotations(QualityAnnotationPageQuery query) {
        RefinementTask task = refinementTaskRepository.getByTaskId(
                RefinementTaskId.ofNullable(query == null ? null : query.getRefinementTaskId()));
        List<QualityAnnotationResult> records = qualityAnnotationRepository
                .listBySource(
                        query == null ? null : query.getObjectType(),
                        task.getSourceContentType(),
                        task.getSourceContentId(),
                        task.getGraphVersionId())
                .stream()
                .map(annotation -> new QualityAnnotationResult(
                        annotation.getAnnotationId(),
                        annotation.getObjectType(),
                        annotation.getObjectKey(),
                        annotation.getGraphVersionId(),
                        annotation.getAnnotationStatus(),
                        annotation.getAnnotationLabel(),
                        annotation.getComment()))
                .toList();
        int pageNo = query == null ? PageRules.firstPageIndex() : query.getPageNo();
        int pageSize = query == null ? PageRules.defaultPageSize() : query.getPageSize();
        int fromIndex = Math.max(0, (pageNo - 1) * pageSize);
        int toIndex = Math.min(records.size(), fromIndex + pageSize);
        List<QualityAnnotationResult> pageRecords =
                fromIndex >= records.size() ? List.of() : records.subList(fromIndex, toIndex);
        return PageResult.of(pageNo, pageSize, records.size(), pageRecords);
    }

    @Override
    public void deleteAnnotation(DeleteQualityAnnotationCommand command) {
        qualityAnnotationRepository.deleteByAnnotationId(command.getAnnotationId());
    }

    @Override
    public RefinementDetailResult applyTask(Long refinementTaskId, Long appliedBy) {
        RefinementTask task = refinementTaskRepository.getByTaskId(RefinementTaskId.ofNullable(refinementTaskId));
        applySupport.applyEntities(task.getGraphVersionId(), entityDraftRepository.listByTaskId(refinementTaskId));
        applySupport.applyRelations(task.getGraphVersionId(), relationDraftRepository.listByTaskId(refinementTaskId));
        applySupport.applyLineageNodes(
                task.getGraphVersionId(), lineageNodeDraftRepository.listByTaskId(refinementTaskId));
        applySupport.applyLineageRelations(
                task.getGraphVersionId(), lineageRelationDraftRepository.listByTaskId(refinementTaskId));
        task.setStatus("APPLIED");
        task.setAppliedBy(appliedBy);
        task.setAppliedAt(new Date());
        refinementTaskRepository.update(task);
        return detail(task);
    }

    @Override
    @Transactional(readOnly = true)
    public QualitySummaryResult qualitySummary(Long refinementTaskId) {
        return qualitySummaryAggregationSupport.aggregate(
                entityDraftRepository.listByTaskId(refinementTaskId),
                relationDraftRepository.listByTaskId(refinementTaskId));
    }

    private void fillEntityDraft(RefinementEntityDraft draft, UpsertRefinementEntityCommand command, boolean created) {
        Date now = new Date();
        draft.setRefinementTaskId(command.getRefinementTaskId());
        draft.setEntityId(command.getEntityId());
        draft.setEntityKey(
                created && command.getEntityKey() == null
                        ? manualKeySupport.nextEntityKey()
                        : defaultIfBlank(command.getEntityKey(), draft.getEntityKey()));
        draft.setOriginType(created && command.getEntityId() == null ? "MANUAL_CREATED" : "AI_EXTRACTED");
        draft.setOperationType(created && command.getEntityId() == null ? "ADDED" : "UPDATED");
        draft.setName(command.getName());
        draft.setEntityType(command.getEntityType());
        draft.setDescription(command.getDescription());
        draft.setConfirmationStatus(created ? "PENDING" : defaultIfBlank(draft.getConfirmationStatus(), "PENDING"));
        draft.setSourceRefsJson(command.getSourceRefsJson());
        draft.setSortOrder(command.getSortOrder());
        if (created) {
            draft.setCreatedBy(command.getOperatorId());
            draft.setCreatedAt(now);
        }
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(now);
    }

    private void fillRelationDraft(
            RefinementRelationDraft draft, UpsertRefinementRelationCommand command, boolean created) {
        Date now = new Date();
        draft.setRefinementTaskId(command.getRefinementTaskId());
        draft.setRelationId(command.getRelationId());
        draft.setRelationKey(
                created && command.getRelationKey() == null
                        ? manualKeySupport.nextRelationKey()
                        : defaultIfBlank(command.getRelationKey(), draft.getRelationKey()));
        draft.setOriginType(created && command.getRelationId() == null ? "MANUAL_CREATED" : "AI_EXTRACTED");
        draft.setOperationType(created && command.getRelationId() == null ? "ADDED" : "UPDATED");
        draft.setSourceEntityKey(command.getSourceEntityKey());
        draft.setTargetEntityKey(command.getTargetEntityKey());
        draft.setSourceName(command.getSourceName());
        draft.setTargetName(command.getTargetName());
        draft.setRelationType(command.getRelationType());
        draft.setEvidence(command.getEvidence());
        draft.setConfirmationStatus(created ? "PENDING" : defaultIfBlank(draft.getConfirmationStatus(), "PENDING"));
        draft.setSourceRefsJson(command.getSourceRefsJson());
        draft.setSortOrder(command.getSortOrder());
        if (created) {
            draft.setCreatedBy(command.getOperatorId());
            draft.setCreatedAt(now);
        }
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(now);
    }

    private void fillLineageNodeDraft(
            RefinementLineageNodeDraft draft, UpsertRefinementLineageNodeCommand command, boolean created) {
        Date now = new Date();
        draft.setRefinementTaskId(command.getRefinementTaskId());
        draft.setNodeId(command.getNodeId());
        draft.setNodeKey(
                created && command.getNodeKey() == null
                        ? manualKeySupport.nextLineageNodeKey()
                        : defaultIfBlank(command.getNodeKey(), draft.getNodeKey()));
        draft.setOriginType(created && command.getNodeId() == null ? "MANUAL_CREATED" : "AI_EXTRACTED");
        draft.setOperationType(created && command.getNodeId() == null ? "ADDED" : "UPDATED");
        draft.setName(command.getName());
        draft.setNodeType(command.getNodeType());
        draft.setGeneration(command.getGeneration());
        draft.setGender(command.getGender());
        draft.setConfirmationStatus(created ? "PENDING" : defaultIfBlank(draft.getConfirmationStatus(), "PENDING"));
        draft.setSourceRefsJson(command.getSourceRefsJson());
        draft.setSortOrder(command.getSortOrder());
        if (created) {
            draft.setCreatedBy(command.getOperatorId());
            draft.setCreatedAt(now);
        }
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(now);
    }

    private void fillLineageRelationDraft(
            RefinementLineageRelationDraft draft, UpsertRefinementLineageRelationCommand command, boolean created) {
        Date now = new Date();
        draft.setRefinementTaskId(command.getRefinementTaskId());
        draft.setRelationId(command.getRelationId());
        draft.setRelationKey(
                created && command.getRelationKey() == null
                        ? manualKeySupport.nextLineageRelationKey()
                        : defaultIfBlank(command.getRelationKey(), draft.getRelationKey()));
        draft.setOriginType(created && command.getRelationId() == null ? "MANUAL_CREATED" : "AI_EXTRACTED");
        draft.setOperationType(created && command.getRelationId() == null ? "ADDED" : "UPDATED");
        draft.setSourceNodeKey(command.getSourceNodeKey());
        draft.setTargetNodeKey(command.getTargetNodeKey());
        draft.setSourceName(command.getSourceName());
        draft.setTargetName(command.getTargetName());
        draft.setRelationType(command.getRelationType());
        draft.setEvidence(command.getEvidence());
        draft.setConfirmationStatus(created ? "PENDING" : defaultIfBlank(draft.getConfirmationStatus(), "PENDING"));
        draft.setSourceRefsJson(command.getSourceRefsJson());
        draft.setSortOrder(command.getSortOrder());
        if (created) {
            draft.setCreatedBy(command.getOperatorId());
            draft.setCreatedAt(now);
        }
        draft.setUpdatedBy(command.getOperatorId());
        draft.setUpdatedAt(now);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private RefinementDetailResult detail(RefinementTask task) {
        Long refinementTaskId = task == null || task.getRefinementTaskId() == null
                ? null
                : task.getRefinementTaskId().value();
        List<RefinementEntityDraft> entities = entityDraftRepository.listByTaskId(refinementTaskId);
        List<RefinementRelationDraft> relations = relationDraftRepository.listByTaskId(refinementTaskId);
        List<RefinementLineageNodeDraft> lineageNodes = lineageNodeDraftRepository.listByTaskId(refinementTaskId);
        List<RefinementLineageRelationDraft> lineageRelations =
                lineageRelationDraftRepository.listByTaskId(refinementTaskId);
        QualitySummaryResult qualitySummary = qualitySummaryAggregationSupport.aggregate(entities, relations);
        return new RefinementDetailResult(
                refinementTaskId,
                task == null ? null : task.getGraphVersionId(),
                task == null ? null : task.getTaskType(),
                task == null ? null : task.getSourceContentType(),
                task == null ? null : task.getSourceContentId(),
                task == null ? null : task.getSourceCategoryCode(),
                task == null ? null : task.getSourceCategoryName(),
                task == null ? null : task.getStatus(),
                new RefinementProgressSummaryResult(
                        pendingEntities(entities),
                        confirmedEntities(entities),
                        pendingRelations(relations),
                        confirmedRelations(relations)),
                entities.stream().map(this::toResult).toList(),
                relations.stream().map(this::toResult).toList(),
                lineageNodes.stream().map(this::toResult).toList(),
                lineageRelations.stream().map(this::toResult).toList(),
                entities.stream()
                        .map(item -> new RefinementEntityOptionResult(item.getEntityKey(), item.getName()))
                        .toList());
    }

    private RefinementWorkbenchItemResult toWorkbenchItem(RefinementTask task) {
        List<RefinementEntityDraft> entities =
                entityDraftRepository.listByTaskId(task.getRefinementTaskId().value());
        List<RefinementRelationDraft> relations =
                relationDraftRepository.listByTaskId(task.getRefinementTaskId().value());
        return new RefinementWorkbenchItemResult(
                task.getRefinementTaskId().value(),
                task.getGraphVersionId(),
                task.getTaskType(),
                task.getSourceContentType(),
                task.getSourceContentId(),
                task.getSourceCategoryCode(),
                task.getSourceCategoryName(),
                task.getStatus(),
                task.getOpenedBy(),
                task.getOpenedAt() == null ? null : task.getOpenedAt().getTime(),
                new RefinementProgressSummaryResult(
                        pendingEntities(entities),
                        confirmedEntities(entities),
                        pendingRelations(relations),
                        confirmedRelations(relations)));
    }

    private int pendingEntities(List<RefinementEntityDraft> drafts) {
        return drafts == null ? 0 : drafts.size() - confirmedEntities(drafts);
    }

    private int confirmedEntities(List<RefinementEntityDraft> drafts) {
        return drafts == null
                ? 0
                : (int) drafts.stream()
                        .filter(item -> "MANUAL_CONFIRMED".equals(item.getConfirmationStatus()))
                        .count();
    }

    private int pendingRelations(List<RefinementRelationDraft> drafts) {
        return drafts == null ? 0 : drafts.size() - confirmedRelations(drafts);
    }

    private int confirmedRelations(List<RefinementRelationDraft> drafts) {
        return drafts == null
                ? 0
                : (int) drafts.stream()
                        .filter(item -> "MANUAL_CONFIRMED".equals(item.getConfirmationStatus()))
                        .count();
    }

    private boolean keyEquals(String left, String right) {
        return left != null && left.equals(right);
    }

    private boolean idEquals(Long left, Long right) {
        return left != null && left.equals(right);
    }

    private RefinementEntityResult toResult(RefinementEntityDraft draft) {
        return new RefinementEntityResult(
                draft.getDraftId(),
                draft.getEntityId(),
                draft.getEntityKey(),
                draft.getOriginType(),
                draft.getOperationType(),
                draft.getName(),
                draft.getEntityType(),
                draft.getDescription(),
                draft.getConfirmationStatus(),
                draft.getSourceRefsJson(),
                draft.getSortOrder());
    }

    private RefinementRelationResult toResult(RefinementRelationDraft draft) {
        return new RefinementRelationResult(
                draft.getDraftId(),
                draft.getRelationId(),
                draft.getRelationKey(),
                draft.getOriginType(),
                draft.getOperationType(),
                draft.getSourceEntityKey(),
                draft.getTargetEntityKey(),
                draft.getSourceName(),
                draft.getTargetName(),
                draft.getRelationType(),
                draft.getEvidence(),
                draft.getConfirmationStatus(),
                draft.getSourceRefsJson(),
                draft.getSortOrder());
    }

    private RefinementLineageNodeResult toResult(RefinementLineageNodeDraft draft) {
        return new RefinementLineageNodeResult(
                draft.getDraftId(),
                draft.getNodeId(),
                draft.getNodeKey(),
                draft.getOriginType(),
                draft.getOperationType(),
                draft.getName(),
                draft.getNodeType(),
                draft.getGeneration(),
                draft.getGender(),
                draft.getConfirmationStatus(),
                draft.getSourceRefsJson(),
                draft.getSortOrder());
    }

    private RefinementLineageRelationResult toResult(RefinementLineageRelationDraft draft) {
        return new RefinementLineageRelationResult(
                draft.getDraftId(),
                draft.getRelationId(),
                draft.getRelationKey(),
                draft.getOriginType(),
                draft.getOperationType(),
                draft.getSourceNodeKey(),
                draft.getTargetNodeKey(),
                draft.getSourceName(),
                draft.getTargetName(),
                draft.getRelationType(),
                draft.getEvidence(),
                draft.getConfirmationStatus(),
                draft.getSourceRefsJson(),
                draft.getSortOrder());
    }
}
