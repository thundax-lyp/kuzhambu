package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateApplyCheck;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.domain.knowledge.service.KnowledgeAiExtractionDomainService;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.KnowledgeGraphCandidateApplySupport;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class KnowledgeGraphExtractionApplicationServiceImpl implements KnowledgeGraphExtractionApplicationService {

    private static final String TASK_TYPE_RELATION = "RELATION";
    private static final String TASK_TYPE_GRAPH = "GRAPH";
    private static final String TASK_TYPE_LINEAGE = "LINEAGE";
    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_APPLIED = "APPLIED";

    private final GraphExtractionTaskRepository repository;
    private final GraphVersionRepository graphVersionRepository;
    private final AiInvocationRepository aiInvocationRepository;
    private final KnowledgeAiExtractionDomainService knowledgeAiExtractionDomainService;
    private final AiCandidateDomainService aiCandidateDomainService;
    private final KnowledgeGraphCandidateApplySupport candidateApplySupport;

    public KnowledgeGraphExtractionApplicationServiceImpl(
            GraphExtractionTaskRepository repository,
            GraphVersionRepository graphVersionRepository,
            AiInvocationRepository aiInvocationRepository,
            KnowledgeAiExtractionDomainService knowledgeAiExtractionDomainService,
            AiCandidateDomainService aiCandidateDomainService,
            KnowledgeGraphCandidateApplySupport candidateApplySupport) {
        this.repository = repository;
        this.graphVersionRepository = graphVersionRepository;
        this.aiInvocationRepository = aiInvocationRepository;
        this.knowledgeAiExtractionDomainService = knowledgeAiExtractionDomainService;
        this.aiCandidateDomainService = aiCandidateDomainService;
        this.candidateApplySupport = candidateApplySupport;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult requestRelationExtraction(RequestRelationExtractionCommand command) {
        validateCommand(
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getInputPayloadJson());
        return requestTask(
                TASK_TYPE_RELATION,
                command == null ? null : command.getScopeType(),
                command == null ? null : command.getScopeJson(),
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getRequestedBy(),
                toAiRequest(
                        TASK_TYPE_RELATION,
                        command == null ? null : command.getScopeType(),
                        command == null ? null : command.getScopeJson(),
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
                        command == null ? null : command.getLocale()),
                knowledgeAiExtractionDomainService::extractRelations);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult requestGraphExtraction(RequestGraphExtractionCommand command) {
        validateCommand(
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getInputPayloadJson());
        return requestTask(
                TASK_TYPE_GRAPH,
                command == null ? null : command.getScopeType(),
                command == null ? null : command.getScopeJson(),
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getRequestedBy(),
                toAiRequest(
                        TASK_TYPE_GRAPH,
                        command == null ? null : command.getScopeType(),
                        command == null ? null : command.getScopeJson(),
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
                        command == null ? null : command.getLocale()),
                knowledgeAiExtractionDomainService::extractGraph);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult requestLineageExtraction(RequestLineageExtractionCommand command) {
        validateCommand(
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getModelName(),
                command == null ? null : command.getRequestId(),
                command == null ? null : command.getTraceId(),
                command == null ? null : command.getPromptMessagesJson(),
                command == null ? null : command.getInputPayloadJson());
        return requestTask(
                TASK_TYPE_LINEAGE,
                command == null ? null : command.getScopeType(),
                command == null ? null : command.getScopeJson(),
                command == null ? null : command.getSourceContentType(),
                command == null ? null : command.getSourceContentId(),
                command == null ? null : command.getRequestedBy(),
                toAiRequest(
                        TASK_TYPE_LINEAGE,
                        command == null ? null : command.getScopeType(),
                        command == null ? null : command.getScopeJson(),
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
                        command == null ? null : command.getLocale()),
                knowledgeAiExtractionDomainService::extractLineage);
    }

    @Override
    public PageResult<GraphExtractionTaskResult> pageTasks(
            String taskType, String status, String sourceContentType, Long sourceContentId, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<GraphExtractionTask> taskPage = repository.page(
                taskType,
                status,
                sourceContentType,
                sourceContentId,
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
    public PageResult<GraphVersionResult> pageVersions(
            String taskType, String status, String sourceContentType, Long sourceContentId, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<GraphVersion> versionPage = graphVersionRepository.page(
                taskType,
                status,
                sourceContentType,
                sourceContentId,
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
        GraphVersion version = graphVersionRepository.getByVersionId(versionId);
        if (version == null) {
            throw new BizException("Graph version not found: " + versionId);
        }
        return toGraphVersionResult(version);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult applyTaskCandidate(GraphExtractionTaskId taskId) {
        GraphExtractionTask task = repository.getByTaskId(taskId);
        if (task == null) {
            throw new BizException("Graph extraction task not found: " + (taskId == null ? null : taskId.value()));
        }
        if (aiCandidateDomainService == null || candidateApplySupport == null) {
            throw new BizException("Knowledge graph candidate apply support is not ready");
        }
        if (task.getAiCandidateId() == null) {
            throw new BizException("Knowledge graph extraction task has no AI candidate");
        }
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(task.getAiCandidateId());
        check.setContentType(task.getSourceContentType());
        check.setContentId(task.getSourceContentId());
        check.setCapability(resolveCapability(task.getTaskType()));
        AiCandidate candidate = aiCandidateDomainService.requirePendingForApply(check);
        candidateApplySupport.apply(task, candidate);
        aiCandidateDomainService.markApplied(
                candidate.getCandidateId(), candidate.getResultFormat(), candidate.getResultPayload(), Instant.now());
        task.setStatus(STATUS_APPLIED);
        task.setAppliedAt(new Date());
        repository.update(task);
        return syncTaskResult(task);
    }

    private GraphExtractionTaskResult requestTask(
            String taskType,
            String scopeType,
            String scopeJson,
            String sourceContentType,
            Long sourceContentId,
            Long requestedBy,
            KnowledgeAiExtractionRequest aiRequest,
            KnowledgeInvokeOperation operation) {
        GraphExtractionTask task = new GraphExtractionTask();
        task.setTaskType(taskType);
        task.setScopeType(scopeType);
        task.setScopeJson(scopeJson);
        task.setSourceContentType(sourceContentType);
        task.setSourceContentId(sourceContentId);
        task.setRequestedBy(requestedBy);
        task.setStatus(STATUS_REQUESTED);
        task.setRequestedAt(new Date());
        GraphExtractionTaskId taskId = repository.save(task);
        task.setTaskId(taskId);
        try {
            KnowledgeAiExtractionResult result = operation.invoke(aiRequest);
            task.setAiCallId(result == null ? null : result.getCallId());
            task.setAiCandidateId(result == null ? null : result.getCandidateId());
            task.setStatus(
                    result != null && STATUS_SUCCEEDED.equals(result.getStatus()) ? STATUS_SUCCEEDED : STATUS_FAILED);
            task.setErrorType(result == null ? "KNOWLEDGE_AI_EMPTY_RESULT" : result.getErrorType());
            task.setErrorMessage(
                    result == null ? "Knowledge AI extraction returned empty result" : result.getErrorMessage());
            task.setCompletedAt(new Date());
            repository.update(task);
            return toResult(task);
        } catch (RuntimeException ex) {
            task.setStatus(STATUS_FAILED);
            task.setErrorType("KNOWLEDGE_AI_EXTRACTION_FAILED");
            task.setErrorMessage(ex.getMessage());
            task.setCompletedAt(new Date());
            repository.update(task);
            throw ex;
        }
    }

    private KnowledgeAiExtractionRequest toAiRequest(
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
        return new KnowledgeAiExtractionRequest(
                taskType,
                scopeType,
                scopeJson,
                sourceContentType,
                sourceContentId,
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
                locale);
    }

    private GraphExtractionTaskResult toResult(GraphExtractionTask task) {
        if (task == null) {
            return null;
        }
        return new GraphExtractionTaskResult(
                task.getTaskId() == null
                        ? null
                        : String.valueOf(task.getTaskId().value()),
                task.getTaskType(),
                task.getScopeType(),
                task.getScopeJson(),
                task.getSourceContentType(),
                task.getSourceContentId(),
                task.getAiCallId(),
                task.getAiCandidateId(),
                task.getStatus(),
                task.getErrorType(),
                task.getErrorMessage(),
                task.getRequestedBy(),
                timeValue(task.getRequestedAt()),
                timeValue(task.getCompletedAt()),
                timeValue(task.getAppliedAt()));
    }

    private GraphExtractionTaskResult syncTaskResult(GraphExtractionTask task) {
        GraphExtractionTaskResult result = toResult(task);
        if (result == null || aiInvocationRepository == null) {
            return result;
        }
        AiCallRecord callRecord = task == null || task.getAiCallId() == null
                ? null
                : aiInvocationRepository.getCallRecord(task.getAiCallId());
        AiCandidate candidate = task == null || task.getAiCandidateId() == null
                ? null
                : aiInvocationRepository.getCandidate(task.getAiCandidateId());
        if (callRecord != null) {
            result.setAiCallId(callRecord.getCallId());
            if (result.getCompletedAt() == null && callRecord.getCompletedAt() != null) {
                result.setCompletedAt(callRecord.getCompletedAt().toEpochMilli());
            }
            if (STATUS_FAILED.equals(callRecord.getStatus())) {
                result.setStatus(STATUS_FAILED);
                result.setErrorType(callRecord.getErrorType());
                result.setErrorMessage(callRecord.getErrorMessage());
            } else if (STATUS_REQUESTED.equals(result.getStatus()) && STATUS_SUCCEEDED.equals(callRecord.getStatus())) {
                result.setStatus(STATUS_SUCCEEDED);
            }
        }
        if (candidate != null) {
            result.setAiCandidateId(candidate.getCandidateId());
            if (candidate.getAppliedAt() != null) {
                result.setAppliedAt(candidate.getAppliedAt().toEpochMilli());
                result.setStatus(STATUS_APPLIED);
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
        return new GraphVersionResult(
                version.getVersionId(),
                version.getTaskId() == null
                        ? null
                        : String.valueOf(version.getTaskId().value()),
                version.getCandidateId(),
                version.getTaskType(),
                version.getSourceContentType(),
                version.getSourceContentId(),
                version.getVersionNo(),
                version.getStatus(),
                version.getAppliedAt() == null ? null : version.getAppliedAt().getTime());
    }

    private Long timeValue(Date value) {
        return value == null ? null : value.getTime();
    }

    private String resolveCapability(String taskType) {
        return switch (taskType) {
            case TASK_TYPE_RELATION -> "relation_extraction";
            case TASK_TYPE_GRAPH -> "knowledge_graph";
            case TASK_TYPE_LINEAGE -> "lineage_extraction";
            default -> throw new BizException("Unsupported knowledge graph extraction task type: " + taskType);
        };
    }

    private void validateCommand(
            String sourceContentType,
            Long sourceContentId,
            Long modelId,
            String modelName,
            String requestId,
            String traceId,
            String promptMessagesJson,
            String inputPayloadJson) {
        if (StringUtils.isBlank(sourceContentType)
                || sourceContentId == null
                || modelId == null
                || StringUtils.isBlank(modelName)
                || StringUtils.isBlank(requestId)
                || StringUtils.isBlank(traceId)
                || StringUtils.isBlank(promptMessagesJson)
                || StringUtils.isBlank(inputPayloadJson)) {
            throw new BizException("Knowledge graph extraction request is incomplete");
        }
    }

    @FunctionalInterface
    private interface KnowledgeInvokeOperation {
        KnowledgeAiExtractionResult invoke(KnowledgeAiExtractionRequest request);
    }
}
