package com.thundax.kuzhambu.operations.application.task.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskDetailQuery;
import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskPageQuery;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskDetailResult;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskPageResult;
import com.thundax.kuzhambu.operations.application.task.service.TaskApplicationService;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class TaskApplicationServiceImpl implements TaskApplicationService {

    private final LongTaskSnapshotRepository longTaskSnapshotRepository;

    public TaskApplicationServiceImpl(LongTaskSnapshotRepository longTaskSnapshotRepository) {
        this.longTaskSnapshotRepository = longTaskSnapshotRepository;
    }

    @Override
    public PageResult<OperationsTaskPageResult> page(OperationsTaskPageQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<LongTaskSnapshot> taskPage = longTaskSnapshotRepository.page(
                query == null ? null : query.getSourceDomain(),
                query == null ? null : query.getTaskType(),
                query == null ? null : query.getTaskStatus(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<OperationsTaskPageResult> results =
                taskPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList());
        return PageResult.of(taskPage.getPageNo(), taskPage.getPageSize(), taskPage.getTotalCount(), results);
    }

    @Override
    public OperationsTaskDetailResult detail(OperationsTaskDetailQuery query) {
        return toDetailResult(longTaskSnapshotRepository.getById(query == null ? null : query.getSnapshotId()));
    }

    private OperationsTaskPageResult toPageResult(LongTaskSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
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
}
