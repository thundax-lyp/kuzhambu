package com.thundax.kuzhambu.operations.application.task.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthAlertStrategy;
import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskDetailQuery;
import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskQuery;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskDetailResult;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskPageResult;
import com.thundax.kuzhambu.operations.application.task.service.TaskApplicationService;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class TaskApplicationServiceImpl implements TaskApplicationService {

    private static final String TASK_STATUS_FAILED = "FAILED";

    private final LongTaskSnapshotRepository longTaskSnapshotRepository;
    private final OperationsHealthAlertStrategy healthAlertStrategy;

    public TaskApplicationServiceImpl(LongTaskSnapshotRepository longTaskSnapshotRepository) {
        this(longTaskSnapshotRepository, null);
    }

    @Autowired
    public TaskApplicationServiceImpl(
            LongTaskSnapshotRepository longTaskSnapshotRepository, OperationsHealthAlertStrategy healthAlertStrategy) {
        this.longTaskSnapshotRepository = longTaskSnapshotRepository;
        this.healthAlertStrategy = healthAlertStrategy;
    }

    @Override
    public PageResult<OperationsTaskPageResult> page(OperationsTaskQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<LongTaskSnapshot> taskPage = longTaskSnapshotRepository.page(
                query == null ? null : query.sourceDomain(),
                query == null ? null : query.taskType(),
                query == null ? null : query.taskStatus(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<OperationsTaskPageResult> results =
                taskPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList());
        return PageResult.of(taskPage.getPageNo(), taskPage.getPageSize(), taskPage.getTotalCount(), results);
    }

    @Override
    public OperationsTaskDetailResult detail(OperationsTaskDetailQuery query) {
        LongTaskSnapshot snapshot = longTaskSnapshotRepository.getById(query == null ? null : query.snapshotId());
        recordLongTaskFailure(snapshot);
        return toDetailResult(snapshot);
    }

    private OperationsTaskPageResult toPageResult(LongTaskSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        recordLongTaskFailure(snapshot);
        return new OperationsTaskPageResult(
                snapshot.getId(),
                snapshot.getSourceDomain(),
                snapshot.getTaskType(),
                snapshot.getTaskKey(),
                snapshot.getTaskStatus(),
                snapshot.getTotalCount(),
                snapshot.getSuccessCount(),
                snapshot.getFailedCount(),
                snapshot.getFailureReason(),
                snapshot.getRequestedByUserId(),
                snapshot.getStartedAt(),
                snapshot.getCompletedAt(),
                snapshot.getSnapshotAt());
    }

    private OperationsTaskDetailResult toDetailResult(LongTaskSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new OperationsTaskDetailResult(
                snapshot.getId(),
                snapshot.getSourceDomain(),
                snapshot.getTaskType(),
                snapshot.getTaskKey(),
                snapshot.getTaskStatus(),
                snapshot.getTotalCount(),
                snapshot.getSuccessCount(),
                snapshot.getFailedCount(),
                snapshot.getFailureReason(),
                snapshot.getRequestedByUserId(),
                snapshot.getStartedAt(),
                snapshot.getCompletedAt(),
                snapshot.getSnapshotAt());
    }

    private void recordLongTaskFailure(LongTaskSnapshot snapshot) {
        if (healthAlertStrategy == null
                || snapshot == null
                || snapshot.getId() == null
                || !TASK_STATUS_FAILED.equals(snapshot.getTaskStatus())) {
            return;
        }
        healthAlertStrategy.recordLongTaskFailed(snapshot.getId().value(), snapshot.getFailureReason());
    }
}
