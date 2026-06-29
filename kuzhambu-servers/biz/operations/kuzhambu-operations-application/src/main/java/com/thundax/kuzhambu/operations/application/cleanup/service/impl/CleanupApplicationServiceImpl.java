package com.thundax.kuzhambu.operations.application.cleanup.service.impl;

import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_ITEM_STATUS_FAILED;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_ITEM_STATUS_SUCCEEDED;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_STATUS_FAILED;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_STATUS_RUNNING;
import static com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport.CLEANUP_STATUS_SUCCEEDED;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.service.CleanupApplicationService;
import com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupItem;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupJob;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import com.thundax.kuzhambu.operations.domain.cleanup.repository.CleanupJobRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class CleanupApplicationServiceImpl implements CleanupApplicationService {

    private static final int FAILURE_REASON_MAX_LENGTH = 1024;

    private final CleanupJobRepository cleanupJobRepository;

    public CleanupApplicationServiceImpl(CleanupJobRepository cleanupJobRepository) {
        this.cleanupJobRepository = cleanupJobRepository;
    }

    @Override
    public OperationsCleanupDetailResult execute(OperationsCleanupExecuteCommand command) {
        validateExecuteCommand(command);
        String cleanupType = OperationsCleanupSupport.normalizeType(command.getCleanupType());
        if (!OperationsCleanupSupport.isSupportedType(cleanupType)) {
            throw new IllegalArgumentException("Operations cleanup type is not supported: " + command.getCleanupType());
        }

        Date startedAt = new Date();
        CleanupJob cleanupJob = new CleanupJob(
                null,
                cleanupType,
                CLEANUP_STATUS_RUNNING,
                0,
                0,
                0,
                null,
                command.getRequesterUserId(),
                startedAt,
                null,
                new ArrayList<>());
        CleanupJobId cleanupId = cleanupJobRepository.insert(cleanupJob);
        cleanupJob.setId(cleanupId);
        try {
            List<CleanupItem> cleanupItems = discoverCleanupItems(cleanupId.value(), cleanupType, startedAt);
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
        } catch (RuntimeException exception) {
            cleanupJob.setCleanupStatus(CLEANUP_STATUS_FAILED);
            cleanupJob.setFailureReason(truncateFailureReason(exception.getMessage()));
            cleanupJob.setCompletedAt(new Date());
            cleanupJobRepository.update(cleanupJob);
        }
        return toDetailResult(cleanupJobRepository.getById(cleanupId));
    }

    private List<CleanupItem> discoverCleanupItems(Long cleanupJobId, String cleanupType, Date processedAt) {
        List<Long> targetIds = List.of();
        return targetIds.stream()
                .map(targetId -> new CleanupItem(
                        null,
                        cleanupJobId,
                        OperationsCleanupSupport.resolveItemType(cleanupType),
                        targetId,
                        CLEANUP_ITEM_STATUS_SUCCEEDED,
                        null,
                        processedAt))
                .collect(Collectors.toList());
    }

    private boolean executeCleanupItem(CleanupItem item) {
        try {
            cleanupJobRepository.insertItem(item);
            return true;
        } catch (RuntimeException exception) {
            item.setItemStatus(CLEANUP_ITEM_STATUS_FAILED);
            item.setFailureReason(truncateFailureReason(exception.getMessage()));
            return false;
        }
    }

    private void validateExecuteCommand(OperationsCleanupExecuteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Operations cleanup execute command must not be null.");
        }
        if (StringUtils.isBlank(command.getCleanupType())) {
            throw new IllegalArgumentException("Operations cleanup type must not be blank.");
        }
        if (command.getRequesterUserId() == null) {
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
}
