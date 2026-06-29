package com.thundax.kuzhambu.operations.application.cleanup.service;

import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;

public interface CleanupApplicationService {

    OperationsCleanupDetailResult execute(OperationsCleanupExecuteCommand command);
}
