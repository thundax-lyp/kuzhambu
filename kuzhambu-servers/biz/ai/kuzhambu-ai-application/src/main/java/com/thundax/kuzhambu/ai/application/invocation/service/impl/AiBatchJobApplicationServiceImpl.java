package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.ExpireRunningAiBatchJobsCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RecordAiBatchJobFailureCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.CanDispatchNextAiBatchUnitQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.GetAiBatchJobQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsByCapabilitiesQuery;
import com.thundax.kuzhambu.ai.application.invocation.query.PageAiBatchJobsQuery;
import com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.query.AiBatchJobQuery;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiBatchJobRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiBatchJobApplicationServiceImpl implements AiBatchJobApplicationService {

    private final AiBatchJobRepository aiBatchJobRepository;

    public AiBatchJobApplicationServiceImpl(AiBatchJobRepository aiBatchJobRepository) {
        this.aiBatchJobRepository = aiBatchJobRepository;
    }

    @Override
    public AiBatchJobResult get(GetAiBatchJobQuery query) {
        return AiBatchJobResult.from(getRequired(query == null ? null : query.batchId()));
    }

    @Override
    public PageResult<AiBatchJobResult> page(PageAiBatchJobsQuery query) {
        PageQuery effectivePage = effectivePage(query == null ? null : query.getPageQuery());
        AiContentRef contentRef = query == null ? null : query.getContentRef();
        return page(new AiBatchJobQuery(
                query == null ? null : query.getScope(),
                query == null ? null : query.getCapability(),
                query == null ? null : query.getStatus(),
                contentRef == null ? null : contentRef.contentType(),
                contentRef == null ? null : contentRef.contentId(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize()));
    }

    @Override
    public PageResult<AiBatchJobResult> pageByCapabilities(PageAiBatchJobsByCapabilitiesQuery query) {
        PageQuery effectivePage = effectivePage(query == null ? null : query.getPageQuery());
        AiContentRef contentRef = query == null ? null : query.getContentRef();
        return page(new AiBatchJobQuery(
                query == null ? null : query.getScope(),
                query == null ? null : query.getCapabilities(),
                query == null ? null : query.getStatus(),
                contentRef == null ? null : contentRef.contentType(),
                contentRef == null ? null : contentRef.contentId(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize()));
    }

    private PageResult<AiBatchJobResult> page(AiBatchJobQuery query) {
        long total = aiBatchJobRepository.countJobs(query);
        List<AiBatchJobResult> records = new ArrayList<>();
        for (AiBatchJob job : aiBatchJobRepository.listJobs(query)) {
            records.add(AiBatchJobResult.from(job));
        }
        return PageResult.of(query.getPageNo(), query.getPageSize(), total, records);
    }

    private PageQuery effectivePage(PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        return effectivePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobId create(AiBatchJobCreateCommand command) {
        validateCreateCommand(command);
        return aiBatchJobRepository.insert(toEntity(command));
    }

    @Override
    public boolean canDispatchNextUnit(CanDispatchNextAiBatchUnitQuery query) {
        AiBatchJobId batchId = query == null ? null : query.batchId();
        AiBatchJob job = getRequired(batchId);
        return AiBatchJobStatus.CANCELLED != job.getStatus()
                && job.getSuccessCount() + job.getFailedCount() + job.getCancelledCount() < job.getTotalCount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordSuccess(RecordAiBatchJobCommand command) {
        AiBatchJobId batchId = command == null ? null : command.getBatchId();
        AiBatchJob job = getRequired(batchId);
        if (AiBatchJobStatus.CANCELLED == job.getStatus()) {
            consumeCancelledSlot(job);
            if (job.getSuccessCount() + job.getFailedCount() < job.getTotalCount()) {
                job.setSuccessCount(job.getSuccessCount() + 1);
            }
        } else {
            job.recordSuccess();
        }
        aiBatchJobRepository.update(job);
        return AiBatchJobResult.from(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordSuccessIfRunning(RecordAiBatchJobCommand command) {
        return recordIfRunning(command == null ? null : command.getBatchId(), AiBatchJob::recordSuccess);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordFailure(RecordAiBatchJobFailureCommand command) {
        AiBatchJobId batchId = command == null ? null : command.getBatchId();
        AiBatchJob job = getRequired(batchId);
        String failureSummaryJson = command == null ? null : command.getFailureSummaryJson();
        job.setFailureSummaryJson(failureSummaryJson);
        if (AiBatchJobStatus.CANCELLED == job.getStatus()) {
            consumeCancelledSlot(job);
            if (job.getSuccessCount() + job.getFailedCount() < job.getTotalCount()) {
                job.setFailedCount(job.getFailedCount() + 1);
            }
        } else {
            job.recordFailure();
        }
        aiBatchJobRepository.update(job);
        return AiBatchJobResult.from(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordFailureIfRunning(RecordAiBatchJobFailureCommand command) {
        AiBatchJobId batchId = command == null ? null : command.getBatchId();
        String failureSummaryJson = command == null ? null : command.getFailureSummaryJson();
        return recordIfRunning(batchId, job -> {
            job.setFailureSummaryJson(failureSummaryJson);
            job.recordFailure();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordPartialIfRunning(RecordAiBatchJobFailureCommand command) {
        AiBatchJobId batchId = command == null ? null : command.getBatchId();
        String failureSummaryJson = command == null ? null : command.getFailureSummaryJson();
        return recordIfRunning(batchId, job -> {
            job.setFailureSummaryJson(failureSummaryJson);
            job.recordPartial();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int expireRunning(ExpireRunningAiBatchJobsCommand command) {
        Instant requestedBefore = command == null ? null : command.getRequestedBefore();
        if (requestedBefore == null) {
            throw new BizException("AI batch job expire cutoff is required");
        }
        int expiredCount = 0;
        for (AiBatchJob job : aiBatchJobRepository.listRunningJobsRequestedBefore(
                command.getScope(), command.getCapabilities(), requestedBefore, command.getLimit())) {
            job.setFailureSummaryJson(command.getFailureSummaryJson());
            job.recordFailure();
            expiredCount += aiBatchJobRepository.updateIfStatus(job, AiBatchJobStatus.RUNNING);
        }
        return expiredCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult cancel(CancelAiBatchJobCommand command) {
        AiBatchJobId batchId = command == null ? null : command.batchId();
        AiBatchJob job = getRequired(batchId);
        if (isTerminal(job.getStatus())) {
            return AiBatchJobResult.from(job);
        }
        int finishedCount = job.getSuccessCount() + job.getFailedCount();
        job.setCancelledCount(Math.max(0, job.getTotalCount() - finishedCount));
        job.cancel(Instant.now());
        int updated = aiBatchJobRepository.updateIfStatus(job, AiBatchJobStatus.RUNNING);
        return updated == 0 ? AiBatchJobResult.from(getRequired(batchId)) : AiBatchJobResult.from(job);
    }

    private AiBatchJobResult recordIfRunning(AiBatchJobId batchId, Consumer<AiBatchJob> recorder) {
        AiBatchJob job = getRequired(batchId);
        if (AiBatchJobStatus.RUNNING != job.getStatus()) {
            return AiBatchJobResult.from(job);
        }
        recorder.accept(job);
        int updated = aiBatchJobRepository.updateIfStatus(job, AiBatchJobStatus.RUNNING);
        return updated == 0 ? AiBatchJobResult.from(getRequired(batchId)) : AiBatchJobResult.from(job);
    }

    private AiBatchJob getRequired(AiBatchJobId batchId) {
        if (batchId == null) {
            throw new BizException("AI batchId is required");
        }
        AiBatchJob job = aiBatchJobRepository.get(batchId);
        if (job == null) {
            throw new BizException("AI batch job not found: " + batchId);
        }
        return job;
    }

    private AiBatchJob toEntity(AiBatchJobCreateCommand command) {
        AiBatchJob job = new AiBatchJob();
        job.setScope(command.getScope());
        job.setCapability(command.getCapability());
        job.setContentType(command.getContentRef().contentType());
        job.setContentId(command.getContentRef().contentId());
        job.setTotalCount(command.getTotalCount());
        job.setFailureSummaryJson(command.getFailureSummaryJson());
        job.setRequestedAt(Instant.now());
        return job;
    }

    private void validateCreateCommand(AiBatchJobCreateCommand command) {
        if (command == null
                || isBlank(command.getScope())
                || command.getCapability() == null
                || command.getContentRef() == null
                || isBlank(command.getContentRef().contentType())
                || command.getTotalCount() <= 0) {
            throw new BizException("AI batch job create command is incomplete");
        }
    }

    private void consumeCancelledSlot(AiBatchJob job) {
        if (job.getCancelledCount() > 0) {
            job.setCancelledCount(job.getCancelledCount() - 1);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isTerminal(AiBatchJobStatus status) {
        return AiBatchJobStatus.SUCCEEDED == status
                || AiBatchJobStatus.FAILED == status
                || AiBatchJobStatus.PARTIAL == status
                || AiBatchJobStatus.CANCELLED == status;
    }
}
