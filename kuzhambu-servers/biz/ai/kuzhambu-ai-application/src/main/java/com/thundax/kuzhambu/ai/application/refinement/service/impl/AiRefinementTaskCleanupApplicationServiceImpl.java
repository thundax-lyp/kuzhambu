package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import com.thundax.kuzhambu.ai.application.refinement.service.AiRefinementTaskCleanupApplicationService;
import com.thundax.kuzhambu.ai.domain.refinement.model.entity.AiRefinementTask;
import com.thundax.kuzhambu.ai.domain.refinement.repository.AiRefinementTaskRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@BizExceptionBoundary
public class AiRefinementTaskCleanupApplicationServiceImpl implements AiRefinementTaskCleanupApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiRefinementTaskCleanupApplicationServiceImpl.class);

    static final Duration TASK_RETENTION = Duration.ofHours(12);
    static final String FAILURE_STAGE_WORKER_RESULT = "WORKER_RESULT";
    static final String ERROR_TYPE_TASK_EXPIRED = "TASK_EXPIRED";
    static final String ERROR_MESSAGE_TASK_EXPIRED = "任务超过 12 小时未完成，系统自动关闭";
    static final String ERROR_TYPE_TASK_ORPHANED_BY_RESTART = "TASK_ORPHANED_BY_RESTART";
    static final String ERROR_MESSAGE_TASK_ORPHANED_BY_RESTART = "服务重启后任务执行上下文已丢失，系统自动关闭";
    private static final String ADMIN_APPLICATION_NAME = "kuzhambu-admin-starter";

    private final AiRefinementTaskRepository taskRepository;
    private final Environment environment;

    public AiRefinementTaskCleanupApplicationServiceImpl(
            AiRefinementTaskRepository taskRepository, Environment environment) {
        this.taskRepository = taskRepository;
        this.environment = environment;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeInterruptedActiveTasks() {
        List<AiRefinementTask> activeTasks = taskRepository.listActiveTasks();
        int closedCount = 0;
        Instant now = now();
        if (activeTasks != null) {
            for (AiRefinementTask task : activeTasks) {
                if (task == null) {
                    continue;
                }
                task.markFailed(
                        FAILURE_STAGE_WORKER_RESULT,
                        ERROR_TYPE_TASK_ORPHANED_BY_RESTART,
                        ERROR_MESSAGE_TASK_ORPHANED_BY_RESTART,
                        now);
                taskRepository.update(task);
                closedCount++;
            }
        }
        return closedCount;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(rollbackFor = Exception.class)
    public void closeInterruptedActiveTasksOnStartup() {
        if (!ADMIN_APPLICATION_NAME.equals(environment.getProperty("spring.application.name"))) {
            return;
        }
        int closedCount = closeInterruptedActiveTasks();
        if (closedCount > 0) {
            LOGGER.info("Closed interrupted AI refinement tasks after backend startup, count={}", closedCount);
        }
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
