package com.thundax.kuzhambu.operations.application.cleanup.service.impl;

import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_ITEM_STATUS_FAILED;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_ITEM_STATUS_SUCCEEDED;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_STATUS_FAILED;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_STATUS_RUNNING;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_STATUS_SUCCEEDED;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_BACKUP;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_HEALTH_CHECK;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_LONG_TASK;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_REPORT;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.ClassicsCleanupTargetsFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsCleanupExecutionFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsCleanupTargetsFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupDetailQuery;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupPageQuery;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupPageResult;
import com.thundax.kuzhambu.operations.application.cleanup.service.CleanupApplicationService;
import com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthAlertStrategy;
import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupItem;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupJob;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import com.thundax.kuzhambu.operations.domain.cleanup.repository.CleanupJobRepository;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import com.thundax.kuzhambu.operations.domain.report.codec.ReportIdCodec;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import com.thundax.kuzhambu.operations.domain.task.codec.LongTaskSnapshotIdCodec;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class CleanupApplicationServiceImpl implements CleanupApplicationService {

    private static final int FAILURE_REASON_MAX_LENGTH = 1024;

    private final CleanupJobRepository cleanupJobRepository;
    private final BackupRepository backupRepository;
    private final ReportRepository reportRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final LongTaskSnapshotRepository longTaskSnapshotRepository;
    private final ClassicsFacade classicsFacade;
    private final OperationsHealthAlertStrategy healthAlertStrategy;

    public CleanupApplicationServiceImpl(
            CleanupJobRepository cleanupJobRepository,
            BackupRepository backupRepository,
            ClassicsFacade classicsFacade) {
        this(cleanupJobRepository, backupRepository, null, null, null, classicsFacade, null);
    }

    public CleanupApplicationServiceImpl(
            CleanupJobRepository cleanupJobRepository,
            BackupRepository backupRepository,
            ClassicsFacade classicsFacade,
            OperationsHealthAlertStrategy healthAlertStrategy) {
        this(cleanupJobRepository, backupRepository, null, null, null, classicsFacade, healthAlertStrategy);
    }

    @Autowired
    public CleanupApplicationServiceImpl(
            CleanupJobRepository cleanupJobRepository,
            BackupRepository backupRepository,
            ReportRepository reportRepository,
            HealthCheckRepository healthCheckRepository,
            LongTaskSnapshotRepository longTaskSnapshotRepository,
            ClassicsFacade classicsFacade,
            OperationsHealthAlertStrategy healthAlertStrategy) {
        this.cleanupJobRepository = cleanupJobRepository;
        this.backupRepository = backupRepository;
        this.reportRepository = reportRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.longTaskSnapshotRepository = longTaskSnapshotRepository;
        this.classicsFacade = classicsFacade;
        this.healthAlertStrategy = healthAlertStrategy;
    }

    @Override
    public OperationsCleanupDetailResult execute(OperationsCleanupExecuteCommand command) {
        return executeInternal(command, true);
    }

    @Override
    public OperationsCleanupDetailResult executeScheduled(OperationsCleanupExecuteCommand command) {
        return executeInternal(command, false);
    }

    private OperationsCleanupDetailResult executeInternal(
            OperationsCleanupExecuteCommand command, boolean requireRequesterUserId) {
        validateExecuteCommand(command, requireRequesterUserId);
        String cleanupType = OperationsCleanupSupport.normalizeType(command.getCleanupType());
        if (!OperationsCleanupSupport.isSupportedType(cleanupType)) {
            throw new IllegalArgumentException("Operations cleanup type is not supported: " + command.getCleanupType());
        }

        Date requestedAt = command.getRequestedAt() == null ? new Date() : command.getRequestedAt();
        int limit = effectiveLimit(command.getLimit());
        CleanupJob cleanupJob = new CleanupJob(
                null,
                cleanupType,
                CLEANUP_STATUS_RUNNING,
                0,
                0,
                0,
                null,
                command.getRequesterUserId(),
                requestedAt,
                null,
                new ArrayList<>());
        CleanupJobId cleanupId = cleanupJobRepository.insert(cleanupJob);
        cleanupJob.setId(cleanupId);
        try {
            List<CleanupItem> cleanupItems = discoverCleanupItems(
                    cleanupId.value(), cleanupType, requestedAt, command.getRetentionDays(), limit);
            int successCount = 0;
            int failedCount = 0;
            for (CleanupItem item : cleanupItems) {
                if (executeCleanupItem(item)) {
                    successCount++;
                } else {
                    failedCount++;
                }
            }

            cleanupJob.setTotalCount(cleanupItems.size());
            cleanupJob.setSuccessCount(successCount);
            cleanupJob.setFailedCount(failedCount);
            cleanupJob.setCleanupStatus(failedCount > 0 ? CLEANUP_STATUS_FAILED : CLEANUP_STATUS_SUCCEEDED);
            cleanupJob.setCompletedAt(new Date());
            cleanupJobRepository.update(cleanupJob);
            if (failedCount > 0) {
                recordCleanupFailure(cleanupJob);
            }
        } catch (RuntimeException exception) {
            cleanupJob.setCleanupStatus(CLEANUP_STATUS_FAILED);
            cleanupJob.setFailureReason(truncateFailureReason(exception.getMessage()));
            cleanupJob.setCompletedAt(new Date());
            cleanupJobRepository.update(cleanupJob);
            recordCleanupFailure(cleanupJob);
        }
        return toDetailResult(cleanupJobRepository.getById(cleanupId));
    }

    private int effectiveLimit(Integer commandLimit) {
        if (commandLimit == null || commandLimit <= 0) {
            return OperationsCleanupSupport.DEFAULT_CLEANUP_TARGET_LIMIT;
        }
        return commandLimit;
    }

    private void recordCleanupFailure(CleanupJob cleanupJob) {
        if (healthAlertStrategy == null || cleanupJob == null || cleanupJob.getId() == null) {
            return;
        }
        String failureReason = StringUtils.defaultIfBlank(
                cleanupJob.getFailureReason(), "cleanup failed items: " + cleanupJob.getFailedCount());
        healthAlertStrategy.recordCleanupFailed(cleanupJob.getId().value(), failureReason);
    }

    @Override
    public PageResult<OperationsCleanupPageResult> page(OperationsCleanupPageQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<CleanupJob> jobPage = cleanupJobRepository.page(
                query == null ? null : query.getCleanupType(),
                query == null ? null : query.getCleanupStatus(),
                query == null ? null : query.getRequesterUserId(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        return PageResult.of(
                jobPage.getPageNo(),
                jobPage.getPageSize(),
                jobPage.getTotalCount(),
                jobPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList()));
    }

    @Override
    public OperationsCleanupDetailResult detail(OperationsCleanupDetailQuery query) {
        return toDetailResult(cleanupJobRepository.getById(query == null ? null : query.getCleanupId()));
    }

    private List<CleanupItem> discoverCleanupItems(
            Long cleanupJobId, String cleanupType, Date processedAt, Integer retentionDays, int limit) {
        List<DiscoveredCleanupTarget> targets = discoverCleanupTargets(cleanupType, processedAt, retentionDays, limit);
        return targets.stream()
                .map(targetId -> new CleanupItem(
                        null, cleanupJobId, targetId.targetType(), targetId.targetId(), null, null, processedAt))
                .collect(Collectors.toList());
    }

    private boolean executeCleanupItem(CleanupItem item) {
        try {
            CleanupExecutionOutcome result = executeCleanupTarget(item);
            item.setItemStatus(result.success() ? CLEANUP_ITEM_STATUS_SUCCEEDED : CLEANUP_ITEM_STATUS_FAILED);
            item.setFailureReason(truncateFailureReason(result.failureReason()));
            item.setProcessedAt(new Date());
            cleanupJobRepository.insertItem(item);
            return result.success();
        } catch (RuntimeException exception) {
            item.setItemStatus(CLEANUP_ITEM_STATUS_FAILED);
            item.setFailureReason(truncateFailureReason(exception.getMessage()));
            item.setProcessedAt(new Date());
            cleanupJobRepository.insertItem(item);
            return false;
        }
    }

    private List<DiscoveredCleanupTarget> discoverCleanupTargets(
            String cleanupType, Date requestedAt, Integer retentionDays, int limit) {
        if (CLEANUP_TYPE_EXPIRED_BACKUP.equals(cleanupType)) {
            return backupRepository
                    .listExpiredBackupIds(backupCleanupThreshold(requestedAt, retentionDays), limit)
                    .stream()
                    .map(BackupId::value)
                    .map(targetId -> new DiscoveredCleanupTarget(
                            OperationsCleanupSupport.resolveItemType(cleanupType), targetId))
                    .toList();
        }
        if (CLEANUP_TYPE_EXPIRED_REPORT.equals(cleanupType)) {
            return reportRepository.listExpiredReportIds(cleanupThreshold(requestedAt, retentionDays), limit).stream()
                    .map(ReportId::value)
                    .map(targetId -> new DiscoveredCleanupTarget(
                            OperationsCleanupSupport.resolveItemType(cleanupType), targetId))
                    .toList();
        }
        if (CLEANUP_TYPE_EXPIRED_HEALTH_CHECK.equals(cleanupType)) {
            return healthCheckRepository
                    .listExpiredCheckIds(cleanupThreshold(requestedAt, retentionDays), limit)
                    .stream()
                    .map(HealthCheckId::value)
                    .map(targetId -> new DiscoveredCleanupTarget(
                            OperationsCleanupSupport.resolveItemType(cleanupType), targetId))
                    .toList();
        }
        if (CLEANUP_TYPE_EXPIRED_LONG_TASK.equals(cleanupType)) {
            return longTaskSnapshotRepository
                    .listExpiredSnapshotIds(cleanupThreshold(requestedAt, retentionDays), limit)
                    .stream()
                    .map(LongTaskSnapshotId::value)
                    .map(targetId -> new DiscoveredCleanupTarget(
                            OperationsCleanupSupport.resolveItemType(cleanupType), targetId))
                    .toList();
        }
        ClassicsCleanupTargetsFacadeResponse response =
                classicsFacade.listCleanupTargets(ClassicsCleanupTargetsFacadeRequest.builder()
                        .cleanupType(cleanupType)
                        .requestedAt(requestedAt)
                        .retentionDays(retentionDays)
                        .limit(limit)
                        .build());
        if (response == null || !response.isSupported() || response.getTargets() == null) {
            return List.of();
        }
        return response.getTargets().stream()
                .map(target -> new DiscoveredCleanupTarget(target.getTargetType(), target.getTargetId()))
                .toList();
    }

    private CleanupExecutionOutcome executeCleanupTarget(CleanupItem item) {
        if (OperationsCleanupSupport.CLEANUP_ITEM_TYPE_BACKUP.equals(item.getTargetType())) {
            int affectedRows = backupRepository.deleteById(BackupIdCodec.toDomain(item.getTargetId()));
            return new CleanupExecutionOutcome(affectedRows > 0, affectedRows > 0 ? null : "TARGET_NOT_FOUND");
        }
        if (OperationsCleanupSupport.CLEANUP_ITEM_TYPE_REPORT.equals(item.getTargetType())) {
            int affectedRows = reportRepository.deleteById(ReportIdCodec.toDomain(item.getTargetId()));
            return new CleanupExecutionOutcome(affectedRows > 0, affectedRows > 0 ? null : "TARGET_NOT_FOUND");
        }
        if (OperationsCleanupSupport.CLEANUP_ITEM_TYPE_HEALTH_CHECK.equals(item.getTargetType())) {
            int affectedRows = healthCheckRepository.deleteById(HealthCheckIdCodec.toDomain(item.getTargetId()));
            return new CleanupExecutionOutcome(affectedRows > 0, affectedRows > 0 ? null : "TARGET_NOT_FOUND");
        }
        if (OperationsCleanupSupport.CLEANUP_ITEM_TYPE_LONG_TASK.equals(item.getTargetType())) {
            int affectedRows =
                    longTaskSnapshotRepository.deleteById(LongTaskSnapshotIdCodec.toDomain(item.getTargetId()));
            return new CleanupExecutionOutcome(affectedRows > 0, affectedRows > 0 ? null : "TARGET_NOT_FOUND");
        }
        ClassicsCleanupExecutionFacadeResponse response =
                classicsFacade.executeCleanupTargets(ClassicsCleanupTargetsFacadeRequest.builder()
                        .cleanupType(resolveCleanupType(item.getTargetType()))
                        .targetIds(List.of(item.getTargetId()))
                        .build());
        if (response == null || !response.isSupported() || response.getItemResults() == null) {
            String failureReason = response == null ? "CLASSICS_CLEANUP_NO_RESPONSE" : response.getFailureReason();
            return new CleanupExecutionOutcome(false, failureReason);
        }
        return response.getItemResults().stream()
                .findFirst()
                .map(result -> new CleanupExecutionOutcome(result.isSuccess(), result.getFailureReason()))
                .orElseGet(() -> new CleanupExecutionOutcome(false, "CLASSICS_CLEANUP_NO_ITEM_RESULT"));
    }

    private String resolveCleanupType(String targetType) {
        if (OperationsCleanupSupport.CLEANUP_ITEM_TYPE_SHARE.equals(targetType)) {
            return OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_SHARE;
        }
        if (OperationsCleanupSupport.CLEANUP_ITEM_TYPE_DRAFT.equals(targetType)) {
            return OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_DRAFT;
        }
        if (OperationsCleanupSupport.CLEANUP_ITEM_TYPE_EXPORT.equals(targetType)) {
            return OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_EXPORT;
        }
        return null;
    }

    private Date backupCleanupThreshold(Date requestedAt, Integer retentionDays) {
        return cleanupThreshold(requestedAt, retentionDays);
    }

    private Date cleanupThreshold(Date requestedAt, Integer retentionDays) {
        if (retentionDays == null || retentionDays <= 0) {
            return requestedAt;
        }
        return Date.from(requestedAt.toInstant().minus(Duration.ofDays(retentionDays)));
    }

    private void validateExecuteCommand(OperationsCleanupExecuteCommand command, boolean requireRequesterUserId) {
        if (command == null) {
            throw new IllegalArgumentException("Operations cleanup execute command must not be null.");
        }
        if (StringUtils.isBlank(command.getCleanupType())) {
            throw new IllegalArgumentException("Operations cleanup type must not be blank.");
        }
        if (requireRequesterUserId && command.getRequesterUserId() == null) {
            throw new IllegalArgumentException("Operations cleanup requesterUserId must not be null.");
        }
    }

    private OperationsCleanupDetailResult toDetailResult(CleanupJob cleanupJob) {
        if (cleanupJob == null) {
            return null;
        }
        return new OperationsCleanupDetailResult(
                cleanupJob.getId(),
                cleanupJob.getCleanupType(),
                cleanupJob.getCleanupStatus(),
                cleanupJob.getTotalCount(),
                cleanupJob.getSuccessCount(),
                cleanupJob.getFailedCount(),
                cleanupJob.getFailureReason(),
                cleanupJob.getRequesterUserId(),
                cleanupJob.getStartedAt(),
                cleanupJob.getCompletedAt(),
                cleanupJob.getCleanupItems() == null
                        ? List.of()
                        : cleanupJob.getCleanupItems().stream()
                                .map(item -> new OperationsCleanupDetailResult.Item(
                                        item.getId(),
                                        item.getTargetType(),
                                        item.getTargetId(),
                                        item.getItemStatus(),
                                        item.getFailureReason(),
                                        item.getProcessedAt()))
                                .toList());
    }

    private OperationsCleanupPageResult toPageResult(CleanupJob cleanupJob) {
        if (cleanupJob == null) {
            return null;
        }
        return new OperationsCleanupPageResult(
                cleanupJob.getId(),
                cleanupJob.getCleanupType(),
                cleanupJob.getCleanupStatus(),
                cleanupJob.getTotalCount(),
                cleanupJob.getSuccessCount(),
                cleanupJob.getFailedCount(),
                cleanupJob.getFailureReason(),
                cleanupJob.getRequesterUserId(),
                cleanupJob.getStartedAt(),
                cleanupJob.getCompletedAt());
    }

    private String truncateFailureReason(String failureReason) {
        if (failureReason == null) {
            return null;
        }
        return failureReason.length() > FAILURE_REASON_MAX_LENGTH
                ? failureReason.substring(0, FAILURE_REASON_MAX_LENGTH)
                : failureReason;
    }

    private record DiscoveredCleanupTarget(String targetType, Long targetId) {}

    private record CleanupExecutionOutcome(boolean success, String failureReason) {}
}
