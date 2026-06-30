package com.thundax.kuzhambu.ai.application.refinement.service;

public interface AiRefinementTaskCleanupService {

    CleanupResult cleanupExpiredTasks();

    record CleanupResult(int expiredRunningCount, int deletedTerminalCount) {}
}
