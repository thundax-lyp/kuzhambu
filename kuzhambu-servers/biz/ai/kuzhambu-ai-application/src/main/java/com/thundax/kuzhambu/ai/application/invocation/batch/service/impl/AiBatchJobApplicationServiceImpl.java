package com.thundax.kuzhambu.ai.application.invocation.batch.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.batch.command.AiBatchJobCreateCommand;
import com.thundax.kuzhambu.ai.application.invocation.batch.result.AiBatchJobResult;
import com.thundax.kuzhambu.ai.application.invocation.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.query.AiBatchJobQuery;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
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
    public AiBatchJobResult get(Long batchId) {
        return AiBatchJobResult.from(getRequired(batchId));
    }

    @Override
    public PageResult<AiBatchJobResult> page(
            String scope, String capability, String status, String contentType, Long contentId, PageQuery pageQuery) {
        PageQuery effectivePage = effectivePage(pageQuery);
        return page(new AiBatchJobQuery(
                scope,
                parseCapability(capability),
                parseStatus(status),
                contentType,
                contentId,
                effectivePage.getPageNo(),
                effectivePage.getPageSize()));
    }

    @Override
    public PageResult<AiBatchJobResult> pageByCapabilities(
            String scope,
            List<String> capabilities,
            String status,
            String contentType,
            Long contentId,
            PageQuery pageQuery) {
        PageQuery effectivePage = effectivePage(pageQuery);
        return page(new AiBatchJobQuery(
                scope,
                parseCapabilities(capabilities),
                parseStatus(status),
                contentType,
                contentId,
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
    public Long create(AiBatchJobCreateCommand command) {
        validateCreateCommand(command);
        return AiBatchJobIdCodec.toValue(aiBatchJobRepository.insert(toEntity(command)));
    }

    @Override
    public boolean canDispatchNextUnit(Long batchId) {
        AiBatchJob job = getRequired(batchId);
        return AiBatchJobStatus.CANCELLED != job.getStatus()
                && job.getSuccessCount() + job.getFailedCount() + job.getCancelledCount() < job.getTotalCount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordSuccess(Long batchId) {
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
    public AiBatchJobResult recordSuccessIfRunning(Long batchId) {
        return recordIfRunning(batchId, AiBatchJob::recordSuccess);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordFailure(Long batchId, String failureSummaryJson) {
        AiBatchJob job = getRequired(batchId);
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
    public AiBatchJobResult recordFailureIfRunning(Long batchId, String failureSummaryJson) {
        return recordIfRunning(batchId, job -> {
            job.setFailureSummaryJson(failureSummaryJson);
            job.recordFailure();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult recordPartialIfRunning(Long batchId, String failureSummaryJson) {
        return recordIfRunning(batchId, job -> {
            job.setFailureSummaryJson(failureSummaryJson);
            job.recordPartial();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int expireRunning(
            String scope, List<String> capabilities, Instant requestedBefore, String failureSummaryJson, int limit) {
        if (requestedBefore == null) {
            throw new BizException("AI batch job expire cutoff is required");
        }
        List<AiBusinessCapability> parsedCapabilities = parseCapabilities(capabilities);
        int expiredCount = 0;
        for (AiBatchJob job : aiBatchJobRepository.listRunningJobsRequestedBefore(
                scope, parsedCapabilities, requestedBefore, limit)) {
            job.setFailureSummaryJson(failureSummaryJson);
            job.recordFailure();
            expiredCount += aiBatchJobRepository.updateIfStatus(job, AiBatchJobStatus.RUNNING);
        }
        return expiredCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBatchJobResult cancel(Long batchId) {
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

    private AiBatchJobResult recordIfRunning(Long batchId, Consumer<AiBatchJob> recorder) {
        AiBatchJob job = getRequired(batchId);
        if (AiBatchJobStatus.RUNNING != job.getStatus()) {
            return AiBatchJobResult.from(job);
        }
        recorder.accept(job);
        int updated = aiBatchJobRepository.updateIfStatus(job, AiBatchJobStatus.RUNNING);
        return updated == 0 ? AiBatchJobResult.from(getRequired(batchId)) : AiBatchJobResult.from(job);
    }

    private AiBatchJob getRequired(Long batchId) {
        if (batchId == null) {
            throw new BizException("AI batchId is required");
        }
        AiBatchJobId id = AiBatchJobIdCodec.toDomain(batchId);
        AiBatchJob job = aiBatchJobRepository.get(id);
        if (job == null) {
            throw new BizException("AI batch job not found: " + batchId);
        }
        return job;
    }

    private AiBatchJob toEntity(AiBatchJobCreateCommand command) {
        AiBatchJob job = new AiBatchJob();
        job.setScope(command.getScope());
        job.setCapability(AiBusinessCapability.fromAlias(command.getCapability()));
        job.setContentType(command.getContentType());
        job.setContentId(command.getContentId());
        job.setTotalCount(command.getTotalCount());
        job.setFailureSummaryJson(command.getFailureSummaryJson());
        job.setRequestedAt(Instant.now());
        return job;
    }

    private void validateCreateCommand(AiBatchJobCreateCommand command) {
        if (command == null
                || isBlank(command.getScope())
                || isBlank(command.getCapability())
                || isBlank(command.getContentType())
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

    private AiBusinessCapability parseCapability(String capability) {
        return isBlank(capability) ? null : AiBusinessCapability.fromAlias(capability);
    }

    private List<AiBusinessCapability> parseCapabilities(List<String> capabilities) {
        List<AiBusinessCapability> parsedCapabilities = new ArrayList<>();
        if (capabilities == null) {
            return parsedCapabilities;
        }
        for (String capability : capabilities) {
            if (!isBlank(capability)) {
                parsedCapabilities.add(AiBusinessCapability.fromAlias(capability));
            }
        }
        return parsedCapabilities;
    }

    private AiBatchJobStatus parseStatus(String status) {
        return isBlank(status) ? null : AiBatchJobStatus.from(status);
    }
}
