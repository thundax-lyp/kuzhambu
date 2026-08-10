package com.thundax.kuzhambu.knowledge.application.refinement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ApplyRefinementTaskCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ConfirmRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.DeleteRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.OpenRefinementTaskCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertQualityAnnotationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementEntityCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageNodeCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementLineageRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.UpsertRefinementRelationCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityAnnotationQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualitySummaryQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementDetailQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.RefinementWorkbenchQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.RefinementApplyResult;
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
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.codec.RefinementTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityAnnotationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementEntityDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageNodeDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@BizExceptionBoundary
public class KnowledgeGraphRefinementApplicationServiceImpl implements KnowledgeGraphRefinementApplicationService {

    private static final String STATUS_APPLIED = "APPLIED";
    private static final String TRIGGER_SOURCE_REFINEMENT_APPLIED = "REFINEMENT_APPLIED";
    private static final String NEXT_ACTION_OPEN_GRAPH_VERSION = "OPEN_GRAPH_VERSION";

    private final GraphVersionRepository graphVersionRepository;
    private final GraphExtractionTaskRepository graphExtractionTaskRepository;
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
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public KnowledgeGraphRefinementApplicationServiceImpl(
            GraphVersionRepository graphVersionRepository,
            GraphExtractionTaskRepository graphExtractionTaskRepository,
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
        this.graphExtractionTaskRepository = graphExtractionTaskRepository;
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
    public PageResult<RefinementWorkbenchItemResult> pageTasks(RefinementWorkbenchQuery query, PageQuery pageQuery) {
        RefinementWorkbenchQuery effective =
                query == null ? new RefinementWorkbenchQuery(null, null, null, null, null) : query;
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        PageResult<RefinementTask> page = refinementTaskRepository.page(
                effective.taskType(),
                effective.sourceContentType(),
                effective.sourceContentId(),
                effective.sourceCategoryCode(),
                effective.status(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        return PageResult.of(
                page.getPageNo(),
                page.getPageSize(),
                page.getTotalCount(),
                page.getRecords().stream().map(this::toWorkbenchItem).toList());
    }

    @Override
    public RefinementDetailResult openTask(OpenRefinementTaskCommand command) {
        Long graphVersionId = command == null ? null : command.graphVersionId();
        Long openedBy = command == null ? null : command.openedBy();
        GraphVersion version = graphVersionRepository.getByVersionId(GraphVersionIdCodec.toDomain(graphVersionId));
        RefinementTask existing = refinementTaskRepository.findLatestDraft(
                graphVersionTaskTypeValue(version),
                version.getSourceContentType(),
                GraphExtractionSourceContentIdCodec.toValue(version.getSourceContentId()),
                graphVersionId);
        if (existing != null) {
            return detail(existing);
        }
        Instant now = Instant.now();
        RefinementTask task = new RefinementTask(
                null,
                null,
                graphVersionTaskTypeValue(version),
                version.getSourceContentType(),
                GraphExtractionSourceContentIdCodec.toValue(version.getSourceContentId()),
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
        task.setRefinementTaskId(RefinementTaskIdCodec.toDomain(taskId));
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
                RefinementTaskIdCodec.toDomain(query == null ? null : query.refinementTaskId())));
    }

    @Override
    public RefinementEntityResult upsertEntity(UpsertRefinementEntityCommand command) {
        List<RefinementEntityDraft> drafts =
                new ArrayList<>(entityDraftRepository.listByTaskId(command.refinementTaskId()));
        RefinementEntityDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getEntityKey(), command.entityKey())
                        || idEquals(item.getEntityId(), command.entityId()))
                .findFirst()
                .orElseGet(() -> new RefinementEntityDraft());
        boolean created = draft.getDraftId() == null;
        fillEntityDraft(draft, command, created);
        entityDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public RefinementEntityResult confirmEntity(ConfirmRefinementEntityCommand command) {
        List<RefinementEntityDraft> drafts = entityDraftRepository.listByTaskId(command.refinementTaskId());
        RefinementEntityDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getEntityKey(), command.entityKey()))
                .findFirst()
                .orElseThrow();
        draft.setConfirmationStatus("MANUAL_CONFIRMED");
        draft.setOperationType("CONFIRMED");
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(Instant.now());
        entityDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public void deleteEntity(DeleteRefinementEntityCommand command) {
        List<RefinementEntityDraft> drafts = entityDraftRepository.listByTaskId(command.refinementTaskId());
        RefinementEntityDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getEntityKey(), command.entityKey()))
                .findFirst()
                .orElseThrow();
        draft.setOperationType("DELETED");
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(Instant.now());
        entityDraftRepository.saveOrUpdateBatch(List.of(draft));
    }

    @Override
    public RefinementRelationResult upsertRelation(UpsertRefinementRelationCommand command) {
        List<RefinementRelationDraft> drafts =
                new ArrayList<>(relationDraftRepository.listByTaskId(command.refinementTaskId()));
        RefinementRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.relationKey())
                        || idEquals(item.getRelationId(), command.relationId()))
                .findFirst()
                .orElseGet(RefinementRelationDraft::new);
        boolean created = draft.getDraftId() == null;
        fillRelationDraft(draft, command, created);
        relationDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public RefinementRelationResult confirmRelation(ConfirmRefinementRelationCommand command) {
        List<RefinementRelationDraft> drafts = relationDraftRepository.listByTaskId(command.refinementTaskId());
        RefinementRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.relationKey()))
                .findFirst()
                .orElseThrow();
        draft.setConfirmationStatus("MANUAL_CONFIRMED");
        draft.setOperationType("CONFIRMED");
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(Instant.now());
        relationDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public void deleteRelation(DeleteRefinementRelationCommand command) {
        List<RefinementRelationDraft> drafts = relationDraftRepository.listByTaskId(command.refinementTaskId());
        RefinementRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.relationKey()))
                .findFirst()
                .orElseThrow();
        draft.setOperationType("DELETED");
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(Instant.now());
        relationDraftRepository.saveOrUpdateBatch(List.of(draft));
    }

    @Override
    public RefinementLineageNodeResult upsertLineageNode(UpsertRefinementLineageNodeCommand command) {
        List<RefinementLineageNodeDraft> drafts =
                new ArrayList<>(lineageNodeDraftRepository.listByTaskId(command.refinementTaskId()));
        RefinementLineageNodeDraft draft = drafts.stream()
                .filter(item ->
                        keyEquals(item.getNodeKey(), command.nodeKey()) || idEquals(item.getNodeId(), command.nodeId()))
                .findFirst()
                .orElseGet(RefinementLineageNodeDraft::new);
        boolean created = draft.getDraftId() == null;
        fillLineageNodeDraft(draft, command, created);
        lineageNodeDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public RefinementLineageNodeResult confirmLineageNode(ConfirmRefinementLineageNodeCommand command) {
        List<RefinementLineageNodeDraft> drafts = lineageNodeDraftRepository.listByTaskId(command.refinementTaskId());
        RefinementLineageNodeDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getNodeKey(), command.nodeKey()))
                .findFirst()
                .orElseThrow();
        draft.setConfirmationStatus("MANUAL_CONFIRMED");
        draft.setOperationType("CONFIRMED");
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(Instant.now());
        lineageNodeDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public void deleteLineageNode(DeleteRefinementLineageNodeCommand command) {
        List<RefinementLineageNodeDraft> drafts = lineageNodeDraftRepository.listByTaskId(command.refinementTaskId());
        RefinementLineageNodeDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getNodeKey(), command.nodeKey()))
                .findFirst()
                .orElseThrow();
        draft.setOperationType("DELETED");
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(Instant.now());
        lineageNodeDraftRepository.saveOrUpdateBatch(List.of(draft));
    }

    @Override
    public RefinementLineageRelationResult upsertLineageRelation(UpsertRefinementLineageRelationCommand command) {
        List<RefinementLineageRelationDraft> drafts =
                new ArrayList<>(lineageRelationDraftRepository.listByTaskId(command.refinementTaskId()));
        RefinementLineageRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.relationKey())
                        || idEquals(item.getRelationId(), command.relationId()))
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
                lineageRelationDraftRepository.listByTaskId(command.refinementTaskId());
        RefinementLineageRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.relationKey()))
                .findFirst()
                .orElseThrow();
        draft.setConfirmationStatus("MANUAL_CONFIRMED");
        draft.setOperationType("CONFIRMED");
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(Instant.now());
        lineageRelationDraftRepository.saveOrUpdateBatch(List.of(draft));
        return toResult(draft);
    }

    @Override
    public void deleteLineageRelation(DeleteRefinementLineageRelationCommand command) {
        List<RefinementLineageRelationDraft> drafts =
                lineageRelationDraftRepository.listByTaskId(command.refinementTaskId());
        RefinementLineageRelationDraft draft = drafts.stream()
                .filter(item -> keyEquals(item.getRelationKey(), command.relationKey()))
                .findFirst()
                .orElseThrow();
        draft.setOperationType("DELETED");
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(Instant.now());
        lineageRelationDraftRepository.saveOrUpdateBatch(List.of(draft));
    }

    @Override
    public QualityAnnotationResult upsertAnnotation(UpsertQualityAnnotationCommand command) {
        QualityAnnotation annotation = new QualityAnnotation(
                null,
                command.annotationId(),
                command.objectType(),
                command.objectKey(),
                command.sourceContentType(),
                command.sourceContentId(),
                command.graphVersionId(),
                command.annotationStatus(),
                command.annotationLabel(),
                command.comment(),
                command.operatorId(),
                Instant.now(),
                command.operatorId(),
                Instant.now());
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
    public PageResult<QualityAnnotationResult> pageAnnotations(QualityAnnotationQuery query, PageQuery pageQuery) {
        RefinementTask task = refinementTaskRepository.getByTaskId(
                RefinementTaskIdCodec.toDomain(query == null ? null : query.refinementTaskId()));
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        List<QualityAnnotationResult> records = qualityAnnotationRepository
                .listBySource(
                        query == null ? null : query.objectType(),
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
        int pageNo = effectivePage.getPageNo();
        int pageSize = effectivePage.getPageSize();
        int fromIndex = Math.max(0, (pageNo - 1) * pageSize);
        int toIndex = Math.min(records.size(), fromIndex + pageSize);
        List<QualityAnnotationResult> pageRecords =
                fromIndex >= records.size() ? List.of() : records.subList(fromIndex, toIndex);
        return PageResult.of(pageNo, pageSize, records.size(), pageRecords);
    }

    @Override
    public void deleteAnnotation(DeleteQualityAnnotationCommand command) {
        qualityAnnotationRepository.deleteByAnnotationId(command.annotationId());
    }

    @Override
    public RefinementApplyResult applyTask(ApplyRefinementTaskCommand command) {
        Long refinementTaskId = command == null ? null : command.refinementTaskId();
        Long appliedBy = command == null ? null : command.appliedBy();
        RefinementTask task = refinementTaskRepository.getByTaskId(RefinementTaskIdCodec.toDomain(refinementTaskId));
        applySupport.applyEntities(task.getGraphVersionId(), entityDraftRepository.listByTaskId(refinementTaskId));
        applySupport.applyRelations(task.getGraphVersionId(), relationDraftRepository.listByTaskId(refinementTaskId));
        applySupport.applyLineageNodes(
                task.getGraphVersionId(), lineageNodeDraftRepository.listByTaskId(refinementTaskId));
        applySupport.applyLineageRelations(
                task.getGraphVersionId(), lineageRelationDraftRepository.listByTaskId(refinementTaskId));
        task.setStatus(STATUS_APPLIED);
        task.setAppliedBy(appliedBy);
        task.setAppliedAt(Instant.now());
        refinementTaskRepository.update(task);
        return toApplyResult(task);
    }

    @Override
    @Transactional(readOnly = true)
    public QualitySummaryResult qualitySummary(QualitySummaryQuery query) {
        Long refinementTaskId = query == null ? null : query.refinementTaskId();
        return qualitySummaryAggregationSupport.aggregate(
                entityDraftRepository.listByTaskId(refinementTaskId),
                relationDraftRepository.listByTaskId(refinementTaskId));
    }

    private void fillEntityDraft(RefinementEntityDraft draft, UpsertRefinementEntityCommand command, boolean created) {
        Instant now = Instant.now();
        draft.setRefinementTaskId(command.refinementTaskId());
        draft.setEntityId(command.entityId());
        draft.setEntityKey(
                created && command.entityKey() == null
                        ? manualKeySupport.nextEntityKey()
                        : defaultIfBlank(command.entityKey(), draft.getEntityKey()));
        draft.setOriginType(created && command.entityId() == null ? "MANUAL_CREATED" : "AI_EXTRACTED");
        draft.setOperationType(created && command.entityId() == null ? "ADDED" : "UPDATED");
        draft.setName(command.name());
        draft.setEntityType(command.entityType());
        draft.setDescription(command.description());
        draft.setConfirmationStatus(created ? "PENDING" : defaultIfBlank(draft.getConfirmationStatus(), "PENDING"));
        draft.setSourceRefsJson(command.sourceRefsJson());
        draft.setSortOrder(command.sortOrder());
        if (created) {
            draft.setCreatedBy(command.operatorId());
            draft.setCreatedAt(now);
        }
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(now);
    }

    private void fillRelationDraft(
            RefinementRelationDraft draft, UpsertRefinementRelationCommand command, boolean created) {
        Instant now = Instant.now();
        draft.setRefinementTaskId(command.refinementTaskId());
        draft.setRelationId(command.relationId());
        draft.setRelationKey(
                created && command.relationKey() == null
                        ? manualKeySupport.nextRelationKey()
                        : defaultIfBlank(command.relationKey(), draft.getRelationKey()));
        draft.setOriginType(created && command.relationId() == null ? "MANUAL_CREATED" : "AI_EXTRACTED");
        draft.setOperationType(created && command.relationId() == null ? "ADDED" : "UPDATED");
        draft.setSourceEntityKey(command.sourceEntityKey());
        draft.setTargetEntityKey(command.targetEntityKey());
        draft.setSourceName(command.sourceName());
        draft.setTargetName(command.targetName());
        draft.setRelationType(command.relationType());
        draft.setEvidence(command.evidence());
        draft.setConfirmationStatus(created ? "PENDING" : defaultIfBlank(draft.getConfirmationStatus(), "PENDING"));
        draft.setSourceRefsJson(command.sourceRefsJson());
        draft.setSortOrder(command.sortOrder());
        if (created) {
            draft.setCreatedBy(command.operatorId());
            draft.setCreatedAt(now);
        }
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(now);
    }

    private void fillLineageNodeDraft(
            RefinementLineageNodeDraft draft, UpsertRefinementLineageNodeCommand command, boolean created) {
        Instant now = Instant.now();
        draft.setRefinementTaskId(command.refinementTaskId());
        draft.setNodeId(command.nodeId());
        draft.setNodeKey(
                created && command.nodeKey() == null
                        ? manualKeySupport.nextLineageNodeKey()
                        : defaultIfBlank(command.nodeKey(), draft.getNodeKey()));
        draft.setOriginType(created && command.nodeId() == null ? "MANUAL_CREATED" : "AI_EXTRACTED");
        draft.setOperationType(created && command.nodeId() == null ? "ADDED" : "UPDATED");
        draft.setName(command.name());
        draft.setNodeType(command.nodeType());
        draft.setGeneration(command.generation());
        draft.setGender(command.gender());
        draft.setConfirmationStatus(created ? "PENDING" : defaultIfBlank(draft.getConfirmationStatus(), "PENDING"));
        draft.setSourceRefsJson(command.sourceRefsJson());
        draft.setSortOrder(command.sortOrder());
        if (created) {
            draft.setCreatedBy(command.operatorId());
            draft.setCreatedAt(now);
        }
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(now);
    }

    private void fillLineageRelationDraft(
            RefinementLineageRelationDraft draft, UpsertRefinementLineageRelationCommand command, boolean created) {
        Instant now = Instant.now();
        draft.setRefinementTaskId(command.refinementTaskId());
        draft.setRelationId(command.relationId());
        draft.setRelationKey(
                created && command.relationKey() == null
                        ? manualKeySupport.nextLineageRelationKey()
                        : defaultIfBlank(command.relationKey(), draft.getRelationKey()));
        draft.setOriginType(created && command.relationId() == null ? "MANUAL_CREATED" : "AI_EXTRACTED");
        draft.setOperationType(created && command.relationId() == null ? "ADDED" : "UPDATED");
        draft.setSourceNodeKey(command.sourceNodeKey());
        draft.setTargetNodeKey(command.targetNodeKey());
        draft.setSourceName(command.sourceName());
        draft.setTargetName(command.targetName());
        draft.setRelationType(command.relationType());
        draft.setEvidence(command.evidence());
        draft.setConfirmationStatus(created ? "PENDING" : defaultIfBlank(draft.getConfirmationStatus(), "PENDING"));
        draft.setSourceRefsJson(command.sourceRefsJson());
        draft.setSortOrder(command.sortOrder());
        if (created) {
            draft.setCreatedBy(command.operatorId());
            draft.setCreatedAt(now);
        }
        draft.setUpdatedBy(command.operatorId());
        draft.setUpdatedAt(now);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String graphVersionTaskTypeValue(GraphVersion version) {
        return version == null || version.getTaskType() == null
                ? null
                : version.getTaskType().value();
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
                task.getOpenedAt() == null ? null : task.getOpenedAt().toEpochMilli(),
                new RefinementProgressSummaryResult(
                        pendingEntities(entities),
                        confirmedEntities(entities),
                        pendingRelations(relations),
                        confirmedRelations(relations)));
    }

    private RefinementApplyResult toApplyResult(RefinementTask task) {
        GraphVersion version =
                graphVersionRepository.getByVersionId(GraphVersionIdCodec.toDomain(task.getGraphVersionId()));
        GraphExtractionTask sourceTask = sourceTask(version);
        Long sourceTaskId = version == null || version.getTaskId() == null
                ? null
                : version.getTaskId().value();
        String sourceContentType =
                defaultIfBlank(version == null ? null : version.getSourceContentType(), task.getSourceContentType());
        Long sourceContentId = version == null || version.getSourceContentId() == null
                ? task.getSourceContentId()
                : GraphExtractionSourceContentIdCodec.toValue(version.getSourceContentId());
        String sourceCategoryCode =
                defaultIfBlank(version == null ? null : version.getSourceCategoryCode(), task.getSourceCategoryCode());
        String sourceCategoryName =
                defaultIfBlank(version == null ? null : version.getSourceCategoryName(), task.getSourceCategoryName());
        return new RefinementApplyResult(
                task.getRefinementTaskId() == null
                        ? null
                        : task.getRefinementTaskId().value(),
                task.getGraphVersionId(),
                defaultIfBlank(graphVersionTaskTypeValue(version), task.getTaskType()),
                sourceContentType,
                sourceContentId,
                sourceCategoryCode,
                sourceCategoryName,
                task.getStatus(),
                task.getAppliedAt() == null ? null : task.getAppliedAt().toEpochMilli(),
                true,
                sourceTaskId != null,
                sourceTaskId,
                selectionScopeJson(task, version, sourceTask, sourceContentType, sourceContentId, sourceCategoryCode),
                true,
                TRIGGER_SOURCE_REFINEMENT_APPLIED,
                NEXT_ACTION_OPEN_GRAPH_VERSION,
                true);
    }

    private GraphExtractionTask sourceTask(GraphVersion version) {
        return version == null || version.getTaskId() == null
                ? null
                : graphExtractionTaskRepository.getByTaskId(version.getTaskId());
    }

    private String selectionScopeJson(
            RefinementTask task,
            GraphVersion version,
            GraphExtractionTask sourceTask,
            String sourceContentType,
            Long sourceContentId,
            String sourceCategoryCode) {
        if (sourceTask != null && sourceTask.getSelectionScopeJson() != null) {
            return sourceTask.getSelectionScopeJson();
        }
        Map<String, Object> scope = new LinkedHashMap<>();
        scope.put("graphVersionId", task.getGraphVersionId());
        scope.put("sourceContentType", sourceContentType);
        scope.put("sourceContentId", sourceContentId);
        scope.put("sourceCategoryCode", sourceCategoryCode);
        scope.put(
                "sourceCategoryName", version == null ? task.getSourceCategoryName() : version.getSourceCategoryName());
        try {
            return objectMapper.writeValueAsString(scope);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Knowledge refinement apply scope is invalid", ex);
        }
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
