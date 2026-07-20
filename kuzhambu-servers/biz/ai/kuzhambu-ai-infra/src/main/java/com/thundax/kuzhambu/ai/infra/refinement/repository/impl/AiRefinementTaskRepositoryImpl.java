package com.thundax.kuzhambu.ai.infra.refinement.repository.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.domain.refinement.repository.AiRefinementTaskRepository;
import com.thundax.kuzhambu.ai.infra.refinement.persistence.dataobject.AiRefinementTaskDO;
import com.thundax.kuzhambu.ai.infra.refinement.persistence.mapper.AiRefinementTaskMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiRefinementTaskRepositoryImpl implements AiRefinementTaskRepository {

    private final AiRefinementTaskMapper aiRefinementTaskMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AiRefinementTaskRepositoryImpl(AiRefinementTaskMapper aiRefinementTaskMapper) {
        this.aiRefinementTaskMapper = aiRefinementTaskMapper;
    }

    @Override
    public AiRefinementTask get(Long taskId) {
        return toRefinementTaskDomain(aiRefinementTaskMapper.selectTask(taskId));
    }

    @Override
    public Long insert(AiRefinementTask task) {
        AiRefinementTaskDO dataObject = toTaskObject(task);
        if (dataObject.getTaskId() == null) {
            dataObject.setTaskId(nextId());
        }
        if (dataObject.getRequestedAt() == null) {
            dataObject.setRequestedAt(Instant.now());
        }
        aiRefinementTaskMapper.insertTask(dataObject);
        return dataObject.getTaskId();
    }

    @Override
    public int update(AiRefinementTask task) {
        return updateWithWrapper(
                task,
                new LambdaUpdateWrapper<AiRefinementTaskDO>().eq(AiRefinementTaskDO::getTaskId, task.getTaskId()));
    }

    @Override
    public int updateWhenStatusIn(AiRefinementTask task, Collection<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return 0;
        }
        return updateWithWrapper(
                task,
                new LambdaUpdateWrapper<AiRefinementTaskDO>()
                        .eq(AiRefinementTaskDO::getTaskId, task.getTaskId())
                        .in(AiRefinementTaskDO::getStatus, statuses));
    }

    private int updateWithWrapper(AiRefinementTask task, LambdaUpdateWrapper<AiRefinementTaskDO> updateWrapper) {
        AiRefinementTaskDO dataObject = toTaskObject(task);
        return aiRefinementTaskMapper.update(
                null,
                updateWrapper
                        .set(AiRefinementTaskDO::getScope, dataObject.getScope())
                        .set(AiRefinementTaskDO::getCapability, dataObject.getCapability())
                        .set(AiRefinementTaskDO::getContentType, dataObject.getContentType())
                        .set(AiRefinementTaskDO::getContentId, dataObject.getContentId())
                        .set(AiRefinementTaskDO::getObjectId, dataObject.getObjectId())
                        .set(AiRefinementTaskDO::getRequestedBy, dataObject.getRequestedBy())
                        .set(AiRefinementTaskDO::getRequestId, dataObject.getRequestId())
                        .set(AiRefinementTaskDO::getTraceId, dataObject.getTraceId())
                        .set(AiRefinementTaskDO::getStatus, dataObject.getStatus())
                        .set(AiRefinementTaskDO::getServiceRole, dataObject.getServiceRole())
                        .set(AiRefinementTaskDO::getModelId, dataObject.getModelId())
                        .set(AiRefinementTaskDO::getModelName, dataObject.getModelName())
                        .set(AiRefinementTaskDO::getPromptVersionId, dataObject.getPromptVersionId())
                        .set(AiRefinementTaskDO::getCallId, dataObject.getCallId())
                        .set(AiRefinementTaskDO::getCandidateId, dataObject.getCandidateId())
                        .set(AiRefinementTaskDO::getFailureStage, dataObject.getFailureStage())
                        .set(AiRefinementTaskDO::getErrorType, dataObject.getErrorType())
                        .set(AiRefinementTaskDO::getErrorMessage, dataObject.getErrorMessage())
                        .set(AiRefinementTaskDO::getResultFormat, dataObject.getResultFormat())
                        .set(AiRefinementTaskDO::getResultPreview, dataObject.getResultPreview())
                        .set(AiRefinementTaskDO::getStreamEnabled, dataObject.getStreamEnabled())
                        .set(AiRefinementTaskDO::getRequestedAt, dataObject.getRequestedAt())
                        .set(AiRefinementTaskDO::getStartedAt, dataObject.getStartedAt())
                        .set(AiRefinementTaskDO::getCompletedAt, dataObject.getCompletedAt())
                        .set(AiRefinementTaskDO::getCancelledAt, dataObject.getCancelledAt()));
    }

    @Override
    public List<AiRefinementTask> listTasks(
            String capability,
            String status,
            String contentType,
            Long contentId,
            Long requestedBy,
            Integer pageNo,
            Integer pageSize) {
        int safePageNo = resolvePageNo(pageNo);
        int safePageSize = resolvePageSize(pageSize);
        return toRefinementTaskDomainList(aiRefinementTaskMapper.selectTasks(
                capability,
                status,
                contentType,
                contentId,
                requestedBy,
                (safePageNo - 1) * safePageSize,
                safePageSize));
    }

    @Override
    public long countTasks(String capability, String status, String contentType, Long contentId, Long requestedBy) {
        return aiRefinementTaskMapper.countTasks(capability, status, contentType, contentId, requestedBy);
    }

    @Override
    public List<AiRefinementTask> listActiveTasks() {
        return toRefinementTaskDomainList(aiRefinementTaskMapper.selectActiveTasks());
    }

    @Override
    public List<AiRefinementTask> listExpiredRunningTasks(Instant threshold) {
        return toRefinementTaskDomainList(aiRefinementTaskMapper.selectExpiredRunningTasks(threshold));
    }

    @Override
    public int deleteExpiredTerminalTasks(Instant threshold) {
        return aiRefinementTaskMapper.deleteExpiredTerminalTasks(threshold);
    }

    private AiRefinementTaskDO toTaskObject(AiRefinementTask task) {
        if (task == null) {
            return null;
        }
        AiRefinementTaskDO dataObject = new AiRefinementTaskDO();
        dataObject.setId(task.getId());
        dataObject.setTaskId(task.getTaskId());
        dataObject.setScope(task.getScope());
        dataObject.setCapability(task.getCapability());
        dataObject.setContentType(task.getContentType());
        dataObject.setContentId(task.getContentId());
        dataObject.setObjectId(task.getObjectId());
        dataObject.setRequestedBy(task.getRequestedBy());
        dataObject.setRequestId(task.getRequestId());
        dataObject.setTraceId(task.getTraceId());
        dataObject.setStatus(task.getStatus());
        dataObject.setServiceRole(task.getServiceRole());
        dataObject.setModelId(task.getModelId());
        dataObject.setModelName(task.getModelName());
        dataObject.setPromptVersionId(task.getPromptVersionId());
        dataObject.setCallId(task.getCallId());
        dataObject.setCandidateId(task.getCandidateId());
        dataObject.setFailureStage(task.getFailureStage());
        dataObject.setErrorType(task.getErrorType());
        dataObject.setErrorMessage(task.getErrorMessage());
        dataObject.setResultFormat(task.getResultFormat());
        dataObject.setResultPreview(task.getResultPreview());
        dataObject.setStreamEnabled(task.isStreamEnabled());
        dataObject.setRequestedAt(task.getRequestedAt());
        dataObject.setStartedAt(task.getStartedAt());
        dataObject.setCompletedAt(task.getCompletedAt());
        dataObject.setCancelledAt(task.getCancelledAt());
        return dataObject;
    }

    private AiRefinementTask toRefinementTaskDomain(AiRefinementTaskDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiRefinementTask(
                dataObject.getId(),
                dataObject.getTaskId(),
                dataObject.getScope(),
                dataObject.getCapability(),
                dataObject.getContentType(),
                dataObject.getContentId(),
                dataObject.getObjectId(),
                dataObject.getRequestedBy(),
                dataObject.getRequestId(),
                dataObject.getTraceId(),
                dataObject.getStatus(),
                dataObject.getServiceRole(),
                dataObject.getModelId(),
                dataObject.getModelName(),
                dataObject.getPromptVersionId(),
                dataObject.getCallId(),
                dataObject.getCandidateId(),
                dataObject.getResultFormat(),
                dataObject.getResultPreview(),
                dataObject.getFailureStage(),
                dataObject.getErrorType(),
                dataObject.getErrorMessage(),
                Boolean.TRUE.equals(dataObject.getStreamEnabled()),
                dataObject.getRequestedAt(),
                dataObject.getStartedAt(),
                dataObject.getCompletedAt(),
                dataObject.getCancelledAt());
    }

    private List<AiRefinementTask> toRefinementTaskDomainList(List<AiRefinementTaskDO> dataObjects) {
        List<AiRefinementTask> tasks = new ArrayList<>();
        if (dataObjects == null) {
            return tasks;
        }
        for (AiRefinementTaskDO dataObject : dataObjects) {
            tasks.add(toRefinementTaskDomain(dataObject));
        }
        return tasks;
    }

    private int resolvePageNo(Integer pageNo) {
        return pageNo == null || pageNo <= 0 ? 1 : pageNo;
    }

    private int resolvePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 20;
        }
        if (pageSize > 200) {
            return 200;
        }
        return pageSize;
    }

    private Long nextId() {
        return idGenerator.nextId().value();
    }
}
