package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.dto.AiInvocationLogFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobFailureFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.CreateAiBatchJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.GetAiInvocationLogFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeAiExtractionFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.KnowledgeAiExtractionFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RegenerateGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.configure.KnowledgeGraphExtractionExecutorConfiguration;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchCancelResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeEntityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageNodeResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeLineageRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.KnowledgeRelationResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.KnowledgeGraphCandidateApplySupport;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCallIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionBatchJobIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionModelIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionModelNameCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionPromptVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionRequestIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionRequesterIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTraceIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class KnowledgeGraphExtractionApplicationServiceImpl implements KnowledgeGraphExtractionApplicationService {

    private static final String CAPABILITY_KNOWLEDGE_RELATION_EXTRACT = "KNOWLEDGE_RELATION_EXTRACT";
    private static final String CAPABILITY_KNOWLEDGE_GRAPH_EXTRACT = "KNOWLEDGE_GRAPH_EXTRACT";
    private static final String CAPABILITY_KNOWLEDGE_LINEAGE_EXTRACT = "KNOWLEDGE_LINEAGE_EXTRACT";
    private static final String TASK_TYPE_RELATION = "RELATION";
    private static final String TASK_TYPE_GRAPH = "GRAPH";
    private static final String TASK_TYPE_LINEAGE = "LINEAGE";
    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_APPLIED = "APPLIED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PARTIAL = "PARTIAL";
    private static final String TRIGGER_SOURCE_REGENERATE = "REGENERATE";

    private final GraphExtractionTaskRepository repository;
    private final GraphVersionRepository graphVersionRepository;
    private final KnowledgeEntityRepository knowledgeEntityRepository;
    private final KnowledgeRelationRepository knowledgeRelationRepository;
    private final KnowledgeLineageNodeRepository knowledgeLineageNodeRepository;
    private final KnowledgeLineageRelationRepository knowledgeLineageRelationRepository;
    private final RefinementTaskRepository refinementTaskRepository;
    private final AiFacade aiFacade;
    private final KnowledgeGraphCandidateApplySupport candidateApplySupport;
    private final Executor taskExecutor;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public KnowledgeGraphExtractionApplicationServiceImpl(
            GraphExtractionTaskRepository repository,
            GraphVersionRepository graphVersionRepository,
            KnowledgeEntityRepository knowledgeEntityRepository,
            KnowledgeRelationRepository knowledgeRelationRepository,
            KnowledgeLineageNodeRepository knowledgeLineageNodeRepository,
            KnowledgeLineageRelationRepository knowledgeLineageRelationRepository,
            RefinementTaskRepository refinementTaskRepository,
            AiFacade aiFacade,
            KnowledgeGraphCandidateApplySupport candidateApplySupport) {
        this(
                repository,
                graphVersionRepository,
                knowledgeEntityRepository,
                knowledgeRelationRepository,
                knowledgeLineageNodeRepository,
                knowledgeLineageRelationRepository,
                refinementTaskRepository,
                aiFacade,
                candidateApplySupport,
                null,
                null);
    }

    @Autowired
    public KnowledgeGraphExtractionApplicationServiceImpl(
            GraphExtractionTaskRepository repository,
            GraphVersionRepository graphVersionRepository,
            KnowledgeEntityRepository knowledgeEntityRepository,
            KnowledgeRelationRepository knowledgeRelationRepository,
            KnowledgeLineageNodeRepository knowledgeLineageNodeRepository,
            KnowledgeLineageRelationRepository knowledgeLineageRelationRepository,
            RefinementTaskRepository refinementTaskRepository,
            AiFacade aiFacade,
            KnowledgeGraphCandidateApplySupport candidateApplySupport,
            @Qualifier(KnowledgeGraphExtractionExecutorConfiguration.TASK_EXECUTOR) Executor taskExecutor,
            PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.graphVersionRepository = graphVersionRepository;
        this.knowledgeEntityRepository = knowledgeEntityRepository;
        this.knowledgeRelationRepository = knowledgeRelationRepository;
        this.knowledgeLineageNodeRepository = knowledgeLineageNodeRepository;
        this.knowledgeLineageRelationRepository = knowledgeLineageRelationRepository;
        this.refinementTaskRepository = refinementTaskRepository;
        this.aiFacade = aiFacade;
        this.candidateApplySupport = candidateApplySupport;
        this.taskExecutor = taskExecutor;
        this.transactionTemplate = transactionManager == null ? null : new TransactionTemplate(transactionManager);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult requestRelationExtraction(RequestRelationExtractionCommand command) {
        validateCommandBase(
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getInputPayloadJson());
        return requestTasks(
                TASK_TYPE_RELATION,
                command == null ? null : command.getScopeType(),
                command == null ? null : command.getScopeJson(),
                command == null ? null : command.getTriggerSource(),
                command == null ? null : command.getSelectionScopeJson(),
                command == null ? null : command.getReplaceUnconfirmedOnly(),
                command == null ? null : command.getParentTaskId(),
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getRequestedBy(),
                command == null ? null : command.getServiceId(),
                command == null ? null : command.getServiceRole(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getPromptVersionId(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getPromptVariablesJson(),
                command == null ? null : command.getPromptHash(),
                command == null ? null : command.getInputPayloadJson(),
                command == null ? null : command.getOutputSchemaJson(),
                command != null && command.isForceJson(),
                command == null ? null : command.getLocale(),
                resolveOperation(TASK_TYPE_RELATION));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult requestGraphExtraction(RequestGraphExtractionCommand command) {
        validateCommandBase(
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getInputPayloadJson());
        return requestTasks(
                TASK_TYPE_GRAPH,
                command == null ? null : command.getScopeType(),
                command == null ? null : command.getScopeJson(),
                command == null ? null : command.getTriggerSource(),
                command == null ? null : command.getSelectionScopeJson(),
                command == null ? null : command.getReplaceUnconfirmedOnly(),
                command == null ? null : command.getParentTaskId(),
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getRequestedBy(),
                command == null ? null : command.getServiceId(),
                command == null ? null : command.getServiceRole(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getPromptVersionId(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getPromptVariablesJson(),
                command == null ? null : command.getPromptHash(),
                command == null ? null : command.getInputPayloadJson(),
                command == null ? null : command.getOutputSchemaJson(),
                command != null && command.isForceJson(),
                command == null ? null : command.getLocale(),
                resolveOperation(TASK_TYPE_GRAPH));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult requestLineageExtraction(RequestLineageExtractionCommand command) {
        validateCommandBase(
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getInputPayloadJson());
        return requestTasks(
                TASK_TYPE_LINEAGE,
                command == null ? null : command.getScopeType(),
                command == null ? null : command.getScopeJson(),
                command == null ? null : command.getTriggerSource(),
                command == null ? null : command.getSelectionScopeJson(),
                command == null ? null : command.getReplaceUnconfirmedOnly(),
                command == null ? null : command.getParentTaskId(),
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getRequestedBy(),
                command == null ? null : command.getServiceId(),
                command == null ? null : command.getServiceRole(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getPromptVersionId(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getPromptVariablesJson(),
                command == null ? null : command.getPromptHash(),
                command == null ? null : command.getInputPayloadJson(),
                command == null ? null : command.getOutputSchemaJson(),
                command != null && command.isForceJson(),
                command == null ? null : command.getLocale(),
                resolveOperation(TASK_TYPE_LINEAGE));
    }

    @Override
    public PageResult<GraphExtractionTaskResult> pageTasks(
            String taskType,
            Long batchJobId,
            String triggerSource,
            String status,
            String sourceContentType,
            Long sourceContentId,
            PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<GraphExtractionTask> taskPage = repository.page(
                taskType,
                GraphExtractionBatchJobIdCodec.toDomain(batchJobId),
                triggerSource,
                status,
                sourceContentType,
                GraphExtractionSourceContentIdCodec.toDomain(sourceContentId),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<GraphExtractionTaskResult> records =
                taskPage.getRecords().stream().map(this::syncTaskResult).toList();
        return PageResult.of(taskPage.getPageNo(), taskPage.getPageSize(), taskPage.getTotalCount(), records);
    }

    @Override
    public GraphExtractionTaskResult getTaskDetail(GraphExtractionTaskId taskId) {
        return syncTaskResult(repository.getByTaskId(taskId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult regenerateTask(
            String taskType,
            GraphExtractionTaskId sourceTaskId,
            String selectionScopeJson,
            Boolean replaceUnconfirmedOnly,
            Long requestedBy) {
        return regenerateTask(new RegenerateGraphExtractionCommand(
                taskType,
                sourceTaskId,
                TRIGGER_SOURCE_REGENERATE,
                selectionScopeJson,
                replaceUnconfirmedOnly,
                requestedBy));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult regenerateTask(RegenerateGraphExtractionCommand command) {
        GraphExtractionTaskId sourceTaskId = command == null ? null : command.getSourceTaskId();
        GraphExtractionTask sourceTask = repository.getByTaskId(sourceTaskId);
        if (sourceTask == null) {
            throw new BizException(
                    "Knowledge graph source task not found: " + (sourceTaskId == null ? null : sourceTaskId.value()));
        }
        validateRegenerateSourceTask(sourceTask);
        String commandTaskType = command == null ? null : command.getTaskType();
        String commandTriggerSource = command == null ? null : command.getTriggerSource();
        String commandSelectionScopeJson = command == null ? null : command.getSelectionScopeJson();
        Boolean commandReplaceUnconfirmedOnly = command == null ? null : command.getReplaceUnconfirmedOnly();
        Long commandRequestedBy = command == null ? null : command.getRequestedBy();
        String sourceTaskType = taskTypeValue(sourceTask);
        String resolvedTaskType = StringUtils.defaultIfBlank(commandTaskType, sourceTaskType);
        if (!StringUtils.equals(resolvedTaskType, sourceTaskType)) {
            throw new BizException("Knowledge graph regenerate task type does not match source task");
        }
        Boolean replaceUnconfirmedOnly =
                commandReplaceUnconfirmedOnly == null ? Boolean.TRUE : commandReplaceUnconfirmedOnly;
        String requestId = nextEventId("graph-regenerate");
        String traceId = nextEventId("graph-trace");
        return requestTasks(
                resolvedTaskType,
                sourceTask.getScopeType(),
                sourceTask.getScopeJson(),
                StringUtils.defaultIfBlank(commandTriggerSource, TRIGGER_SOURCE_REGENERATE),
                StringUtils.defaultIfBlank(commandSelectionScopeJson, sourceTask.getSelectionScopeJson()),
                replaceUnconfirmedOnly,
                sourceTaskId == null ? null : sourceTaskId.value(),
                sourceTask.getSourceContentType(),
                GraphExtractionSourceContentIdCodec.toValue(sourceTask.getSourceContentId()),
                commandRequestedBy == null
                        ? GraphExtractionRequesterIdCodec.toValue(sourceTask.getRequestedBy())
                        : commandRequestedBy,
                null,
                null,
                GraphExtractionModelIdCodec.toValue(sourceTask.getModelId()),
                GraphExtractionModelNameCodec.toValue(sourceTask.getModelName()),
                GraphExtractionPromptVersionIdCodec.toValue(sourceTask.getPromptVersionId()),
                requestId,
                traceId,
                sourceTask.getPromptMessagesJson(),
                sourceTask.getPromptVariablesJson(),
                sourceTask.getPromptHash(),
                sourceTask.getInputPayloadJson(),
                sourceTask.getOutputSchemaJson(),
                Boolean.TRUE.equals(sourceTask.getForceJson()),
                StringUtils.defaultIfBlank(sourceTask.getLocale(), "zh-CN"),
                resolveOperation(resolvedTaskType));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionBatchCancelResult cancelBatch(Long batchJobId, Long requestedBy) {
        if (batchJobId == null) {
            throw new BizException("Knowledge graph batchJobId is required");
        }
        if (aiFacade == null) {
            throw new BizException("AI batch job service is not ready");
        }
        List<GraphExtractionTask> tasks =
                repository.listByBatchJobId(GraphExtractionBatchJobIdCodec.toDomain(batchJobId));
        if (tasks.isEmpty()) {
            throw new BizException("Knowledge graph batch task not found: " + batchJobId);
        }
        Instant cancelledAt = Instant.now();
        for (GraphExtractionTask task : tasks) {
            if (!isBatchChildTask(task) || !GraphExtractionTaskStatus.REQUESTED.equals(task.getStatus())) {
                continue;
            }
            task.setStatus(GraphExtractionTaskStatus.CANCELLED);
            task.setRequestedBy(
                    requestedBy == null
                            ? task.getRequestedBy()
                            : GraphExtractionRequesterIdCodec.toDomain(requestedBy));
            task.setCompletedAt(cancelledAt);
            repository.update(task);
        }
        AiBatchJobFacadeResponse batchResult = aiFacade.cancelBatchJob(batchJobId);
        for (GraphExtractionTask task : tasks) {
            if (!isBatchParentTask(task)) {
                continue;
            }
            task.setStatus(
                    GraphExtractionTaskStatus.from(batchResult == null ? STATUS_CANCELLED : batchResult.getStatus()));
            task.setCompletedAt(resolveBatchCompletedAt(batchResult, cancelledAt));
            repository.update(task);
            break;
        }
        return toBatchCancelResult(batchJobId, batchResult);
    }

    @Override
    public PageResult<GraphVersionResult> pageVersions(
            String taskType, String status, String sourceContentType, Long sourceContentId, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<GraphVersion> versionPage = graphVersionRepository.page(
                GraphExtractionTaskType.from(taskType),
                GraphVersionStatus.from(status),
                sourceContentType,
                GraphExtractionSourceContentIdCodec.toDomain(sourceContentId),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        return PageResult.of(
                versionPage.getPageNo(),
                versionPage.getPageSize(),
                versionPage.getTotalCount(),
                versionPage.getRecords().stream()
                        .map(this::toGraphVersionResult)
                        .toList());
    }

    @Override
    public GraphVersionResult getVersionDetail(Long versionId) {
        GraphVersion version = graphVersionRepository.getByVersionId(GraphVersionIdCodec.toDomain(versionId));
        if (version == null) {
            throw new BizException("Graph version not found: " + versionId);
        }
        return toGraphVersionResult(version);
    }

    @Override
    public PageResult<KnowledgeEntityResult> pageEntities(
            Long versionId, String keyword, String entityType, String confirmationStatus, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<KnowledgeEntity> entityPage = knowledgeEntityRepository.page(
                GraphVersionIdCodec.toDomain(versionId),
                keyword,
                entityType,
                KnowledgeConfirmationStatus.from(confirmationStatus),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        return PageResult.of(
                entityPage.getPageNo(),
                entityPage.getPageSize(),
                entityPage.getTotalCount(),
                entityPage.getRecords().stream()
                        .map(this::toKnowledgeEntityResult)
                        .toList());
    }

    @Override
    public KnowledgeEntityResult getEntityDetail(Long entityId) {
        KnowledgeEntity entity = knowledgeEntityRepository.getByEntityId(KnowledgeEntityIdCodec.toDomain(entityId));
        if (entity == null) {
            throw new BizException("Knowledge entity not found: " + entityId);
        }
        return toKnowledgeEntityResult(entity);
    }

    @Override
    public PageResult<KnowledgeRelationResult> pageRelations(
            Long versionId, String keyword, String relationType, String confirmationStatus, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<KnowledgeRelation> relationPage = knowledgeRelationRepository.page(
                versionId,
                keyword,
                relationType,
                confirmationStatus,
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        return PageResult.of(
                relationPage.getPageNo(),
                relationPage.getPageSize(),
                relationPage.getTotalCount(),
                relationPage.getRecords().stream()
                        .map(this::toKnowledgeRelationResult)
                        .toList());
    }

    @Override
    public KnowledgeRelationResult getRelationDetail(Long relationId) {
        KnowledgeRelation relation = knowledgeRelationRepository.getByRelationId(relationId);
        if (relation == null) {
            throw new BizException("Knowledge relation not found: " + relationId);
        }
        return toKnowledgeRelationResult(relation);
    }

    @Override
    public PageResult<KnowledgeLineageNodeResult> pageLineageNodes(
            Long versionId, String keyword, String nodeType, String confirmationStatus, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<KnowledgeLineageNode> nodePage = knowledgeLineageNodeRepository.page(
                versionId,
                keyword,
                nodeType,
                confirmationStatus,
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        return PageResult.of(
                nodePage.getPageNo(),
                nodePage.getPageSize(),
                nodePage.getTotalCount(),
                nodePage.getRecords().stream()
                        .map(this::toKnowledgeLineageNodeResult)
                        .toList());
    }

    @Override
    public KnowledgeLineageNodeResult getLineageNodeDetail(Long nodeId) {
        KnowledgeLineageNode node = knowledgeLineageNodeRepository.getByNodeId(nodeId);
        if (node == null) {
            throw new BizException("Knowledge lineage node not found: " + nodeId);
        }
        return toKnowledgeLineageNodeResult(node);
    }

    @Override
    public PageResult<KnowledgeLineageRelationResult> pageLineageRelations(
            Long versionId, String keyword, String relationType, String confirmationStatus, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<KnowledgeLineageRelation> relationPage = knowledgeLineageRelationRepository.page(
                versionId,
                keyword,
                relationType,
                confirmationStatus,
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        return PageResult.of(
                relationPage.getPageNo(),
                relationPage.getPageSize(),
                relationPage.getTotalCount(),
                relationPage.getRecords().stream()
                        .map(this::toKnowledgeLineageRelationResult)
                        .toList());
    }

    @Override
    public KnowledgeLineageRelationResult getLineageRelationDetail(Long relationId) {
        KnowledgeLineageRelation relation = knowledgeLineageRelationRepository.getByRelationId(relationId);
        if (relation == null) {
            throw new BizException("Knowledge lineage relation not found: " + relationId);
        }
        return toKnowledgeLineageRelationResult(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult applyTaskCandidate(GraphExtractionTaskId taskId, String applyMode) {
        GraphExtractionTask task = repository.getByTaskId(taskId);
        if (task == null) {
            throw new BizException("Graph extraction task not found: " + (taskId == null ? null : taskId.value()));
        }
        if (aiFacade == null || candidateApplySupport == null) {
            throw new BizException("Knowledge graph candidate apply support is not ready");
        }
        if (task.getAiCandidateId() == null) {
            throw new BizException("Knowledge graph extraction task has no AI candidate");
        }
        AiCandidateFacadeDto candidate =
                aiFacade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                        .candidateId(GraphExtractionAiCandidateIdCodec.toValue(task.getAiCandidateId()))
                        .contentType(task.getSourceContentType())
                        .contentId(GraphExtractionSourceContentIdCodec.toValue(task.getSourceContentId()))
                        .capability(resolveCapability(taskTypeValue(task)))
                        .build());
        candidateApplySupport.apply(task, candidate, applyMode);
        aiFacade.markCandidateApplied(MarkAiCandidateAppliedFacadeRequest.builder()
                .candidateId(candidate.getCandidateId())
                .resultFormat(candidate.getResultFormat())
                .resultPayload(candidate.getResultPayload())
                .appliedAt(Instant.now())
                .build());
        task.setStatus(GraphExtractionTaskStatus.APPLIED);
        task.setAppliedAt(Instant.now());
        repository.update(task);
        return syncTaskResult(task);
    }

    private GraphExtractionTaskResult requestTasks(
            String taskType,
            String scopeType,
            String scopeJson,
            String triggerSource,
            String selectionScopeJson,
            Boolean replaceUnconfirmedOnly,
            Long parentTaskId,
            String sourceContentType,
            Long sourceContentId,
            Long requestedBy,
            Long serviceId,
            String serviceRole,
            Long modelId,
            String modelName,
            Long promptVersionId,
            String requestId,
            String traceId,
            String promptMessagesJson,
            String promptVariablesJson,
            String promptHash,
            String inputPayloadJson,
            String outputSchemaJson,
            boolean forceJson,
            String locale,
            KnowledgeInvokeOperation operation) {
        List<ExtractionTarget> targets = resolveTargets(sourceContentId, scopeJson, selectionScopeJson);
        if (targets.size() <= 1) {
            ExtractionTarget target = targets.get(0);
            validateTarget(sourceContentType, target.sourceContentId());
            GraphExtractionTask task = buildTask(
                    null,
                    GraphExtractionTaskIdCodec.toDomain(parentTaskId),
                    taskType,
                    scopeType,
                    target.scopeJson(),
                    triggerSource,
                    selectionScopeJson,
                    replaceUnconfirmedOnly,
                    sourceContentType,
                    target.sourceContentId(),
                    requestedBy);
            fillRequestSnapshot(
                    task,
                    toAiRequest(
                            taskType,
                            scopeType,
                            target.scopeJson(),
                            sourceContentType,
                            target.sourceContentId(),
                            requestedBy,
                            serviceId,
                            serviceRole,
                            modelId,
                            modelName,
                            promptVersionId,
                            requestId,
                            traceId,
                            promptMessagesJson,
                            promptVariablesJson,
                            promptHash,
                            inputPayloadJson,
                            outputSchemaJson,
                            forceJson,
                            locale));
            GraphExtractionTaskId taskId = repository.save(task);
            task.setId(taskId);
            scheduleTaskExecution(taskId);
            return toResult(task);
        }
        if (aiFacade == null) {
            throw new BizException("AI batch job service is not ready");
        }
        AiBatchJobActionFacadeResponse batchAction = aiFacade.createBatchJob(CreateAiBatchJobFacadeRequest.builder()
                .scope(StringUtils.defaultIfBlank(selectionScopeJson, scopeJson))
                .capability(resolveCapability(taskType))
                .contentType(sourceContentType)
                .totalCount(targets.size())
                .failureSummaryJson(null)
                .build());
        Long batchJobId = batchAction == null ? null : batchAction.getBatchId();
        GraphExtractionTask parentTask = new GraphExtractionTask();
        parentTask.setBatchJobId(GraphExtractionBatchJobIdCodec.toDomain(batchJobId));
        parentTask.setTaskType(GraphExtractionTaskType.from(taskType));
        parentTask.setScopeType(scopeType);
        parentTask.setScopeJson(scopeJson);
        parentTask.setTriggerSource(triggerSource);
        parentTask.setSelectionScopeJson(selectionScopeJson);
        parentTask.setReplaceUnconfirmedOnly(replaceUnconfirmedOnly);
        parentTask.setParentTaskId(GraphExtractionTaskIdCodec.toDomain(parentTaskId));
        parentTask.setSourceContentType(sourceContentType);
        parentTask.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(sourceContentId));
        parentTask.setRequestedBy(GraphExtractionRequesterIdCodec.toDomain(requestedBy));
        parentTask.setStatus(GraphExtractionTaskStatus.REQUESTED);
        parentTask.setRequestedAt(Instant.now());
        fillRequestSnapshot(
                parentTask,
                modelId,
                modelName,
                promptVersionId,
                requestId,
                traceId,
                promptMessagesJson,
                promptVariablesJson,
                promptHash,
                inputPayloadJson,
                outputSchemaJson,
                forceJson,
                locale);
        GraphExtractionTaskId parentId = repository.save(parentTask);
        parentTask.setId(parentId);
        for (ExtractionTarget target : targets) {
            validateTarget(sourceContentType, target.sourceContentId());
            GraphExtractionTask childTask = buildTask(
                    batchJobId,
                    parentId,
                    taskType,
                    scopeType,
                    target.scopeJson(),
                    triggerSource,
                    selectionScopeJson,
                    replaceUnconfirmedOnly,
                    sourceContentType,
                    target.sourceContentId(),
                    requestedBy);
            GraphExtractionTaskId childId = repository.save(childTask);
            childTask.setId(childId);
            if (!aiFacade.canDispatchNextBatchUnit(batchJobId)) {
                childTask.setStatus(GraphExtractionTaskStatus.CANCELLED);
                childTask.setCompletedAt(Instant.now());
                repository.update(childTask);
                continue;
            }
            requestTask(
                    batchJobId,
                    parentId,
                    taskType,
                    scopeType,
                    target.scopeJson(),
                    triggerSource,
                    selectionScopeJson,
                    replaceUnconfirmedOnly,
                    null,
                    sourceContentType,
                    target.sourceContentId(),
                    requestedBy,
                    toAiRequest(
                            taskType,
                            scopeType,
                            target.scopeJson(),
                            sourceContentType,
                            target.sourceContentId(),
                            requestedBy,
                            serviceId,
                            serviceRole,
                            modelId,
                            modelName,
                            promptVersionId,
                            requestId,
                            traceId,
                            promptMessagesJson,
                            promptVariablesJson,
                            promptHash,
                            inputPayloadJson,
                            outputSchemaJson,
                            forceJson,
                            locale),
                    operation,
                    false,
                    childTask);
        }
        AiBatchJobFacadeResponse batchResult = aiFacade.getBatchJob(batchJobId);
        parentTask.setStatus(
                GraphExtractionTaskStatus.from(batchResult == null ? STATUS_REQUESTED : batchResult.getStatus()));
        if (batchResult != null && batchResult.getCompletedAt() != null) {
            parentTask.setCompletedAt(batchResult.getCompletedAt());
        } else if (GraphExtractionTaskStatus.CANCELLED.equals(parentTask.getStatus())) {
            parentTask.setCompletedAt(Instant.now());
        }
        repository.update(parentTask);
        return syncTaskResult(parentTask);
    }

    private GraphExtractionTaskResult requestTask(
            Long batchJobId,
            GraphExtractionTaskId resolvedParentTaskId,
            String taskType,
            String scopeType,
            String scopeJson,
            String triggerSource,
            String selectionScopeJson,
            Boolean replaceUnconfirmedOnly,
            Long parentTaskId,
            String sourceContentType,
            Long sourceContentId,
            Long requestedBy,
            KnowledgeAiExtractionFacadeRequest aiRequest,
            KnowledgeInvokeOperation operation,
            boolean rethrowOnFailure) {
        return requestTask(
                batchJobId,
                resolvedParentTaskId,
                taskType,
                scopeType,
                scopeJson,
                triggerSource,
                selectionScopeJson,
                replaceUnconfirmedOnly,
                parentTaskId,
                sourceContentType,
                sourceContentId,
                requestedBy,
                aiRequest,
                operation,
                rethrowOnFailure,
                null);
    }

    private GraphExtractionTaskResult requestTask(
            Long batchJobId,
            GraphExtractionTaskId resolvedParentTaskId,
            String taskType,
            String scopeType,
            String scopeJson,
            String triggerSource,
            String selectionScopeJson,
            Boolean replaceUnconfirmedOnly,
            Long parentTaskId,
            String sourceContentType,
            Long sourceContentId,
            Long requestedBy,
            KnowledgeAiExtractionFacadeRequest aiRequest,
            KnowledgeInvokeOperation operation,
            boolean rethrowOnFailure,
            GraphExtractionTask preparedTask) {
        GraphExtractionTask task = preparedTask == null
                ? buildTask(
                        batchJobId,
                        resolvedParentTaskId == null
                                ? GraphExtractionTaskIdCodec.toDomain(parentTaskId)
                                : resolvedParentTaskId,
                        taskType,
                        scopeType,
                        scopeJson,
                        triggerSource,
                        selectionScopeJson,
                        replaceUnconfirmedOnly,
                        sourceContentType,
                        sourceContentId,
                        requestedBy)
                : preparedTask;
        fillRequestSnapshot(task, aiRequest);
        if (task.getId() == null) {
            GraphExtractionTaskId taskId = repository.save(task);
            task.setId(taskId);
        }
        try {
            executeTask(task, aiRequest, operation);
            updateBatchOnTaskFinished(batchJobId, task);
            return toResult(task);
        } catch (RuntimeException ex) {
            task.setStatus(GraphExtractionTaskStatus.FAILED);
            task.setErrorType("KNOWLEDGE_AI_EXTRACTION_FAILED");
            task.setErrorMessage(ex.getMessage());
            task.setCompletedAt(Instant.now());
            repository.update(task);
            updateBatchOnTaskFinished(batchJobId, task);
            if (rethrowOnFailure) {
                throw ex;
            }
            return toResult(task);
        }
    }

    private GraphExtractionTask executeRequestedTask(GraphExtractionTask task, boolean rethrowOnFailure) {
        if (task == null || !GraphExtractionTaskStatus.REQUESTED.equals(task.getStatus())) {
            return task;
        }
        try {
            KnowledgeAiExtractionFacadeRequest aiRequest = toAiRequest(task);
            executeTask(task, aiRequest, resolveOperation(taskTypeValue(task)));
            updateBatchOnTaskFinished(GraphExtractionBatchJobIdCodec.toValue(task.getBatchJobId()), task);
        } catch (RuntimeException ex) {
            task.setStatus(GraphExtractionTaskStatus.FAILED);
            task.setErrorType("KNOWLEDGE_AI_EXTRACTION_FAILED");
            task.setErrorMessage(ex.getMessage());
            task.setCompletedAt(Instant.now());
            repository.update(task);
            updateBatchOnTaskFinished(GraphExtractionBatchJobIdCodec.toValue(task.getBatchJobId()), task);
            if (rethrowOnFailure) {
                throw ex;
            }
        }
        return task;
    }

    private void scheduleTaskExecution(GraphExtractionTaskId taskId) {
        if (taskExecutor == null || transactionTemplate == null || taskId == null) {
            return;
        }
        Runnable submittedTask = () -> taskExecutor.execute(() -> executeScheduledTask(taskId));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            submittedTask.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                submittedTask.run();
            }
        });
    }

    private void executeScheduledTask(GraphExtractionTaskId taskId) {
        GraphExtractionTask task = transactionTemplate.execute(status -> {
            GraphExtractionTask latestTask = repository.getByTaskId(taskId);
            if (latestTask == null
                    || !GraphExtractionTaskStatus.REQUESTED.equals(latestTask.getStatus())
                    || latestTask.getAiCallId() != null
                    || latestTask.getAiCandidateId() != null) {
                return null;
            }
            latestTask.setStatus(GraphExtractionTaskStatus.RUNNING);
            repository.update(latestTask);
            return latestTask;
        });
        if (task == null) {
            return;
        }
        try {
            KnowledgeAiExtractionFacadeResponse result =
                    resolveOperation(taskTypeValue(task)).invoke(toAiRequest(task));
            task.setAiCallId(GraphExtractionAiCallIdCodec.toDomain(result == null ? null : result.getCallId()));
            task.setAiCandidateId(
                    GraphExtractionAiCandidateIdCodec.toDomain(result == null ? null : result.getCandidateId()));
            task.setStatus(
                    result != null && STATUS_SUCCEEDED.equals(result.getStatus())
                            ? GraphExtractionTaskStatus.SUCCEEDED
                            : GraphExtractionTaskStatus.FAILED);
            task.setErrorType(result == null ? "KNOWLEDGE_AI_EMPTY_RESULT" : result.getErrorType());
            task.setErrorMessage(
                    result == null ? "Knowledge AI extraction returned empty result" : result.getErrorMessage());
        } catch (RuntimeException ex) {
            task.setStatus(GraphExtractionTaskStatus.FAILED);
            task.setErrorType("KNOWLEDGE_AI_EXTRACTION_FAILED");
            task.setErrorMessage(ex.getMessage());
        }
        task.setCompletedAt(Instant.now());
        transactionTemplate.executeWithoutResult(status -> {
            repository.update(task);
            updateBatchOnTaskFinished(GraphExtractionBatchJobIdCodec.toValue(task.getBatchJobId()), task);
        });
    }

    private void executeTask(
            GraphExtractionTask task,
            KnowledgeAiExtractionFacadeRequest aiRequest,
            KnowledgeInvokeOperation operation) {
        task.setStatus(GraphExtractionTaskStatus.RUNNING);
        repository.update(task);
        KnowledgeAiExtractionFacadeResponse result = operation.invoke(aiRequest);
        task.setAiCallId(GraphExtractionAiCallIdCodec.toDomain(result == null ? null : result.getCallId()));
        task.setAiCandidateId(
                GraphExtractionAiCandidateIdCodec.toDomain(result == null ? null : result.getCandidateId()));
        task.setStatus(
                result != null && STATUS_SUCCEEDED.equals(result.getStatus())
                        ? GraphExtractionTaskStatus.SUCCEEDED
                        : GraphExtractionTaskStatus.FAILED);
        task.setErrorType(result == null ? "KNOWLEDGE_AI_EMPTY_RESULT" : result.getErrorType());
        task.setErrorMessage(
                result == null ? "Knowledge AI extraction returned empty result" : result.getErrorMessage());
        task.setCompletedAt(Instant.now());
        repository.update(task);
    }

    private GraphExtractionTask buildTask(
            Long batchJobId,
            GraphExtractionTaskId parentTaskId,
            String taskType,
            String scopeType,
            String scopeJson,
            String triggerSource,
            String selectionScopeJson,
            Boolean replaceUnconfirmedOnly,
            String sourceContentType,
            Long sourceContentId,
            Long requestedBy) {
        GraphExtractionTask task = new GraphExtractionTask();
        task.setBatchJobId(GraphExtractionBatchJobIdCodec.toDomain(batchJobId));
        task.setTaskType(GraphExtractionTaskType.from(taskType));
        task.setScopeType(scopeType);
        task.setScopeJson(scopeJson);
        task.setTriggerSource(triggerSource);
        task.setSelectionScopeJson(selectionScopeJson);
        task.setReplaceUnconfirmedOnly(replaceUnconfirmedOnly);
        task.setParentTaskId(parentTaskId);
        task.setSourceContentType(sourceContentType);
        task.setSourceContentId(GraphExtractionSourceContentIdCodec.toDomain(sourceContentId));
        task.setRequestedBy(GraphExtractionRequesterIdCodec.toDomain(requestedBy));
        task.setStatus(GraphExtractionTaskStatus.REQUESTED);
        task.setRequestedAt(Instant.now());
        return task;
    }

    private void fillRequestSnapshot(GraphExtractionTask task, KnowledgeAiExtractionFacadeRequest request) {
        if (task == null || request == null) {
            return;
        }
        fillRequestSnapshot(
                task,
                request.getModelId(),
                request.getModelName(),
                request.getPromptVersionId(),
                request.getRequestId(),
                request.getTraceId(),
                request.getPromptMessagesJson(),
                request.getPromptVariablesJson(),
                request.getPromptHash(),
                request.getInputPayloadJson(),
                request.getOutputSchemaJson(),
                request.isForceJson(),
                request.getLocale());
    }

    private void fillRequestSnapshot(
            GraphExtractionTask task,
            Long modelId,
            String modelName,
            Long promptVersionId,
            String requestId,
            String traceId,
            String promptMessagesJson,
            String promptVariablesJson,
            String promptHash,
            String inputPayloadJson,
            String outputSchemaJson,
            boolean forceJson,
            String locale) {
        if (task == null) {
            return;
        }
        task.setModelId(GraphExtractionModelIdCodec.toDomain(modelId));
        task.setModelName(GraphExtractionModelNameCodec.toDomain(modelName));
        task.setPromptVersionId(GraphExtractionPromptVersionIdCodec.toDomain(promptVersionId));
        task.setRequestId(GraphExtractionRequestIdCodec.toDomain(requestId));
        task.setTraceId(GraphExtractionTraceIdCodec.toDomain(traceId));
        task.setPromptMessagesJson(promptMessagesJson);
        task.setPromptVariablesJson(promptVariablesJson);
        task.setPromptHash(promptHash);
        task.setInputPayloadJson(inputPayloadJson);
        task.setOutputSchemaJson(outputSchemaJson);
        task.setForceJson(forceJson);
        task.setLocale(locale);
    }

    private KnowledgeAiExtractionFacadeRequest toAiRequest(
            String taskType,
            String scopeType,
            String scopeJson,
            String sourceContentType,
            Long sourceContentId,
            Long requestedBy,
            Long serviceId,
            String serviceRole,
            Long modelId,
            String modelName,
            Long promptVersionId,
            String requestId,
            String traceId,
            String promptMessagesJson,
            String promptVariablesJson,
            String promptHash,
            String inputPayloadJson,
            String outputSchemaJson,
            boolean forceJson,
            String locale) {
        return KnowledgeAiExtractionFacadeRequest.builder()
                .taskType(taskType)
                .scopeType(scopeType)
                .scopeJson(scopeJson)
                .sourceContentType(sourceContentType)
                .sourceContentId(sourceContentId)
                .requestedBy(requestedBy)
                .serviceId(serviceId)
                .serviceRole(serviceRole)
                .modelId(modelId)
                .modelName(modelName)
                .promptVersionId(promptVersionId)
                .requestId(requestId)
                .traceId(traceId)
                .promptMessagesJson(promptMessagesJson)
                .promptVariablesJson(promptVariablesJson)
                .promptHash(promptHash)
                .inputPayloadJson(inputPayloadJson)
                .outputSchemaJson(outputSchemaJson)
                .forceJson(forceJson)
                .locale(locale)
                .build();
    }

    private KnowledgeAiExtractionFacadeRequest toAiRequest(GraphExtractionTask task) {
        return KnowledgeAiExtractionFacadeRequest.builder()
                .taskType(taskTypeValue(task))
                .scopeType(task.getScopeType())
                .scopeJson(task.getScopeJson())
                .sourceContentType(task.getSourceContentType())
                .sourceContentId(GraphExtractionSourceContentIdCodec.toValue(task.getSourceContentId()))
                .requestedBy(GraphExtractionRequesterIdCodec.toValue(task.getRequestedBy()))
                .modelId(GraphExtractionModelIdCodec.toValue(task.getModelId()))
                .modelName(GraphExtractionModelNameCodec.toValue(task.getModelName()))
                .promptVersionId(GraphExtractionPromptVersionIdCodec.toValue(task.getPromptVersionId()))
                .requestId(GraphExtractionRequestIdCodec.toValue(task.getRequestId()))
                .traceId(GraphExtractionTraceIdCodec.toValue(task.getTraceId()))
                .promptMessagesJson(task.getPromptMessagesJson())
                .promptVariablesJson(task.getPromptVariablesJson())
                .promptHash(task.getPromptHash())
                .inputPayloadJson(task.getInputPayloadJson())
                .outputSchemaJson(task.getOutputSchemaJson())
                .forceJson(Boolean.TRUE.equals(task.getForceJson()))
                .locale(task.getLocale())
                .build();
    }

    private GraphExtractionTaskResult toResult(GraphExtractionTask task) {
        if (task == null) {
            return null;
        }
        return new GraphExtractionTaskResult(
                task.getId() == null ? null : String.valueOf(task.getId().value()),
                GraphExtractionBatchJobIdCodec.toValue(task.getBatchJobId()),
                taskTypeValue(task),
                task.getScopeType(),
                task.getScopeJson(),
                task.getTriggerSource(),
                task.getSelectionScopeJson(),
                task.getReplaceUnconfirmedOnly(),
                task.getParentTaskId() == null ? null : task.getParentTaskId().value(),
                task.getSourceContentType(),
                GraphExtractionSourceContentIdCodec.toValue(task.getSourceContentId()),
                GraphExtractionAiCallIdCodec.toValue(task.getAiCallId()),
                GraphExtractionAiCandidateIdCodec.toValue(task.getAiCandidateId()),
                statusValue(task),
                task.getErrorType(),
                task.getErrorMessage(),
                GraphExtractionRequesterIdCodec.toValue(task.getRequestedBy()),
                timeValue(task.getRequestedAt()),
                timeValue(task.getCompletedAt()),
                timeValue(task.getAppliedAt()));
    }

    private GraphExtractionBatchCancelResult toBatchCancelResult(
            Long batchJobId, AiBatchJobFacadeResponse batchResult) {
        if (batchResult == null) {
            return new GraphExtractionBatchCancelResult(batchJobId, STATUS_CANCELLED, 0, 0, 0);
        }
        return new GraphExtractionBatchCancelResult(
                batchResult.getBatchId(),
                batchResult.getStatus(),
                batchResult.getCancelledCount(),
                batchResult.getSuccessCount(),
                batchResult.getFailedCount());
    }

    private GraphExtractionTaskResult syncTaskResult(GraphExtractionTask task) {
        GraphExtractionTaskResult result = toResult(task);
        if (result == null || aiFacade == null) {
            return result;
        }
        AiInvocationLogFacadeDto invocationLog = task == null || task.getAiCallId() == null
                ? null
                : aiFacade.getInvocationLog(GetAiInvocationLogFacadeRequest.builder()
                        .callId(GraphExtractionAiCallIdCodec.toValue(task.getAiCallId()))
                        .build());
        AiCandidateFacadeDto candidate = task == null || task.getAiCandidateId() == null
                ? null
                : aiFacade.getCandidate(GetAiCandidateFacadeRequest.builder()
                        .candidateId(GraphExtractionAiCandidateIdCodec.toValue(task.getAiCandidateId()))
                        .build());
        if (invocationLog != null) {
            result.setAiCallId(invocationLog.getCallId());
            if (result.getCompletedAt() == null && invocationLog.getCompletedAt() != null) {
                result.setCompletedAt(invocationLog.getCompletedAt().toEpochMilli());
            }
            if (STATUS_FAILED.equals(invocationLog.getStatus())) {
                result.setStatus(STATUS_FAILED);
                result.setErrorType(invocationLog.getErrorType());
                result.setErrorMessage(invocationLog.getErrorMessage());
            } else if (STATUS_REQUESTED.equals(result.getStatus())
                    && STATUS_SUCCEEDED.equals(invocationLog.getStatus())) {
                result.setStatus(STATUS_SUCCEEDED);
            }
        }
        if (candidate != null) {
            result.setAiCandidateId(candidate.getCandidateId());
            if (result.getAiCallId() == null) {
                result.setAiCallId(candidate.getCallId());
            }
            if (candidate.getAppliedAt() != null) {
                result.setAppliedAt(candidate.getAppliedAt().toEpochMilli());
                result.setStatus(STATUS_APPLIED);
            }
            if ("REJECTED".equals(candidate.getStatus())) {
                result.setStatus(STATUS_FAILED);
            }
            if (StringUtils.isBlank(result.getErrorType())) {
                result.setErrorType(candidate.getErrorType());
            }
            if (StringUtils.isBlank(result.getErrorMessage())) {
                result.setErrorMessage(candidate.getErrorMessage());
            }
        }
        return result;
    }

    private GraphVersionResult toGraphVersionResult(GraphVersion version) {
        if (version == null) {
            return null;
        }
        RefinementTask lastAppliedRefinement = refinementTaskRepository == null
                ? null
                : refinementTaskRepository.findLatestAppliedByGraphVersionId(
                        GraphVersionIdCodec.toValue(version.getId()));
        return new GraphVersionResult(
                GraphVersionIdCodec.toValue(version.getId()),
                version.getTaskId() == null
                        ? null
                        : String.valueOf(version.getTaskId().value()),
                GraphExtractionAiCandidateIdCodec.toValue(version.getCandidateId()),
                version.getTaskType() == null ? null : version.getTaskType().value(),
                version.getSourceContentType(),
                GraphExtractionSourceContentIdCodec.toValue(version.getSourceContentId()),
                version.getVersionNo(),
                version.getStatus() == null ? null : version.getStatus().value(),
                version.getAppliedAt() == null ? null : version.getAppliedAt().toEpochMilli(),
                lastAppliedRefinement != null,
                lastAppliedRefinement == null || lastAppliedRefinement.getRefinementTaskId() == null
                        ? null
                        : lastAppliedRefinement.getRefinementTaskId().value(),
                lastAppliedRefinement == null || lastAppliedRefinement.getAppliedAt() == null
                        ? null
                        : lastAppliedRefinement.getAppliedAt().toEpochMilli());
    }

    private KnowledgeEntityResult toKnowledgeEntityResult(KnowledgeEntity entity) {
        if (entity == null) {
            return null;
        }
        return new KnowledgeEntityResult(
                KnowledgeEntityIdCodec.toValue(entity.getId()),
                entity.getEntityKey(),
                entity.getName(),
                entity.getEntityType(),
                entity.getDescription(),
                entity.getConfirmationStatus() == null
                        ? null
                        : entity.getConfirmationStatus().value(),
                GraphVersionIdCodec.toValue(entity.getLatestVersionId()),
                entity.getSourceRefsJson(),
                timeValue(entity.getFirstExtractedAt()),
                timeValue(entity.getLastExtractedAt()),
                timeValue(entity.getConfirmedAt()));
    }

    private KnowledgeRelationResult toKnowledgeRelationResult(KnowledgeRelation relation) {
        if (relation == null) {
            return null;
        }
        KnowledgeEntity sourceEntity = knowledgeEntityRepository.getByEntityKey(relation.getSourceEntityKey());
        KnowledgeEntity targetEntity = knowledgeEntityRepository.getByEntityKey(relation.getTargetEntityKey());
        return new KnowledgeRelationResult(
                relation.getId(),
                relation.getRelationKey(),
                relation.getSourceName(),
                sourceEntity == null ? null : sourceEntity.getEntityType(),
                relation.getTargetName(),
                targetEntity == null ? null : targetEntity.getEntityType(),
                relation.getRelationType(),
                relation.getEvidence(),
                relation.getConfirmationStatus(),
                relation.getLatestVersionId(),
                relation.getSourceRefsJson(),
                timeValue(relation.getFirstExtractedAt()),
                timeValue(relation.getLastExtractedAt()),
                timeValue(relation.getConfirmedAt()));
    }

    private KnowledgeLineageNodeResult toKnowledgeLineageNodeResult(KnowledgeLineageNode node) {
        if (node == null) {
            return null;
        }
        return new KnowledgeLineageNodeResult(
                node.getId(),
                node.getNodeKey(),
                node.getName(),
                node.getNodeType(),
                node.getGeneration(),
                node.getGender(),
                node.getConfirmationStatus(),
                node.getLatestVersionId(),
                node.getSourceRefsJson(),
                timeValue(node.getFirstExtractedAt()),
                timeValue(node.getLastExtractedAt()),
                timeValue(node.getConfirmedAt()));
    }

    private KnowledgeLineageRelationResult toKnowledgeLineageRelationResult(KnowledgeLineageRelation relation) {
        if (relation == null) {
            return null;
        }
        return new KnowledgeLineageRelationResult(
                relation.getId(),
                relation.getRelationKey(),
                relation.getSourceName(),
                relation.getTargetName(),
                relation.getRelationType(),
                relation.getEvidence(),
                relation.getConfirmationStatus(),
                relation.getLatestVersionId(),
                relation.getSourceRefsJson(),
                timeValue(relation.getFirstExtractedAt()),
                timeValue(relation.getLastExtractedAt()),
                timeValue(relation.getConfirmedAt()));
    }

    private Long timeValue(Instant value) {
        return value == null ? null : value.toEpochMilli();
    }

    private String taskTypeValue(GraphExtractionTask task) {
        return task == null || task.getTaskType() == null
                ? null
                : task.getTaskType().value();
    }

    private String statusValue(GraphExtractionTask task) {
        return task == null || task.getStatus() == null
                ? null
                : task.getStatus().value();
    }

    private String resolveCapability(String taskType) {
        return switch (taskType) {
            case TASK_TYPE_RELATION -> CAPABILITY_KNOWLEDGE_RELATION_EXTRACT;
            case TASK_TYPE_GRAPH -> CAPABILITY_KNOWLEDGE_GRAPH_EXTRACT;
            case TASK_TYPE_LINEAGE -> CAPABILITY_KNOWLEDGE_LINEAGE_EXTRACT;
            default -> throw new BizException("Unsupported knowledge graph extraction task type: " + taskType);
        };
    }

    private KnowledgeInvokeOperation resolveOperation(String taskType) {
        return switch (taskType) {
            case TASK_TYPE_RELATION -> aiFacade::extractKnowledgeRelations;
            case TASK_TYPE_GRAPH -> aiFacade::extractKnowledgeGraph;
            case TASK_TYPE_LINEAGE -> aiFacade::extractKnowledgeLineage;
            default -> throw new BizException("Unsupported knowledge graph extraction task type: " + taskType);
        };
    }

    private void validateRegenerateSourceTask(GraphExtractionTask sourceTask) {
        if (sourceTask == null) {
            throw new BizException("Knowledge graph source task is required");
        }
        validateCommandBase(
                sourceTask.getSourceContentType(),
                GraphExtractionModelIdCodec.toValue(sourceTask.getModelId()),
                GraphExtractionModelNameCodec.toValue(sourceTask.getModelName()),
                GraphExtractionRequestIdCodec.toValue(sourceTask.getRequestId()),
                GraphExtractionTraceIdCodec.toValue(sourceTask.getTraceId()),
                sourceTask.getPromptMessagesJson(),
                sourceTask.getInputPayloadJson());
    }

    private String nextEventId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private void validateCommandBase(
            String sourceContentType,
            Long modelId,
            String modelName,
            String requestId,
            String traceId,
            String promptMessagesJson,
            String inputPayloadJson) {
        if (StringUtils.isBlank(sourceContentType)
                || StringUtils.isBlank(requestId)
                || StringUtils.isBlank(traceId)
                || StringUtils.isBlank(promptMessagesJson)
                || StringUtils.isBlank(inputPayloadJson)) {
            throw new BizException("Knowledge graph extraction request is incomplete");
        }
    }

    private void validateTarget(String sourceContentType, Long sourceContentId) {
        if (StringUtils.isBlank(sourceContentType) || sourceContentId == null) {
            throw new BizException("Knowledge graph extraction target is incomplete");
        }
    }

    private List<ExtractionTarget> resolveTargets(Long sourceContentId, String scopeJson, String selectionScopeJson) {
        List<Long> ids = parseSelectionIds(selectionScopeJson);
        if (ids.isEmpty()) {
            return List.of(new ExtractionTarget(sourceContentId, scopeJson));
        }
        List<ExtractionTarget> targets = new ArrayList<>();
        for (Long id : ids) {
            targets.add(new ExtractionTarget(id, buildTargetScopeJson(scopeJson, id)));
        }
        return targets;
    }

    private List<Long> parseSelectionIds(String selectionScopeJson) {
        if (StringUtils.isBlank(selectionScopeJson)) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(selectionScopeJson);
            List<Long> ids = new ArrayList<>();
            collectIds(root, ids);
            return ids;
        } catch (Exception ex) {
            throw new BizException("Knowledge graph selectionScopeJson is invalid");
        }
    }

    private void collectIds(JsonNode root, List<Long> ids) {
        if (root == null || ids == null) {
            return;
        }
        if (root.isArray()) {
            for (JsonNode node : root) {
                if (node != null && node.canConvertToLong()) {
                    ids.add(node.longValue());
                } else if (node != null
                        && node.has("sourceContentId")
                        && node.get("sourceContentId").canConvertToLong()) {
                    ids.add(node.get("sourceContentId").longValue());
                }
            }
            return;
        }
        JsonNode sourceContentIds = root.get("sourceContentIds");
        if (sourceContentIds != null && sourceContentIds.isArray()) {
            for (JsonNode node : sourceContentIds) {
                if (node != null && node.canConvertToLong()) {
                    ids.add(node.longValue());
                }
            }
            return;
        }
        JsonNode contentIds = root.get("contentIds");
        if (contentIds != null && contentIds.isArray()) {
            for (JsonNode node : contentIds) {
                if (node != null && node.canConvertToLong()) {
                    ids.add(node.longValue());
                }
            }
            return;
        }
        JsonNode sourceContentIdNode = root.get("sourceContentId");
        if (sourceContentIdNode != null && sourceContentIdNode.canConvertToLong()) {
            ids.add(sourceContentIdNode.longValue());
        }
    }

    private String buildTargetScopeJson(String scopeJson, Long sourceContentId) {
        if (StringUtils.isNotBlank(scopeJson)) {
            return scopeJson;
        }
        return "{\"sourceContentId\":" + sourceContentId + "}";
    }

    private void updateBatchOnTaskFinished(Long batchJobId, GraphExtractionTask task) {
        if (batchJobId == null || aiFacade == null || task == null) {
            return;
        }
        if (GraphExtractionTaskStatus.SUCCEEDED.equals(task.getStatus())) {
            aiFacade.recordBatchSuccess(batchJobId);
            return;
        }
        if (GraphExtractionTaskStatus.FAILED.equals(task.getStatus())) {
            aiFacade.recordBatchFailure(AiBatchJobFailureFacadeRequest.builder()
                    .batchId(batchJobId)
                    .failureSummaryJson(summarizeFailure(task))
                    .build());
        }
    }

    private String summarizeFailure(GraphExtractionTask task) {
        if (task == null) {
            return null;
        }
        String type = StringUtils.defaultIfBlank(task.getErrorType(), "KNOWLEDGE_AI_EXTRACTION_FAILED");
        String message = StringUtils.defaultIfBlank(task.getErrorMessage(), "Knowledge graph extraction failed");
        return type + ": " + message;
    }

    private boolean isBatchParentTask(GraphExtractionTask task) {
        return task != null && task.getBatchJobId() != null && task.getParentTaskId() == null;
    }

    private boolean isBatchChildTask(GraphExtractionTask task) {
        return task != null && task.getBatchJobId() != null && task.getParentTaskId() != null;
    }

    private Instant resolveBatchCompletedAt(AiBatchJobFacadeResponse batchResult, Instant fallback) {
        if (batchResult == null) {
            return fallback;
        }
        if (batchResult.getCompletedAt() != null) {
            return batchResult.getCompletedAt();
        }
        if (batchResult.getCancelledAt() != null) {
            return batchResult.getCancelledAt();
        }
        return fallback;
    }

    @FunctionalInterface
    private interface KnowledgeInvokeOperation {
        KnowledgeAiExtractionFacadeResponse invoke(KnowledgeAiExtractionFacadeRequest request);
    }

    private record ExtractionTarget(Long sourceContentId, String scopeJson) {}
}
