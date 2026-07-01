package com.thundax.kuzhambu.ai.application.refinement.service;

public interface AiRefinementTaskCleanupApplicationService {

    CleanupResult cleanupExpiredTasks();

    record CleanupResult(int expiredRunningCount, int deletedTerminalCount) {}
}
