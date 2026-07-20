package com.thundax.kuzhambu.ai.application.refinement.service;

public interface AiRefinementTaskCleanupApplicationService {

    CleanupResult cleanupExpiredTasks();

    int closeInterruptedActiveTasks();

    record CleanupResult(int expiredRunningCount, int deletedTerminalCount) {}
}
