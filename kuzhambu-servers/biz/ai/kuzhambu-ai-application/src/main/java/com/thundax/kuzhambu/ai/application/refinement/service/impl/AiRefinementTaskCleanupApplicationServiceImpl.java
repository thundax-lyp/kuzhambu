package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementTaskCleanupApplicationService;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.domain.refinement.repository.AiRefinementTaskRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@BizExceptionBoundary
public class AiRefinementTaskCleanupApplicationServiceImpl implements AiRefinementTaskCleanupApplicationService {

    static final Duration TASK_RETENTION = Duration.ofHours(12);
    static final String FAILURE_STAGE_WORKER_RESULT = "WORKER_RESULT";
    static final String ERROR_TYPE_TASK_EXPIRED = "TASK_EXPIRED";
    static final String ERROR_MESSAGE_TASK_EXPIRED = "任务超过 12 小时未完成，系统自动关闭";

    private final AiRefinementTaskRepository taskRepository;

    public AiRefinementTaskCleanupApplicationServiceImpl(AiRefinementTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CleanupResult cleanupExpiredTasks() {
        Instant now = now();
        Instant threshold = now.minus(TASK_RETENTION);
        List<AiRefinementTask> expiredRunningTasks = taskRepository.listExpiredRunningTasks(threshold);
        int expiredCount = 0;
        if (expiredRunningTasks != null) {
            for (AiRefinementTask task : expiredRunningTasks) {
                if (task == null) {
                    continue;
                }
                task.markFailed(FAILURE_STAGE_WORKER_RESULT, ERROR_TYPE_TASK_EXPIRED, ERROR_MESSAGE_TASK_EXPIRED, now);
                taskRepository.update(task);
                expiredCount++;
            }
        }
        int deletedCount = taskRepository.deleteExpiredTerminalTasks(threshold);
        return new CleanupResult(expiredCount, deletedCount);
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanupExpiredTasksOnSchedule() {
        cleanupExpiredTasks();
    }

    protected Instant now() {
        return Instant.now();
    }
}
