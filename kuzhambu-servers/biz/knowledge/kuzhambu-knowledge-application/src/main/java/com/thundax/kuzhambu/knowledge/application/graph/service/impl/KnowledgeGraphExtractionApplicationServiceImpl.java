package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

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
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
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

    private final GraphExtractionTaskRepository repository;
    private final KnowledgeAiExtractionDomainService knowledgeAiExtractionDomainService;

    public KnowledgeGraphExtractionApplicationServiceImpl(
            GraphExtractionTaskRepository repository,
            KnowledgeAiExtractionDomainService knowledgeAiExtractionDomainService) {
        this.repository = repository;
        this.knowledgeAiExtractionDomainService = knowledgeAiExtractionDomainService;
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
                taskPage.getRecords().stream().map(this::toResult).toList();
        return PageResult.of(taskPage.getPageNo(), taskPage.getPageSize(), taskPage.getTotalCount(), records);
    }

    @Override
    public GraphExtractionTaskResult getTaskDetail(GraphExtractionTaskId taskId) {
        return toResult(repository.getByTaskId(taskId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult applyTaskCandidate(GraphExtractionTaskId taskId) {
        GraphExtractionTask task = repository.getByTaskId(taskId);
        if (task == null) {
            throw new BizException("Graph extraction task not found: " + (taskId == null ? null : taskId.value()));
        }
        return toResult(task);
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

    private Long timeValue(Date value) {
        return value == null ? null : value.getTime();
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
